package webapp

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import service.DcaService
import kotlin.test.assertEquals
import data.macro.MacroData
import data.market.MarketData

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportControllerTest {
    @TestConfiguration
    class Config {
        @Bean
        fun dcaService(): DcaService = DcaService(
            fetchMarketData = {
                MarketData(1000.0, 1200.0, 1100.0, 1150.0, 30.0, 5.0, 6.0)
            },
            fetchMacroData = { MacroData(70.0, 10.0, 9.5) }
        )
    }

    @Autowired
    lateinit var rest: TestRestTemplate

    @Test
    fun reportReturnsText() {
        val response = rest.getForEntity("/api/report", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assert(response.body!!.isNotEmpty())
    }
}
