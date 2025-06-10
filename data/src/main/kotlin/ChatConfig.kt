package data

import java.time.LocalTime

import data.OnboardingStage

data class ChatConfig(
    var monthlyFlow: Double = 100_000.0,
    var baseDcaAmount: Double = 50_000.0,
    var minCushionRatio: Double = 3.0,
    var reportTime: LocalTime = LocalTime.of(10, 0),
    var onboardingStage: OnboardingStage = OnboardingStage.NONE,
    var expenses: Double = 0.0,
    var flow: Double = 0.0,
)
