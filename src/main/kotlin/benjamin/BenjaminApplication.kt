package benjamin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
class BenjaminApplication

fun main(args: Array<String>) {
    runApplication<BenjaminApplication>(*args)
}
