package com.hoc081098.github_search_kmm.utils

import arrow.core.getOrHandle
import arrow.core.left
import arrow.core.right
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.test.runTest

class ParZipEitherTest {
  @Test
  fun `parZipEither with 2 Right values`() = runTest {
    val either = parZipEither<Int, Int, String, Int>(
      fa = { 1.right() },
      fb = { 2.right() },
      combiner = { a, b -> a + b }
    )

    assertEquals(
      3,
      either.getOrHandle { fail(it) }
    )
  }

  @Test
  fun `parZipEither with 1 Right and 1 Left`() = runTest {
    val either = parZipEither<Int, Int, String, Int>(
      fa = { 1.right() },
      fb = { "2".left() },
      combiner = { a, b -> a + b }
    )
    assertEquals("2".left(), either)
  }

  @Test
  fun `parZipEither with 1 Left and 1 Right`() = runTest {
    val either = parZipEither<Int, Int, String, Int>(
      fa = { "1".left() },
      fb = { 2.right() },
      combiner = { a, b -> a + b }
    )
    assertEquals("1".left(), either)
  }

  @Test
  fun `parZipEither with 2 Left values`() = runTest {
    val either = parZipEither<Int, Int, String, Int>(
      fa = { "1".left() },
      fb = { "2".left() },
      combiner = { a, b -> a + b }
    )
    assertEquals("1".left(), either)
  }
}
