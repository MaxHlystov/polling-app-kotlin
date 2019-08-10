package ru.fmtk.khlystov.hw_polling_app.rest.dto

import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount

data class VotesCountDTO(val pollItem: PollItemDTO,
                         val total: Long,
                         val selectedByUser: Boolean) {
    constructor(votesCount: VotesCount, selectedByUser: Boolean) :
            this(PollItemDTO(votesCount.pollItem), votesCount.total, selectedByUser)
}