package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DaggerGithubSearchViewModel @Inject constructor(searchRepoItemsUseCase: SearchRepoItemsUseCase) :
  GithubSearchViewModel(searchRepoItemsUseCase)
