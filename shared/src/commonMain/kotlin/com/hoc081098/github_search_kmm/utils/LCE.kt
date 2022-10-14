package com.hoc081098.github_search_kmm.utils

import arrow.core.Either
import com.hoc081098.flowext.flowFromSuspend
import kotlin.jvm.JvmInline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface EitherLCE<out L, out R> {
  data object Loading : EitherLCE<Nothing, Nothing>

  @JvmInline
  value class ContentOrError<out L, out R>(val either: Either<L, R>) : EitherLCE<L, R>
}

fun <L, R> eitherLceFlow(function: suspend () -> Either<L, R>): Flow<EitherLCE<L, R>> =
  flowFromSuspend(function)
    .map<Either<L, R>, EitherLCE<L, R>> { EitherLCE.ContentOrError(it) }
    .onStart { emit(EitherLCE.Loading) }

sealed class LCE<out T> {
  object Loading : LCE<Nothing>()
  data class Content<out T>(val content: T) : LCE<T>()
  data class Error(val error: Throwable) : LCE<Nothing>()

  @Suppress("NOTHING_TO_INLINE")
  companion object Factory {
    inline fun <T> content(content: T): LCE<T> = Content(content)
    inline fun <T> loading(): LCE<T> = Loading
    inline fun <T> error(error: Throwable): LCE<T> = Error(error)
  }

  inline fun <R> map(f: (T) -> R): LCE<R> = when (this) {
    is Content -> Content(f(content))
    is Error -> Error(error)
    Loading -> Loading
  }

  inline fun <R> bimap(f: (T) -> R, fe: (Throwable) -> Throwable): LCE<R> =
    when (this) {
      is Content -> Content(f(content))
      is Error -> Error(fe(error))
      Loading -> Loading
    }

  inline val isLoading: Boolean get() = this === Loading

  inline val contentOrNull: T? get() = (this as? Content)?.content
}
