package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.core.publisher.toMono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails


@RestController
class UserController(private val userRepository: UserRepository,
                     private val authenticationManager: ReactiveAuthenticationManager) {

    @CrossOrigin
    @PostMapping("/auth")
    fun createUser(@RequestParam(required = true) userName: String,
                   @RequestParam(required = true) password: String,
                   @RequestParam(defaultValue = "") email: String): Mono<UserDTO> {
        return userRepository.findByName(userName)
                .flatMap { user ->
                    val r = user == null
                    getMonoHttpError<User>(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: user with such login already exists.")
                }
                .switchIfEmpty(userRepository.save(User(null, userName, email, password)))
                .filter { user: User -> user.id != null }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user."))
                .map { user -> UserDTO(user) }
    }

    @CrossOrigin
    @PostMapping("/login")
    fun loginUser(@RequestParam(required = true) userName: String,
                  @RequestParam(required = true) password: String): Mono<UserDTO> {
        val authenticationToken = UsernamePasswordAuthenticationToken(userName, password)
        val authentication = this.authenticationManager.authenticate(authenticationToken)
        authentication.doOnError { throw BadCredentialsException("Bad credentials") }
        ReactiveSecurityContextHolder.withAuthentication(authenticationToken)
        return authentication.map { auth -> auth.principal }
                .cast(CustomUserDetails::class.java)
                .map(CustomUserDetails::user)
                .map { user ->
                    if (user.id == null) {
                        throw BadCredentialsException("User with null id.")
                    } else {
                        UserDTO(user)
                    }
                }
    }

    @CrossOrigin
    @PostMapping("/auth/get")
    fun getCredentials(): Mono<UserDTO> {
        return ReactiveSecurityContextHolder.getContext()
                .map { securityContext -> securityContext.authentication }
                .map { authentication -> authentication.principal }
                .cast(CustomUserDetails::class.java)
                .map { userDetails ->
                    println("Polls getting for user: ${userDetails.username}")
                    UserDTO(userDetails.user)
                }
    }
}