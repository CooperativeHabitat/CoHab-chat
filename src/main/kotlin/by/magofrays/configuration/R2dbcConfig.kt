package by.magofrays.configuration

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.binding.BindMarkersFactory


@Configuration
class R2dbcConfig {

    @Bean
    fun databaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .bindMarkers(BindMarkersFactory.anonymous("?"))
            .build()
    }
}