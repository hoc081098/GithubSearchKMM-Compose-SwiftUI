package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.createFlowReduxStore
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
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
    .onEach { Napier.d("Action: $it", tag = "GithubSearchViewModel") }
    .mapNotNull { it.toGithubSearchSingleEventOrNull() }
    .buffer(Channel.UNLIMITED)
    .produceIn(viewModelScope)

  fun dispatch(action: GithubSearchAction) = store.dispatch(action)

  val stateFlow: NonNullStateFlowWrapper<GithubSearchState> = store.stateFlow.wrap()

  val eventFlow: NonNullFlowWrapper<GithubSearchSingleEvent> = eventChannel.receiveAsFlow().wrap()
}
