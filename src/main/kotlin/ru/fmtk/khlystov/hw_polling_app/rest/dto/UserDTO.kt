package ru.fmtk.khlystov.hw_polling_app.rest.dto

import ru.fmtk.khlystov.hw_polling_app.domain.User

data class UserDTO(var id: String?, val name: String) {
    constructor(user: User) : this(user.id, user.name)
}