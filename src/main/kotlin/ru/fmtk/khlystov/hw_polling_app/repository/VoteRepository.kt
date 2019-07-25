package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote

@Repository
interface VoteRepository : MongoRepository<Vote, String>, voteRepositoryCusom

interface voteRepositoryCusom {
    fun saveVote(user: User, poll: Poll, item: String): Vote
    fun getVotes(poll: Poll): Map<PollItem, Int>
}