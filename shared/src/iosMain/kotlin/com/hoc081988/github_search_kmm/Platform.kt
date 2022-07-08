package com.hoc081988.github_search_kmm

import platform.UIKit.UIDevice

actual class Platform actual constructor() {
  actual val platform: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}
