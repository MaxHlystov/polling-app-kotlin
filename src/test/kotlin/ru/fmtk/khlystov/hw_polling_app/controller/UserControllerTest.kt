package ru.fmtk.khlystov.hw_polling_app.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
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
        val redirectString = "redirect:/polls/list?userId=$testId"
        given(userRepository.findByName(trustedUserName))
                .willReturn(User(testId, trustedUserName))
        val view = userController.userAuth(trustedUserName)
        Assertions.assertEquals(redirectString, view)
    }

    @Test
    @DisplayName("Save user not stored in DB to authenticate")
    fun notSavedUserAuth() {
        val newUserName = "NewInDB"
        val testId = "123456789"
        val newUser = User(testId, newUserName)
        val redirectString = "redirect:/polls/list?userId=$testId"
        given(userRepository.findByName(newUserName))
                .willReturn(null)
        given(userRepository.save(User(newUserName)))
                .willReturn(newUser)
        val view = userController.userAuth(newUserName)
        Assertions.assertEquals(redirectString, view)
    }
}