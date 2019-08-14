package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.ui.Model
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
internal class PollsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var pollRepository: PollRepository

    @MockBean
    lateinit var model: Model

    companion object {
        val trustedUserName = "StoredInDB"
        val trustedUserNameWithoutPolls = "User without polls"
        val trustedUserId = "123456789"
        val trustedUserIdWithoutPolls = "777777777777"
        val notTrustedUserId = "0000000000"
        val notValidPollId = "0000000000"
        val trustedUser = User(trustedUserId, trustedUserName)
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
        given(userRepository.findById(trustedUserId))
                .willReturn(Optional.ofNullable(trustedUser))
        given(userRepository.findById(trustedUserIdWithoutPolls))
                .willReturn(Optional.ofNullable(trustedUserWithoutPolls))
        given(userRepository.findById(notTrustedUserId))
                .willReturn(Optional.empty())
        given(pollRepository.findById(validPollId))
                .willReturn(Optional.ofNullable(validPoll))
        given(pollRepository.findById(notValidPollId))
                .willReturn(Optional.empty())
        given(pollRepository.findAll())
                .willReturn(validPolls)
    }

    @Test
    @DisplayName("Get list of polls")
    fun gettingPolls() {
        val pollsDTO = validPolls.map { poll -> PollDTO(poll, true) }
        val jsonMatch = jsonMapper.writeValueAsString(pollsDTO) ?: ""
        mockMvc.perform(MockMvcRequestBuilders.get("/polls?userId=$trustedUserId"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(jsonMatch))
    }

    @Test
    @DisplayName("Add a poll")
    fun addPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(newPollSaved)
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        val jsonRequest = jsonMapper.writeValueAsString(addingRequest)
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(newPollSaved, true))
        mockMvc.perform(MockMvcRequestBuilders
                .post("/polls")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(jsonMatch))
    }

    @Test
    @DisplayName("Edit an existing poll by owner")
    fun editExistingPollByOwner() {
        val poll = Poll("123", "New poll before edit", trustedUser, genPollItems(4))
        val pollSaved = Poll("123", "New poll after edit", trustedUser, genPollItems(3))
        given(pollRepository.findById("123"))
                .willReturn(Optional.ofNullable(poll))
        given(pollRepository.save(poll))
                .willReturn(pollSaved)
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(poll, true))
        val jsonRequest = jsonMapper.writeValueAsString(addingRequest)
        val jsonMatch = jsonMapper.writeValueAsString(PollDTO(pollSaved, true))
        mockMvc.perform(MockMvcRequestBuilders
                .put("/polls")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(jsonMatch))
    }

    @Test
    @DisplayName("Throw exception if edit an existing poll not by owner")
    fun editExistingPollNotByOwner() {
        val poll = Poll("123", "New poll before edit", User("#1234", "Owner of the poll"),
                genPollItems(4))
        given(pollRepository.findById("123"))
                .willReturn(Optional.ofNullable(poll))
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(poll, true))
        val jsonRequest = jsonMapper.writeValueAsString(addingRequest)
        mockMvc.perform(MockMvcRequestBuilders
                .put("/polls")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Throw exception if edit not an existing poll")
    fun editNotExistingPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(newPollSaved)
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        val jsonRequest = jsonMapper.writeValueAsString(addingRequest)
        mockMvc.perform(MockMvcRequestBuilders
                .put("/polls")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Delete an existing by owner is accepted")
    fun deleteExistingPollByOwner() {
        mockMvc.perform(delete("/polls?userId=$trustedUserId&pollId=$validPollId"))
                .andExpect(MockMvcResultMatchers.status().isAccepted)
    }

    @Test
    @DisplayName("Delete an existing poll not by owner must throw BAD_REQUEST")
    fun deleteExistingPollNotByOwner() {
        mockMvc.perform(delete("/polls?userId=$trustedUserIdWithoutPolls&pollId=$validPollId"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @DisplayName("Delete not an existing poll must throw BAD_REQUEST")
    fun deleteNotExistingPoll() {
        mockMvc.perform(delete("/polls?userId=$trustedUserId&pollId=$notValidPollId"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}