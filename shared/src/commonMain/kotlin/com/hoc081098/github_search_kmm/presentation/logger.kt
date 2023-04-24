package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.FlowReduxLogger
import com.hoc081098.github_search_kmm.isDebug
import com.hoc081098.github_search_kmm.utils.EitherLCE
import io.github.aakira.napier.Napier

private val GithubSearchAction.debugString: String
  get() = when (this) {
    GithubSearchAction.LoadNextPage,
    GithubSearchAction.Retry,
    is GithubSearchAction.Search,
    is SideEffectAction.TextChanged -> toString()

    is SideEffectAction.SearchLCE -> arrayOf(
      "term" to term,
      "nextPage" to nextPage,
      "lce" to when (lce) {
        EitherLCE.Loading -> "Loading"
        is EitherLCE.ContentOrError -> lce.either.fold(
          ifLeft = { "Left($it)" },
          ifRight = { "Right(${it.size})" },
        )
      },
    ).joinToString(
      prefix = "SearchLCE { ",
      postfix = " }",
      separator = ", ",
    ) { (k, v) -> "$k: $v" }
  }

private inline val GithubSearchState.debugString: String
  get() = arrayOf(
    "page" to page,
    "term" to term,
    "items.size" to items.size,
    "isLoading" to isLoading,
    "error" to error,
    "hasReachedMax" to hasReachedMax,
  ).joinToString(
    prefix = "GithubSearchState { ",
    postfix = " }",
    separator = ", ",
  ) { (k, v) -> "$k: $v" }

internal fun githubSearchFlowReduxLogger(): FlowReduxLogger<GithubSearchAction, GithubSearchState> =
  if (isDebug()) {
    FlowReduxLogger { action, prevState, nextState ->
      Napier.d(
        """onReduced {
        |   Action    : ${action.debugString}
        |   Prev state: ${prevState.debugString}
        |   Next state: ${nextState.debugString}
        |}
        """.trimMargin(),
        tag = "GithubSearchViewModel"
      )
    }
  } else {
    FlowReduxLogger.empty()
  }
