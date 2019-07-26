package ru.fmtk.khlystov.hw_polling_app.mongoEventListener

import org.apache.commons.codec.binary.Base32
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent
import org.springframework.stereotype.Component
import ru.fmtk.khlystov.hw_polling_app.domain.Poll
import ru.fmtk.khlystov.hw_polling_app.domain.PollItem
import java.security.SecureRandom

@Component
class CustomMongoEventListener(private val mongoOperations: MongoOperations) : AbstractMongoEventListener<Poll>() {
    override fun onBeforeConvert(event: BeforeConvertEvent<Poll>) {
        super.onBeforeConvert(event)
        val poll = event.source
        synchronized(poll) {
            poll.items.forEach { pollItem: PollItem ->
                if (pollItem.id.isNullOrEmpty()) {
                    pollItem.id = generateGuid()
                }
            }
        }
    }

    private fun generateGuid(): String {
        val randomGen = SecureRandom()
        val byteArray = ByteArray(16)
        randomGen.nextBytes(byteArray)
        return Base32().encodeToString(byteArray).substring(0, 26)
    }
}