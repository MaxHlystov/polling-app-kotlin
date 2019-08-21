package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(@Id var id: String?,
                val name: String,
                val email: String = "",
                val password: String = "") {
    var accountNonExpired: Boolean = true
    var accountNonLocked: Boolean = true
    var credentialsNonExpired: Boolean = true
    var enabled: Boolean = true

    constructor(name: String) : this(null, name)
    constructor(user: User) : this(user.id, user.name, user.email, user.password) {
        accountNonExpired = user.accountNonExpired
        accountNonLocked = user.accountNonLocked
        credentialsNonExpired = user.credentialsNonExpired
        enabled = user.enabled
    }

    override fun toString(): String {
        return "User(id=$id, name='$name', password='$password')"
    }
}