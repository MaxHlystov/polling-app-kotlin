package ru.fmtk.khlystov.hw_polling_app.rest.dto

import ru.fmtk.khlystov.hw_polling_app.domain.User
import kotlin.streams.toList

data class UserDTO(var id: String?,
                   val name: String,
                   val email: String = "",
                   val accountNonExpired: Boolean = true,
                   val accountNonLocked: Boolean = true,
                   val credentialsNonExpired: Boolean = true,
                   val enabled: Boolean = true,
                   val roles: Collection<String> = ArrayList()) {
    constructor(user: User) : this(user.id,
            user.name,
            user.email,
            user.accountNonExpired,
            user.accountNonLocked,
            user.credentialsNonExpired,
            user.enabled,
            user.roles.stream().toList())
}