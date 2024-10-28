package ecowind.ru.auth

import ecowind.ru.auth.configs.properties.ClientProps
import ecowind.ru.auth.configs.properties.TokenProps
import ecowind.ru.auth.configs.properties.TraceProps
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["ecowind"])
@OpenAPIDefinition(info = Info(title = "Authorization service AUTH"))
@EnableConfigurationProperties(TraceProps::class, TokenProps::class, ClientProps::class)
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
