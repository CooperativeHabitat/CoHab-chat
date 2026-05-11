package by.magofrays.configuration

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.dialect.MySqlDialect
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.binding.BindMarkersFactory


@Configuration
@EnableR2dbcRepositories
class R2dbcConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun getDialect(connectionFactory: ConnectionFactory): R2dbcDialect {
        return MySqlDialect.INSTANCE
    }

    @Bean
    @Primary
    fun newDatabaseClient(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .bindMarkers(BindMarkersFactory.anonymous("?"))
            .build()
    }

    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactories.get("r2dbc:clickhouse://events:events@localhost:8123/events")
    }
}
