package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.presentation.common.Immutable

@Immutable
sealed interface GithubSearchSingleEvent {
  data class SearchFailure(val appError: AppError) : GithubSearchSingleEvent
  data object ReachedMaxItems : GithubSearchSingleEvent
}
