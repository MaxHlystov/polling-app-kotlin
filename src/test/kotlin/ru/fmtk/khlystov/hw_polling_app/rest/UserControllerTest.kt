package ru.fmtk.khlystov.hw_polling_app.rest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO

@WebFluxTest(UserController::class)
@ExtendWith(SpringExtension::class)
internal class UserControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockBean
    lateinit var userRepository: UserRepository

    @Test
    @DisplayName("Get user stored in DB to authenticate")
    fun savedUserAuth() {
        val trustedUserName = "StoredInDB"
        val testId = "123456789"
        val trustedUser = User(testId, trustedUserName)
        given(userRepository.findByName(trustedUserName))
                .willReturn(Mono.just(trustedUser))
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.empty())
        client.post()
                .uri("/auth?userName=$trustedUserName")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json("{'id': '$testId', 'name': '$trustedUserName'}")
    }

    @Test
    @DisplayName("Save user not stored in DB to authenticate")
    fun notSavedUserAuth() {
        val newUserName = "NewInDB"
        val testId = "123456789"
        val newUser = User(testId, newUserName)
        given(userRepository.findByName(newUserName))
                .willReturn(Mono.empty())
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.just(newUser))
        client.post()
                .uri("/auth?userName=$newUserName")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json("{'id': '$testId', 'name': '$newUserName'}")
    }

    @Test
    @DisplayName("Exception if error when saving a user")
    fun exceptionIfErrorSave() {
        val newUserName = "Name of user not in DB"
        val newUser = User(null, newUserName)
        given(userRepository.findByName(Mockito.anyString()))
                .willReturn(Mono.empty())
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.just(newUser))
        client.post()
                .uri("/auth?userName=$newUserName")
                .exchange()
                .expectStatus().is5xxServerError
    }


}