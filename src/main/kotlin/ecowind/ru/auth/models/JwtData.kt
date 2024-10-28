package ecowind.ru.auth.models

import java.util.concurrent.TimeUnit

data class JwtData(
    val ttl: Long,
    val timeUnit: TimeUnit
)
