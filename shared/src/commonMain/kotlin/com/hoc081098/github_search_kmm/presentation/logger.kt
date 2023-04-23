package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.FlowReduxLogger
import com.hoc081098.github_search_kmm.isDebug
import io.github.aakira.napier.Napier

private inline val GithubSearchState.debugString: String
  get() = """GithubSearchState {
  |   page: $page,
  |   term: $term,
  |   items.size: ${items.size},
  |   isLoading: $isLoading,
  |   error: $error,
  |   hasReachedMax: $hasReachedMax
  |}
  """.trimIndent()

internal fun githubSearchFlowReduxLogger(): FlowReduxLogger<GithubSearchAction, GithubSearchState> =
  if (isDebug()) {
    FlowReduxLogger.empty()
  } else {
    object : FlowReduxLogger<GithubSearchAction, GithubSearchState> {
      override fun onReducer(action: GithubSearchAction, oldState: GithubSearchState, newState: GithubSearchState) {
        Napier.d(
          """onReducer {
            |   Action: $action
            |   Old state: ${oldState.debugString}
            |   New state: ${newState.debugString}
            |}
          """.trimIndent(),
          tag = "FlowReduxLogger"
        )
      }

      override fun onNewState(state: GithubSearchState) {
        Napier.d(
          "onNewState: ${state.debugString}",
          tag = "FlowReduxLogger"
        )
      }
    }
  }
