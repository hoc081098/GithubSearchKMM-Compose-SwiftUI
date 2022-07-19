package com.hoc081988.github_search_kmm.android.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumedWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hoc081988.github_search_kmm.android.R
import com.hoc081988.github_search_kmm.android.core_ui.AppBackground
import com.hoc081988.github_search_kmm.android.core_ui.AppTheme

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalLayoutApi::class
)
@Composable
fun GithubSearchKmmApp() {
  AppTheme {
    AppBackground {
      Scaffold(
        topBar = {
          CenterAlignedTopAppBar(
            title = {
              Text(text = stringResource(id = R.string.app_name))
            }
          )
        }
      ) { innerPadding ->
        BoxWithConstraints(
          modifier = Modifier
            .padding(innerPadding)
            .consumedWindowInsets(innerPadding)
        ) {
          GithubRepoItemsSearchScreen()
        }
      }
    }
  }
}

@Preview
@Composable
fun GithubSearchKmmAppPreview() {
  GithubSearchKmmApp()
}
