package ru.fmtk.khlystov.hw_polling_app.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

fun <T> Flux<T>.withRandomDelay(): Flux<T> = this.flatMap { value ->
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val newFlux = Flux.just(value)
    val rand = Random()
    if (rand.nextInt(3) == 0) {
        newFlux.delaySubscription(Duration.ofSeconds(3))
        log.info("Delay in 3 seconds for value $value.")
    }
    newFlux
}

fun <T> Mono<T>.withRandomDelay(): Mono<T> = this.flatMap { value ->
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    val newFlux = Mono.just(value)
    val rand = Random()
    if (rand.nextInt(3) == 0) {
        newFlux.delaySubscription(Duration.ofSeconds(3))
        log.info("Delay in 3 seconds for value $value.")
    }
    newFlux
}