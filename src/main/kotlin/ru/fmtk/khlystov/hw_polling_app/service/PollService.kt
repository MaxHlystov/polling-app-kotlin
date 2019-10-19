package ru.fmtk.khlystov.hw_polling_app.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll

interface PollService {
    fun findAll(): Flux<Poll>
    fun findById(id: String): Mono<Poll>
    fun save(poll: Poll): Mono<Poll>
    fun delete(poll: Poll): Mono<Void>
}