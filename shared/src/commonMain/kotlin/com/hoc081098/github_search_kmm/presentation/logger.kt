package com.hoc081098.github_search_kmm.presentation

import com.hoc081098.flowredux.FlowReduxLogger
import com.hoc081098.github_search_kmm.isDebug
import com.hoc081098.github_search_kmm.utils.EitherLCE
import com.hoc081098.github_search_kmm.utils.diff
import io.github.aakira.napier.Napier
import kotlin.LazyThreadSafetyMode.NONE

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

private inline val GithubSearchState.debugMap: Map<String, Any?>
  get() = mapOf(
    "page" to page,
    "term" to term,
    "items.size" to items.size,
    "isLoading" to isLoading,
    "error" to error,
    "hasReachedMax" to hasReachedMax,
  )

private inline val GithubSearchState.debugString: String
  get() = debugMap.entries.joinToString(
    prefix = "GithubSearchState { ",
    postfix = " }",
    separator = ", ",
  ) { (k, v) -> "$k: $v" }

internal fun githubSearchFlowReduxLogger(): FlowReduxLogger<GithubSearchAction, GithubSearchState> =
  if (isDebug()) {
    FlowReduxLogger { action, prevState, nextState ->
      val diffString by lazy(NONE) {
        prevState.debugMap
          .diff(nextState.debugMap)
          .joinToString(separator = ", ", prefix = "{ ", postfix = " }") { (k, v1, v2) -> "$k: ($v1 -> $v2)" }
      }

      Napier.d(
        """onReduced {
        |   Action    : ${action.debugString}
        |   Prev state: ${prevState.debugString}
        |   Next state: ${nextState.debugString}
        |   Diff      : ${if (prevState == nextState) "{ }" else diffString}
        |}
        """.trimMargin(),
        tag = "GithubSearchViewModel"
      )
    }
  } else {
    FlowReduxLogger.empty()
  }
