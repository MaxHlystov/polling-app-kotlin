package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.AnnotationConfigContextLoader
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetailsService
import ru.fmtk.khlystov.hw_polling_app.security.SecurityConfiguration

@ContextConfiguration(classes = [SecurityConfiguration::class, CustomUserDetailsService::class])
@WebFluxTest(PollsController::class)
@ExtendWith(SpringExtension::class)
internal class PollsControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var pollRepository: PollRepository

    companion object {
        const val trustedUserName = "StoredInDB"
        const val trustedUserNameWithoutPolls = "User without polls"
        const val notTrustedUserName = "Not trusted user name"
        const val trustedUserId = "123456789"
        const val trustedUserIdWithoutPolls = "777777777777"
        const val notTrustedUserId = "0000000000"
        const val notValidPollId = "0000000000"
        val trustedUser = User(trustedUserId, trustedUserName, "", "111111")
        val notTrustedUser = User(notTrustedUserId, notTrustedUserName, "111111")
        val trustedUserWithoutPolls = User(trustedUserIdWithoutPolls, trustedUserNameWithoutPolls)
        val validPolls = generateSequence(1000) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { id -> Poll(id, "Valid Poll #$id", trustedUser, genPollItems(4)) }
                .toList()
        val validPoll = validPolls[0]
        val validPollId = validPoll.id ?: "1234"
        private fun genPollItems(number: Int): List<PollItem> = generateSequence(1) { i -> i + 1 }
                .take(number)
                .map(Int::toString)
                .map { PollItem(it, "Item $it") }
                .toList()

        val jsonMapper = jacksonObjectMapper()
    }

    @BeforeEach
    fun initMockRepositories() {
        given(userRepository.findByName(trustedUserName))
                .willReturn(Mono.just(trustedUser))
        given(userRepository.findByName(trustedUserNameWithoutPolls))
                .willReturn(Mono.just(trustedUserWithoutPolls))
        given(userRepository.findByName(notTrustedUserName))
                .willReturn(Mono.empty())
        given(pollRepository.findById(validPollId))
                .willReturn(Mono.just(validPoll))
        given(pollRepository.findById(notValidPollId))
                .willReturn(Mono.empty())
        given(pollRepository.findAll())
                .willReturn(Flux.fromIterable(validPolls))
        given<Mono<Void>>(pollRepository.delete(Mockito.any()))
                .willReturn(Mono.empty())
    }

    @Test
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Get list of polls for trusted user")
    fun gettingPolls() {
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
    @WithMockUser(username = trustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Add a poll")
    fun addPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(Mono.just(newPollSaved))
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(newPollSaved, true))
        client.post()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(addingRequest), AddOrEditRequestDTO::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithMockUser(username = notTrustedUserName, authorities = ["ROLE_ADMIN"])
    @DisplayName("Edit an existing poll by owner")
    fun editExistingPollByOwner() {
        val poll = Poll("123", "New poll before edit", trustedUser, genPollItems(4))
        val pollSaved = Poll("123", "New poll after edit", trustedUser, genPollItems(3))
        given(pollRepository.findById("123"))
                .willReturn(Mono.just(poll))
        given(pollRepository.save(poll))
                .willReturn(Mono.just(pollSaved))
        val editRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(poll, true))
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(pollSaved, true))
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), AddOrEditRequestDTO::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithMockUser(username = trustedUserNameWithoutPolls, authorities = ["ROLE_ADMIN"])
    @DisplayName("Throw exception if edit an existing poll not by owner")
    fun editExistingPollNotByOwner() {
        val poll = Poll("123", "New poll before edit", User("#1234", "Owner of the poll"),
                genPollItems(4))
        given(pollRepository.findById("123"))
                .willReturn(Mono.just(poll))
        val editRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(poll, true))
        val jsonRequest = jsonMapper.writeValueAsString(editRequest)
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), AddOrEditRequestDTO::class.java)
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithMockUser(username = trustedUserNameWithoutPolls, authorities = ["ROLE_ADMIN"])
    @DisplayName("Throw exception if edit not an existing poll")
    fun editNotExistingPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(Mono.just(newPollSaved))
        val editRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        client.put()
                .uri("/polls")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), AddOrEditRequestDTO::class.java)
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @DisplayName("Delete an existing by owner is accepted")
    fun deleteExistingPollByOwner() {
        client.delete()
                .uri("/polls?userId=$trustedUserId&pollId=$validPollId")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    @DisplayName("Delete an existing poll not by owner must throw BAD_REQUEST")
    fun deleteExistingPollNotByOwner() {
        client.delete()
                .uri("/polls?userId=$trustedUserIdWithoutPolls&pollId=$validPollId")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @DisplayName("Delete not an existing poll must throw BAD_REQUEST")
    fun deleteNotExistingPoll() {
        client.delete()
                .uri("/polls?userId=$trustedUserId&pollId=$notValidPollId")
                .exchange()
                .expectStatus().isBadRequest
    }
}
