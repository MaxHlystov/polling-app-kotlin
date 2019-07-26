package ru.fmtk.khlystov.hw_polling_app.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.Vote


@Repository
class VoteRepositoryImpl(private val mongoTemplate: MongoTemplate) : voteRepositoryCustom {

    @Transactional
    override fun save(vote: Vote): Vote {
        val query = Query.query(Criteria().andOperator(
                Criteria.where("user.id").`is`(ObjectId(vote.user.id)),
                Criteria.where("poll.id").`is`(ObjectId(vote.poll.id))))
        mongoTemplate.remove(query, Vote::class.java)
        return mongoTemplate.save(vote)
    }

    override fun getVotes(poll: Poll): Map<PollItem, Int> {
        val agg = newAggregation(
                match(Criteria.where("poll.id").`is`(poll.id)),
                group("pollItem").count().`as`("total"),
                project("total").and("pollItem").previousOperation())
        val itemsCounts: List<VotesCount> = mongoTemplate.aggregate(agg, Vote::class.java, VotesCount::class.java).getMappedResults()
        return itemsCounts.asSequence().groupingBy { it -> it.pollItem }.eachCount()
    }
}

private class VotesCount(val pollItem: PollItem, val total: Long)