package com.hoc081988.github_search_kmm

import com.hoc081098.flowext.Event
import com.hoc081098.flowext.materialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun interface Closeable {
  fun close()
}

class NonNullFlowWrapper<T : Any>(private val flow: Flow<T>) : Flow<T> by flow {
  fun subscribe(
    scope: CoroutineScope,
    onValue: (value: T) -> Unit,
    onError: (throwable: Throwable) -> Unit,
    onComplete: () -> Unit,
  ): Closeable {
    val job = flow
      .materialize()
      .onEach { event ->
        when (event) {
          is Event.Value -> onValue(event.value)
          is Event.Error -> onError(event.error)
          Event.Complete -> onComplete()
        }
      }
      .launchIn(scope)

    return Closeable { job.cancel() }
  }
}
