package ru.fmtk.khlystov.hw_polling_app.security

enum class Roles(val role: String) {
    User("USER"),
    Admin("ADMIN");

    override fun toString(): String = role
}