package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.*
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VoteDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VotesCountDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetailsService
import ru.fmtk.khlystov.hw_polling_app.security.SecurityConfiguration

@ContextConfiguration(classes = [SecurityConfiguration::class, CustomUserDetailsService::class])
@WebFluxTest(VoteController::class)
@ExtendWith(SpringExtension::class)
internal class VoteControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var pollRepository: PollRepository

    @MockBean
    lateinit var voteRepository: VoteRepository

    companion object {
        const val trustedUserIdWithoutVotes = "777777777777"
        const val trustedUserNameWithoutVotes = "User without votes"
        const val notTrustedUserId = "0000000000"
        const val notTrustedUserName = "Not trusted user name"
        const val notValidPollId = "0000000000"
        val users = generateSequence(1) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { userId -> User(userId, "StoredInDB-$userId") }
                .toList()
        val trustedUser = users[0]
        const val trustedUserName = "StoredInDB-1"
        val trustedUserWithoutVotes = User(trustedUserIdWithoutVotes, trustedUserNameWithoutVotes)
        val validPolls = generateSequence(1000) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { id -> Poll(id, "Valid Poll #$id", trustedUser, genPollItems(4)) }
                .toList()
        val votes = genVotes(users.asSequence(), validPolls.asSequence())
        val jsonMapper = jacksonObjectMapper()

        private fun genPollItems(number: Int): List<PollItem> = generateSequence(1) { i -> i + 1 }
                .take(number)
                .map(Int::toString)
                .map { PollItem(it, "Item $it") }
                .toList()

        private fun genVotes(users: Sequence<User>, polls: Sequence<Poll>): List<Vote> {
            return users.mapIndexed { index, user -> index to user }
                    .flatMap { (userIdx, user) ->
                        polls.mapIndexed { pollIdx, poll -> genVote(poll, user, userIdx + pollIdx) }
                    }
                    .toList()
        }

        private fun genVote(poll: Poll, user: User, optionIdx: Int): Vote =
                Vote(user.id + poll.id + optionIdx.toString(), user, poll, poll.items[optionIdx % poll.items.size])

        private fun getVotesCount(votes: List<Vote>, poll: Poll): List<VotesCount> =
                votes.filter { vote -> vote.poll == poll }
                        .map(Vote::pollItem)
                        .groupingBy { pollItem -> pollItem }
                        .eachCount()
                        .entries.map { (pollItem, total) -> VotesCount(pollItem, total.toLong()) }
    }

    @BeforeEach
    fun initMockRepositories() {
        users.forEach { user ->
            given(userRepository.findById(user.id ?: ""))
                    .willReturn(Mono.just(user))
            given(userRepository.findByName(user.name ?: ""))
                    .willReturn(Mono.just(user))
        }
        validPolls.forEach { poll ->
            given(pollRepository.findById(poll.id ?: ""))
                    .willReturn(Mono.just(poll))
            given(voteRepository.getVotes(poll))
                    .willReturn(Flux.fromIterable(getVotesCount(votes, poll)))
        }
        votes.forEach { vote ->
            given(voteRepository.findAllByPollAndUser(vote.poll, vote.user))
                    .willReturn(Flux.just(vote))
        }
        given(userRepository.findByName(notTrustedUserName))
                .willReturn(Mono.empty())
        given(userRepository.findByName(trustedUserNameWithoutVotes))
                .willReturn(Mono.just(trustedUserWithoutVotes))
        given(pollRepository.findById(notValidPollId))
                .willReturn(Mono.empty())
        given(pollRepository.findAll())
                .willReturn(Flux.fromIterable(validPolls))
    }

    @Test
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Get list of polls for existing poll and trusted user")
    fun statisticsForExistingPollAndTrustedUser() {
        val poll = validPolls[0]
        val userItem = votes.filter { vote -> vote.poll == poll && vote.user == trustedUser }
                .map(Vote::pollItem).first()
        val votesCountDTO = getVotesCount(votes, poll)
                .map { votesCount -> VotesCountDTO(votesCount, votesCount.pollItem == userItem) }
                .toList()
        val jsonMatch = jsonMapper.writeValueAsString(votesCountDTO) ?: ""
        client.get()
                .uri("/votes?pollId=${poll.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithMockUser(username = notTrustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Get error for existing poll and not trusted user")
    fun statisticsForExistingPollAndNotTrustedUser() {
        val poll = validPolls[0]
        client.get()
                .uri("/votes?pollId=${poll.id}")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Get error for not existing poll and trusted user")
    fun statisticsForNotExistingPollAndTrustedUser() {
        client.get()
                .uri("/votes?pollId=${notValidPollId}")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Saving existing vote should return vote DTO with id")
    fun saveExistingVote() {
        val vote = votes[0]
        given(voteRepository.save(any()))
                .willReturn(Mono.just(vote))
        val jsonMatch = jsonMapper.writeValueAsString(VoteDTO(vote))
        client.post()
                .uri("/votes?pollId=${vote.poll.id}&option=${vote.pollItem.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Saving not existing vote should return error")
    fun saveNotExistingVote() {
        given(voteRepository.save(any()))
                .willReturn(Mono.empty())
        val optionId = "123"
        client.post()
                .uri("/votes?userId=${trustedUserIdWithoutVotes}&pollId=${notValidPollId}&option=${optionId}")
                .exchange()
                .expectStatus().isBadRequest
    }
}