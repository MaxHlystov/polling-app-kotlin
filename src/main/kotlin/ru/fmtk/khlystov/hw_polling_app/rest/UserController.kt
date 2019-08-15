package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO


@RestController
class UserController(private val userRepository: UserRepository) {

    @CrossOrigin
    @PostMapping("auth")
    fun userAuth(@RequestParam(required = true) userName: String): UserDTO {
        val user: User = userRepository.findByName(userName)
                ?: userRepository.save(User(userName))
        if (user.id == null) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving user.")
        }
        return UserDTO(user)
    }

}