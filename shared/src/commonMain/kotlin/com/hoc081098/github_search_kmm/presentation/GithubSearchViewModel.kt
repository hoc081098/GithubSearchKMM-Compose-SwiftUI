package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.Reducer
import com.hoc081098.flowredux.allActionsToOutputChannelSideEffect
import com.hoc081098.flowredux.createFlowReduxStore
import com.hoc081098.flowredux.withLogger
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.utils.flip
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap
import kotlinx.coroutines.flow.receiveAsFlow

open class GithubSearchViewModel(
  searchRepoItemsUseCase: SearchRepoItemsUseCase,
) : ViewModel() {
  private val sendSingleEventSideEffect =
    allActionsToOutputChannelSideEffect<GithubSearchAction, GithubSearchState, GithubSearchSingleEvent> {
      it.toGithubSearchSingleEventOrNull()
    }

  private val storeLogger = githubSearchFlowReduxLogger()

  private val store = viewModelScope.createFlowReduxStore(
    initialState = GithubSearchState.initial(),
    sideEffects = GithubSearchSideEffects(searchRepoItemsUseCase).sideEffects +
      sendSingleEventSideEffect.first,
    reducer = Reducer(flip(GithubSearchAction::reduce))
      .withLogger(storeLogger)
  )

  fun dispatch(action: GithubSearchAction) = store.dispatch(action)

  val stateFlow: NonNullStateFlowWrapper<GithubSearchState> = store.stateFlow.wrap()

  val eventFlow: NonNullFlowWrapper<GithubSearchSingleEvent> = sendSingleEventSideEffect.second.receiveAsFlow().wrap()
}
