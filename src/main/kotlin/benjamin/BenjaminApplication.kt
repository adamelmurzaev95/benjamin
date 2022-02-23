package benjamin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BenjaminApplication

fun main(args: Array<String>) {
    runApplication<BenjaminApplication>(*args)
}
