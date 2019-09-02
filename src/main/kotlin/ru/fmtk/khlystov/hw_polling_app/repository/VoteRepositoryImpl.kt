package ru.fmtk.khlystov.hw_polling_app.repository

import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.core.query.TextQuery
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import reactor.core.publisher.Operators.`as`


open class VoteRepositoryImpl(private val mongoTemplate: ReactiveMongoTemplate) : VoteRepositoryCustom {

    @Transactional
    override fun save(vote: Vote): Mono<Vote> {
        val query = Query.query(Criteria().andOperator(
                Criteria.where("user.\$id").`is`(ObjectId(vote.user.id)),
                Criteria.where("poll.\$id").`is`(ObjectId(vote.poll.id))))
        return mongoTemplate.find(query, Vote::class.java)
                .take(1).toMono()
                .flatMap { oldVote ->
                    mongoTemplate.save(Vote(oldVote.id, oldVote.user, oldVote.poll, vote.pollItem))
                }
                .switchIfEmpty(mongoTemplate.save(vote).toMono())

    }

    override fun getVotes(poll: Poll): Flux<VotesCount> {
        // Mongodb query:
        // db.poll.aggregate([{ $match: {"_id" : ObjectId("5d515912891a7728c45c119e") }},
        //    {$project: {"items": 1, "_id": 0}},
        //    {$unwind: "$items"},
        //    {$lookup: {from: "vote", localField: "items._id", foreignField: "pollItem._id", as: "item"}},
        //    {$project: {_id: "$items", total: { $size: "$item"}}}])
        val agg = newAggregation(
                match(Criteria.where("_id").`is`(ObjectId(poll.id))),
                project("items").andExclude("_id"),
                unwind("items"),
                lookup("vote", "items._id", "pollItem._id", "item"),
                project()
                        .andExclude("_id")
                        .andExpression("items").`as`("pollItem")
                        .and(ArrayOperators.arrayOf("item").length()).`as`("total")
        )
        return mongoTemplate.aggregate(agg, Poll::class.java, VotesCount::class.java)
    }
}
