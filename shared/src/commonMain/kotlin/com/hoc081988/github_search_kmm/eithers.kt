package com.hoc081988.github_search_kmm

import arrow.core.Either
import arrow.core.continuations.either
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend inline fun <A, B, L, R> parZipEither(
  ctx: CoroutineContext = EmptyCoroutineContext,
  crossinline fa: suspend () -> Either<L, A>,
  crossinline fb: suspend () -> Either<L, B>,
  crossinline combiner: (A, B) -> R,
): Either<L, R> = either {
  coroutineScope {
    val a = async(ctx) { fa().bind() }
    val b = async(ctx) { fb().bind() }
    combiner(a.await(), b.await())
  }
}
