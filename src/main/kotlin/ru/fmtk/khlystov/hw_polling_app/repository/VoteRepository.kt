package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.*

@Repository
interface VoteRepository : MongoRepository<Vote, String>, voteRepositoryCustom {
    fun findAllByPoll(poll: Poll): List<Vote>
    fun findAllByUser(user: User): List<Vote>
    fun countAllByPoll(poll: Poll): Long
    fun deleteByPollAndUser(poll: Poll, user: User)
}

interface voteRepositoryCustom {
    fun save(vote: Vote): Vote
    fun getVotes(poll: Poll): List<VotesCount>
}