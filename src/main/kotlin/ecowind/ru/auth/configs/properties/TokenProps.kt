package ecowind.ru.auth.configs.properties

import ecowind.ru.auth.models.JwtData
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.tokens")
data class TokenProps(
    val access: JwtData,
    val refresh: JwtData,
    val secret: String
)