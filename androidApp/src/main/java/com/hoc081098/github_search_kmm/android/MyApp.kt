package com.hoc081098.github_search_kmm.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@HiltAndroidApp
class MyApp : Application() {
  override fun onCreate() {
    super.onCreate()
    Napier.base(DebugAntilog())
  }
}
