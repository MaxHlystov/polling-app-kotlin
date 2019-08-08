package ru.fmtk.khlystov.hw_polling_app.rest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.ui.Model
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.AddOrEditRequestDTO
import ru.fmtk.khlystov.hw_polling_app.rest.dto.PollDTO
import java.util.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(PollsController::class)
internal class PollsControllerTest {

    @Autowired
    lateinit var pollsController: PollsController

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var pollRepository: PollRepository

    @MockBean
    lateinit var voteRepository: VoteRepository

    @MockBean
    lateinit var model: Model

    companion object {
        val trustedUserName = "StoredInDB"
        val trustedUserId = "123456789"
        val notTrustedUserId = "0000000000"
        val notValidPollId = "0000000000"
        val trustedUser = User(trustedUserId, trustedUserName)
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
    }

    @BeforeEach
    fun initMockRepositories() {
        given(userRepository.findById(trustedUserId))
                .willReturn(Optional.ofNullable(trustedUser))
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
        val res = pollsController.listPolls(trustedUserId)
        assertEquals(validPolls.map { poll -> PollDTO(poll, false) }, res)
    }

    @Test
    @DisplayName("Add a poll")
    fun addPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(newPollSaved)
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        val res = pollsController.addPoll(addingRequest)
        assertEquals(PollDTO(newPollSaved, true), res)
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
        val res = pollsController.editPoll(addingRequest)
        assertEquals(PollDTO(pollSaved, true), res)
    }

    @Test
    @DisplayName("Throw exception if edit an existing poll not by owner")
    fun editExistingPollNotByOwner() {
        val poll = Poll("123", "New poll before edit", User("#1234", "Owner of the poll"),
                genPollItems(4))
        given(pollRepository.findById("123"))
                .willReturn(Optional.ofNullable(poll))
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(poll, true))
        assertThrows<ResponseStatusException> { pollsController.editPoll(addingRequest) }
    }

    @Test
    @DisplayName("Throw exception if edit not an existing poll")
    fun editNotExistingPoll() {
        val newPoll = Poll(null, "New poll", trustedUser, genPollItems(4))
        val newPollSaved = Poll("123", "New poll", trustedUser, genPollItems(4))
        given(pollRepository.save(newPoll))
                .willReturn(newPollSaved)
        val addingRequest = AddOrEditRequestDTO(trustedUserId, PollDTO(newPoll, true))
        assertThrows<ResponseStatusException> { pollsController.editPoll(addingRequest) }
    }

    @Test
    @DisplayName("Delete an existing poll not by owner")
    fun deleteExistingPollNotByOwner() {
        assertThrows<ResponseStatusException> { pollsController.deletePoll("123", trustedUserId) }
    }

    @Test
    @DisplayName("Delete not an existing poll")
    fun deleteNotExistingPoll() {
    }
}