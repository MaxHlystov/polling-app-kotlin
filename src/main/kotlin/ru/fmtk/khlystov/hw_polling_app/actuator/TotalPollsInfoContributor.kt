package ru.fmtk.khlystov.hw_polling_app.actuator

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import ru.fmtk.khlystov.hw_polling_app.repository.PollRepository
import java.util.HashMap



class TotalPollsInfoContributor(private val pollRepository: PollRepository) : InfoContributor {
    override fun contribute(builder: Info.Builder?) {
        val pollsStatistics = HashMap<String, Long>()
        pollsStatistics["total"] = pollRepository.count().block() ?: 0
        builder?.withDetail("polls", pollsStatistics)
    }

}