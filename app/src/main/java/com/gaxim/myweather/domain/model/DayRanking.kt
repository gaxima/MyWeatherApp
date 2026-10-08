package com.gaxim.myweather.domain.model

import java.time.LocalDate

/** All activities for one [date], best first. */
data class DayRanking(
    val date: LocalDate,
    val scores: List<ActivityScore>,
)
