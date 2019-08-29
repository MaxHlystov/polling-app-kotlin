package ru.fmtk.khlystov.hw_polling_app.rest

import org.junit.jupiter.api.BeforeAll
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
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetailsService
import ru.fmtk.khlystov.hw_polling_app.security.SecurityConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication


@ContextConfiguration(classes = [SecurityConfiguration::class,
    CustomUserDetailsService::class,
    UserController::class])
//@Import(value = [SecurityConfiguration::class, CustomUserDetailsService::class])
@WebFluxTest(UserController::class)
@ExtendWith(SpringExtension::class)
class UserControllerTest {

    @Autowired
    lateinit var context: ApplicationContext

    lateinit var client: WebTestClient

    @MockBean
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    lateinit var trustedUser: User

    companion object {
        const val trustedUserName = "StoredInDB"
        const val testId = "123456789"
        const val email = "test@email.localhost"
        const val password = "111111"
    }

    @BeforeEach
    fun initTest() {
        client = WebTestClient
                .bindToApplicationContext(context)
                .apply { springSecurity() }
                .configureClient()
                .build();
        trustedUser = User(testId, trustedUserName, email, passwordEncoder.encode(password))
    }

    @Test
    @DisplayName("Error when stored existing user")
    fun saveExistingUser() {
        given(userRepository.findByName(trustedUserName))
                .willReturn(Mono.just(trustedUser))
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.empty())
        client.post()
                .uri("/submit?username=$trustedUserName&email=$email&password=$password")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @DisplayName("Accept user in db")
    fun loginUser() {
        given(userRepository.findByName(trustedUserName))
                .willReturn(Mono.just(trustedUser))
        val formData = LinkedMultiValueMap<String, String>()
        formData.add("username", trustedUserName)
        formData.add("password", password)
        client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json("{'id': '$testId', 'name': '$trustedUserName'}")
    }

    @Test
    @DisplayName("Save user not stored in DB")
    fun savedNewUser() {
        val newUserName = "NewInDB"
        val testId = "123456789"
        val newUser = User(testId, newUserName)
        given(userRepository.findByName(newUserName))
                .willReturn(Mono.empty())
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.just(newUser))
        client.post()
                .uri("/submit?username=$newUserName&email=$email&password=$password")
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
                .uri("/submit?username=$newUserName&email=$email&password=$password")
                .exchange()
                .expectStatus().is5xxServerError
    }


}