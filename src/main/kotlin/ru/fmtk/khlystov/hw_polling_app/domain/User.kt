package ru.fmtk.khlystov.hw_polling_app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*
import kotlin.streams.toList

@Document
data class User(@Id var id: String?,
                val name: String,
                val email: String = "",
                val password: String = "",
                val accountNonExpired: Boolean = true,
                val accountNonLocked: Boolean = true,
                val credentialsNonExpired: Boolean = true,
                val enabled: Boolean = true,
                val roles: Set<String> = HashSet()) {

    constructor(name: String) : this(null, name)
    constructor(user: User) : this(user.id,
            user.name,
            user.email,
            user.password,
            user.accountNonExpired,
            user.accountNonLocked,
            user.credentialsNonExpired,
            user.enabled,
            user.roles.asSequence().toHashSet())


    fun newWithRoles(roles: Collection<String>) = User(
            id,
            name,
            email,
            password,
            accountNonExpired,
            accountNonLocked,
            credentialsNonExpired,
            enabled,
            this.roles + roles)

    override fun toString(): String {
        return "User(id=$id, name='$name', password='$password')"
    }
}