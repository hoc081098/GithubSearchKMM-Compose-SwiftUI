package com.hoc081098.github_search_kmm.utils

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend inline fun <A, B, L, R> parZipEither(
  ctx: CoroutineContext = EmptyCoroutineContext,
  crossinline fa: suspend () -> Either<L, A>,
  crossinline fb: suspend () -> Either<L, B>,
  crossinline combiner: suspend (A, B) -> R,
): Either<L, R> = either {
  parZip(
    ctx = ctx,
    fa = { fa().bind() },
    fb = { fb().bind() },
    f = { a, b -> combiner(a, b) }
  )
}
