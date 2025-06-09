package webapp

import data.ChatConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpMethod

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigControllerTest(@Autowired val rest: TestRestTemplate) {
    @Test
    fun updateAndGetConfig() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val cfg = ChatConfig(monthlyFlow = 200000.0)
        rest.exchange("/api/config/1", HttpMethod.POST, HttpEntity(cfg, headers), String::class.java)
        val result = rest.getForObject("/api/config/1", ChatConfig::class.java)
        assertEquals(200000.0, result.monthlyFlow)
    }
}
