package ru.fmtk.khlystov.hw_polling_app.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@WebMvcTest(RootController::class)
class RootControllerTest {

    @Autowired
    lateinit var rootController: RootController

    @Test
    @DisplayName("Root page is an authentification")
    fun rootPageMapper() {
        val view = rootController.rootPageMapper()
        assertEquals("auth", view)
    }
}