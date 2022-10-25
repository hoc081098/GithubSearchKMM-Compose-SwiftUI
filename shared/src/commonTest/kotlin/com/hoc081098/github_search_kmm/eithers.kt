package com.hoc081098.github_search_kmm

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.identity

inline val <L, R> Either<L, R>.leftValueOrThrow: L
  get() = fold(::identity) { this.throws(false) }

inline val <L, R> Either<L, R>.getOrThrow: R
  get() = getOrHandle { this.throws(true) }

@PublishedApi
internal fun <L, R> Either<L, R>.throws(right: Boolean): Nothing {
  if (right) {
    error("Expect a Right but got $this")
  } else {
    error("Expect a Left but got $this")
  }
}
