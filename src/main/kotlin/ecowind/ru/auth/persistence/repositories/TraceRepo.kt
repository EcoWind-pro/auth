package ecowind.ru.auth.persistence.repositories

import ecowind.ru.auth.configs.properties.TraceProps
import ecowind.ru.exceptionhandler.excepions.ContentNotFoundException
import ecowind.ru.exceptionhandler.excepions.NotUniqueException
import ecowind.ru.utils.messages.auth.TraceMessages
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository

@Repository
class TraceRepo(
    private val redisTemplate: StringRedisTemplate,
    private val traceProps: TraceProps,
) {
    /**
     * Saving token to trace in database with lifetime which is specified in properties.
     *
     * @param traceId incoming trace identification
     * @param token token of authorized user
     * @return If all is ok so returns nothing, else throwing exceptions (if trace already authorized)
     */
    fun save(traceId: String, token: String) {
        if (isAccessTokenExistsByTrace(traceId))
            throw NotUniqueException(String.format(TraceMessages.TOKEN_FOR_TRACE_WITH_ID_ALREADY_EXISTS_SF.message, traceId))
        redisTemplate.opsForValue().set(traceId, token)
        redisTemplate.expire(traceId, traceProps.ttl, traceProps.timeUnit)
    }

    /**
     * Checking if trace is already authorized and token for this trace is already saved.
     *
     * @param traceId incoming trace identification
     * @return Can only return boolean value, exists or not, can not throw exception
     */
    fun isAccessTokenExistsByTrace(traceId: String): Boolean = !redisTemplate.opsForValue().get(traceId).isNullOrEmpty()

    /**
     * Getting token of authorized trace which is storing in database.
     *
     * @param traceId incoming trace identification
     * @return If all is ok so returns saved token, else throwing exceptions (if trace is not authorized or not stored in database)
     */
    fun findAccessTokenByTrace(traceId: String): String =
        redisTemplate.opsForValue().get(traceId)
            ?: throw ContentNotFoundException(String.format(TraceMessages.TRACE_WITH_ID_NOT_AUTHORIZED_SF.message, traceId))
}