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
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import ru.fmtk.khlystov.hw_polling_app.changelog.UpdateMongoDb


@SpringBootApplication
@EnableWebFlux
class HwPollingAppApplication {
    val log: Logger = LoggerFactory.getLogger(HwPollingAppApplication::class.java)

    @Autowired
    lateinit var updateMongoDb: UpdateMongoDb

    @Bean
    fun start(): CommandLineRunner {
        return CommandLineRunner {
            updateMongoDb.update()

        }
    }
}

fun main(args: Array<String>) {
    runApplication<HwPollingAppApplication>(*args)
}
