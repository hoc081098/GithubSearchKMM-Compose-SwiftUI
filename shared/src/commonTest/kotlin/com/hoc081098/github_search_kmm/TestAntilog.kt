package com.hoc081098.github_search_kmm

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

class TestAntilog : Antilog() {
  override fun performLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?
  ) {
    println("$priority [$tag]: $message")
    if (throwable != null) {
      println(throwable)
    }
  }
}
