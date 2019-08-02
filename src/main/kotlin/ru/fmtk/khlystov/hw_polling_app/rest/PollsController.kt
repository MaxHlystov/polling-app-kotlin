package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
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
    fun addPoll(@RequestBody(required = true) request: AddOrEditRequestDTO): PollDTO {
        val (userId, pollDTO) = request
        return withUser(userId) { user ->
            val poll = pollDTO.toPoll(user)
            PollDTO(pollRepository.save(poll))
        }.orElseThrow {
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when adding a poll.")
        }
    }

    @GetMapping("/polls")
    fun listPolls(@RequestParam(required = true) userId: String): List<PollDTO> {
        return withUser(userId) { pollRepository.findAll().map(::PollDTO) }
                .orElseThrow {
                    ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when getting a list of polls.")
                }
    }

    @PutMapping("/polls")
    fun editPoll(@RequestBody(required = true) request: AddOrEditRequestDTO): PollDTO {
        val (userId, pollDTO) = request
        val pollId = pollDTO.id
        if (pollId == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Poll id was not specify.")
        }
        return withUserAndPoll(userId, pollId) { user, poll ->
            if (poll.owner.id == userId) {
                val newPoll = pollDTO.toPoll(user)
                PollDTO(pollRepository.save(newPoll))
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't edit a poll isn't belonged to you.")
            }
        }.orElseThrow {
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error editing the poll.")
        }
    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String) {
        return withUserAndPoll(userId, pollId) { user, poll ->
            if(user.id == poll.owner.id) {
                pollRepository.delete(poll)
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't delete a poll isn't belonged to you.")
            }
        }.orElseThrow {
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting a poll.")
        }
    }

    private fun <T> withUser(userId: String, block: (user: User) -> T?): Optional<T> {
        val optUser = userRepository.findById(userId)
        if (optUser.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find a poll.")
        }
        return return optUser.map(block)
    }

    private fun <T> withUserAndPoll(userId: String,
                                    pollId: String,
                                    block: (user: User, poll: Poll) -> T?): Optional<T> {
        return withUser(userId) { user ->
            val optPoll = pollRepository.findById(pollId)
            if (optPoll.isEmpty) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find a poll.")
            }
            block(user, optPoll.get())
        }
    }
}