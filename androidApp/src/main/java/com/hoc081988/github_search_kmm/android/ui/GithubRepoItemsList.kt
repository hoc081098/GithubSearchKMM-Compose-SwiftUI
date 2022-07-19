package com.hoc081988.github_search_kmm.android.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import kotlinx.collections.immutable.PersistentList

@Composable
internal fun GithubRepoItemsList(
  items: PersistentList<RepoItem>
) {
  LazyColumn {
    // Add a single item
    items(
      items = items,
      key = { it.id }
    ) { item ->
      Text(text = item.fullName)
    }

    // Add another single item
    item {
      Text(text = "Last item")
    }
  }
}
