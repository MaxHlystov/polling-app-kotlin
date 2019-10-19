package ru.fmtk.khlystov.hw_polling_app.service

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User

interface UsersService {
    fun findAll(): Flux<User>
    fun findById(id: String): Mono<User>
    fun findByName(name: String): Mono<User>
    fun save(user: User): Mono<User>
    fun delete(user: User): Mono<Void>
}