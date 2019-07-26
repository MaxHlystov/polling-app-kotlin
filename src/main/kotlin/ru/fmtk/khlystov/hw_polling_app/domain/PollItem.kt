package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class PollItem(@Id val id: String?,
                    val title: String)