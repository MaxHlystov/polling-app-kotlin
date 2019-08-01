package ru.fmtk.khlystov.hw_polling_app.controller.dto

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import java.util.*

@Document
data class PollDTO(@Id var id: String?,
                   val title: String,
                   val items: List<PollItemDTO>) {
    constructor(poll: Poll) : this(poll.id, poll.title, poll.items.map(::PollItemDTO))

    fun getPollItem(id: String): Optional<PollItemDTO> {
        return Optional.ofNullable(items.asSequence().firstOrNull { pollItem -> pollItem.id == id })
    }
}