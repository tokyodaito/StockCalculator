package data

import java.time.LocalTime

data class ChatConfig(
    var monthlyFlow: Double = 100_000.0,
    var baseDcaAmount: Double = 50_000.0,
    var minCushionRatio: Double = 3.0,
    var reportTime: LocalTime = LocalTime.of(10, 0)
)
