package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.*
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository

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
        const val notValidPollId = "0000000000"
        val users = generateSequence(1) { i -> i + 1 }
                        .take(4)
                        .map(Int::toString)
                        .map { userId -> User(userId, "StoredInDB-$userId") }
                        .toList()
        val trustedUser = users[0]
        val trustedUserWithoutPolls = User(trustedUserIdWithoutVotes, "User without votes")
        val validPolls = generateSequence(1000) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { id -> Poll(id, "Valid Poll #$id", trustedUser, genPollItems(4)) }
                .toList()
        val validPoll = validPolls[0]
        val validPollId = validPoll.id ?: "1234"
        val jsonMapper = jacksonObjectMapper()

        private fun genPollItems(number: Int): List<PollItem> = generateSequence(1) { i -> i + 1 }
                .take(number)
                .map(Int::toString)
                .map { PollItem(it, "Item $it") }
                .toList()

        private fun genVotes(number: Int): List<Vote> = generateSequence(1) { i -> i + 1 }
                .take(number)
                .map(Int::toString)
                .map { PollItem(it, "Item $it") }
                .toList()

    }

    @BeforeEach
    fun initMockRepositories() {
        BDDMockito.given(userRepository.findById(trustedUserId))
                .willReturn(Mono.just(trustedUser))
        BDDMockito.given(userRepository.findById(trustedUserIdWithoutPolls))
                .willReturn(Mono.just(trustedUserWithoutPolls))
        BDDMockito.given(userRepository.findById(notTrustedUserId))
                .willReturn(Mono.empty())
        BDDMockito.given(pollRepository.findById(validPollId))
                .willReturn(Mono.just(validPoll))
        BDDMockito.given(pollRepository.findById(notValidPollId))
                .willReturn(Mono.empty())
        BDDMockito.given(pollRepository.findAll())
                .willReturn(Flux.fromIterable(validPolls))
        BDDMockito.given<Mono<Void>>(pollRepository.delete(Mockito.any()))
                .willReturn(Mono.empty())
    }

    @Test
    @DisplayName("Get list of polls")
    fun statistics() {
    }

    @Test
    @DisplayName("Saving existing vote should return vote DTO with id")
    fun saveExistingVote() {
    }

    @Test
    @DisplayName("Saving not existing vote should return error")
    fun saveNotExistingVote() {
        val optionId = "123"
        client.post()
                .uri("/votes?userId=${PollsControllerTest.trustedUserId}&pollId=${PollsControllerTest.validPollId}&option=${optionId}")
                .exchange()
                .expectStatus().isBadRequest
    }
}