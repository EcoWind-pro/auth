package ecowind.ru.auth.models

data class JwtPair(
    val accessToken: String,
    val refreshToken: String
)
