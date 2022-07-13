package com.hoc081988.github_search_kmm.presentation

import com.hoc081098.flowredux.createFlowReduxStore
import com.hoc081988.github_search_kmm.ViewModel
import com.hoc081988.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow

open class GithubSearchViewModel(
  searchRepoItemsUseCase: SearchRepoItemsUseCase,
) : ViewModel() {
  private val store = viewModelScope.createFlowReduxStore(
    initialState = GithubSearchState.initial(),
    sideEffects = GithubSearchSideEffects(
      searchRepoItemsUseCase = searchRepoItemsUseCase,
    ).sideEffects,
    reducer = { state, action -> action.reduce(state) }
  )
  private val eventChannel = store.actionSharedFlow
    .mapNotNull { it.toGithubSearchSingleEventOrNull() }
    .buffer(Channel.UNLIMITED)
    .produceIn(viewModelScope)

  fun dispatch(action: GithubSearchAction) = store.dispatch(action)
  val stateFlow: StateFlow<GithubSearchState> by store::stateFlow
  val eventFlow: Flow<GithubSearchSingleEvent> get() = eventChannel.receiveAsFlow()
}
