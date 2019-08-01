package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import ru.fmtk.khlystov.hw_polling_app.controller.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import java.util.*

@RestController
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository,
                      private val voteRepository: VoteRepository) {

    @PostMapping("/polls")
    fun addPoll(@RequestParam(required = true) userId: String,
                @RequestParam(required = true) pollDTO: PollDTO): String {
        return withUser(userId) { user ->
            var poll = Poll(pollDTO.id, pollDTO.title, user, pollDTO.items.map { pollItemDTO -> PollItem(pollItemDTO.id, pollItemDTO.title) })
            pollRepository.save(poll)
            "ok"
        }.orElse("error adding a poll")
    }

    @GetMapping("/polls")
    fun listPolls(@RequestParam(required = true) userId: String): List<PollDTO> {
        return withUser(userId) { _ ->
            pollRepository.findAll().map(::PollDTO)
        }.orElseGet { ArrayList<PollDTO>() }
    }

    @PutMapping("/polls")
    fun editPoll(@RequestParam(required = true) pollId: String,
                 @RequestParam(required = true) userId: String): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            if (poll.owner.id == userId) {
                "ok"
            } else {
                null
            }
        }.orElse("error editing")
    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): String {
        return withUserAndPoll(userId, pollId) { user, poll ->
            pollRepository.delete(poll)
            "ok"
        }.orElse("error deleting")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(ex: MissingServletRequestParameterException): String {
        return "error 1234"
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