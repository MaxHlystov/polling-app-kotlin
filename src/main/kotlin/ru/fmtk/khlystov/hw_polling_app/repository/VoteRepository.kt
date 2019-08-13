package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.domain.Vote
import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount

@Repository
interface VoteRepository : ReactiveMongoRepository<Vote, String>, VoteRepositoryCustom {
    fun findAllByPoll(poll: Poll): Flux<Vote>
    fun findAllByUser(user: User): Flux<Vote>
    fun countAllByPoll(poll: Poll): Mono<Long>
    fun findAllByPollAndUser(poll: Poll, user: User): Flux<Vote>
}

interface VoteRepositoryCustom {
    fun save(vote: Vote): Mono<Vote>
    fun getVotes(poll: Poll): Mono<List<VotesCount>>
}