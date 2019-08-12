package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User

@Repository
interface UserRepository : ReactiveMongoRepository<User, String> {
    fun findByName(name: String): Mono<User>
}