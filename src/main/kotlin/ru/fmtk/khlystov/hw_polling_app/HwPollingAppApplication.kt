package ru.fmtk.khlystov.hw_polling_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.web.reactive.config.EnableWebFlux

@EnableReactiveMongoRepositories
@SpringBootApplication
class HwPollingAppApplication

fun main(args: Array<String>) {
	runApplication<HwPollingAppApplication>(*args)
}
