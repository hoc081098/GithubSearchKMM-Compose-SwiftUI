package com.hoc081098.github_search_kmm.presentation.common

import com.hoc081098.github_search_kmm.isDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext

@OptIn(ExperimentalStdlibApi::class)
internal suspend inline fun debugCheckImmediateMainDispatcher() {
  if (isDebug()) {
    val dispatcher = currentCoroutineContext()[CoroutineDispatcher]!!

    check(
      dispatcher === Dispatchers.Main.immediate ||
        !dispatcher.isDispatchNeeded(Dispatchers.Main.immediate),
    ) {
      "Expected CoroutineDispatcher to be Dispatchers.Main.immediate but was $dispatcher"
    }
  }
}
