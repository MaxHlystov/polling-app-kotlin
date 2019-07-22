package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository


@Controller
data class RootController(private val userRepository: UserRepository) {

    @GetMapping("/")
    fun rootPageMapper(): String {
        return "auth"
    }
}
