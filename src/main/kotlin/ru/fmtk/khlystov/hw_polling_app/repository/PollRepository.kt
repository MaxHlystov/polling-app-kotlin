package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User

@Repository
interface PollRepository : MongoRepository<Poll, String> {
    fun findAllByOwner(user: User): List<Poll>
}