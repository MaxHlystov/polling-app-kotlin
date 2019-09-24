package ru.fmtk.khlystov.hw_polling_app.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetailsService
import ru.fmtk.khlystov.hw_polling_app.security.Roles
import ru.fmtk.khlystov.hw_polling_app.security.SecurityConfiguration

@Import(value = [SecurityConfiguration::class,
    CustomUserDetailsService::class,
    UserController::class
])
@WebFluxTest(UserController::class)
@ExtendWith(SpringExtension::class)
class UserControllerTest {
    @Autowired
    lateinit var client: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var trustedUser: User

    @Autowired
    lateinit var adminUser: User

    @Autowired
    @Qualifier("validUsers")
    lateinit var validUsers: List<User>

    companion object {
        const val testId = "123456789"
        const val adminId = "555555555"
        const val notTrustedUserId = "notTrustedUserId"
        const val adminUserName = "Admin name"
        const val trustedUserName = "StoredInDB"
        const val notTrustedUserName = "Not trusted user name"
        const val email = "test@email.localhost"
        const val password = "111111"
        val jsonMapper = jacksonObjectMapper()
    }

    @Test
    @DisplayName("Error when stored existing user")
    fun saveExistingUser() {
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
        val formData = LinkedMultiValueMap<String, String>()
        formData.add("username", trustedUserName)
        formData.add("password", password)
        client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
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
        val testId = "NewInDBId"
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
        given(userRepository.findByName(newUserName))
                .willReturn(Mono.empty())
        given<Mono<User>>(userRepository.save(Mockito.any()))
                .willReturn(Mono.just(newUser))
        client.post()
                .uri("/submit?username=$newUserName&email=$email&password=$password")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @WithUserDetails(adminUserName)
    @DisplayName("Get list of users for admin is correct")
    fun gettingUsersForAdmin() {
        val dtoUsers = validUsers.map(::UserDTO)
        val jsonMatch = jsonMapper.writeValueAsString(dtoUsers) ?: ""
        client.get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Getting list of users for USER role is denied")
    fun gettingUsersForUserIsDenied() {
        client.get()
                .uri("/users")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @WithUserDetails(adminUserName)
    @DisplayName("Edit an existing user by ADMIN role is correct")
    fun editExistingUserByAdminRole() {
        val userForEdit = User("123", "User before edit", roles = listOf("USER").toSet())
        val savedUser = User("123", "User after edit", roles = listOf("USER").toSet())
        given(userRepository.findById("123"))
                .willReturn(Mono.just(userForEdit))
        given(userRepository.save(userForEdit))
                .willReturn(Mono.just(savedUser))
        val userDTO = UserDTO(userForEdit)
        val jsonMatch = jsonMapper.writeValueAsString(UserDTO(savedUser))
        client.put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(userDTO), UserDTO::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json(jsonMatch)
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Throw exception if edit an existing user not by ADMIN role")
    fun editExistingUserNotByAdminRole() {
        val userDTO = UserDTO(trustedUser)
        client.put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(userDTO), UserDTO::class.java)
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @WithUserDetails(adminUserName)
    @DisplayName("Throw exception if edit not an existing user by ADMIN role")
    fun editNotExistingPoll() {
        val userForEdit = User("123", "User before edit", roles = listOf("USER").toSet())
        val savedUser = User("123", "User after edit", roles = listOf("USER").toSet())
        given(userRepository.findById("123"))
                .willReturn(Mono.empty())
        given(userRepository.save(userForEdit))
                .willReturn(Mono.just(savedUser))
        val editRequest = UserDTO(userForEdit)
        client.put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(editRequest), UserDTO::class.java)
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    @WithUserDetails(adminUserName)
    @DisplayName("Delete an existing user by ADMIN role is accepted")
    fun deleteExistingUserByAdminRole() {
        client.delete()
                .uri("/users?userid=${testId}")
                .exchange()
                .expectStatus().isOk
    }

    @Test
    @WithUserDetails(trustedUserName)
    @DisplayName("Delete an existing poll not by ADMIN role must throw BAD_REQUEST")
    fun deleteExistingUserNotByUserRole() {
        client.delete()
                .uri("/users?userid=${notTrustedUserId}")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Test
    @WithUserDetails(adminUserName)
    @DisplayName("Delete not an existing user by ADMIN role must throw an exception")
    fun deleteNotExistingUserByAdminRole() {
        client.delete()
                .uri("/users?userid=${notTrustedUserId}")
                .exchange()
                .expectStatus().is5xxServerError
    }

    @Configuration
    class TestConfig {

        private lateinit var trustedUser: User
        private lateinit var adminUser: User
        private lateinit var encodedPassword: String
        private lateinit var validUsers: List<User>

        init {
            val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
            encodedPassword = passwordEncoder.encode(password)
            trustedUser = User(testId, trustedUserName, email, encodedPassword,
                    roles = listOf<String>(Roles.User.role).toSet())
            adminUser = User(adminId, adminUserName, email, encodedPassword,
                    roles = listOf<String>(Roles.Admin.role).toSet())
            validUsers = listOf(adminUser, trustedUser)
        }

        @Bean(name = ["userRepository"])
        fun getUserRepository(): UserRepository {
            val userRepository: UserRepository = Mockito.mock(UserRepository::class.java)
            given(userRepository.findAll())
                    .willReturn(Flux.fromIterable(validUsers))
            validUsers.forEach { user ->
                given(userRepository.findByName(user.name))
                        .willReturn(Mono.just(user))
                given(userRepository.findById(user.id ?: ""))
                        .willReturn(Mono.just(user))
            }
            given(userRepository.findByName(notTrustedUserName))
                    .willReturn(Mono.empty())
            given<Mono<Void>>(userRepository.delete(Mockito.any()))
                    .willReturn(Mono.empty())
            return userRepository
        }

        @Bean(name = ["trustedUser"])
        fun getTrustedUser(): User = trustedUser

        @Bean(name = ["adminUser"])
        fun getAdminUser(): User = adminUser

        @Bean(name = ["validUsers"])
        @Qualifier("validUsers")
        fun getValidUsers(): List<User> = validUsers
    }
}