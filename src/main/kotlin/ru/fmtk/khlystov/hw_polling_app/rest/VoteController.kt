package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VoteDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VotesCountDTO
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import java.util.*

@RestController
class VoteController(private val userRepository: UserRepository,
                     private val pollRepository: PollRepository,
                     private val voteRepository: VoteRepository) {

    @GetMapping("/votes")
    fun votePoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String): List<VoteDTO> {
        return withUserAndPoll(userId, pollId) { user, poll ->
            voteRepository.findAllByPollAndUser(poll, user).map(::VoteDTO)
        }.orElseThrow {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting votes of the poll.")
        }
    }

    @GetMapping("/votes/statistics")
    fun statistics(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): List<VotesCountDTO> {
        return withUserAndPoll(userId, pollId) { _, poll ->
            voteRepository.getVotes(poll).map(::VotesCountDTO)
        }
                .orElseThrow {
                    ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error getting statistics of the poll.")
                }
    }

    @PostMapping("/votes")
    fun vote(@RequestParam(required = true) pollId: String,
             @RequestParam(required = true) userId: String,
             @RequestParam(name = "option", required = true) itemId: String): VoteDTO {
        return withUserAndPoll(userId, pollId) { user, poll ->
            poll.getPollItem(itemId).map { pollItem ->
                VoteDTO(voteRepository.save(Vote(null, user, poll, pollItem)))
            }.orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find poll item with specified id.")
            }
        }.orElseThrow {
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving of your vote.")
        }
    }

    private fun <T> withUser(userId: String, block: (user: User) -> T?): Optional<T> {
        val optUser = userRepository.findById(userId)
        if (optUser.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find a poll")
        }
        return return optUser.map(block)
    }

    private fun <T> withUserAndPoll(userId: String,
                                    pollId: String,
                                    block: (user: User, poll: Poll) -> T?): Optional<T> {
        return withUser(userId) { user ->
            val optPoll = pollRepository.findById(pollId)
            if (optPoll.isEmpty) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find a poll")
            }
            block(user, optPoll.get())
        }
    }
}