package ru.fmtk.khlystov.hw_polling_app.rest.dto

class StatusDTO(val ok: Boolean, val description: String) {
    companion object {
        fun error(description: String) = StatusDTO(false, description)
        fun ok(description: String) = StatusDTO(true, description)
    }
}