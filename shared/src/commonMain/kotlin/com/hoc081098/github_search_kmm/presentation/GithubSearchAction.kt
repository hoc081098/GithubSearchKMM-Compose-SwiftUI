package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.presentation.GithubSearchState.Companion.FIRST_PAGE
import com.hoc081098.github_search_kmm.utils.EitherLCE
import dev.icerock.moko.kswift.KSwiftInclude
import kotlinx.collections.immutable.persistentListOf

@KSwiftInclude
sealed interface GithubSearchAction {
  fun reduce(state: GithubSearchState): GithubSearchState

  data class Search(val term: String) : GithubSearchAction {
    override fun reduce(state: GithubSearchState) = state
  }

  data object LoadNextPage : GithubSearchAction {
    override fun reduce(state: GithubSearchState) = state
  }

  data object Retry : GithubSearchAction {
    override fun reduce(state: GithubSearchState) = state
  }
}

internal sealed interface SideEffectAction : GithubSearchAction {
  data class TextChanged(val term: String) : SideEffectAction {
    override fun reduce(state: GithubSearchState) = state
  }

  data class SearchLCE(
    val lce: EitherLCE<AppError, List<RepoItem>>,
    val term: String,
    val nextPage: UInt,
  ) : SideEffectAction {
    override fun reduce(state: GithubSearchState) = when (lce) {
      is EitherLCE.ContentOrError -> {
        lce.either.fold(
          ifLeft = { error ->
            state.copy(
              term = term,
              isLoading = false,
              error = error,
            )
          },
          ifRight = { items ->
            val ids = state.items.mapTo(hashSetOf()) { it.id }
            val filtered = items.filterNot { it.id in ids }

            state.copy(
              term = term,
              isLoading = false,
              error = null,
              page = state.page + if (filtered.isEmpty()) {
                0u
              } else {
                1u
              },
              items = state.items.addAll(filtered),
              hasReachedMax = filtered.isEmpty(),
            )
          }
        )
      }
      EitherLCE.Loading -> {
        if (nextPage == FIRST_PAGE + 1u) {
          state.copy(
            term = term,
            isLoading = true,
            error = null,
            page = FIRST_PAGE,
            items = persistentListOf(),
            hasReachedMax = false,
          )
        } else {
          state.copy(
            term = term,
            isLoading = true,
            error = null
          )
        }
      }
    }
  }
}

internal fun GithubSearchAction.toGithubSearchSingleEventOrNull(): GithubSearchSingleEvent? =
  when (this) {
    GithubSearchAction.LoadNextPage -> null
    GithubSearchAction.Retry -> null
    is GithubSearchAction.Search -> null
    is SideEffectAction.TextChanged -> null
    is SideEffectAction.SearchLCE -> {
      when (lce) {
        is EitherLCE.ContentOrError -> {
          lce.either.fold(
            ifRight = { items ->
              items
                .takeIf { it.isEmpty() }
                ?.let { GithubSearchSingleEvent.ReachedMaxItems }
            },
            ifLeft = GithubSearchSingleEvent::SearchFailure
          )
        }
        EitherLCE.Loading -> null
      }
    }
  }
