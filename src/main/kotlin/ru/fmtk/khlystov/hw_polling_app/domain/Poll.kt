package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Poll(@Id val id: String,
                val title: String,
                val owner: User,
                val items: List<PollItem>) {
}