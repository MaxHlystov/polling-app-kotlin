package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.repository.withUser
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO


@RestController
class UserController(private val userRepository: UserRepository) {

    @CrossOrigin
    @PostMapping("/submit")
    fun createUser(@RequestParam(required = true) userName: String,
                   @RequestParam(required = true) password: String,
                   @RequestParam(defaultValue = "") email: String): Mono<UserDTO> {
        return userRepository.findByName(userName)
                .flatMap {
                    getMonoHttpError<User>(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: user with such login already exists.")
                }
                .switchIfEmpty(userRepository.save(User(null, userName, email, password)))
                .filter { user: User -> user.id != null }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user."))
                .map { user -> UserDTO(user) }
    }

    @CrossOrigin
    @PostMapping("/login")
    fun login(exchange: ServerWebExchange): Mono<UserDTO> {
        return withUser()
                .map { user -> UserDTO(user) }
        /*.doOnNext { userDTO ->
            //addTokenHeader(exchange.response, userDTO)
        }*/
    }
}