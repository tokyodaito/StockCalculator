package service.utils

import data.ChatConfig
import service.dto.StrategyConfig

fun ChatConfig.toStrategyConfig(): StrategyConfig = StrategyConfig(monthlyFlow, baseDcaAmount, minCushionRatio)
