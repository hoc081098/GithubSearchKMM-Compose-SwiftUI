@file:Suppress("unused")

package com.hoc081098.github_search_kmm.utils

import arrow.core.Either

object EitherExt {
  fun <L, R> left(value: L): Either<L, R> = Either.Left(value)
  fun <L, R> right(value: R): Either<L, R> = Either.Right(value)
}
