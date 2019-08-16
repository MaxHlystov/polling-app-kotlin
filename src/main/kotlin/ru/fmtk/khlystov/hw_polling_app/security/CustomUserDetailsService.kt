package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.repository.UserRepository

@Service
open class CustomUserDetailsService (private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(name: String): Mono<UserDetails> {
        return CustomUserDetails(userRepository.findByName(name))
    }

}