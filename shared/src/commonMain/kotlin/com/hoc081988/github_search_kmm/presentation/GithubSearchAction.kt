package com.hoc081988.github_search_kmm.presentation

import com.hoc081988.github_search_kmm.domain.model.AppError
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import com.hoc081988.github_search_kmm.utils.EitherLCE
import kotlinx.collections.immutable.persistentListOf

sealed interface GithubSearchAction {
  fun reduce(state: GithubSearchState): GithubSearchState

  data class Search(val term: String) : GithubSearchAction {
    override fun reduce(state: GithubSearchState) = state
  }

  object LoadNextPage : GithubSearchAction {
    override fun reduce(state: GithubSearchState) = state
  }

  object Retry : GithubSearchAction {
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
            )
          }
        )
      }
      EitherLCE.Loading -> {
        if (state.isFirstPage) {
          state.copy(
            term = term,
            isLoading = true,
            error = null,
            page = GithubSearchState.FIRST_PAGE,
            items = persistentListOf(),
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
            ifRight = { null },
            ifLeft = GithubSearchSingleEvent::SearchFailure
          )
        }
        EitherLCE.Loading -> null
      }
    }
  }
