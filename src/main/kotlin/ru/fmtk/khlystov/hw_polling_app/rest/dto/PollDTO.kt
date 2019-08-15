package ru.fmtk.khlystov.hw_polling_app.rest.dto

import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.User
import java.util.*

data class PollDTO(var id: String?,
                   val title: String,
                   val items: List<PollItemDTO>,
                   val editable: Boolean) {
    constructor(poll: Poll, editable: Boolean) : this(poll.id, poll.title, poll.items.map(::PollItemDTO), editable)

    fun toPoll(user: User): Poll = Poll(id, title, user, items.map(PollItemDTO::toPollItem))
}