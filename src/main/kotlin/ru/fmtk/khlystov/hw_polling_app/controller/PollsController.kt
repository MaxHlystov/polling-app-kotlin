package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Controller
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository) {

    @GetMapping("/polls/add")
    fun addPoll(@RequestParam(required = true) userId: String,
                model: Model): String {
        return userRepository.findById(userId).map { user ->
            model.addAttribute("addOperation", true)
            model.addAttribute("user", user)
            model.addAttribute("poll", null)
            "polls/edit"
        }
                .orElse("auth")
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
        return userRepository.findById(userId).flatMap { user ->
            pollRepository.findById(pollId).map { poll ->
                model.addAttribute("addOperation", false)
                model.addAttribute("user", user)
                model.addAttribute("poll", poll)
                "polls/edit"
            }
        }
                .orElse("auth")
    }

    @PostMapping("/polls/save")
    fun savePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 @RequestParam(required = true) title: String,
                 @RequestParam(required = true) items: List<String>): String {
        return "polls/list"
    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): String {
        pollRepository.deleteById(pollId)
        return "polls/list"
    }

    @GetMapping("/polls/vote")
    fun votePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {

        return "polls/vote"

    }
}