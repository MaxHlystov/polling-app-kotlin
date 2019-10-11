package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails
import ru.fmtk.khlystov.hw_polling_app.service.PollService

@CrossOrigin
@RestController
class PollsController(private val pollService: PollService) {

    @PostMapping("/polls")
    fun addPoll(@AuthenticationPrincipal userDetails: CustomUserDetails,
                @RequestBody(required = true) pollDTO: PollDTO): Mono<PollDTO> {
        val user = userDetails.user
        val poll = pollDTO.toPoll(user)
        val editablePoll = user.id == poll.owner.id
        return pollService.save(poll)
                .map { savedPoll -> PollDTO(savedPoll, editablePoll) }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error when adding a poll."))
    }

    @GetMapping("/polls")
    fun listPolls(@AuthenticationPrincipal userDetails: CustomUserDetails): Flux<PollDTO> {
        val user = userDetails.user
        return pollService.findAll()
                .map { poll -> PollDTO(poll, user.id == poll.owner.id) }
    }

    @PutMapping("/polls")
    fun editPoll(@AuthenticationPrincipal userDetails: CustomUserDetails,
                 @RequestBody(required = true) pollDTO: PollDTO): Mono<PollDTO> {
        val user = userDetails.user
        val pollId = pollDTO.id
        if (pollId == null) {
            return getMonoHttpError(HttpStatus.BAD_REQUEST, "Poll id was not specify.")
        }
        return pollService.findById(pollId)
                .flatMap { poll ->
                    if (poll.owner.id == user.id) {
                        val newPoll = pollDTO.toPoll(user)
                        pollService.save(newPoll)
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
    fun deletePoll(@AuthenticationPrincipal userDetails: CustomUserDetails,
                   @RequestParam(required = true) pollId: String): Mono<Void> {
        val user = userDetails.user
        return pollService.findById(pollId)
                .filter { poll -> user.id == poll.owner.id }
                .switchIfEmpty(
                        getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "You can't delete a poll isn't belonged to you."))
                .flatMap { poll ->
                    pollService.delete(poll)
                }
    }
}