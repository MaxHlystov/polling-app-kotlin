package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.*
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.VoteDTO

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
        const val notTrustedUserId = "0000000000"
        const val notValidPollId = "0000000000"
        val users = generateSequence(1) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { userId -> User(userId, "StoredInDB-$userId") }
                .toList()
        val trustedUser = users[0]
        val trustedUserWithoutVotes = User(trustedUserIdWithoutVotes, "User without votes")
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
                Vote(null, user, poll, poll.items[optionIdx % poll.items.size])
    }

    @BeforeEach
    fun initMockRepositories() {
        users.forEach { user ->
            BDDMockito.given(userRepository.findById(user.id ?: ""))
                    .willReturn(Mono.just(user))
        }
        validPolls.forEach { poll ->
            BDDMockito.given(pollRepository.findById(poll.id ?: ""))
                    .willReturn(Mono.just(poll))
        }
        votes.forEach { vote ->
            BDDMockito.given(voteRepository.findAllByPollAndUser(vote.poll, vote.user))
                    .willReturn(Flux.just(vote))
        }
        BDDMockito.given(userRepository.findById(notTrustedUserId))
                .willReturn(Mono.empty())
        BDDMockito.given(userRepository.findById(trustedUserIdWithoutVotes))
                .willReturn(Mono.just(trustedUserWithoutVotes))
        BDDMockito.given(pollRepository.findById(notValidPollId))
                .willReturn(Mono.empty())
        BDDMockito.given(pollRepository.findAll())
                .willReturn(Flux.fromIterable(validPolls))
    }

    @Test
    @DisplayName("Get list of polls")
    fun statistics() {
    }

    @Test
    @DisplayName("Saving existing vote should return vote DTO with id")
    fun saveExistingVote() {
        val vote = votes[0]
        given(voteRepository.save(Mockito.any(Vote::class.java)))
                .willReturn(Mono.just(vote))
        val jsonMatch = jsonMapper.writeValueAsString(VoteDTO(vote))
        client.post()
                .uri("/votes?userId=${vote.user.id}&pollId=${vote.poll.id}&option=${vote.pollItem.id}")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @DisplayName("Saving not existing vote should return error")
    fun saveNotExistingVote() {
        given(voteRepository.save(Mockito.any()))
                .willReturn(Mono.empty())
        val optionId = "123"
        client.post()
                .uri("/votes?userId=${trustedUserIdWithoutVotes}&pollId=${notValidPollId}&option=${optionId}")
                .exchange()
                .expectStatus().isBadRequest
    }
}