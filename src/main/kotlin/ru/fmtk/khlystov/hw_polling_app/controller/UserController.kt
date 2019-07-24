package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import javax.servlet.http.HttpServletRequest


@Controller
class UserController(private val userRepository: UserRepository) {

    @PostMapping("auth")
    fun userAuth(@RequestParam(required = true) userName: String,
                 request: HttpServletRequest,
                 model: Model): String {
        val user: User = userRepository.findByName(userName)
                ?: userRepository.save(User(userName))
        return "redirect:/polls/list?userId=" + user.id
    }

}