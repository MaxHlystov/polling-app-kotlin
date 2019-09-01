package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails


@RestController
class UserController(private val userRepository: UserRepository,
                     private val passwordEncoder: PasswordEncoder) {

    @CrossOrigin
    @PostMapping("/submit")
    fun createUser(@RequestParam(required = true, name = "username") userName: String,
                   @RequestParam(required = true) password: String,
                   @RequestParam(defaultValue = "") email: String): Mono<UserDTO> {
        return userRepository.findByName(userName)
                .flatMap {
                    getMonoHttpError<User>(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: user with such login already exists.")
                }
                .switchIfEmpty(userRepository.save(User(null, userName, email, passwordEncoder.encode(password))))
                .filter { user: User -> user.id != null }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user."))
                .map { user -> UserDTO(user) }
    }

    @CrossOrigin
    @PostMapping("/login")
    fun login(@AuthenticationPrincipal userDetails: CustomUserDetails): Mono<UserDTO> {
        return Mono.just(UserDTO(userDetails.user))
    }
}