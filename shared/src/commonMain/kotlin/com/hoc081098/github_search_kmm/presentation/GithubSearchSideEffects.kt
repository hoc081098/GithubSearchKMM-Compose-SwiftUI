package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import com.hoc081098.flowredux.SideEffect
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.presentation.GithubSearchState.Companion.FIRST_PAGE
import com.hoc081098.github_search_kmm.utils.eitherLceFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

@Suppress("NOTHING_TO_INLINE")
internal class GithubSearchSideEffects(
  private val searchRepoItemsUseCase: SearchRepoItemsUseCase,
) {
  inline val sideEffects
    get() = listOf(
      // [Search]s -> [TextChanged]s
      searchActionToTextChangedAction(),
      // [TextChanged]s -> [SearchLCE]s
      search(),
      // [LoadNextPage]s -> [SearchLCE]s
      nextPage(),
      // [Retry] -> [SearchLCE]s
      retry(),
    )

  /**
   * [GithubSearchAction.Search]s to [SideEffectAction.TextChanged]s
   */
  private inline fun searchActionToTextChangedAction() =
    SideEffect<GithubSearchAction, GithubSearchState> { actionFlow, _, _ ->
      actionFlow
        .filterIsInstance<GithubSearchAction.Search>()
        .map { it.term.trim() }
        .debounce(DEBOUNCE_TIME)
        .filter { it.isNotBlank() }
        .distinctUntilChanged()
        .map { SideEffectAction.TextChanged(term = it) }
    }

  /**
   * Load first page after text changed.
   *
   * [SideEffectAction.TextChanged]s to [SideEffectAction.SearchLCE]s
   */
  private inline fun search() =
    SideEffect<GithubSearchAction, GithubSearchState> { actionFlow, _, _ ->
      actionFlow
        .filterIsInstance<SideEffectAction.TextChanged>()
        .flatMapLatest { action ->
          executeSearchRepoItemsUseCase(
            term = action.term,
            nextPage = PAGE_1
          )
        }
    }

  /**
   * Load next page.
   *
   * [GithubSearchAction.LoadNextPage]s to [SideEffectAction.SearchLCE]s
   */
  private inline fun nextPage() =
    SideEffect<GithubSearchAction, GithubSearchState> { actionFlow, stateFlow, coroutineScope ->
      val actionSharedFlow = actionFlow.shareIn(coroutineScope, WhileSubscribed())

      actionSharedFlow
        .filterIsInstance<GithubSearchAction.LoadNextPage>()
        .flatMapFirst {
          flowFromSuspend { stateFlow.value }
            .filter { it.canLoadNextPage }
            .flatMapConcat {
              executeSearchRepoItemsUseCase(
                term = it.term,
                nextPage = it.page + 1u
              )
            }
            .takeUntil(
              actionSharedFlow
                .filterIsInstance<SideEffectAction.TextChanged>()
            )
        }
    }

  /**
   * Retry first/next page.
   *
   * [GithubSearchAction.Retry]s to [SideEffectAction.SearchLCE]s
   */
  private inline fun retry() =
    SideEffect<GithubSearchAction, GithubSearchState> { actionFlow, stateFlow, coroutineScope ->
      val actionSharedFlow = actionFlow.shareIn(coroutineScope, WhileSubscribed())

      actionSharedFlow
        .filterIsInstance<GithubSearchAction.Retry>()
        .flatMapFirst {
          flowFromSuspend { stateFlow.value }
            .filter { it.canRetry }
            .flatMapConcat {
              executeSearchRepoItemsUseCase(
                term = it.term,
                nextPage = it.page + 1u,
              )
            }
            .takeUntil(
              actionSharedFlow
                .filterIsInstance<SideEffectAction.TextChanged>()
            )
        }
    }

  private fun executeSearchRepoItemsUseCase(
    term: String,
    nextPage: UInt
  ): Flow<SideEffectAction.SearchLCE> =
    eitherLceFlow {
      searchRepoItemsUseCase(
        term = term,
        page = nextPage.toInt()
      )
    }.map {
      SideEffectAction.SearchLCE(
        lce = it,
        term = term,
        nextPage = nextPage,
      )
    }

  companion object {
    private val PAGE_1 = FIRST_PAGE + 1u
    val DEBOUNCE_TIME: Duration = 600.milliseconds
  }
}
