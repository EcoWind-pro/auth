package ecowind.ru.auth

import ecowind.ru.auth.configs.properties.MsClientProps
import ecowind.ru.auth.configs.properties.TokenProps
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["ecowind"])
@OpenAPIDefinition(info = Info(title = "Authorization service AUTH", version = "1.0"))
@EnableConfigurationProperties(TokenProps::class, MsClientProps::class)
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args)
}
