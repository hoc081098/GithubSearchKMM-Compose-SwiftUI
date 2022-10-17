package com.hoc081098.github_search_kmm

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TestAntilog : Antilog() {
  override fun performLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?
  ) {
    if (BuildKonfig.IS_CI_BUILD) {
      return
    }

    val dateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    val priorityChar = when (priority) {
      LogLevel.VERBOSE -> 'V'
      LogLevel.DEBUG -> 'D'
      LogLevel.INFO -> 'I'
      LogLevel.WARNING -> 'W'
      LogLevel.ERROR -> 'E'
      LogLevel.ASSERT -> 'A'
    }
    println("$dateTime $priorityChar/$tag: $message")

    if (throwable != null) {
      println(throwable)
    }
  }
}
