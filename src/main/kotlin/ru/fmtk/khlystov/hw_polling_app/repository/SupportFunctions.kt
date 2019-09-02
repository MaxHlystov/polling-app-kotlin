package ru.fmtk.khlystov.hw_polling_app.repository

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

fun <T> getMonoHttpError(status: HttpStatus, errorMessage: String): Mono<T> =
        Mono.defer {
            Mono.error<T>(ResponseStatusException(status, errorMessage))
        }