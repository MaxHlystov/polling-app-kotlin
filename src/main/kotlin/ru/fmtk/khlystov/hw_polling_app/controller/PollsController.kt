package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Controller
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository) {

    @GetMapping("/polls/add")
    fun addPoll(@RequestParam(required = true) userId: String,
                model: Model): String {
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            model.addAttribute("user", user)
            return "polls/add"
        }
        return "auth"
    }

    @RequestMapping("/polls/list")
    fun listPolls(@RequestParam(required = true) userId: String,
                  model: Model): String {
        var optUser = userRepository.findById(userId)
        return optUser.map { user ->
            model.addAttribute("user", user)
            model.addAttribute("polls", pollRepository.findAllByOwner(user))
            "polls/list"
        }
                .orElse("auth")
    }

    @GetMapping("/polls/edit")
    fun editPoll(@RequestParam(name = "id", required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
//        var user = userRepository.findById(userId)
//        model.addAttribute("user", user)
//        model.addAttribute("polls", pollRepository.findAllByOwner(user))
        return "polls/edit"

    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(name = "id", required = true) pollId: String,
                   @RequestParam(required = true) userId: String): String {
        return "polls/list"
    }

    @GetMapping("/polls/vote")
    fun votePoll(@RequestParam(name = "id", required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
//        var user: User = userRepository.findByName(userName)
//                ?: userRepository.save(User(userName))
//        model.addAttribute("user", user)
//        model.addAttribute("polls", pollRepository.findAllByOwner(user))
        return "polls/vote"

    }
}