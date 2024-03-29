package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.Poll

@Repository
interface PollRepository : ReactiveMongoRepository<Poll, String>