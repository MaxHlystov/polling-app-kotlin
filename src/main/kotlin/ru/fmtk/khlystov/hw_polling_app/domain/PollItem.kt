package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id

data class PollItem(@Id var id: String?,
                    val title: String)