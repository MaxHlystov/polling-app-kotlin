package ru.fmtk.khlystov.hw_polling_app.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.repository.*
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VoteDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VotesCountDTO

@CrossOrigin
@RestController
class VoteController(private val userRepository: UserRepository,
                     private val pollRepository: PollRepository,
                     private val voteRepository: VoteRepository) {

    @GetMapping("/votes")
    fun statistics(@RequestParam(required = true) pollId: String,
                   @RequestParam(required = true) userId: String): Mono<List<VotesCountDTO>> {
        return withUserAndPoll(userRepository, pollRepository, userId, pollId)
                .flatMap { (user, poll) ->
                    voteRepository.findAllByPollAndUser(poll, user)
                            .take(1)
                            .toMono()
                            .map { vote -> vote.pollItem.id to poll }
                            .switchIfEmpty(Mono.just("" to poll))
                }
                .flatMap { (selectedItemId, poll) ->
                    voteRepository.getVotes(poll)
                            .map { votesCountList ->
                                votesCountList.asSequence()
                                        .map { votesCount ->
                                            VotesCountDTO(votesCount, votesCount.pollItem.id == selectedItemId)
                                        }
                                        .toList()
                            }
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error getting statistics of the poll."))
    }

    @PostMapping("/votes")
    fun vote(@RequestParam(required = true) pollId: String,
             @RequestParam(required = true) userId: String,
             @RequestParam(name = "option", required = true) itemId: String): Mono<VoteDTO> {
        return withUserAndPoll(userRepository, pollRepository, userId, pollId)
                .flatMap { (user, poll) ->
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