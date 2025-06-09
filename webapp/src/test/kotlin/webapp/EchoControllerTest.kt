package webapp

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EchoControllerTest {
    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun echoReturnsText() {
        val response = rest.getForEntity("/api/echo?text=hi", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("hi", response.body)
    }
}
