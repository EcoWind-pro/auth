package ecowind.ru.auth.actions

import ecowind.ru.auth.configs.properties.ClientProps
import ecowind.ru.exceptionhandler.WebClientExceptionsHandler.exchangeCatchingErrors
import ecowind.ru.utils.ActionUtils.generateUri
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ClientAction(
    private val webClient: WebClient,
    private val clientProps: ClientProps
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
                    scheme = clientProps.scheme,
                    host = clientProps.host,
                    port = clientProps.port
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
                    scheme = clientProps.scheme,
                    host = clientProps.host,
                    port = clientProps.port
                )
            )
            .exchangeToMono { exchangeCatchingErrors(it) }
            .awaitSingle()
    }
}