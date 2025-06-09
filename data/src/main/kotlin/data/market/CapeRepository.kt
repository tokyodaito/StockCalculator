package data.market

interface CapeRepository {
    suspend fun fetchCape(): Double
}
