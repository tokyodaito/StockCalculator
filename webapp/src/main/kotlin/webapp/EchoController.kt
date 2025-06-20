package webapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoController {
    @GetMapping("/api/echo")
    fun echo(@RequestParam text: String): String = text
}
