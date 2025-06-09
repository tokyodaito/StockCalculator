package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebAppBackendApplication

fun main(args: Array<String>) {
    runApplication<WebAppBackendApplication>(*args)
}
