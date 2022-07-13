package com.hoc081988.github_search_kmm.utils

import kotlinx.cinterop.convert
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey

/**
 * Converts a [Throwable] to a [NSError].
 *
 * The returned [NSError] has `KotlinException` as the [NSError.domain], `0` as the [NSError.code] and
 * the [NSError.localizedDescription] is set to the [Throwable.message].
 *
 * The Kotlin throwable can be retrieved from the [NSError.userInfo] with the key `KotlinException`.
 */
fun Throwable.asNSError(): NSError {
  val userInfo = buildMap<Any?, Any> {
    this["KotlinException"] = this
    message?.let {
      this[NSLocalizedDescriptionKey] = it
    }
  }
  return NSError.errorWithDomain(
    domain = "KotlinException",
    code = 0.convert(),
    userInfo = userInfo
  )
}
