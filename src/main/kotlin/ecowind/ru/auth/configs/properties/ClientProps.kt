package ecowind.ru.auth.configs.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "rest.ms-client")
data class ClientProps(
    val scheme: String,
    val host: String,
    val port: Number?
)