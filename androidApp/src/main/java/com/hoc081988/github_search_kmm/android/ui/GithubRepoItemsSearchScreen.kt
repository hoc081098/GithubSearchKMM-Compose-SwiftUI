package com.hoc081988.github_search_kmm.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoc081988.github_search_kmm.android.core_ui.AppBackground
import com.hoc081988.github_search_kmm.android.core_ui.AppTheme
import com.hoc081988.github_search_kmm.android.core_ui.LoadingIndicator
import com.hoc081988.github_search_kmm.android.core_ui.RetryButton
import com.hoc081988.github_search_kmm.android.getReadableMessage
import com.hoc081988.github_search_kmm.domain.model.Owner
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import com.hoc081988.github_search_kmm.presentation.DaggerGithubSearchViewModel
import com.hoc081988.github_search_kmm.presentation.GithubSearchAction
import com.hoc081988.github_search_kmm.presentation.GithubSearchState
import com.hoc081988.github_search_kmm.presentation.GithubSearchState.Companion.FIRST_PAGE
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GithubRepoItemsSearchScreen(
  vm: DaggerGithubSearchViewModel = hiltViewModel()
) {
  val state by vm.stateFlow.collectAsStateWithLifecycle()

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    GithubSearchTermBox(
      modifier = Modifier.fillMaxWidth(),
      onTermChanged = {
        vm.dispatch(GithubSearchAction.Search(term = it))
      },
      initialTerm = state.term,
    )

    GithubRepoItemsSearchContent(
      modifier = Modifier.weight(1f),
      state = state,
      dispatch = vm::dispatch
    )
  }
}

@Composable
internal fun GithubRepoItemsSearchContent(
  modifier: Modifier = Modifier,
  state: GithubSearchState,
  dispatch: (GithubSearchAction) -> Unit
) {
  if (state.isFirstPage && state.isLoading) {
    return LoadingIndicator()
  }

  val error = state.error
  if (state.isFirstPage && error != null) {
    return RetryButton(
      errorMessage = error.getReadableMessage(),
      onRetry = { dispatch(GithubSearchAction.Retry) }
    )
  }

  if (state.items.isEmpty()) {
    return Column(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = if (state.term.isNotBlank()) {
          "Empty results"
        } else {
          "Search github repositories..."
        },
        style = MaterialTheme.typography.titleLarge
      )
    }
  }

  GithubRepoItemsList(
    items = state.items,
    isLoading = state.isLoading,
    error = state.error,
    hasReachedMax = state.hasReachedMax,
    onRetry = { dispatch(GithubSearchAction.Retry) },
    onLoadNextPage = { dispatch(GithubSearchAction.LoadNextPage) }
  )
}

@Preview(name = "phone", device = "spec:shape=Normal,width=360,height=640,unit=dp,dpi=480")
// @Preview(name = "landscape", device = "spec:shape=Normal,width=640,height=360,unit=dp,dpi=480")
// @Preview(name = "foldable", device = "spec:shape=Normal,width=673,height=841,unit=dp,dpi=480")
// @Preview(name = "tablet", device = "spec:shape=Normal,width=1280,height=800,unit=dp,dpi=480")
@Composable
fun SearchScreenContentPreview() {
  AppTheme {
    AppBackground {
      GithubRepoItemsSearchContent(
        state = GithubSearchState(
          page = FIRST_PAGE,
          term = "term",
          items = persistentListOf<RepoItem>()
            .addAll(
              (0 until 50).map {
                RepoItem(
                  id = it,
                  fullName = "ReactiveX/rxdart $it",
                  language = null,
                  starCount = 0,
                  name = "rxdart $it",
                  repoDescription = null,
                  languageColor = null,
                  htmlUrl = "",
                  owner = Owner(
                    id = 0,
                    username = "",
                    avatar = ""
                  ),
                  updatedAt = Clock.System.now()
                )
              }
            ),
          isLoading = false,
          error = null,
          hasReachedMax = false
        ),
        dispatch = {}
      )
    }
  }
}
