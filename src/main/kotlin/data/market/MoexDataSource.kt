package data.market

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class MoexDataSource(
     val repository: MoexRepository,
) {
    suspend fun fetchMarketData(): MarketData = coroutineScope {
        val today = LocalDate.now()
        val from = today.minusDays(365)

        val historyDef = async {
            val closes = mutableListOf<Double>()
            val highs = mutableListOf<Double>()
            var start = 0
            var total: Int
            var pageSize: Int
            do {
                val root = repository.fetchPage(from, today, start)
                val page = MarketDataSerializer.parsePage(root)
                closes += page.closes
                highs += page.highs
                total = page.total
                pageSize = page.pageSize
                start += pageSize
            } while (start < total)
            closes to highs
        }

        val analyticsDef = async { MarketDataSerializer.parseAnalytics(repository.fetchAnalytics()) }
        val zcycDef = async { MarketDataSerializer.parseZcyc(repository.fetchZcyc(from, today)) }

        val (closes, highs) = historyDef.await()
        val (pe, dy) = analyticsDef.await()
        val ofz = zcycDef.await()
        MarketDataSerializer.toMarketData(closes, highs, pe, dy, ofz)
    }
}