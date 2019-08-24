package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import ru.fmtk.khlystov.hw_polling_app.repository.*
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails

@CrossOrigin
@RestController
class PollsController(private val userRepository: UserRepository,
                      private val pollRepository: PollRepository) {

    @PostMapping("/polls")
    fun addPoll(@RequestBody(required = true) pollDTO: PollDTO): Mono<PollDTO> {
        return withUser()
                .map { user -> user to pollDTO.toPoll(user) }
                .flatMap { (user, poll) ->
                    pollRepository.save(poll)
                            .map { savedPoll -> PollDTO(savedPoll, user.id == poll.owner.id) }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error when adding a poll."))
    }

    @GetMapping("/polls")
    fun listPolls(): Flux<PollDTO> {
        return withUser()
                .flatMapMany { user ->
                    pollRepository.findAll()
                            .map { poll -> PollDTO(poll, user.id == poll.owner.id) }
                            .switchIfEmpty(
                                    getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                                            "Error when getting a list of polls."))
                }
    }

    @PutMapping("/polls")
    fun editPoll(@RequestBody(required = true) pollDTO: PollDTO): Mono<PollDTO> {
        val pollId = pollDTO.id
        if (pollId == null) {
            return getMonoHttpError(HttpStatus.BAD_REQUEST, "Poll id was not specify.")
        }
        return withUserAndPoll(pollRepository, pollId)
                .flatMap { (user, poll) ->
                    if (poll.owner.id == user.id) {
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
    fun deletePoll(@RequestParam(required = true) pollId: String): Mono<Void> {
        return withUserAndPoll(pollRepository, pollId)
                .filter { (user, poll) -> user.id == poll.owner.id }
                .switchIfEmpty(
                        getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "You can't delete a poll isn't belonged to you."))
                .flatMap { (_, poll) ->
                    pollRepository.delete(poll)
                }
    }
}