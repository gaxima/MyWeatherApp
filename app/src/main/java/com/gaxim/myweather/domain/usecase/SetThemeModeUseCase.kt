package com.gaxim.myweather.domain.usecase

import com.gaxim.myweather.domain.model.ThemeMode
import com.gaxim.myweather.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository,
) {
    suspend operator fun invoke(mode: ThemeMode) = themeRepository.setThemeMode(mode)
}
