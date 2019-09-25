package ru.fmtk.khlystov.hw_polling_app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType
import org.springframework.web.reactive.config.EnableWebFlux
import ru.fmtk.khlystov.hw_polling_app.changelog.UpdateMongoDb

@SpringBootApplication
@EnableWebFlux
@EnableHypermediaSupport(type = [HypermediaType.HAL])
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
