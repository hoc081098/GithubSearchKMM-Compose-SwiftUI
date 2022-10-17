package com.hoc081098.github_search_kmm.utils

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hoc081098.github_search_kmm.delay1Ms
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.fail
import kotlinx.coroutines.test.runTest

class LCETest {
  @Test
  fun `map LCE_Loading`() {
    assertSame(
      LCE.Loading,
      LCE.loading<Int>().map { it.toString() }
    )
  }

  @Test
  fun `map LCE_Error`() {
    val lce = LCE.error<Int>(RuntimeException()).map { it.toString() }

    assertIs<LCE.Error>(lce)
    assertSame(lce, lce)
  }

  @Test
  fun `map LCE_Content`() {
    val content = 123
    val lce = LCE.content(content).map { it.toString() }

    assertIs<LCE.Content<String>>(lce)
    assertEquals(LCE.Content(content.toString()), lce)
  }

  @Test
  fun `bimap LCE_Loading`() {
    val lce = LCE.loading<Int>().bimap<String>(f = { fail() }, fe = { fail() })
    assertSame(LCE.Loading, lce)
  }

  @Test
  fun `bimap LCE_Error`() {
    val lce = LCE.error<Int>(RuntimeException()).bimap<String>(
      f = { fail() },
      fe = {
        assertIs<RuntimeException>(it)
        TestException
      }
    )

    assertIs<LCE.Error>(lce)
    assertEquals(LCE.Error(TestException), lce)
  }

  @Test
  fun `bimap LCE_Content`() {
    val content = 123
    val lce = LCE.content(content).bimap(
      f = {
        assertEquals(content, it)
        it.toString()
      },
      fe = { fail() }
    )

    assertIs<LCE.Content<String>>(lce)
    assertEquals(LCE.Content(content.toString()), lce)
  }

  @Test
  fun `LCE_isLoading`() {
    assertEquals(true, LCE.loading<Int>().isLoading)
    assertEquals(false, LCE.error<Int>(RuntimeException()).isLoading)
    assertEquals(false, LCE.content(123).isLoading)
  }

  @Test
  fun `LCE_contentOrNull`() {
    assertEquals(null, LCE.loading<Int>().contentOrNull)
    assertEquals(null, LCE.error<Int>(RuntimeException()).contentOrNull)
    assertEquals(123, LCE.content(123).contentOrNull)
  }

  @Test
  fun `test eitherLceFlow with a Right value`() = runTest {
    val aRight: Either<String, Int> = 42.right()

    eitherLceFlow {
      delay1Ms()
      aRight
    }.test {
      assertEquals(EitherLCE.Loading, awaitItem())
      assertEquals(EitherLCE.ContentOrError(aRight), awaitItem())
      awaitComplete()
    }
  }

  @Test
  fun `test eitherLceFlow with a Left value`() = runTest {
    val aLeft: Either<String, Int> = "42".left()

    eitherLceFlow {
      delay1Ms()
      aLeft
    }.test {
      assertEquals(EitherLCE.Loading, awaitItem())
      assertEquals(EitherLCE.ContentOrError(aLeft), awaitItem())
      awaitComplete()
    }
  }
}

private object TestException : Exception()
