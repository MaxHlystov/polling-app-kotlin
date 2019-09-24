package ru.fmtk.khlystov.hw_polling_app.security

import org.springframework.security.core.GrantedAuthority

data class Authority(val name: String): GrantedAuthority {
    override fun getAuthority(): String = name
}