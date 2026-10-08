package com.gaxim.myweather.domain.scoring

import com.gaxim.myweather.domain.model.ActivityScore
import com.gaxim.myweather.domain.model.DailyForecast

/** Pure, deterministic scoring of one activity for one day. No I/O, no clock. */
interface ActivityScorer {
    fun score(forecast: DailyForecast): ActivityScore
}
