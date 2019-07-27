package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount

@Repository
interface VoteRepository : MongoRepository<Vote, String>, VoteRepositoryCustom {
    fun findAllByPoll(poll: Poll): List<Vote>
    fun findAllByUser(user: User): List<Vote>
    fun countAllByPoll(poll: Poll): Long
    fun findAllByPollAndUser(poll: Poll, user: User): List<Vote>
}

interface VoteRepositoryCustom {
    fun save(vote: Vote): Vote
    fun getVotes(poll: Poll): List<VotesCount>
}