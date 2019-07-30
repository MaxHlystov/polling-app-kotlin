package ru.fmtk.khlystov.hw_polling_app.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.ui.Model
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

    @Test
    @DisplayName("Open add poll view for trusted user")
    fun openAddPollViewForTrustedUser() {
        val trustedUserName = "StoredInDB"
        val trustedId = "123456789"
        BDDMockito.given(userRepository.findById(trustedId))
                .willReturn(Optional.ofNullable(User(trustedId, trustedUserName)))
        val res = pollsController.addPoll(trustedId, model)
        BDDMockito.verify(model).addAttribute("addOperation", true)
        assertEquals("polls/edit", res)
    }

    @Test
    @DisplayName("Open authentication view for not trusted user")
    fun openAuthViewForNotTrustedUser() {
        val notTrustedId = "123456789"
        BDDMockito.given(userRepository.findById(notTrustedId))
                .willReturn(Optional.empty())
        val res = pollsController.addPoll(notTrustedId, model)
        assertEquals("redirect:/", res)
    }
}