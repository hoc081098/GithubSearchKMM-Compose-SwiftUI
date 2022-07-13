package com.hoc081988.github_search_kmm.presentation

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import com.hoc081098.flowredux.SideEffect
import com.hoc081988.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081988.github_search_kmm.eitherLCEFlow
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
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
      // [TextChanged]s -> [SearchLCE]s
      textChanged(),
      search(),
      // [LoadNextPage]s -> [SearchLCE]s
      nextPage(),
      // Retry -> [SearchLCE]s
      retry(),
    )

  /**
   * [GithubSearchAction.Search]s to [SideEffectAction.TextChanged]s
   */
  private inline fun textChanged() =
    SideEffect<GithubSearchState, GithubSearchAction> { actions, getState, _ ->
      actions
        .filterIsInstance<GithubSearchAction.Search>()
        .map { it.term }
        .debounce(600.milliseconds)
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .map { SideEffectAction.TextChanged(term = it) }
    }

  /**
   * Load first page after text changed.
   *
   * [SideEffectAction.TextChanged]s to [SideEffectAction.SearchLCE]s
   */
  private inline fun search() =
    SideEffect<GithubSearchState, GithubSearchAction> { actions, getState, _ ->
      actions
        .filterIsInstance<SideEffectAction.TextChanged>()
        .flatMapLatest { action ->
          executeSearchRepoItemsUseCase(
            term = action.term,
            nextPage = 1u
          )
        }
    }

  /**
   * Load next page.
   *
   * [GithubSearchAction.LoadNextPage]s to [SideEffectAction.SearchLCE]s
   */
  private inline fun nextPage() =
    SideEffect<GithubSearchState, GithubSearchAction> { actions, getState, scope ->
      val textChangedActionSharedFlow = actions
        .filterIsInstance<SideEffectAction.TextChanged>()
        .shareIn(
          scope = scope,
          started = SharingStarted.Eagerly,
        )

      actions
        .filterIsInstance<GithubSearchAction.LoadNextPage>()
        .flatMapFirst {
          flowFromSuspend { getState() }
            .filter { it.canLoadNextPage }
            .flatMapConcat {
              executeSearchRepoItemsUseCase(
                term = it.term,
                nextPage = it.page + 1u
              )
            }
            .takeUntil(textChangedActionSharedFlow)
        }
    }

  /**
   * Retry first/next page.
   *
   * [GithubSearchAction.Retry]s to [SideEffectAction.SearchLCE]s
   */
  private fun retry() =
    SideEffect<GithubSearchState, GithubSearchAction> { actions, getState, coroutineScope ->
      val textChangedActionSharedFlow = actions
        .filterIsInstance<SideEffectAction.TextChanged>()
        .shareIn(
          scope = coroutineScope,
          started = SharingStarted.Eagerly,
        )

      actions
        .filterIsInstance<GithubSearchAction.Retry>()
        .flatMapFirst {
          flowFromSuspend { getState() }
            .filter { it.canRetry }
            .flatMapConcat {
              executeSearchRepoItemsUseCase(
                term = it.term,
                nextPage = it.page + 1u,
              )
            }
            .takeUntil(textChangedActionSharedFlow)
        }
    }

  private fun executeSearchRepoItemsUseCase(
    term: String,
    nextPage: UInt
  ): Flow<SideEffectAction.SearchLCE> =
    eitherLCEFlow {
      searchRepoItemsUseCase(
        term = term,
        page = nextPage.toInt()
      )
    }.map {
      SideEffectAction.SearchLCE(
        lce = it,
        term = term
      )
    }
}
