package org.example

data class StrategyConfig(
    val monthlyFlow: Double = 100_000.0,
    val baseDcaAmount: Double = 50_000.0,
    val minCushionRatio: Double = 3.0,
)
