package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Vote(@Id var id: String?,
                 @DBRef val user: User,
                 @DBRef val poll: Poll,
                 @DBRef val pollItem: PollItem)