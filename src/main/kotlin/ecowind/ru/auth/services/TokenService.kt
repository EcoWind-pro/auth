package ecowind.ru.auth.services

import ecowind.ru.auth.actions.MsClientAction
import ecowind.ru.auth.mappers.TokenMapper
import ecowind.ru.auth.models.JwtPair
import ecowind.ru.auth.models.TokenType
import ecowind.ru.auth.persistence.repositories.TokenRepo
import ecowind.ru.authapi.requests.CreateTokensRq
import ecowind.ru.authapi.responses.CreateTokensRs
import ecowind.ru.exceptionhandler.excepions.TokenException
import ecowind.ru.utils.messages.auth.TokenMessages
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TokenService(
    private val tokenRepo: TokenRepo,
    private val jwtService: JwtService,
    private val tokenMapper: TokenMapper,
    private val msClientAction: MsClientAction,
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * Creating a pair of access and refresh tokens for user after validation with another microservice
     *
     * @param createTokenRq object with user credentials to create tokens
     * @return If validation is successful, so a pair of access and refresh token, else throwing exceptions from validation microservice
     */
    @Transactional
    fun createTokens(createTokenRq: CreateTokensRq): CreateTokensRs = runBlocking {
        msClientAction.checkIfUserExists(
            login = createTokenRq.login,
            password = createTokenRq.password
        )

        val jwtPair: JwtPair = jwtService.createAndSaveJwtPair(createTokenRq.login)
        log.info("Created tokens for user with login '${createTokenRq.login}'")

        return@runBlocking tokenMapper.jwtPairToCreateRs(jwtPair)
    }

    /**
     * Creating a new access token by refresh token after validations
     *
     * @param refreshToken
     * @return If all validations is successful, so an access and refresh token, else throwing exceptions from validation microservice or from other validations
     */
    @Transactional
    fun refreshToken(refreshToken: String): String = runBlocking {
        val refreshTokenClaims: Claims
        try {
            refreshTokenClaims = jwtService.getClaims(refreshToken)
        } catch (ex: ExpiredJwtException) {
            log.info(ex.message)
            throw TokenException(TokenMessages.REFRESH_TOKEN_IS_EXPIRED.message)
        }

        val login: String = refreshTokenClaims.subject

        try {
            jwtService.getClaims(tokenRepo.getAccessTokenByLoginAndRefreshToken(login, refreshToken))
            throw TokenException(TokenMessages.ACCESS_TOKEN_NOT_EXPIRED.message)
        } catch (_: ExpiredJwtException) {
        }

        msClientAction.checkIfUserExists(login)

        if (refreshTokenClaims[JwtService.TOKEN_TYPE_FIELD] != TokenType.REFRESH.toString())
            throw TokenException(TokenMessages.INVALID_TOKEN_TYPE_TO_REFRESH.message)

        val newAccessToken: String = jwtService.recreateAccessToken(login)
        log.info("Refreshed access token for user with login '$login'")

        return@runBlocking newAccessToken
    }

    /**
     * Validating access token before trace authorization
     *
     * @param accessToken incoming access token to validate
     */
    @Transactional(readOnly = true)
    fun validateAccessTokenToAuth(accessToken: String) = runBlocking {
        val type: TokenType
        val login: String

        try {
            val claims: Claims = jwtService.getClaims(accessToken)
            type = TokenType.valueOf(claims[JwtService.TOKEN_TYPE_FIELD].toString())
            login = claims.subject
        } catch (ex: Exception) {
            log.error(ex.message)
            throw TokenException(TokenMessages.ACCESS_TOKEN_NOT_VALID.message)
        }

        msClientAction.checkIfUserExists(login)

        if (type != TokenType.ACCESS)
            throw TokenException(TokenMessages.INVALID_TOKEN_TYPE.message)

        if (!tokenRepo.existsByLoginAndToken(login, accessToken))
            throw TokenException(TokenMessages.ACCESS_TOKEN_NOT_VALID.message)
    }
}
