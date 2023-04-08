package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GithubSearchState(
  val page: UInt,
  val term: String,
  val items: PersistentList<RepoItem>,
  val isLoading: Boolean,
  val error: AppError?,
  val hasReachedMax: Boolean
) {
  inline val isFirstPage: Boolean get() = page == FIRST_PAGE

  inline val canLoadNextPage: Boolean
    get() = !isLoading &&
      error === null &&
      items.isNotEmpty() &&
      term.isNotEmpty() &&
      !isFirstPage &&
      !hasReachedMax

  inline val canRetry: Boolean get() = !isLoading && error !== null

  companion object {
    const val FIRST_PAGE = 0u

    fun initial(): GithubSearchState = GithubSearchState(
      page = FIRST_PAGE,
      term = "",
      items = persistentListOf(),
      isLoading = false,
      error = null,
      hasReachedMax = false,
    )
  }
}
