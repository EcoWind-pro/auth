package ecowind.ru.auth.controllers

import ecowind.ru.auth.services.TraceService
import ecowind.ru.authapi.TraceAPI
import ecowind.ru.utils.endpoints.Controller
import ecowind.ru.utils.endpoints.PostEndpoint
import org.springframework.web.bind.annotation.RequestHeader

@Controller(value = TraceAPI.PREFIX, name = "The TraceController API")
class TraceController(
    private val traceService: TraceService
) {
    @PostEndpoint(value = TraceAPI.AUTH_TRACE, summary = "Granting access to the current trace to the system")
    fun authTrace(@RequestHeader("Authorization") accessToken: String) = traceService.authTrace(accessToken)

    @PostEndpoint(value = TraceAPI.CHECK_ACCESS, summary = "Checking the current trace`s access to the system")
    fun checkAccess(@RequestHeader("Authorization") accessToken: String) = traceService.checkTraceAuth(accessToken)
}