package com.hoc081098.github_search_kmm.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoc081098.flowext.ThrottleConfiguration
import com.hoc081098.flowext.throttleTime
import com.hoc081098.github_search_kmm.android.StableWrapper
import com.hoc081098.github_search_kmm.android.core_ui.LoadingIndicator
import com.hoc081098.github_search_kmm.android.core_ui.RetryButton
import com.hoc081098.github_search_kmm.android.getReadableMessage
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import io.github.aakira.napier.Napier
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.filter

@Composable
internal fun GithubRepoItemsList(
  items: PersistentList<RepoItem>,
  isLoading: Boolean,
  error: AppError?,
  hasReachedMax: Boolean,
  onRetry: () -> Unit,
  onLoadNextPage: () -> Unit
) {
  val lazyListState = rememberLazyListState()
  val currentOnLoadNextPage by rememberUpdatedState(onLoadNextPage)
  val currentHasReachedMax by rememberUpdatedState(hasReachedMax)

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.layoutInfo }
      .throttleTime(
        duration = 300.milliseconds,
        ThrottleConfiguration.LEADING_AND_TRAILING
      )
      .filter {
        val index = it.visibleItemsInfo.lastOrNull()?.index
        val totalItemsCount = it.totalItemsCount

        Napier.d(
          message = "lazyListState: currentHasReachedMax=$currentHasReachedMax - lastVisible=$index - totalItemsCount=$totalItemsCount",
          tag = "GithubRepoItemsList"
        )
        !currentHasReachedMax && index != null && index + 2 >= totalItemsCount
      }
      .collect {
        Napier.d(
          message = "load next page",
          tag = "GithubRepoItemsList"
        )
        currentOnLoadNextPage()
      }
  }

  val decimalFormat = remember { StableWrapper(DecimalFormat("#,###")) }

  LazyColumn(
    modifier = Modifier
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    state = lazyListState
  ) {
    items(
      items = items,
      key = { it.id }
    ) { item ->
      GithubRepoItemRow(
        modifier = Modifier
          .fillParentMaxWidth(),
        item = item,
        decimalFormat = decimalFormat,
      )
    }

    if (isLoading) {
      item {
        LoadingIndicator(
          modifier = Modifier.height(128.dp)
        )
      }
    } else if (error !== null) {
      item {
        RetryButton(
          modifier = Modifier.height(128.dp),
          errorMessage = error.getReadableMessage(),
          onRetry = onRetry
        )
      }
    } else if (!hasReachedMax) {
      item {
        Spacer(modifier = Modifier.height(128.dp))
      }
    }
  }
}
