package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import java.util.*

@RestController
class VoteController(private val userRepository: UserRepository,
                     private val pollRepository: PollRepository,
                     private val voteRepository: VoteRepository) {

    @GetMapping("/polls/votes")
    fun votePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String): List<Vote> {
        return withUserAndPoll(userId, pollId) { user, poll ->
            voteRepository.findAllByPollAndUser(poll, user)
        }.orElseGet { ArrayList<Vote>() }
    }

    @GetMapping("/polls/votes/statistics")
    fun statistics(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): List<VotesCount> {
        return withUserAndPoll(userId, pollId) { _, poll -> voteRepository.getVotes(poll) }
                .orElseGet { ArrayList<VotesCount>() }
    }

    @PostMapping("/polls/votes")
    fun vote(@RequestParam(required = true) pollId: String,
             @RequestParam(required = true) userId: String,
             @RequestParam(name = "option", required = true) itemId: String): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            poll.getPollItem(itemId).map { pollItem ->
                voteRepository.save(Vote(null, user, poll, pollItem))
                "ok"
            }.orElse("error voting")
        }.orElse("error voting")
    }

    private fun <T> withUser(userId: String, block: (user: User) -> T?): Optional<T> {
        return userRepository.findById(userId).map(block)
    }

    private fun <T> withUserAndPoll(userId: String,
                                    pollId: String,
                                    block: (user: User, poll: Poll) -> T?): Optional<T> {
        return withUser(userId) { user ->
            pollRepository.findById(pollId).map { poll ->
                block(user, poll)
            }.orElse(null)
        }
    }
}