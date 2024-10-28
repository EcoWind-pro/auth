package ecowind.ru.auth.controllers

import ecowind.ru.auth.services.TokenService
import ecowind.ru.authapi.TokenAPI
import ecowind.ru.authapi.requests.CreateTokensRq
import ecowind.ru.authapi.requests.RefreshTokenRq
import ecowind.ru.authapi.responses.CreateTokensRs
import ecowind.ru.authapi.responses.RefreshTokenRs
import ecowind.ru.utils.endpoints.Controller
import ecowind.ru.utils.endpoints.PostEndpoint
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Controller(value = TokenAPI.PREFIX, name = "The TokenController API")
class TokenController(
    private val tokenService: TokenService
) {
    @PostEndpoint(value = TokenAPI.GENERATE, summary = "Creates a new pair of access and refresh tokens")
    fun generate(@RequestBody createTokenRq: CreateTokensRq): ResponseEntity<CreateTokensRs> =
        ResponseEntity.ok(tokenService.createTokens(createTokenRq))

    @PostEndpoint(value = TokenAPI.REFRESH, summary = "Refreshes access token for user by his refresh token")
    fun refresh(@RequestBody refreshTokenRq: RefreshTokenRq): ResponseEntity<RefreshTokenRs> =
        ResponseEntity.ok(tokenService.refreshToken(refreshTokenRq))
}