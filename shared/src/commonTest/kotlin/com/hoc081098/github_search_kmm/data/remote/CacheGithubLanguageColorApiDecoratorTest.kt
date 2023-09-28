package com.hoc081098.github_search_kmm.data.remote

import arrow.core.left
import arrow.core.right
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.test_utils.TestAntilog
import com.hoc081098.github_search_kmm.test_utils.getOrThrow
import com.hoc081098.github_search_kmm.test_utils.invokesWithoutArgs
import io.github.aakira.napier.Napier
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import io.mockative.once
import io.mockative.twice
import io.mockative.verify
import io.mockative.verifyNoUnmetExpectations
import io.mockative.verifyNoUnverifiedExpectations
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class CacheGithubLanguageColorApiDecoratorTest {
  private val antilog = TestAntilog()

  private lateinit var cacheGithubLanguageColorApiDecorator: CacheGithubLanguageColorApiDecorator
  private lateinit var decoratee: GithubLanguageColorApi

  @BeforeTest
  fun setup() {
    Napier.base(antilog)

    decoratee = mock(GithubLanguageColorApi::class)
    cacheGithubLanguageColorApiDecorator = CacheGithubLanguageColorApiDecorator(
      decoratee = decoratee
    )
  }

  @AfterTest
  fun teardown() {
    verifyNoUnverifiedExpectations(decoratee)
    verifyNoUnmetExpectations(decoratee)

    Napier.takeLogarithm(antilog)
  }

  @Test
  fun `cache Right value WHEN decoratee_getColors returns a Right value`() = runTest {
    val right = mapOf("kotlin" to ArgbColor.parse("#F18E33").getOrThrow).right()

    every { decoratee.toString() }
      .returns("GithubLanguageColorApi")

    coEvery { decoratee.getColors() }
      .invokesWithoutArgs {
        delay(100)
        right
      }

    (
      (0..20)
        .map { v ->
          async {
            if (v <= 10) {
              delay(200)
            }
            cacheGithubLanguageColorApiDecorator.getColors()
          }
        }
        .awaitAll() + cacheGithubLanguageColorApiDecorator.getColors()
      )
      .forEach { assertEquals(right, it) }

    coVerify { decoratee.getColors() }
      .wasInvoked(exactly = once)
    coVerify { decoratee.toString() }
      .wasInvoked()
  }

  @Test
  fun `cache Right value WHEN decoratee_getColors returns a Left value`() = runTest {
    val left = RuntimeException().left()
    val right = mapOf("kotlin" to ArgbColor.parse("#F18E33").getOrThrow).right()

    var call = 0

    every { decoratee.toString() }
      .returns("GithubLanguageColorApi")

    coEvery { decoratee.getColors() }
      .invokesWithoutArgs {
        delay(100)

        when (call++) {
          0 -> left
          1 -> right
          else -> error("Should not be called")
        }
      }

    val eithers = (
      (0..20)
        .map { v ->
          async {
            if (v in 10..15) {
              delay(200)
            }
            cacheGithubLanguageColorApiDecorator.getColors()
          }
        }
        .awaitAll() + cacheGithubLanguageColorApiDecorator.getColors()
      )

    assertEquals(
      listOf(left) + (1..21).map { right },
      eithers
    )

    coVerify { decoratee.getColors() }
      .wasInvoked(exactly = twice)
    verify { decoratee.toString() }
      .wasInvoked()
  }
}
