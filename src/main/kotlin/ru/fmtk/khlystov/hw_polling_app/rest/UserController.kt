package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails


@CrossOrigin
@RestController
class UserController(private val userRepository: UserRepository,
                     private val passwordEncoder: PasswordEncoder) {

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

    @PostMapping("/login")
    fun login(@AuthenticationPrincipal userDetails: CustomUserDetails): Mono<UserDTO> {
        return Mono.just(UserDTO(userDetails.user))
    }

    @GetMapping("/users")
    fun getUsers(): Flux<UserDTO> {
        return userRepository.findAll().map(::UserDTO)
    }

    @PutMapping("/users")
    fun editUser(@RequestBody(required = true) userDTO: UserDTO): Mono<UserDTO> {
        val userId = userDTO.id ?: ""
        return userRepository.findById(userId)
                .switchIfEmpty(getMonoHttpError<User>(HttpStatus.BAD_REQUEST, "Error user edit: user with id $userId doesn't exist."))
                .flatMap { user ->
                    val userToSave = User(userId,
                            userDTO.name,
                            userDTO.email,
                            user.password,
                            userDTO.accountNonExpired,
                            userDTO.accountNonLocked,
                            userDTO.credentialsNonExpired,
                            userDTO.enabled,
                            userDTO.roles)
                    userRepository.save(userToSave)
                }
                .map(::UserDTO)
    }

    @DeleteMapping("/users")
    fun deleteUser(@RequestParam(required = true, name = "userid") userId: String): Mono<Void> {
        return userRepository.findById(userId)
                .switchIfEmpty(getMonoHttpError<User>(HttpStatus.BAD_REQUEST, "Error user delete: user with such id doesn't exist."))
                .flatMap { userToDelete ->
                    userRepository.delete(userToDelete)
                }
    }
}
