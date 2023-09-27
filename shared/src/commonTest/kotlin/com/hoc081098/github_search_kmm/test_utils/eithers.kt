package com.hoc081098.github_search_kmm.test_utils

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.identity
import kotlin.test.fail

inline val <L, R> Either<L, R>.leftValueOrThrow: L
  get() = fold(::identity) { this.throws(false) }

inline val <L, R> Either<L, R>.getOrThrow: R
  get() = getOrElse { this.throws(true) }

@PublishedApi
internal fun <L, R> Either<L, R>.throws(right: Boolean): Nothing {
  if (right) {
    fail("Expect a Right but got $this")
  } else {
    fail("Expect a Left but got $this")
  }
}
