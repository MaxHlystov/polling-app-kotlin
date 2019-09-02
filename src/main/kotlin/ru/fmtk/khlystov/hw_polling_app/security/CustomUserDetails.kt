package ru.fmtk.khlystov.hw_polling_app.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.fmtk.khlystov.hw_polling_app.domain.User

open class CustomUserDetails(val user: User,
                             val name: String,
                             val email: String = "",
                             val passwordIn: String = "") : UserDetails {

    var accountNonExpired: Boolean = true
    var accountNonLocked: Boolean = true
    var credentialsNonExpired: Boolean = true
    var enabled: Boolean = true

    private val log = LoggerFactory.getLogger(CustomUserDetails::class.java)

    constructor(user: User) : this(user, user.name, user.email, user.password) {
        accountNonExpired = user.accountNonExpired
        accountNonLocked = user.accountNonLocked
        credentialsNonExpired = user.credentialsNonExpired
        enabled = user.enabled
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("User"))
    }

    override fun getUsername(): String {
        return name
    }

    override fun getPassword(): String {
        return passwordIn
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun isCredentialsNonExpired(): Boolean {
        return credentialsNonExpired
    }

    override fun isAccountNonExpired(): Boolean {
        return accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return accountNonLocked
    }
}