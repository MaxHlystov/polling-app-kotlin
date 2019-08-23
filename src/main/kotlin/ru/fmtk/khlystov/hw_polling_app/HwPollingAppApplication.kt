package ru.fmtk.khlystov.hw_polling_app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository


@EnableAutoConfiguration
@EnableWebFlux
class HwPollingAppApplication {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun start(userRepository: UserRepository, passwordEncoder: PasswordEncoder): CommandLineRunner {
        return CommandLineRunner {
            userRepository.findAll()
                    .filter { user -> user.password.isEmpty() }
                    .map { user -> User(user.id, user.name, user.email, "111111") }
                    .switchIfEmpty(
                            Flux.just(User(null, "User", "user@localhost", passwordEncoder.encode("111111"))))
                    .flatMap { user -> userRepository.save(user) }
                    .filter { user -> user.id != null }
                    .flatMap { user -> userRepository.findById(user.id ?: "") }
                    .subscribe()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<HwPollingAppApplication>(*args)
}
