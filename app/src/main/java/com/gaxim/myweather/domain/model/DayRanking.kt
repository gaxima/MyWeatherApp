package com.gaxim.myweather.domain.model

import java.time.LocalDate

data class DayRanking(
    val date: LocalDate,
    val scores: List<ActivityScore>,
)
