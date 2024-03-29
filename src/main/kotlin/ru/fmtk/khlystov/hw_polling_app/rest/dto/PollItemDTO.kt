package ru.fmtk.khlystov.hw_polling_app.rest.dto

import org.springframework.data.annotation.Id
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem

data class PollItemDTO(@Id var id: String?,
                       val title: String) {
    constructor(pollItem: PollItem): this(pollItem.id, pollItem.title)

    fun toPollItem(): PollItem = PollItem(id, title)
}