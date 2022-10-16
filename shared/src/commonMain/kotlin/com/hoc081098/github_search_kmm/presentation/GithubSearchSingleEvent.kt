package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.github_search_kmm.domain.model.AppError
import dev.icerock.moko.kswift.KSwiftInclude

@KSwiftInclude
sealed interface GithubSearchSingleEvent {
  data class SearchFailure(val appError: AppError) : GithubSearchSingleEvent
  data object ReachedMaxItems : GithubSearchSingleEvent
}
