package ru.fmtk.khlystov.hw_polling_app.controller.dto

import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import java.util.*

data class PollDTO(var id: String?,
                   val title: String,
                   val items: List<PollItemDTO>) {
    constructor(poll: Poll) : this(poll.id, poll.title, poll.items.map(::PollItemDTO))

    fun toPoll(user: User): Poll = Poll(id, title, user, items.map(PollItemDTO::toPollItem))

    fun getPollItem(id: String): Optional<PollItemDTO> {
        return Optional.ofNullable(items.asSequence().firstOrNull { pollItem -> pollItem.id == id })
    }
}