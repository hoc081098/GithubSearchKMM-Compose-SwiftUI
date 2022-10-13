package com.hoc081098.github_search_kmm.presentation

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.right
import com.hoc081098.github_search_kmm.TestAppCoroutineDispatchers
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.presentation.GithubSearchState.Companion.FIRST_PAGE
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.given
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock

class GithubSearchViewModelTest {
  private lateinit var vm: GithubSearchViewModel

  @Mock
  private lateinit var repoItemRepository: RepoItemRepository
  private lateinit var searchRepoItemsUseCase: SearchRepoItemsUseCase
  private val testAppCoroutineDispatchers = TestAppCoroutineDispatchers(
    testCoroutineDispatcher = UnconfinedTestDispatcher()
  )

  @BeforeTest
  fun setup() {
    Dispatchers.setMain(testAppCoroutineDispatchers.testCoroutineDispatcher)

    repoItemRepository = mock(classOf())
    searchRepoItemsUseCase = SearchRepoItemsUseCase(repoItemRepository)
    vm = GithubSearchViewModel(
      searchRepoItemsUseCase = searchRepoItemsUseCase,
    )
  }

  @AfterTest
  fun teardown() {
    verify(repoItemRepository).hasNoUnverifiedExpectations()
    verify(repoItemRepository).hasNoUnmetExpectations()

    Dispatchers.resetMain()
  }

  @Test
  fun `rejects blank term WHEN dispatch a Search action with a blank string`() = runTest {
    vm.dispatch(GithubSearchAction.Search("   "))

    vm.stateFlow.test {
      assertEquals(
        GithubSearchState.initial(),
        awaitItem()
      )
      delay(EXTRA_DELAY)
      expectNoEvents()
    }
  }

  @Test
  fun `searches repo items and emits items WHEN dispatch a Search action with a non-blank string and searchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val term = "term"
      val page = FIRST_PAGE.toInt() + 1
      val repoItems = genRepoItems(0..10)
      mockSearchRepoItemsUseCase(term = term, page = page) { repoItems.right() }

      vm.dispatch(GithubSearchAction.Search(term))

      vm.stateFlow.test {
        assertEquals(
          GithubSearchState.initial(),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term, // update term
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = page.toUInt(), // update page
            term = term,
            items = repoItems, // update items
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `debounce search actions and emits items WHEN dispatch multiple Search action with non-blank strings and searchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val terms = List(5) { it.toString() }
      val finalTerm = terms.last()
      val page = FIRST_PAGE.toInt() + 1
      val repoItems = genRepoItems(0..10)
      mockSearchRepoItemsUseCase(term = finalTerm, page = page) { repoItems.right() }

      launch {
        terms.forEach {
          vm.dispatch(GithubSearchAction.Search(it))
          delay(SEMI_DELAY)
        }
      }

      vm.stateFlow.test {
        assertEquals(
          GithubSearchState.initial(),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = finalTerm, // update term
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = page.toUInt(), // update page
            term = finalTerm,
            items = repoItems, // update items
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }

      verify(repoItemRepository)
        .coroutine { searchRepoItems(finalTerm, page) }
        .wasInvoked(exactly = once)
    }

  private suspend inline fun mockSearchRepoItemsUseCase(
    term: String,
    page: Int,
    crossinline result: () -> Either<AppError, List<RepoItem>>
  ) = given(repoItemRepository)
    .coroutine { searchRepoItemsUseCase(term, page) }
    .then { result() }

  private companion object {
    private val EXTRA_DELAY = GithubSearchSideEffects.DEBOUNCE_TIME * 1.5
    private val SEMI_DELAY = GithubSearchSideEffects.DEBOUNCE_TIME * 0.5

    private fun genRepoItems(ids: IntRange): PersistentList<RepoItem> = ids.map { id ->
      RepoItem(
        id = id,
        fullName = "Full name: $id",
        language = "Kotlin",
        starCount = Random.nextInt(),
        name = "Name: $id",
        repoDescription = "Description: $id",
        languageColor = ArgbColor.parse("#FF112233").getOrHandle { error(it) },
        htmlUrl = "url/$id",
        owner = Owner(
          id = id,
          username = "username $id",
          avatar = "avatar/$id"
        ),
        updatedAt = Clock.System.now()
      )
    }.toPersistentList()
  }
}
