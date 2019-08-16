package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository


@Service
class CustomUserDetailsService(@Autowired val userRepository: UserRepository) : ReactiveUserDetailsService {
    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByName(username)
                .switchIfEmpty(Mono.defer {
                    val err: Mono<User> =
                            Mono.error(UsernameNotFoundException("User Not Found"))
                    err
                }).map(::CustomUserDetails)
    }
}