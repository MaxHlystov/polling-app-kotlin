package ru.fmtk.khlystov.hw_polling_app.controller.dto

import ru.fmtk.khlystov.hw_polling_app.domain.VotesCount

data class VotesCountDTO(val pollItem: PollItemDTO, val total: Long) {
    constructor(votesCount: VotesCount) : this(PollItemDTO(votesCount.pollItem), votesCount.total)
}