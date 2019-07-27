package ru.fmtk.khlystov.hw_polling_app.repository

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.transaction.annotation.Transactional
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount

open class VoteRepositoryImpl(private val mongoTemplate: MongoTemplate) : VoteRepositoryCustom {

    @Transactional
    override fun save(vote: Vote): Vote {
        val query = Query.query(Criteria().andOperator(
                Criteria.where("user.\$id").`is`(ObjectId(vote.user.id)),
                Criteria.where("poll.\$id").`is`(ObjectId(vote.poll.id))))
        val votes = mongoTemplate.find(query, Vote::class.java)
        if (votes.size == 0) {
            return mongoTemplate.save(vote)
        }
        val oldVote = votes[0]
        return mongoTemplate.save(Vote(oldVote.id, oldVote.user, oldVote.poll, vote.pollItem))
    }

    override fun getVotes(poll: Poll): List<VotesCount> {
        //db.vote.aggregate({ $match: {"poll.$id" : ObjectId("5d3b7851d606dd318817430b") }},
        //                  { $group: {_id: "$pollItem", total: { $sum: 1}}})
        val agg = newAggregation(
                match(Criteria.where("poll.\$id").`is`(ObjectId(poll.id))),
                group("pollItem").count().`as`("total"),
                project("total").and("pollItem").previousOperation())
        val votesFound = mongoTemplate.aggregate(agg, Vote::class.java, VotesCount::class.java)
                .mappedResults
        // We expect, there will be not greater than 100 poll items, so O(n^2) is not too bad.
        return poll.items.map { pollItem ->
            val vote = votesFound.firstOrNull { votesCount -> votesCount.pollItem.id == pollItem.id }
            VotesCount(pollItem, vote?.total ?: 0)
        }.toList()
    }
}
