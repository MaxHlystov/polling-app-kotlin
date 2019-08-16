package ru.fmtk.khlystov.hw_polling_app.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.fmtk.khlystov.hw_polling_app.domain.User
import java.util.stream.Collectors

open class CustomUserDetails : User, UserDetails {

    private val log = LoggerFactory.getLogger(CustomUserDetails::class.java)

    constructor(user: User) : super(user)

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("User")
    }

    override fun getPassword(): String {
        return super<User>.password
    }

    override fun getUsername(): String {
        return super.name
    }

    override fun isEnabled(): Boolean {
        return super.enabled
    }

    override fun isCredentialsNonExpired(): Boolean {
        return super.credentialsNonExpired
    }

    override fun isAccountNonExpired(): Boolean {
        return super.accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return super.accountNonLocked
    }
}