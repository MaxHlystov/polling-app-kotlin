package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote

@Repository
class VoteRepositoryImpl(private val mongoTemplate: MongoTemplate) : voteRepositoryCusom {
    override fun saveVote(user: User, poll: Poll, itemId: String): Vote {
        TODO("not implemented")
    }

    override fun getVotes(poll: Poll): Map<PollItem, Int> {
        TODO("not implemented")
    }
}