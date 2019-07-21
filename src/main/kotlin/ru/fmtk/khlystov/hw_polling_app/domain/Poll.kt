package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Poll(@Id val id: String, val items: List<PollItem>) {
}