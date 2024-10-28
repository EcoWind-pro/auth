package ecowind.ru.auth.mappers

import ecowind.ru.auth.models.JwtPair
import ecowind.ru.authapi.responses.CreateTokensRs
import ecowind.ru.authapi.responses.RefreshTokenRs
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface TokenMapper {
    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    fun jwtPairToCreateRs(jwtPair: JwtPair): CreateTokensRs

    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    fun jwtPairToRefreshRs(jwtPair: JwtPair): RefreshTokenRs
}