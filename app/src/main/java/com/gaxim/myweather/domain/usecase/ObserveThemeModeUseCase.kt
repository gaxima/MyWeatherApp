package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository,
) {
    operator fun invoke(): Flow<ThemeMode> = themeRepository.observeThemeMode()
}
