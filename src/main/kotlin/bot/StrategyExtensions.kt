package bot

import org.example.StrategyConfig

fun ChatConfig.toStrategyConfig(): StrategyConfig =
    StrategyConfig(monthlyFlow, baseDcaAmount, minCushionRatio)
