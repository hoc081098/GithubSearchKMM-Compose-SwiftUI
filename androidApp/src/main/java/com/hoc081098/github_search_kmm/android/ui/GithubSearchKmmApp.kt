package com.hoc081098.github_search_kmm.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hoc081098.github_search_kmm.android.core_ui.AppBackground
import com.hoc081098.github_search_kmm.android.core_ui.AppTheme

@Composable
fun GithubSearchKmmApp() {
  AppTheme {
    AppBackground {
      GithubRepoItemsSearchScreen()
    }
  }
}

@Preview
@Composable
fun GithubSearchKmmAppPreview() {
  GithubSearchKmmApp()
}
