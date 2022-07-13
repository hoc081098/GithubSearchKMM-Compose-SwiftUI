package com.hoc081988.github_search_kmm.presentation

import com.hoc081988.github_search_kmm.ViewModel
import com.hoc081988.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

open class GithubSearchViewModel(
  private val searchRepoItemsUseCase: SearchRepoItemsUseCase,
) : ViewModel() {
  init {
    viewModelScope.launch {
      val either = searchRepoItemsUseCase(term = "kmm", page = 1)
      Napier.d(message = "searchRepoItemsUseCase: $either", tag = "GithubSearchViewModel")
    }
  }
}
