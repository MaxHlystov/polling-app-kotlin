package ru.fmtk.khlystov.hw_polling_app.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.ui.Model
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.repository.VoteRepository
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
        val validPollId = "ValidPoll1234"
        val notValidPollId = "0000000000"
        val trustedUser = User(trustedUserId, trustedUserName)
        val pollItems = generateSequence(1) { i -> i + 1 }
                .take(4)
                .map(Int::toString)
                .map { PollItem(it, "Item $it") }
                .toList()
        val validPoll = Poll(validPollId, "Valid Poll", trustedUser, pollItems)
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
    }

    @Test
    @DisplayName("Getting a list of polls ")
    fun getingPolls() {
        val res = pollsController.listPolls(trustedUserId)
        assertSame(listOf(trustedUser), res)
    }

}