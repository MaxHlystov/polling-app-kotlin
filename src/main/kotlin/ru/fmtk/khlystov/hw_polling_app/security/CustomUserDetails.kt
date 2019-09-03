package ru.fmtk.khlystov.hw_polling_app.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.fmtk.khlystov.hw_polling_app.domain.User
import kotlin.streams.toList

open class CustomUserDetails(val user: User) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return user.roles.asSequence().map(::Authority).toList()
    }

    override fun getUsername(): String {
        return user.name
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun isEnabled(): Boolean {
        return user.enabled
    }

    override fun isCredentialsNonExpired(): Boolean {
        return user.credentialsNonExpired
    }

    override fun isAccountNonExpired(): Boolean {
        return user.accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return user.accountNonLocked
    }
}