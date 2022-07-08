package com.hoc081988.github_search_kmm

actual class Platform actual constructor() {
  actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}
