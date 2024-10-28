package ecowind.ru.auth.services

import brave.Tracer
import ecowind.ru.auth.persistence.repositories.TraceRepo
import ecowind.ru.exceptionhandler.excepions.AccessDeniedException
import ecowind.ru.utils.messages.auth.TraceMessages
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TraceService(
    private val tracer: Tracer,
    private val traceRepo: TraceRepo,
    private val tokenService: TokenService
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * Authorizing current trace by validating incoming in request header access token.
     *
     * @param accessToken token of authorized user
     * @return If authorization is successful then returns nothing, else throwing exceptions (if trace already authorized or if token is not valid for this service)
     */
    @Transactional
    fun authTrace(accessToken: String) {
        tokenService.validateAccessTokenToAuth(accessToken)
        val currentTraceId: String = getTraceId()
        if (traceRepo.isAccessTokenExistsByTrace(currentTraceId))
            throw AccessDeniedException(
                String.format(
                    TraceMessages.TRACE_WITH_ID_ALREADY_AUTHORIZED_SF.message,
                    currentTraceId
                )
            )
        traceRepo.save(
            traceId = currentTraceId,
            token = accessToken
        )
        log.info(String.format(TraceMessages.SUCCESSFULLY_AUTHORIZED_TRACE_WITH_ID_SF.message, currentTraceId))
    }

    /**
     * Checking in database if current trace was authorized previously by this service by comparing incoming access token to previously saved token for trace.
     *
     * @param accessToken token of authorized user
     * @return If all is ok, then returns nothing, else throwing exceptions (for example, if tokens are not equal or trace was not authorized previously in service)
     */
    @Transactional(readOnly = true)
    fun checkTraceAuth(accessToken: String) {
        val currentTraceId: String = getTraceId()
        val existingAccess: String = traceRepo.findAccessTokenByTrace(currentTraceId)
        if (accessToken != existingAccess) throw AccessDeniedException(TraceMessages.INVALID_ACCESS.message)
    }

    /**
     * Getting identification of current incoming trace.
     *
     * @return Identification of incoming trace
     */
    private fun getTraceId(): String = tracer.currentSpan().context().traceIdString()
}