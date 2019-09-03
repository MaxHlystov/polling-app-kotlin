package ru.fmtk.khlystov.hw_polling_app.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.fmtk.khlystov.hw_polling_app.security.Authority
import java.util.HashSet
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
                val roles: Collection<String> = ArrayList()) {

    constructor(name: String) : this(null, name)
    constructor(user: User) : this(user.id,
            user.name,
            user.email,
            user.password,
            user.accountNonExpired,
            user.accountNonLocked,
            user.credentialsNonExpired,
            user.enabled,
            user.roles.stream().toList())


    override fun toString(): String {
        return "User(id=$id, name='$name', password='$password')"
    }
}