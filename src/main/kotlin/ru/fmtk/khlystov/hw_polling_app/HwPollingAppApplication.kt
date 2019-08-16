package ru.fmtk.khlystov.hw_polling_app

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.web.reactive.config.EnableWebFlux

@EnableAutoConfiguration
@EnableWebFluxSecurity
@EnableWebFlux
class HwPollingAppApplication

fun main(args: Array<String>) {
	runApplication<HwPollingAppApplication>(*args)
}
