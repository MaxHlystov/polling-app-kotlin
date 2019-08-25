package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User

fun withUser(userRepository: UserRepository, userId: String): Mono<User> {
    return userRepository.findById(userId)
            .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST, "Can't find the user."))
}

fun withUserAndPoll(userRepository: UserRepository,
                    pollRepository: PollRepository,
                    userId: String,
                    pollId: String): Mono<Pair<User, Poll>> {
    return withUser(userRepository, userId)
            .flatMap { user ->
                pollRepository.findById(pollId)
                        .switchIfEmpty(getMonoHttpError(HttpStatus.BAD_REQUEST,
                                "Can't find the poll."))
                        .map { poll -> user to poll }

            }
}

fun <T> getMonoHttpError(status: HttpStatus, errorMessage: String): Mono<T> =
        Mono.error(ResponseStatusException(status, errorMessage))