package ru.fmtk.khlystov.hw_polling_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HwPollingAppApplication

fun main(args: Array<String>) {
	runApplication<HwPollingAppApplication>(*args)
}
