package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.SideEffect
import com.hoc081098.flowredux.createFlowReduxStore
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

open class GithubSearchViewModel(
  searchRepoItemsUseCase: SearchRepoItemsUseCase,
) : ViewModel() {
  private val singleEventChannel = Channel<GithubSearchSingleEvent>(Channel.UNLIMITED)
    .apply { addCloseable(::close) }

  private val store = viewModelScope.createFlowReduxStore(
    initialState = GithubSearchState.initial(),
    sideEffects = GithubSearchSideEffects(searchRepoItemsUseCase).sideEffects +
      SideEffect { actionFlow, _, scope ->
        actionFlow
          .mapNotNull { it.toGithubSearchSingleEventOrNull() }
          .onEach { Napier.d("GithubSearchSingleEvent: $it", tag = TAG) }
          .onEach(singleEventChannel::trySend)
          .launchIn(scope)

        emptyFlow()
      },
    reducer = { state, action -> action.reduce(state) }
  )

  fun dispatch(action: GithubSearchAction) = store.dispatch(action)

  val eventFlow: NonNullFlowWrapper<GithubSearchSingleEvent> = singleEventChannel
    .receiveAsFlow()
    .wrap()

  val stateFlow: NonNullStateFlowWrapper<GithubSearchState> = store.stateFlow.wrap()

  private companion object {
    private const val TAG = "GithubSearchViewModel"
  }
}
