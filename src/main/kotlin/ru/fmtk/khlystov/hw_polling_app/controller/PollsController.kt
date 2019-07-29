package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import java.util.*

@Controller
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository,
                      private val voteRepository: VoteRepository) {

    @GetMapping("/polls/add")
    fun addPoll(@RequestParam(required = true) userId: String,
                model: Model): String {
        return withUser(userId) { user ->
            getPollEditView(user, null, model)
        }.orElseGet(this::getAuthView)
    }

    @RequestMapping("/polls/list")
    fun listPolls(@RequestParam(required = true) userId: String,
                  model: Model): String {
        return withUser(userId) { user ->
            getPollsListView(user, model)
        }.orElseGet(this::getAuthView)
    }

    @GetMapping("/polls/edit")
    fun editPoll(@RequestParam(
            required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            if (poll.owner.id == userId) {
                getPollEditView(user, poll, model)
            } else {
                getPollsListView(user, model)
            }
        }.orElseGet(this::getAuthView)
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
                    .filter(String::isNotEmpty).map { title -> PollItem(null, title) }
            var poll = Poll(pollId, title, user, pollItems)
            poll = pollRepository.save(poll)
            getVoteView(user, poll, model)
        }.orElseGet(this::getAuthView)
    }

    @RequestMapping("/polls/delete")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String,
                   model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            pollRepository.delete(poll)
            getPollsListView(user, model)
        }.orElseGet(this::getAuthView)
    }

    @GetMapping("/polls/vote")
    fun votePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            getVoteView(user, poll, model)
        }.orElseGet(this::getAuthView)
    }

    @PostMapping("/polls/vote")
    fun saveVote(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String,
                 @RequestParam(name = "option", required = true) itemId: String,
                 model: Model): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            poll.getPollItem(itemId).map { pollItem ->
                val vote = voteRepository.save(Vote(null, user, poll, pollItem))
                val a = 5
            }
            getPollStatisticsView(user, poll, model)
        }.orElseGet(this::getAuthView)
    }

    private fun getAuthView(): String {
        return "redirect:/auth"
    }

    private fun getPollEditView(user: User, poll: Poll?, model: Model): String {
        model.addAttribute("addOperation", poll == null)
        model.addAttribute("user", user)
        model.addAttribute("poll", poll)
        return "polls/edit"
    }

    private fun getVoteView(user: User, poll: Poll, model: Model): String {
        model.addAttribute("user", user)
        model.addAttribute("poll", poll)
        model.addAttribute("vote",
                voteRepository.findAllByPollAndUser(poll, user).getOrNull(0))
        return "polls/vote"
    }

    private fun getPollStatisticsView(user: User, poll: Poll, model: Model): String {
        model.addAttribute("user", user)
        model.addAttribute("poll", poll)
        model.addAttribute("votesCount", voteRepository.getVotes(poll))
        return "polls/statistics"
    }

    private fun getPollsListView(user: User, model: Model): String {
        model.addAttribute("user", user)
        model.addAttribute("polls", pollRepository.findAll())
        return "polls/list"
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