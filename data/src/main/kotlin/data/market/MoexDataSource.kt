package data.market

import java.time.LocalDate

class MoexDataSource(
    private val repository: MoexRepository,
    private val capeRepository: CapeRepository,
) {
    suspend fun fetchMarketData(): MarketData {
        val today = LocalDate.now()
        val from = today.minusDays(365)
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
        val cape = capeRepository.fetchCape()
        return MarketDataSerializer.toMarketData(closes, highs, cape)
    }
}