package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.data.rest.core.annotation.RestResource
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User

@RepositoryRestResource(path = "admin_users")
interface UserRepository : ReactiveMongoRepository<User, String> {

    @RestResource(rel = "find", path = "find")
    fun findByName(name: String): Mono<User>
}