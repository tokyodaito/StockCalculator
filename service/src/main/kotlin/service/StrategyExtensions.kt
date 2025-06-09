package service

import bot.ChatConfig
import org.example.StrategyConfig

fun ChatConfig.toStrategyConfig(): StrategyConfig =
    StrategyConfig(monthlyFlow, baseDcaAmount, minCushionRatio)
