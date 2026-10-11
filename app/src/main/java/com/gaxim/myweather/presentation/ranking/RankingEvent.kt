package com.gaxim.myweather.presentation.ranking

import com.gaxim.myweather.presentation.common.ErrorKind

sealed interface RankingEvent {
    data class RefreshFailed(val kind: ErrorKind) : RankingEvent
}
