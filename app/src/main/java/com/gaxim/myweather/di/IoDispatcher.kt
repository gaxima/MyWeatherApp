package com.gaxim.myweather.di

import javax.inject.Qualifier

/** Marks the dispatcher for blocking I/O so it can be replaced in tests. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
