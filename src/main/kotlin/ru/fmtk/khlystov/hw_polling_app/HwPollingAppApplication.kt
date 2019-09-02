package ru.fmtk.khlystov.hw_polling_app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Flux
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository


@SpringBootApplication
@EnableWebFlux
class HwPollingAppApplication {

/*    val log: Logger = LoggerFactory.getLogger(HwPollingAppApplication::class.java)

    @Bean
    fun start(@Autowired userRepository: UserRepository,
              @Autowired passwordEncoder: PasswordEncoder): CommandLineRunner {
        return CommandLineRunner {
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
    }*/
}

fun main(args: Array<String>) {
    runApplication<HwPollingAppApplication>(*args)
}
