package com.hoc081098.github_search_kmm.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.hoc081098.github_search_kmm.android.compose_utils.StableWrapper
import com.hoc081098.github_search_kmm.android.core_ui.LoadingIndicator
import com.hoc081098.github_search_kmm.android.core_ui.RetryButton
import com.hoc081098.github_search_kmm.android.core_ui.getReadableMessage
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import io.github.aakira.napier.Napier
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.filter

private const val GithubRepoItemsListLogTag = "GithubRepoItemsList"

@Composable
internal fun GithubRepoItemsList(
  items: PersistentList<RepoItem>,
  isLoading: Boolean,
  error: AppError?,
  hasReachedMax: Boolean,
  onRetry: () -> Unit,
  onLoadNextPage: () -> Unit,
  modifier: Modifier = Modifier,
  lazyListState: LazyListState = rememberLazyListState(),
  decimalFormat: StableWrapper<DecimalFormat> = remember { StableWrapper(DecimalFormat("#,###")) },
) {
  val currentOnLoadNextPage by rememberUpdatedState(onLoadNextPage)
  val currentHasReachedMax by rememberUpdatedState(hasReachedMax)

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.layoutInfo }
      .throttleTime(
        duration = 300.milliseconds,
        throttleConfiguration = ThrottleConfiguration.LEADING_AND_TRAILING,
      )
      .filter {
        val index = it.visibleItemsInfo.lastOrNull()?.index
        val totalItemsCount = it.totalItemsCount

        Napier.d(
          message = "lazyListState: currentHasReachedMax=$currentHasReachedMax " +
            "- lastVisible=$index" +
            " - totalItemsCount=$totalItemsCount",
          tag = GithubRepoItemsListLogTag,
        )

        !currentHasReachedMax && index != null &&
          index + 2 >= totalItemsCount
      }
      .collect {
        Napier.d(
          message = "load next page",
          tag = GithubRepoItemsListLogTag,
        )
        currentOnLoadNextPage()
      }
  }

  LazyColumn(
    modifier = modifier
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    state = lazyListState,
  ) {
    items(
      items = items,
      key = { it.id },
      contentType = { "GithubRepoItemRow" },
    ) { item ->
      GithubRepoItemRow(
        modifier = Modifier
          .fillParentMaxWidth(),
        item = item,
        decimalFormat = decimalFormat,
      )
    }

    when {
      isLoading -> {
        item(contentType = "LoadingIndicator") {
          LoadingIndicator(
            modifier = Modifier.height(128.dp),
          )
        }
      }
      error !== null -> {
        item(contentType = "RetryButton") {
          RetryButton(
            modifier = Modifier.height(128.dp),
            errorMessage = error.getReadableMessage(),
            onRetry = onRetry,
          )
        }
      }
      !hasReachedMax -> {
        item(contentType = "Spacer") {
          Spacer(modifier = Modifier.height(128.dp))
        }
      }
    }
  }
}
