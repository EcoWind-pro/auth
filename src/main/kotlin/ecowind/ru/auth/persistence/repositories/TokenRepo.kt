package ecowind.ru.auth.persistence.repositories

import ecowind.ru.auth.configs.properties.TokenProps
import ecowind.ru.auth.models.TokenType
import ecowind.ru.exceptionhandler.excepions.TokenException
import ecowind.ru.utils.messages.auth.TokenMessages
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TokenRepo(
    private val redisTemplate: RedisTemplate<String, String>,
    private val tokenProps: TokenProps,
) {
    fun save(login: String, tokens: Map<TokenType, String>) {
        val hashOps: HashOperations<String, String, String> = redisTemplate.opsForHash()
        tokens.forEach { hashOps.put(login, it.key.toString(), it.value) }
        redisTemplate.expire(login, tokenProps.refresh.ttl, tokenProps.refresh.timeUnit)
    }

    fun getAccessTokenByLoginAndRefreshToken(login: String, refreshToken: String): String {
        val hashOps: HashOperations<String, String, String> = redisTemplate.opsForHash()
        val tokens = hashOps.entries(login)

        if (!tokens.any { it.value == refreshToken })
            throw TokenException(TokenMessages.REFRESH_TOKEN_NOT_VALID.message)

        return tokens[TokenType.ACCESS.toString()]!!
    }

    fun existsByLoginAndToken(login: String, token: String): Boolean {
        val hashOps: HashOperations<String, String, String> = redisTemplate.opsForHash()
        val tokens = hashOps.entries(login)

        return tokens.any { it.value == token }
    }
}
