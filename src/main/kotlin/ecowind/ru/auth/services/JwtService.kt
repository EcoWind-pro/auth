package ecowind.ru.auth.services

import ecowind.ru.auth.configs.properties.TokenProps
import ecowind.ru.auth.models.JwtPair
import ecowind.ru.auth.models.TokenType
import ecowind.ru.auth.persistence.repositories.TokenRepo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import org.springframework.stereotype.Service

@Service
class JwtService(
    private val tokenProps: TokenProps,
    private val tokenRepo: TokenRepo
) {
    private val secretKey = Keys.hmacShaKeyFor(tokenProps.secret.toByteArray())

    /**
     * Function to get claims from token string
     *
     * @return Claims of token
     */
    fun getClaims(token: String): Claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .payload

    /**
     * Creates a pair of access and refresh token by login, used by methods which are creating such pairs after validations. After creating saving tokens to redis repo.
     *
     * @param login login of user to create tokens
     * @return Pair of access and refresh token
     */
    fun createAndSaveJwtPair(login: String): JwtPair {
        val jwtPair = JwtPair(
            accessToken = createJwt(login, TokenType.ACCESS),
            refreshToken = createJwt(login, TokenType.REFRESH)
        )

        tokenRepo.save(
            login = login,
            tokens = mapOf(
                TokenType.ACCESS to jwtPair.accessToken,
                TokenType.REFRESH to jwtPair.refreshToken
            )
        )

        return jwtPair
    }

    /**
     * Creates an access token by login. After creating saving token to redis repo.
     *
     * @param login login of user to create tokens
     * @return Pair of access and refresh token
     */
    fun recreateAccessToken(login: String): String {
        val accessToken: String = createJwt(login, TokenType.ACCESS)

        tokenRepo.save(
            login = login,
            tokens = mapOf(TokenType.ACCESS to accessToken)
        )

        return accessToken
    }

    /**
     * Creating jwt token of certain type for user
     *
     * @param login login of user to create his token
     * @param tokenType type of token that is need to be created
     * @return Created token of certain type for certain user
     */
    private fun createJwt(
        login: String,
        tokenType: TokenType
    ): String = Jwts.builder()
        .claims()
        .subject(login)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(
            Date(
                System.currentTimeMillis() + when (tokenType) {
                    TokenType.ACCESS -> tokenProps.access.timeUnit.toMillis(tokenProps.access.ttl)
                    TokenType.REFRESH -> tokenProps.refresh.timeUnit.toMillis(tokenProps.refresh.ttl)
                }
            )
        )
        .add(mapOf(TOKEN_TYPE_FIELD to tokenType))
        .and()
        .signWith(secretKey)
        .compact()

    companion object {
        const val TOKEN_TYPE_FIELD: String = "tokenType"
    }
}