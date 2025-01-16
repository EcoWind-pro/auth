package ecowind.ru.auth.actions

import ecowind.ru.auth.configs.properties.MsClientProps
import ecowind.ru.exceptionhandler.WebClientExceptionsHandler.exchangeCatchingErrors
import ecowind.ru.utils.ActionUtils.generateUri
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class MsClientAction(
    private val webClient: WebClient,
    private val msClientProps: MsClientProps
) {
    /**
     * Request to another microservice to check if user with login exists (also validated users` password)
     *
     * @param login login that need to be validated
     * @param password password of certain user to validate if credentials are correct
     */
    suspend fun checkIfUserExists(login: String, password: String) {
        webClient
            .post()
            .uri(
                generateUri(
                    scheme = msClientProps.scheme,
                    host = msClientProps.host,
                    port = msClientProps.port,
                )
            )
            .exchangeToMono { exchangeCatchingErrors(it) }
            .awaitSingle()
    }

    /**
     * Request to another microservice to check if user with login exists
     *
     * @param login login that need to be validated
     */
    suspend fun checkIfUserExists(login: String) {
        webClient
            .post()
            .uri(
                generateUri(
                    scheme = msClientProps.scheme,
                    host = msClientProps.host,
                    port = msClientProps.port
                )
            )
            .exchangeToMono { exchangeCatchingErrors(it) }
            .awaitSingle()
    }
}