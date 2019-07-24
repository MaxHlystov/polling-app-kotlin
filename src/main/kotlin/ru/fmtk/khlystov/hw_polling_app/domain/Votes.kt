package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Votes(@Id val id: String?,
                 @DBRef val poll: Poll,
                 @DBRef val pollItem: PollItem,
                 val votes: Integer,
                 val usersIds: List<String>)