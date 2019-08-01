package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository


@RestController
class UserController(private val userRepository: UserRepository) {

    @PostMapping("auth")
    fun userAuth(@RequestParam(required = true) userName: String): String {
        val user: User = userRepository.findByName(userName)
                ?: userRepository.save(User(userName))
        val id = user.id
        if (id == null) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving user.")
        }
        return id
    }

}