package ru.fmtk.khlystov.hw_polling_app.rest

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@ExtendWith(SpringExtension::class)
@WebMvcTest(UserController::class)
internal class UserControllerTest {

    @Autowired
    lateinit var userController: UserController

    @MockBean
    lateinit var userRepository: UserRepository

    @Test
    @DisplayName("Get user stored in DB to authenticate")
    fun savedUserAuth() {
        val trustedUserName = "StoredInDB"
        val testId = "123456789"
        given(userRepository.findByName(trustedUserName))
                .willReturn(User(testId, trustedUserName))
        val view = userController.userAuth(trustedUserName)
        Assertions.assertEquals(testId, view)
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
        val userId = userController.userAuth(newUserName)
        Assertions.assertEquals(testId, userId)
    }

    @Test
    @DisplayName("Exception if error when saving a user")
    fun exceptionIfErrorSave() {
        val newUserName = "Name of user not in DB"
        val newUser = User(null, newUserName)
        given(userRepository.save(newUser))
                .willReturn(newUser)
        assertThrows<ResponseStatusException> {
            userController.userAuth(newUserName)
        }
    }


}