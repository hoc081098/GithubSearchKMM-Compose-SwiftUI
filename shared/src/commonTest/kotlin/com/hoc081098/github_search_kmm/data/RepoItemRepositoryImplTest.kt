package com.hoc081098.github_search_kmm.data

import arrow.core.left
import arrow.core.right
import com.hoc081098.github_search_kmm.TestAntilog
import com.hoc081098.github_search_kmm.TestAppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.getOrThrow
import com.hoc081098.github_search_kmm.leftValueOrThrow
import io.github.aakira.napier.Napier
import io.mockative.Mock
import io.mockative.given
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock

class RepoItemRepositoryImplTest {
  private lateinit var repoItemRepositoryImpl: RepoItemRepositoryImpl

  @Mock
  private lateinit var repoItemApi: RepoItemApi

  @Mock
  private lateinit var githubLanguageColorApi: GithubLanguageColorApi

  @Mock
  private lateinit var errorMapper: AppErrorMapper

  private val appCoroutineDispatchers = TestAppCoroutineDispatchers()
  private val antilog = TestAntilog()

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(appCoroutineDispatchers.testCoroutineDispatcher)
    Napier.base(antilog)

    repoItemApi = mock(RepoItemApi::class)
    githubLanguageColorApi = mock(GithubLanguageColorApi::class)
    errorMapper = mock(AppErrorMapper::class)

    repoItemRepositoryImpl = RepoItemRepositoryImpl(
      repoItemApi = repoItemApi,
      githubLanguageColorApi = githubLanguageColorApi,
      errorMapper = errorMapper,
      appCoroutineDispatchers = appCoroutineDispatchers
    )
  }

  @AfterTest
  fun teardown() {
    arrayOf(repoItemApi, githubLanguageColorApi, errorMapper).forEach {
      verify(it).hasNoUnverifiedExpectations()
      verify(it).hasNoUnmetExpectations()
    }

    Napier.takeLogarithm(antilog)
    Dispatchers.resetMain()
  }

  @Test
  fun `searchRepoItems returns a Right WHEN both GithubLanguageColorApi_getColors and RepoItemApi_searchRepoItems return Right value`() =
    runTest(appCoroutineDispatchers.testCoroutineDispatcher) {
      val term = "term"
      val page = 1

      given(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .then { FAKE_REPO_ITEMS_SEARCH_RESPONSE.right() }
      given(githubLanguageColorApi)
        .coroutine { getColors() }
        .then { FAKE_GITHUB_LANGUAGE_COLORS.right() }

      val either = repoItemRepositoryImpl.searchRepoItems(term, page)
      assertEquals(
        FAKE_REPO_ITEMS,
        either.getOrThrow
      )

      verify(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
      verify(githubLanguageColorApi)
        .coroutine { getColors() }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `searchRepoItems returns a Left WHEN GithubLanguageColorApi_getColors returns a Left and RepoItemApi_searchRepoItems returns a Right`() =
    runTest(appCoroutineDispatchers.testCoroutineDispatcher) {
      val term = "term"
      val page = 1
      val error = RuntimeException("Broken!")

      given(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .then { error.left() }
      given(githubLanguageColorApi)
        .coroutine { getColors() }
        .then { FAKE_GITHUB_LANGUAGE_COLORS.right() }
      given(errorMapper)
        .invocation { invoke(error) }
        .then { AppError.ApiException.NetworkException(error) }

      val either = repoItemRepositoryImpl.searchRepoItems(term, page)
      assertIs<AppError.ApiException.NetworkException>(either.leftValueOrThrow)

      verify(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
      verify(githubLanguageColorApi)
        .coroutine { getColors() }
        .wasInvoked(exactly = once)
      verify(errorMapper)
        .invocation { invoke(error) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `searchRepoItems returns a Left WHEN GithubLanguageColorApi_getColors returns a Right and RepoItemApi_searchRepoItems returns a Left`() =
    runTest(appCoroutineDispatchers.testCoroutineDispatcher) {
      val term = "term"
      val page = 1
      val error = RuntimeException("Broken!")

      given(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .then { FAKE_REPO_ITEMS_SEARCH_RESPONSE.right() }
      given(githubLanguageColorApi)
        .coroutine { getColors() }
        .then { error.left() }
      given(errorMapper)
        .invocation { invoke(error) }
        .then { AppError.ApiException.NetworkException(error) }

      val either = repoItemRepositoryImpl.searchRepoItems(term, page)
      assertIs<AppError.ApiException.NetworkException>(either.leftValueOrThrow)

      verify(repoItemApi)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
      verify(githubLanguageColorApi)
        .coroutine { getColors() }
        .wasInvoked(exactly = once)
      verify(errorMapper)
        .invocation { invoke(error) }
        .wasInvoked(exactly = once)
    }

  private companion object {
    val FAKE_GITHUB_LANGUAGE_COLORS: Map<String, ArgbColor> = mapOf(
      "Kotlin" to ArgbColor
        .parse("#814CCC")
        .getOrThrow,
      "Java" to ArgbColor
        .parse("#B07219")
        .getOrThrow,
      "JavaScript" to ArgbColor
        .parse("#F1E05A")
        .getOrThrow,
    )

    val FAKE_REPO_ITEMS_SEARCH_RESPONSE = RepoItemsSearchResponse(
      totalCount = 100,
      incompleteResults = true,
      items = (0..10).map { id ->
        RepoItemsSearchResponse.Item(
          id = id,
          fullName = "Full name: $id",
          language = "Kotlin",
          stargazersCount = Random.nextInt(),
          name = "Name: $id",
          description = "Description: $id",
          htmlUrl = "url/$id",
          owner = RepoItemsSearchResponse.Item.Owner(
            id = id,
            login = "username $id",
            avatarUrl = "avatar/$id"
          ),
          updatedAt = Clock.System.now()
        )
      }
    )

    val FAKE_REPO_ITEMS =
      FAKE_REPO_ITEMS_SEARCH_RESPONSE.toRepoItemsList(FAKE_GITHUB_LANGUAGE_COLORS)
  }
}
