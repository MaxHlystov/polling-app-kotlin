package ru.fmtk.khlystov.hw_polling_app.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
class RootController {

    @GetMapping("/")
    fun rootPageMapper(): String {
        return "auth"
    }
}
