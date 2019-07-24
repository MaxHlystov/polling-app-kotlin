package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Votes(@Id val id: String,
            val poll: Poll,
            val votes: Map<String, Integer>,
            val usersVotes: Map<String, List<User>>) {
}