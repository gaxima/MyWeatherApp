package com.gaxim.myweather.presentation.ranking

import com.gaxim.myweather.presentation.common.ErrorKind

/** One-shot effects of the ranking screen. */
sealed interface RankingEvent {
    data class RefreshFailed(val kind: ErrorKind) : RankingEvent
}
