package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class Poll(@Id var id: String?,
                val title: String,
                val owner: User,
                val items: List<PollItem>) {
    fun getPollItem(id: String): Optional<PollItem> {
        return Optional.ofNullable(items.asSequence().firstOrNull { pollItem -> pollItem.id == id })
    }
}