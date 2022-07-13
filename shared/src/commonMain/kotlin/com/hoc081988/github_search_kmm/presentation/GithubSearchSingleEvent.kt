package com.hoc081988.github_search_kmm.presentation

import com.hoc081988.github_search_kmm.domain.model.AppError

sealed interface GithubSearchSingleEvent {
  data class SearchFailure(val appError: AppError) : GithubSearchSingleEvent
}
