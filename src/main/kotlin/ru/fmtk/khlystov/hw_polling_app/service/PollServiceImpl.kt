package ru.fmtk.khlystov.hw_polling_app.service

import org.springframework.cloud.netflix.hystrix.HystrixCommands
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository

@Service
class PollServiceImpl(private val pollRepository: PollRepository) : PollService {

    override fun findAll(): Flux<Poll> = HystrixCommands
            .from(pollRepository.findAll().withRandomDelay())
            .fallback(Flux.just(Poll(null,
                    "Empty poll",
                    User(null, "Empty user"),
                    listOf())))
            .groupName("Polls")
            .commandName("FindAllUsers")
            .toFlux()

    override fun findById(id: String): Mono<Poll> = HystrixCommands
            .from(pollRepository.findById(id).withRandomDelay())
            .fallback(Mono.just(Poll(null,
                    "Empty poll",
                    User(null, "Empty user"),
                    listOf())))
            .groupName("Polls")
            .commandName("FindAllUsers")
            .toMono()

    override fun save(user: Poll): Mono<Poll> = HystrixCommands
            .from(pollRepository.save(user).withRandomDelay())
            .fallback(Mono.just(Poll(null,
                    "Empty poll",
                    User(null, "Empty user"),
                    listOf())))
            .groupName("Polls")
            .commandName("FindAllUsers")
            .toMono()


    override fun delete(user: Poll): Mono<Void> = pollRepository.delete(user)
}