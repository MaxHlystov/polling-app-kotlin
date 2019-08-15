package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(@Id var id: String?, val name: String) {
    constructor(name: String) : this(null, name)
}