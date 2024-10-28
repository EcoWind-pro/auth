package ecowind.ru.auth.configs.properties

import java.util.concurrent.TimeUnit
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.trace")
data class TraceProps(
    val ttl: Long,
    val timeUnit: TimeUnit
)