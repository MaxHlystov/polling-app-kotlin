package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import java.util.*

@Controller
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository) {

    @GetMapping("/polls/add")
    fun addPoll(@RequestParam(required = true) userId: String,
                model: Model): String {
        return withUser(userId) { user ->
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
        return withUser(userId) { user ->
            model.addAttribute("user", user)
            model.addAttribute("polls", pollRepository.findAll())
            "polls/list"
        }
                .orElse("auth")
    }

    @GetMapping("/polls/edit")
    fun editPoll(@RequestParam(
            required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            if (poll.owner.id == userId) {
                model.addAttribute("addOperation", false)
                model.addAttribute("user", user)
                model.addAttribute("poll", poll)
                return@withUserAndPoll "polls/edit"
            }
            "polls/list"
        }
                .orElse("auth")
    }

    @PostMapping("/polls/save")
    fun savePoll(@RequestParam(required = true) userId: String,
                 @RequestParam pollId: String?,
                 @RequestParam(required = true) title: String,
                 @RequestParam values: MultiValueMap<String, String>,
                 model: Model): String {
        return withUser(userId) { user ->
            val pollItems = values.filter { (key, _) -> key.startsWith("option") }
                    .flatMap { (_, s) -> s }
                    .filter(String::isNotEmpty).map {title -> PollItem(null, title) }
            var poll = Poll(pollId, title, user, pollItems)
            poll = pollRepository.save(poll)
            model.addAttribute("user", user)
            model.addAttribute("poll", poll)
            "polls/vote"
        }.orElse("auth")
    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String,
                   model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            model.addAttribute("user", user)
            pollRepository.delete(poll)
            "polls/list"
        }.orElse("auth")
    }

    @GetMapping("/polls/vote")
    fun votePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            model.addAttribute("user", user)
            model.addAttribute("poll", poll)
            "polls/vote"
        }.orElse("auth")
    }

    @PostMapping("/polls/vote")
    fun saveVote(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 @RequestParam(required = true) optionNumber: Int,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            model.addAttribute("user", user)
            model.addAttribute("poll", poll)
            "polls/statistics"
        }.orElse("auth")
    }

    private fun <T> withUser(userId: String, block: (user: User) -> T): Optional<T> {
        return userRepository.findById(userId).map(block)
    }

    private fun <T> withUserAndPoll(userId: String,
                                    pollId: String,
                                    block: (user: User, poll: Poll) -> T): Optional<T> {
        return withUser(userId) { user ->
            pollRepository.findById(pollId).map { poll ->
                block(user, poll)
            }.get()
        }
    }
}