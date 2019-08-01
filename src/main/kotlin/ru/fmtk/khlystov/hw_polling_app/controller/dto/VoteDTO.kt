package ru.fmtk.khlystov.hw_polling_app.controller.dto

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import ru.fmtk.khlystov.hw_polling_app.domain.Vote

@Document
data class VoteDTO(@Id var id: String?,
                   @DBRef val userId: String?,
                   @DBRef val poll: PollDTO,
                   val pollItem: PollItemDTO) {
    constructor(vote: Vote): this(vote.id, vote.user.id, PollDTO(vote.poll), PollItemDTO(vote.pollItem))
}