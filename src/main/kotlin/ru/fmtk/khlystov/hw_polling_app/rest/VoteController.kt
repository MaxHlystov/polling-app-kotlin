package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import ru.fmtk.khlystov.hw_polling_app.repository.getMonoHttpError
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VoteDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VotesCountDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails

@CrossOrigin
@RestController
class VoteController(private val pollRepository: PollRepository,
                     private val voteRepository: VoteRepository) {

    @GetMapping("/votes")
    fun statistics(@AuthenticationPrincipal userDetails: CustomUserDetails,
                   @RequestParam(required = true) pollId: String): Flux<VotesCountDTO> {
        val user = userDetails.user
        return pollRepository.findById(pollId)
                .toFlux()
                .flatMap { poll ->
                    voteRepository.findAllByPollAndUser(poll, user)
                            .take(1)
                            .map { vote -> vote.pollItem.id to poll }
                            .switchIfEmpty(Flux.just("" to poll))
                }
                .flatMap { (selectedItemId, poll) ->
                    voteRepository.getVotes(poll)
                            .map { votesCount ->
                                VotesCountDTO(votesCount, votesCount.pollItem.id == selectedItemId)
                            }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error getting statistics of the poll."))
    }

    @PostMapping("/votes")
    fun vote(@AuthenticationPrincipal userDetails: CustomUserDetails,
             @RequestParam(required = true) pollId: String,
             @RequestParam(name = "option", required = true) itemId: String): Mono<VoteDTO> {
        val user = userDetails.user
        return pollRepository.findById(pollId)
                .flatMap { poll ->
                    poll.getPollItem(itemId).map { pollItem ->
                        Mono.just(Vote(null, user, poll, pollItem))
                    }
                            .orElseGet { Mono.empty() }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST,
                        "Can't find poll item with specified id."))
                .flatMap { vote ->
                    voteRepository.save(vote)
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error while saving of your vote."))
                .map(::VoteDTO)

    }
}