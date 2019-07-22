package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Votes(@Id val id: String,
            val poll: Poll,
            val votes: Map<PollItem, Integer>,
            val usersVotes: Map<PollItem, List<User>>) {
}