package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.Reducer
import com.hoc081098.flowredux.createFlowReduxStore
import com.hoc081098.flowredux.withLogger
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import com.hoc081098.github_search_kmm.utils.flip
import com.hoc081098.kmp.viewmodel.MainThread
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.hoc081098.kmp.viewmodel.wrapper.NonNullFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.NonNullStateFlowWrapper
import com.hoc081098.kmp.viewmodel.wrapper.wrap

open class GithubSearchViewModel(
  searchRepoItemsUseCase: SearchRepoItemsUseCase,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val effectsContainer = GithubSearchSideEffectsContainer(searchRepoItemsUseCase)

  private val store = viewModelScope.createFlowReduxStore(
    initialState = GithubSearchState.initial(),
    sideEffects = effectsContainer.sideEffects,
    reducer = Reducer(flip(GithubSearchAction::reduce))
      .withLogger(githubSearchFlowReduxLogger())
  )

  val termStateFlow: NonNullStateFlowWrapper<String> = savedStateHandle.getStateFlow(TERM_KEY, "").wrap()

  val stateFlow: NonNullStateFlowWrapper<GithubSearchState> = store.stateFlow.wrap()

  val eventFlow: NonNullFlowWrapper<GithubSearchSingleEvent> = effectsContainer.eventFlow.wrap()

  init {
    store.dispatch(InitialSearchAction(termStateFlow.value))
  }

  @MainThread
  fun dispatch(action: GithubSearchAction): Boolean {
    if (action is GithubSearchAction.Search) {
      savedStateHandle[TERM_KEY] = action.term
    }
    return store.dispatch(action)
  }

  companion object {
    private const val TERM_KEY = "com.hoc081098.github_search_kmm.presentation.GithubSearchViewModel.term"

    /**
     * Used by non-Android platforms.
     */
    fun create(searchRepoItemsUseCase: SearchRepoItemsUseCase): GithubSearchViewModel =
      GithubSearchViewModel(searchRepoItemsUseCase, SavedStateHandle())
  }
}
