package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowext.flatMapFirst
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.takeUntil
import com.hoc081098.flowredux.SideEffect
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.utils.eitherLceFlow
import kotlin.jvm.JvmInline
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

@JvmInline
@Suppress("NOTHING_TO_INLINE")
internal value class GithubSearchSideEffects(
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
      // [Retry] -> [SearchLCE]s
      retry(),
    )

  /**
   * [GithubSearchAction.Search]s to [SideEffectAction.TextChanged]s
   */
  private inline fun textChanged() =
    SideEffect<GithubSearchState, GithubSearchAction> { actionFlow, _, _ ->
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
    SideEffect<GithubSearchState, GithubSearchAction> { actionFlow, _, _ ->
      actionFlow
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
    SideEffect<GithubSearchState, GithubSearchAction> { actionFlow, stateFlow, coroutineScope ->
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
    SideEffect<GithubSearchState, GithubSearchAction> { actionFlow, stateFlow, coroutineScope ->
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
    val DEBOUNCE_TIME: Duration = 600.milliseconds
  }
}
