package com.hoc081988.github_search_kmm.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import kotlinx.collections.immutable.PersistentList

@Composable
internal fun GithubRepoItemsList(
  items: PersistentList<RepoItem>
) {
  LazyColumn(
    modifier = Modifier
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    items(
      items = items,
      key = { it.id }
    ) { item ->
      GithubRepoItemRow(
        modifier = Modifier
          .fillParentMaxWidth(),
        item = item
      )
    }

    item {
      Text(text = "Last item")
    }
  }
}
