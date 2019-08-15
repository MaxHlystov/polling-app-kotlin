package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByName(name: String): User?
}