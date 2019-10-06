package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetailsService
import ru.fmtk.khlystov.hw_polling_app.security.SecurityConfiguration


@Import(value = [SecurityConfiguration::class,
    CustomUserDetailsService::class,
    PollsController::class
])
@WebFluxTest(PollsController::class)
@ExtendWith(SpringExtension::class)
internal class PollsControllerTest() {

    companion object {
        const val password: String = "111111"
        const val email = "test@email.localhost"
        const val trustedUserName = "StoredInDB"
        const val trustedUserNameWithoutPolls = "User without polls"
        const val notTrustedUserName = "Not trusted user name"
        const val trustedUserId = "123456789"
        const val trustedUserIdWithoutPolls = "777777777777"
        const val notTrustedUserId = "0000000000"
        const val notValidPollId = "0000000000"
        val jsonMapper = jacksonObjectMapper()
    }

    @Autowired
    lateinit var client: WebTestClient

    @Autowired
    lateinit var pollRepository: PollRepository

    @Autowired
    lateinit var trustedUser: User

    @Autowired
    @Qualifier("validPolls")
    lateinit var validPolls: List<Poll>

    @Autowired
    lateinit var validPoll: Poll

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Get list of polls for trusted user")
    fun gettingPollsAuth() {
        val pollsDTO = validPolls.map { poll -> PollDTO(poll, true) }
        val jsonMatch = jsonMapper.writeValueAsString(pollsDTO) ?: ""
        client.get()
                .uri("/polls")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @DisplayName("Get list of polls for not trusted user throw error")
    fun gettingPollsNotAuth() {
        val pollsDTO = validPolls.map { poll -> PollDTO(poll, true) }
        client.get()
                .uri("/polls")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Add a poll")
    fun addPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(Mono.just(newPollSaved))
        val addingRequest = PollDTO(newPoll, true)
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(newPollSaved, true))
        client.post()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(addingRequest), PollDTO::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Edit an existing poll by owner")
    fun editExistingPollByOwner() {
        val poll = Poll("123", "New poll before edit", trustedUser, genPollItems(4))
        val pollSaved = Poll("123", "New poll after edit", trustedUser, genPollItems(3))
        given(pollRepository.findById("123"))
                .willReturn(Mono.just(poll))
        given(pollRepository.save(poll))
                .willReturn(Mono.just(pollSaved))
        val editRequest = PollDTO(poll, true)
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(pollSaved, true))
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), PollDTO::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithUserDetails(trustedUserNameWithoutPolls)
    @DisplayName("Throw exception if edit an existing poll not by owner")
    fun editExistingPollNotByOwner() {
        val poll = Poll("123", "New poll before edit", User("#1234", "Owner of the poll"),
                genPollItems(4))
        given(pollRepository.findById("123"))
                .willReturn(Mono.just(poll))
        val editRequest = PollDTO(poll, true)
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), PollDTO::class.java)
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithUserDetails(trustedUserNameWithoutPolls)
    @DisplayName("Throw exception if edit not an existing poll")
    fun editNotExistingPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(Mono.just(newPollSaved))
        val editRequest = PollDTO(newPoll, true)
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), PollDTO::class.java)
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Delete an existing poll by owner is accepted")
    fun deleteExistingPollByOwner() {
        client.delete()
                .uri("/polls?userId=$trustedUserId&pollId=${validPoll.id}")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    @WithUserDetails(trustedUserNameWithoutPolls)
    @DisplayName("Delete an existing poll not by owner must throw BAD_REQUEST")
    fun deleteExistingPollNotByOwner() {
        client.delete()
                .uri("/polls?userId=$trustedUserIdWithoutPolls&pollId=${validPoll.id}")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Delete not an existing poll must throw BAD_REQUEST")
    fun deleteNotExistingPoll() {
        client.delete()
                .uri("/polls?userId=$trustedUserId&pollId=$notValidPollId")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Configuration
    class TestConfig {

        private lateinit var trustedUser: User
        private lateinit var notTrustedUser: User
        private lateinit var trustedUserWithoutPolls: User
        private lateinit var encodedPassword: String
        private lateinit var validPolls: List<Poll>
        private lateinit var validPoll: Poll

        @Bean(name = ["userRepository"])
        fun getUserRepository(): UserRepository {
            val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
            encodedPassword = passwordEncoder.encode(password)
            val userRepository: UserRepository = mock(UserRepository::class.java)
            trustedUser = User(trustedUserId, trustedUserName, email, encodedPassword)
            trustedUserWithoutPolls = User(trustedUserIdWithoutPolls, trustedUserNameWithoutPolls, email, encodedPassword)
            notTrustedUser = User(notTrustedUserId, notTrustedUserName, "")
            given(userRepository.findByName(trustedUserName))
                    .willReturn(Mono.just(trustedUser))
            given(userRepository.findByName(trustedUserNameWithoutPolls))
                    .willReturn(Mono.just(trustedUserWithoutPolls))
            given(userRepository.findByName(notTrustedUserName))
                    .willReturn(Mono.empty())
            return userRepository
        }

        @Bean(name = ["pollRepository"])
        fun getPollsRepository(): PollRepository {
            val pollRepository = mock(PollRepository::class.java)
            validPolls = generateSequence(1000) { i -> i + 1 }
                    .take(4)
                    .map(Int::toString)
                    .map { id -> Poll(id, "Valid Poll #$id", trustedUser, genPollItems(4)) }
                    .toList()
            validPoll = validPolls[0]
            given(pollRepository.findById(validPoll.id ?: "132321321"))
                    .willReturn(Mono.just(validPoll))
            given(pollRepository.findById(notValidPollId))
                    .willReturn(Mono.empty())
            given(pollRepository.findAll())
                    .willReturn(Flux.fromIterable(validPolls))
            given<Mono<Void>>(pollRepository.delete(Mockito.any()))
                    .willReturn(Mono.empty())
            return pollRepository
        }

        @Bean(name = ["trustedUser"])
        fun getTrustedUser(): User = trustedUser

        @Bean(name = ["validPolls"])
        @Qualifier("validPolls")
        fun getValidPolls(): List<Poll> = validPolls

        @Bean(name = ["validPoll"])
        fun getValidPoll(): Poll = validPoll

    }
}

fun genPollItems(number: Int): List<PollItem> = generateSequence(1) { i -> i + 1 }
        .take(number)
        .map(Int::toString)
        .map { PollItem(it, "Item $it") }
        .toList()