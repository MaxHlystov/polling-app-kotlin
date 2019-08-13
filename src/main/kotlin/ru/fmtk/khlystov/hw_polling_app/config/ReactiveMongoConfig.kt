package ru.fmtk.khlystov.hw_polling_app.config

import com.mongodb.MongoClient
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ReactiveMongoConfig {

    @Autowired
    internal var mongoClient: MongoClient? = null

    @Bean
    fun reactiveMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(mongoClient, databaseName)
    }
}