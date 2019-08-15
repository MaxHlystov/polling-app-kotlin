package ru.fmtk.khlystov.hw_polling_app.rest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO


@SpringBootTest
@AutoConfigureMockMvc
internal class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userRepository: UserRepository

    @Test
    @DisplayName("Get user stored in DB to authenticate")
    fun savedUserAuth() {
        val trustedUserName = "StoredInDB"
        val testId = "123456789"
        val trastedUser = User(testId, trustedUserName)
        val trastedUserDTO = UserDTO(trastedUser)
        given(userRepository.findByName(trustedUserName))
                .willReturn(trastedUser)
        mockMvc.perform(post("/auth?userName=" + trustedUserName))
                .andExpect(status().isOk)
                .andExpect(content().json("{'id': '$testId', 'name': '$trustedUserName'}"))
    }

    @Test
    @DisplayName("Save user not stored in DB to authenticate")
    fun notSavedUserAuth() {
        val newUserName = "NewInDB"
        val testId = "123456789"
        val newUser = User(testId, newUserName)
        given(userRepository.findByName(newUserName))
                .willReturn(null)
        given(userRepository.save(User(newUserName)))
                .willReturn(newUser)
        mockMvc.perform(post("/auth?userName=" + newUserName))
                .andExpect(status().isOk)
                .andExpect(content().json("{'id': '$testId', 'name': '$newUserName'}"))
    }

    @Test
    @DisplayName("Exception if error when saving a user")
    fun exceptionIfErrorSave() {
        val newUserName = "Name of user not in DB"
        val newUser = User(null, newUserName)
        given(userRepository.save(newUser))
                .willReturn(newUser)
        mockMvc.perform(post("/auth?userName=" + newUserName))
                .andExpect(status().isInternalServerError)
    }


}