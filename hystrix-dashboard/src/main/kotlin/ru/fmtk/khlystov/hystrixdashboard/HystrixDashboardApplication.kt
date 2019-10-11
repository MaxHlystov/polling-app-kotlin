package ru.fmtk.khlystov.hystrixdashboard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard
import org.springframework.cloud.netflix.turbine.EnableTurbine

@SpringBootApplication
@EnableHystrixDashboard
@EnableTurbine
class HystrixDashboardApplication

fun main(args: Array<String>) {
    runApplication<HystrixDashboardApplication>(*args)
}
