package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Controller
class PollsControllerprivate(private val userRepository: UserRepository,
                             private val pollRepository: PollRepository) {
    @PostMapping("/polls/edit")
    fun rootAuth(@RequestParam(name = "id", required = true) pollId: String,
                 @RequestParam
                 model: Model): String {
//        var user: User = userRepository.findByName(userName)
//                ?: userRepository.save(User(userName))
//        model.addAttribute("user", user)
//        model.addAttribute("polls", pollRepository.findAllByOwner(user))
        return "polls"

    }
}