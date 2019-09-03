package ru.fmtk.khlystov.hw_polling_app.rest

import jdk.internal.joptsimple.internal.Strings
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
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
    @PostMapping(value = ["/submit", "/users"])
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
                .map(::UserDTO)
    }

    @CrossOrigin
    @PostMapping("/login")
    fun login(@AuthenticationPrincipal userDetails: CustomUserDetails): Mono<UserDTO> {
        return Mono.just(UserDTO(userDetails.user))
    }

    @CrossOrigin
    @PutMapping("/users")
    fun editUser(@RequestParam(required = true, name = "userid") userId: String,
                 @RequestParam(name = "username") userName: String,
                 @RequestParam password: String,
                 @RequestParam email: String): Mono<UserDTO> {
        return userRepository.findById(userId)
                .switchIfEmpty(getMonoHttpError<User>(HttpStatus.BAD_REQUEST, "Error user edit: user with such id doesn't exist."))
                .flatMap {user ->
                    val userToSave = User(userId,
                            if(Strings.isNullOrEmpty(userName)) user.name else userName,
                            if(Strings.isNullOrEmpty(email)) user.email else email,
                            if(Strings.isNullOrEmpty(password)) user.password else password)
                    userRepository.save(user)
                }
                .map(::UserDTO)
    }

    @CrossOrigin
    @DeleteMapping("/users")
    fun deleteUser(@RequestParam(required = true, name = "userid") userId: String): Mono<Void> {
        return userRepository.findById(userId)
                .switchIfEmpty(getMonoHttpError<User>(HttpStatus.BAD_REQUEST, "Error user delete: user with such id doesn't exist."))
                .flatMap {userToDelete ->
                    userRepository.delete(userToDelete)
                }
    }
}
