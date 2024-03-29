package ru.fmtk.khlystov.hw_polling_app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.config.EnableWebFlux
import ru.fmtk.khlystov.hw_polling_app.changelog.UpdateMongoDb

@SpringBootApplication
@EnableCircuitBreaker
@EnableWebFlux
class HwPollingAppApplication {
    val log: Logger = LoggerFactory.getLogger(HwPollingAppApplication::class.java)

    @Autowired
    lateinit var updateMongoDb: UpdateMongoDb

    @Value("\${spring.data.mongodb.host}")
    lateinit var host: String

    @Value("\${spring.data.mongodb.port}")
    lateinit var port: String

    @Value("\${spring.data.mongodb.database}")
    lateinit var database: String

    @Bean
    fun start(): CommandLineRunner {
        log.info("Mongo connection string: $host:$port/$database")
        return CommandLineRunner {
            updateMongoDb.update()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<HwPollingAppApplication>(*args)
}
