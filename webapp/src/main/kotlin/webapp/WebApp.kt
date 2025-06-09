package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class WebApp

fun startWebApp() {
    runApplication<WebApp>()
}
