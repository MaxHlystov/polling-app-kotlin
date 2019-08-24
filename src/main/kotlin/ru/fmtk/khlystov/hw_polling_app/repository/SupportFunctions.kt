package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import ru.fmtk.khlystov.hw_polling_app.rest.dto.UserDTO
import ru.fmtk.khlystov.hw_polling_app.security.CustomUserDetails

fun withUser(): Mono<User> =
        ReactiveSecurityContextHolder.getContext()
                .map { securityContext -> securityContext.authentication }
                .map { authentication -> authentication.principal }
                .cast(CustomUserDetails::class.java)
                .map { userDetails ->
                    println("Polls getting for user: ${userDetails.username}")
                    userDetails.user
                }
                .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST, "Can't find the user."))

fun withUserAndPoll(pollRepository: PollRepository,
                    pollId: String): Mono<Pair<User, Poll>> {
    return withUser()
            .flatMap { user ->
                pollRepository.findById(pollId)
                        .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "Can't find the poll."))
                        .map { poll -> user to poll }

            }
}

fun <T> getMonoHttpError(status: HttpStatus, errorMessage: String): Mono<T> =
        Mono.defer {
            Mono.error<T>(ResponseStatusException(status, errorMessage))
        }