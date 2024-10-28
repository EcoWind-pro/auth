package ecowind.ru.auth.services

import ecowind.ru.auth.actions.ClientAction
import ecowind.ru.auth.configs.properties.TokenProps
import ecowind.ru.auth.mappers.TokenMapper
import ecowind.ru.auth.models.JwtPair
import ecowind.ru.auth.models.TokenType
import ecowind.ru.authapi.requests.CreateTokensRq
import ecowind.ru.authapi.requests.RefreshTokenRq
import ecowind.ru.authapi.responses.CreateTokensRs
import ecowind.ru.authapi.responses.RefreshTokenRs
import ecowind.ru.exceptionhandler.excepions.TokenException
import ecowind.ru.utils.messages.auth.TokenMessages
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TokenService(
    private val tokenProps: TokenProps,
    private val tokenMapper: TokenMapper,
    private val clientAction: ClientAction
) {
    private val secretKey = Keys.hmacShaKeyFor(tokenProps.secret.toByteArray())
    private val tokenTypeField: String = "tokenType"
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    /**
     * Creating a pair of access and refresh tokens for user after validation with another microservice
     *
     * @param createTokenRq object with user credentials to create tokens
     * @return If validation is successful, so a pair of access and refresh token, else throwing exceptions from validation microservice
     */
    @Transactional
    fun createTokens(createTokenRq: CreateTokensRq): CreateTokensRs = runBlocking {
        async(Dispatchers.IO) {
            clientAction.checkIfUserExists(
                login = createTokenRq.login,
                password = createTokenRq.password
            )
        }

        val jwtPair: JwtPair = createJwtPair(createTokenRq.login)
        log.info("Created tokens for user with login ${createTokenRq.login}")

        return@runBlocking tokenMapper.jwtPairToCreateRs(jwtPair)
    }

    /**
     * Creating a pair of access and refresh tokens by refresh token after validations
     *
     *
     * @param refreshTokenRq object with users` access and refresh tokens
     * @return If all validations is successful, so a new pair of access and refresh token, else throwing exceptions from validation microservice or from other validations
     */
    @Transactional
    fun refreshToken(refreshTokenRq: RefreshTokenRq): RefreshTokenRs = runBlocking {
        val accessTokenClaims = refreshTokenRq.accessToken.getClaims()
        val refreshTokenClaims = refreshTokenRq.refreshToken.getClaims()

        val login: String = accessTokenClaims.subject
        async(Dispatchers.IO) { clientAction.checkIfUserExists(login) }

        if (accessTokenClaims[tokenTypeField] != TokenType.ACCESS)
            throw TokenException(TokenMessages.INVALID_TOKEN_TYPE_TO_REFRESH.message)

        if (accessTokenClaims.expiration.before(Date(System.currentTimeMillis())))
            throw TokenException(TokenMessages.ACCESS_TOKEN_NOT_EXPIRED.message)

        if (refreshTokenClaims[tokenTypeField] != TokenType.REFRESH)
            throw TokenException(TokenMessages.INVALID_TOKEN_TYPE_TO_REFRESH.message)

        if (refreshTokenClaims.expiration.after(Date(System.currentTimeMillis())))
            throw TokenException(TokenMessages.REFRESH_TOKEN_IS_EXPIRED.message)

        val jwtPair: JwtPair = createJwtPair(login)
        log.info("Refreshed tokens for user with login $login")

        return@runBlocking tokenMapper.jwtPairToRefreshRs(jwtPair)
    }

    /**
     * Validating access token before trace authorization
     *
     * @param accessToken incoming access token to validate
     */
    @Transactional(readOnly = true)
    fun validateAccessTokenToAuth(accessToken: String) {
        runBlocking {
            val type: TokenType
            val expiration: Date
            val login: String

            try {
                val claims: Claims = accessToken.getClaims()
                type = claims[tokenTypeField] as TokenType
                expiration = claims.expiration
                login = claims.subject
            } catch (_: Exception) {
                throw TokenException(TokenMessages.ACCESS_TOKEN_NOT_VALID.message)
            }

            async(Dispatchers.IO) { clientAction.checkIfUserExists(login) }

            if (type != TokenType.ACCESS)
                throw TokenException(TokenMessages.INVALID_TOKEN_TYPE.message)

            if (expiration.after(Date(System.currentTimeMillis())))
                throw TokenException(TokenMessages.ACCESS_TOKEN_IS_EXPIRED.message)
        }
    }

    /**
     * Additional function to get claims from token string
     *
     * @return Claims of token
     */
    private fun String.getClaims(): Claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(this)
        .payload

    /**
     * Creating jwt token of certain type for user
     *
     * @param username login of user to create his token
     * @param tokenType type of token that is need to be created
     * @return Created token of certain type for certain user
     */
    private fun createJwt(
        username: String,
        tokenType: TokenType
    ): String = Jwts.builder()
        .claims()
        .subject(username)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(
            Date(
                System.currentTimeMillis() + when (tokenType) {
                    TokenType.ACCESS -> tokenProps.access.timeUnit.toMillis(tokenProps.access.ttl)
                    TokenType.REFRESH -> tokenProps.refresh.timeUnit.toMillis(tokenProps.refresh.ttl)
                }
            )
        )
        .add(mapOf(tokenTypeField to tokenType))
        .and()
        .signWith(secretKey)
        .compact()

    /**
     * Creates a pair of access and refresh token by login, used by methods which are creating such pairs after validations
     *
     * @param login login of user to create tokens
     * @return Pair of access and refresh token
     */
    private fun createJwtPair(login: String): JwtPair = JwtPair(
        accessToken = createJwt(login, TokenType.ACCESS),
        refreshToken = createJwt(login, TokenType.REFRESH)
    )
}
