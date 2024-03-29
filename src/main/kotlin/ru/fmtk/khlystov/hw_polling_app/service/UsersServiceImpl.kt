package ru.fmtk.khlystov.hw_polling_app.service

import org.springframework.cloud.netflix.hystrix.HystrixCommands
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Service
class UsersServiceImpl(private val userRepository: UserRepository) : UsersService {
    override fun findAll(): Flux<User> = HystrixCommands
            .from(userRepository.findAll().withRandomDelay())
            .fallback(Flux.just(User(null, "Empty user")))
            .groupName("Users")
            .commandName("FindAllUsers")
            .toFlux()

    override fun findById(id: String): Mono<User> = userRepository.findById(id)

    override fun save(user: User): Mono<User> = userRepository.save(user)

    override fun delete(user: User): Mono<Void> = userRepository.delete(user)

    override fun findByName(name: String): Mono<User> = userRepository.findByName(name)
}
