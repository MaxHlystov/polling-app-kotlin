package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.repository.*
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO

@CrossOrigin
@RestController
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository) {

    @PostMapping("/polls")
    fun addPoll(@RequestBody(required = true) request: AddOrEditRequestDTO): Mono<PollDTO> {
        val (userId, pollDTO) = request
        return withUser(userRepository, userId)
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
        return withUser(userRepository, userId)
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
        return withUserAndPoll(userRepository, pollRepository, userId, pollId)
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
        return withUserAndPoll(userRepository, pollRepository, userId, pollId)
                .filter { (user, poll) -> user.id == poll.owner.id }
                .switchIfEmpty(
                        getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "You can't delete a poll isn't belonged to you."))
                .flatMap { (_, poll) ->
                    pollRepository.delete(poll)
                }
    }
}