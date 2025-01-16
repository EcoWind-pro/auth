package ecowind.ru.auth.controllers

import ecowind.ru.auth.services.TokenService
import ecowind.ru.authapi.TokenAPI
import ecowind.ru.authapi.requests.CreateTokensRq
import ecowind.ru.authapi.responses.CreateTokensRs
import ecowind.ru.utils.endpoints.Controller
import ecowind.ru.utils.endpoints.GetEndpoint
import ecowind.ru.utils.endpoints.PostEndpoint
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Controller(value = TokenAPI.PREFIX, name = "The TokenController API")
class TokenController(
    private val tokenService: TokenService
) {
    @PostEndpoint(value = TokenAPI.CREATE, summary = "Creates a new pair of access and refresh tokens")
    fun create(@RequestBody createTokenRq: CreateTokensRq): ResponseEntity<CreateTokensRs> =
        ResponseEntity.ok(tokenService.createTokens(createTokenRq))

    @GetEndpoint(value = TokenAPI.VALIDATE, summary = "Validating given access token")
    fun validate(@PathVariable accessToken: String) =
        ResponseEntity.ok(tokenService.validateAccessTokenToAuth(accessToken))

    @GetEndpoint(value = TokenAPI.REFRESH, summary = "Refreshes tokens for user by his refresh token")
    fun refresh(@PathVariable refreshToken: String): ResponseEntity<String> =
        ResponseEntity.ok(tokenService.refreshToken(refreshToken))
}