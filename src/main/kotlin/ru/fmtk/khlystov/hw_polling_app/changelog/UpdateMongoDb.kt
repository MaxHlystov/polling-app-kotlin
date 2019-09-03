package ru.fmtk.khlystov.hw_polling_app.changelog

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Component
class UpdateMongoDb(val userRepository: UserRepository,
                    val passwordEncoder: PasswordEncoder) {
    val log: Logger = LoggerFactory.getLogger(UpdateMongoDb::class.java)

    fun update() {
        setDefaultPassword()
        setForAllUsersRoleByDefault()
        createDefaultAdmin()
    }

    fun setDefaultPassword() {
        userRepository.findAll()
                .switchIfEmpty(
                        Flux.just(User(null,
                                "User",
                                "user@localhost",
                                "")))
                .filter { user -> user.password.isEmpty() }
                .map { user ->
                    User(user.id, user.name, user.email,
                            passwordEncoder.encode("111111"))
                }
                .flatMap { user -> userRepository.save(user) }
                .doOnNext { user -> log.info("Set \"111111\" password for user ${user.name}") }
                .subscribe()
    }

    fun setForAllUsersRoleByDefault() {
        userRepository.findAll()
                .filter { user -> user.roles.isEmpty() }
                .map { user ->
                    user.newWithRoles(listOf("User"))
                }
                .flatMap { user -> userRepository.save(user) }
                .doOnNext { user -> log.info("Set \"User\" role for user ${user.name}") }
                .subscribe()
    }

    fun createDefaultAdmin() {
        val adminName = "Admin"
        userRepository.findByName(adminName)
                .switchIfEmpty(Mono.defer {
                    val admin = User(null,
                            adminName,
                            "admin@localhost",
                            passwordEncoder.encode("111111"),
                            true,
                            true,
                            true,
                            true,
                            listOf("ADMIN", "USER").toSet())
                    userRepository.save(admin)
                            .doOnNext { log.info("Created user \"Admin\" with password \"111111\" and role ADMIN.") }
                })
                .subscribe()
    }
}

