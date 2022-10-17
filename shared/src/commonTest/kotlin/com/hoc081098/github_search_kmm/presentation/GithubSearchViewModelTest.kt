package com.hoc081098.github_search_kmm.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import app.cash.turbine.testIn
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hoc081098.github_search_kmm.TestAntilog
import com.hoc081098.github_search_kmm.TestAppCoroutineDispatchers
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.genRepoItems
import com.hoc081098.github_search_kmm.presentation.GithubSearchState.Companion.FIRST_PAGE
import io.github.aakira.napier.Napier
import io.mockative.Mock
import io.mockative.given
import io.mockative.mock
import io.mockative.once
import io.mockative.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class GithubSearchViewModelTest {
  private lateinit var vm: GithubSearchViewModel

  @Mock
  private lateinit var repoItemRepository: RepoItemRepository
  private lateinit var searchRepoItemsUseCase: SearchRepoItemsUseCase

  private val testAppCoroutineDispatchers = TestAppCoroutineDispatchers(
    testCoroutineDispatcher = UnconfinedTestDispatcher()
  )
  private val antilog = TestAntilog()

  @BeforeTest
  fun setup() {
    Napier.base(antilog)
    Dispatchers.setMain(testAppCoroutineDispatchers.testCoroutineDispatcher)

    repoItemRepository = mock(RepoItemRepository::class)
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
    Napier.takeLogarithm(antilog)
  }

  @Test
  fun `rejects blank term WHEN dispatching a Search action with a blank string`() = runTest {
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
  fun `debounces and rejects blank term WHEN dispatching multiple Search actions and the last term is blank`() =
    runTest {
      val terms = List(5) { it.toString() } + " "

      launch {
        terms.forEach {
          delay(SEMI_DELAY)
          vm.dispatch(GithubSearchAction.Search(it))
        }
      }

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
  fun `debounces _ rejects blank term and skip subsequent repetitions term WHEN dispatching multiple Search actions`() =
    runTest {
      val finalTerm = "#final"
      val page = PAGE_1
      val repoItems = genRepoItems(0..5)
      mockSearchRepoItemsUseCase(
        term = finalTerm,
        page = page,
      ) { repoItems.right() }

      val termsFlow = flow {
        repeat(5) {
          delay(SEMI_DELAY)
          emit(it.toString())
        }

        delay(SEMI_DELAY)
        emit(" ") // emitted then rejected

        delay(EXTRA_DELAY)
        emit(finalTerm) // [emitted]

        delay(EXTRA_DELAY)
        emit(finalTerm) // skipped
      }

      launch {
        termsFlow.collect {
          vm.dispatch(GithubSearchAction.Search(it))
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
            term = finalTerm, // updated term
            items = persistentListOf(),
            isLoading = true, // toggle isLoading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = page.toUInt(), // updated page
            term = finalTerm,
            items = repoItems, // updated items
            isLoading = false, // toggle isLoading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        delay(EXTRA_DELAY)
        delay(EXTRA_DELAY)
        expectNoEvents()
      }

      verify(repoItemRepository)
        .coroutine { searchRepoItemsUseCase(term = finalTerm, page = page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `emits loading state and items state WHEN SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val term = "term"
      val page = PAGE_1
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
  fun `emits loading state and items state WHEN SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "term"
      val page = PAGE_1
      mockSearchRepoItemsUseCase(term = term, page = page) { emptyList<RepoItem>().right() }

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
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // update hasReachedMax
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.ReachedMaxItems)

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `emits loading state and error state WHEN SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "term"
      val page = PAGE_1
      val networkException = AppError.ApiException.NetworkException(null)

      mockSearchRepoItemsUseCase(term = term, page = page) { networkException.left() }

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
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = networkException, // update error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(networkException))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `debounces _ emits loading state and items state WHEN dispatching multiple Search actions and SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val terms = List(5) { it.toString() }
      val finalTerm = terms.last()
      val page = PAGE_1
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

  @Test
  fun `debounces _ emits loading state and items state WHEN dispatching multiple Search actions and SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val terms = List(5) { it.toString() }
      val finalTerm = terms.last()
      val page = PAGE_1
      mockSearchRepoItemsUseCase(term = finalTerm, page = page) { emptyList<RepoItem>().right() }

      launch {
        terms.forEach {
          delay(SEMI_DELAY)
          vm.dispatch(GithubSearchAction.Search(it))
        }
        delay(EXTRA_DELAY)
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
            page = FIRST_PAGE,
            term = finalTerm,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // update hasReachedMax
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.ReachedMaxItems)

      verify(repoItemRepository)
        .coroutine { searchRepoItems(finalTerm, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `debounces _ emits loading state and error state WHEN dispatching multiple Search actions and SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val terms = List(5) { it.toString() }
      val finalTerm = terms.last()
      val page = PAGE_1
      val networkException = AppError.ApiException.NetworkException(null)
      mockSearchRepoItemsUseCase(term = finalTerm, page = page) { networkException.left() }

      launch {
        terms.forEach {
          delay(SEMI_DELAY)
          vm.dispatch(GithubSearchAction.Search(it))
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
            page = FIRST_PAGE,
            term = finalTerm,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = networkException, // update error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(networkException))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(finalTerm, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `debounces _ cancels previous execution WHEN dispatching multiple Search actions and the previous execution is not completed yet`() =
    runTest {
      val query1 = "#hoc081098"
      val query2 = "#FlowExt"
      val page = PAGE_1
      val items = genRepoItems(0..10)

      mockSearchRepoItemsUseCase(term = query1, page = page) {
        awaitCancellation() // suspend forever
      }
      mockSearchRepoItemsUseCase(term = query2, page = page) { items.right() }

      vm.stateFlow.test {
        assertEquals(
          GithubSearchState.initial(),
          awaitItem()
        )

        launch {
          vm.dispatch(GithubSearchAction.Search(query1))
          delay(EXTRA_DELAY)
          vm.dispatch(GithubSearchAction.Search(query2))
          delay(EXTRA_DELAY)
        }

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = query1, // update term
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        // still loading, but the term is updated
        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = query2, // update term
            items = persistentListOf(),
            isLoading = true,
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = page.toUInt(), // update page
            term = query2,
            items = items, // update items
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
        .coroutine { searchRepoItems(query1, page) }
        .wasInvoked(exactly = once)
      verify(repoItemRepository)
        .coroutine { searchRepoItems(query2, page) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `ignores loading next page WHEN dispatching a LoadNextPage action`() = runTest {
    vm.dispatch(GithubSearchAction.LoadNextPage)
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
  fun `cancels loading next page WHEN dispatching a LoadNextPage action and a Search action`() =
    runTest {
      val term = "#hoc081098"
      val nextTerm = "#FlowExt"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) {
        awaitCancellation() // never completes
      }

      val nextItems = genRepoItems(0..10)
      mockSearchRepoItemsUseCase(
        term = nextTerm,
        page = PAGE_1
      ) { nextItems.right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch {
          vm.dispatch(GithubSearchAction.LoadNextPage)
          delay(SEMI_DELAY)
          vm.dispatch(GithubSearchAction.Search(nextTerm))
        }

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        // switch to next term
        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE, // reset page
            term = nextTerm,
            items = persistentListOf(), // clear items
            isLoading = true,
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(), // increase page
            term = nextTerm,
            items = nextItems, // set items
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
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)

      verify(repoItemRepository)
        .coroutine { searchRepoItems(nextTerm, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page WHEN dispatching LoadNextPage action and SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      val nextPageItems = genRepoItems(11..20)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) { nextPageItems.right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.LoadNextPage)

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = 2u, // update page
            term = term,
            items = (items + nextPageItems).toPersistentList(), // update items
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
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page WHEN dispatching LoadNextPage action and SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) { emptyList<RepoItem>().right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.LoadNextPage)

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // set hasReachedMax to true
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.ReachedMaxItems)

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page WHEN dispatching LoadNextPage action and SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      val nextPageError = AppError.ApiException.NetworkException(null)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) { nextPageError.left() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.LoadNextPage)

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = false, // toggle loading
            error = nextPageError, // set error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(nextPageError))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page _ ignores other LoadNextPage actions WHEN dispatching LoadNextPage actions and SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      val nextPageItems = genRepoItems(11..20)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) {
        delay(1_000)
        nextPageItems.right()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.LoadNextPage)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = 2u, // update page
            term = term,
            items = (items + nextPageItems).toPersistentList(), // update items
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
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page _ ignores other LoadNextPage actions WHEN dispatching LoadNextPage action and SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) {
        delay(1_000)
        emptyList<RepoItem>().right()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.LoadNextPage)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // set hasReachedMax to true
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.ReachedMaxItems)

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `loads next page _ ignores other LoadNextPage actions WHEN dispatching LoadNextPage action and SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val items = genRepoItems(0..10)

      val page1State = reachToPage1(term = term, items = items)

      val nextPageError = AppError.ApiException.NetworkException(null)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_2) {
        delay(1_000)
        nextPageError.left()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.LoadNextPage)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = true, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(),
            term = term,
            items = items,
            isLoading = false, // toggle loading
            error = nextPageError, // set error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(nextPageError))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_2) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `ignores retrying next page WHEN dispatching a Retry action`() = runTest {
    vm.dispatch(GithubSearchAction.Retry)
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
  fun `cancels retrying next page WHEN dispatching a Retry action and a Search action`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val nextTerm = "#FlowExt"
      val error = AppError.ApiException.NetworkException(null)

      val page1State = reachToErrorState(term = term, error = error)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) {
        awaitCancellation() // never completes
      }

      val nextItems = genRepoItems(0..10)
      mockSearchRepoItemsUseCase(
        term = nextTerm,
        page = PAGE_1
      ) { nextItems.right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch {
          vm.dispatch(GithubSearchAction.Retry)
          delay(SEMI_DELAY)
          vm.dispatch(GithubSearchAction.Search(nextTerm))
        }

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        // switch to next term
        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = nextTerm,
            items = persistentListOf(), // clear items
            isLoading = true,
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(), // increase page
            term = nextTerm,
            items = nextItems, // set items
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(error))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
      verify(repoItemRepository)
        .coroutine { searchRepoItems(nextTerm, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page WHEN dispatching Retry action and SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      val nextPageItems = genRepoItems(11..20)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) { nextPageItems.right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.Retry)

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(), // increase page
            term = term,
            items = nextPageItems, // update items
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(error))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page WHEN dispatching Retry action and SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) { emptyList<RepoItem>().right() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.Retry)

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // set hasReachedMax to true
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(
        GithubSearchSingleEvent.SearchFailure(error),
        GithubSearchSingleEvent.ReachedMaxItems
      )

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page WHEN dispatching Retry action and SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      val nextError = AppError.ApiException.UnknownException(null)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) { nextError.left() }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        vm.dispatch(GithubSearchAction.Retry)

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = nextError, // set error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(
        GithubSearchSingleEvent.SearchFailure(error),
        GithubSearchSingleEvent.SearchFailure(nextError)
      )

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page _ ignores other Retry actions WHEN dispatching Retry action and SearchRepoItemsUseCase returns a non-empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      val nextPageItems = genRepoItems(11..20)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) {
        delay(1_000)
        nextPageItems.right()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.Retry)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = PAGE_1.toUInt(), // increase page
            term = term,
            items = nextPageItems, // update items
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(GithubSearchSingleEvent.SearchFailure(error))

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page _ ignores other Retry actions WHEN dispatching Retry action and SearchRepoItemsUseCase returns an empty items`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) {
        delay(1_000)
        emptyList<RepoItem>().right()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.Retry)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = null,
            hasReachedMax = true // set hasReachedMax to true
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(
        GithubSearchSingleEvent.SearchFailure(error),
        GithubSearchSingleEvent.ReachedMaxItems
      )

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  @Test
  fun `retries next page _ ignores other Retry actions WHEN dispatching Retry action and SearchRepoItemsUseCase returns a Left result`() =
    runTest {
      val eventsTurbine = vm.eventFlow.testIn(this)

      val term = "#hoc081098"
      val error = AppError.ApiException.NetworkException(null)
      val page1State = reachToErrorState(term = term, error = error)

      val nextError = AppError.ApiException.UnknownException(null)
      mockSearchRepoItemsUseCase(term = term, page = PAGE_1) {
        delay(1_000)
        nextError.left()
      }

      vm.stateFlow.test {
        assertEquals(page1State, awaitItem())

        launch(start = UNDISPATCHED) {
          repeat(10) {
            vm.dispatch(GithubSearchAction.Retry)
            delay(100)
          }
        }

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = true, // toggle loading
            error = null, // clear error
            hasReachedMax = false
          ),
          awaitItem()
        )

        assertEquals(
          GithubSearchState(
            page = FIRST_PAGE,
            term = term,
            items = persistentListOf(),
            isLoading = false, // toggle loading
            error = nextError, // set error
            hasReachedMax = false
          ),
          awaitItem()
        )

        delay(EXTRA_DELAY)
        expectNoEvents()
      }
      eventsTurbine.assertEvents(
        GithubSearchSingleEvent.SearchFailure(error),
        GithubSearchSingleEvent.SearchFailure(nextError)
      )

      verify(repoItemRepository)
        .coroutine { searchRepoItems(term, PAGE_1) }
        .wasInvoked(exactly = once)
    }

  private suspend fun reachToPage1(term: String, items: List<RepoItem>): GithubSearchState {
    val page = PAGE_1
    mockSearchRepoItemsUseCase(term = term, page = page) { items.right() }

    vm.dispatch(GithubSearchAction.Search(term))

    vm.stateFlow.test {
      while (awaitItem().items.isEmpty()) {
        // wait until the first page is loaded
      }
    }

    verify(repoItemRepository)
      .coroutine { searchRepoItems(term, page) }
      .wasInvoked(exactly = once)

    return vm.stateFlow.value
  }

  private suspend fun reachToErrorState(term: String, error: AppError): GithubSearchState {
    mockSearchRepoItemsUseCase(term = term, page = PAGE_1) { error.left() }

    vm.dispatch(GithubSearchAction.Search(term))

    vm.stateFlow.test {
      while (awaitItem().error == null) {
        // wait until the error state is reached
      }
    }

    verify(repoItemRepository)
      .coroutine { searchRepoItems(term, PAGE_1) }
      .wasInvoked(exactly = once)

    return vm.stateFlow.value
  }

  private suspend inline fun mockSearchRepoItemsUseCase(
    term: String,
    page: Int,
    crossinline result: suspend () -> Either<AppError, List<RepoItem>>
  ) = given(repoItemRepository)
    .coroutine { searchRepoItemsUseCase(term, page) }
    .then { result() }

  private companion object {
    private val EXTRA_DELAY = GithubSearchSideEffects.DEBOUNCE_TIME * 1.5
    private val SEMI_DELAY = GithubSearchSideEffects.DEBOUNCE_TIME * 0.5
    private val PAGE_1 = FIRST_PAGE.toInt() + 1
    private val PAGE_2 = PAGE_1 + 1

    private suspend fun ReceiveTurbine<GithubSearchSingleEvent>.assertEvents(vararg expectedEvents: GithubSearchSingleEvent) {
      expectedEvents.forEach { assertEquals(it, awaitItem()) }
      expectNoEvents()
      cancel()
    }
  }
}
