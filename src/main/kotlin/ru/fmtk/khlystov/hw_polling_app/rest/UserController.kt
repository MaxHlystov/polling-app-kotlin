package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO


@RestController
class UserController(private val userRepository: UserRepository) {

    @CrossOrigin
    @PostMapping("auth")
    fun userAuth(@RequestParam(required = true) userName: String): Mono<UserDTO> {
        return userRepository.findByName(userName)
                .switchIfEmpty(userRepository.save(User(userName)))
                .map { user ->
                    if (user.id == null) {
                        Mono.error<ResponseStatusException>(
                                ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving user."))
                    }
                }
                .map(::UserDTO)
    }

}