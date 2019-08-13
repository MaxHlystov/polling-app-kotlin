package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO

@CrossOrigin
@RestController
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository,
                      private val voteRepository: VoteRepository) {

    @PostMapping("/polls")
    fun addPoll(@RequestBody(required = true) request: AddOrEditRequestDTO): Mono<PollDTO> {
        val (userId, pollDTO) = request
        return withUser(userId)
                .map { user -> user to pollDTO.toPoll(user) }
                .flatMap { (user, poll) ->
                    pollRepository.save(poll)
                            .map { savedPoll -> PollDTO(savedPoll, user.id == poll.owner.id) }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error when adding a poll."))
    }

    @GetMapping("/polls")
    fun listPolls(@RequestParam(required = true) userId: String): Flux<PollDTO> {
        return withUser(userId)
                .flatMapMany { pollRepository.findAll() }
                .map { poll -> PollDTO(poll, userId == poll.owner.id) }
                .switchIfEmpty(
                        getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Error when getting a list of polls."))
    }

    @PutMapping("/polls")
    fun editPoll(@RequestBody(required = true) request: AddOrEditRequestDTO): Mono<PollDTO> {
        val (userId, pollDTO) = request
        val pollId = pollDTO.id
        if (pollId == null) {
            return getMonoHttpError(HttpStatus.BAD_REQUEST, "Poll id was not specify.")
        }
        return withUserAndPoll(userId, pollId)
                .flatMap { (user, poll) ->
                    if (poll.owner.id == userId) {
                        val newPoll = pollDTO.toPoll(user)
                        pollRepository.save(newPoll)
                    } else {
                        getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "You can't edit a poll isn't belonged to you.")
                    }
                }
                .map { savedPoll -> PollDTO(savedPoll, true) }
                .switchIfEmpty(
                        getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR, "Error editing the poll.")
                )
    }

    @DeleteMapping("/polls")
    fun deletePoll(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): Mono<Void> {
        return withUserAndPoll(userId, pollId)
                .flatMap { (user, poll) ->
                    if (user.id == poll.owner.id) {
                        pollRepository.delete(poll)
                    } else {
                        getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "You can't delete a poll isn't belonged to you.")
                    }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error deleting a poll."))
    }

    private fun withUser(userId: String): Mono<User> {
        return userRepository.findById(userId)
                .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST, "Can't find the user."))
    }

    private fun withUserAndPoll(userId: String,
                                pollId: String): Mono<Pair<User, Poll>> {
        return withUser(userId)
                .flatMap { user ->
                    pollRepository.findById(pollId)
                            .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST,
                                    "Can't find the poll."))
                            .map { poll -> user to poll }

                }
    }

    private fun <T> getMonoHttpError(status: HttpStatus, errorMessage: String): Mono<T> =
            Mono.error(ResponseStatusException(status, errorMessage))
}