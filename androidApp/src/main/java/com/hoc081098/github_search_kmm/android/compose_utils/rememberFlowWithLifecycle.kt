package com.hoc081098.github_search_kmm.android.compose_utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun <T> rememberFlowWithLifecycle(
  flow: Flow<T>,
  lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): Flow<T> = remember(flow, lifecycle, minActiveState) {
  flow.flowWithLifecycle(
    lifecycle = lifecycle,
    minActiveState = minActiveState
  )
}

/**
 * Collect the given [Flow] in a Effect that runs in the [Dispatchers.Main.immediate] coroutine,
 * when [LifecycleOwner.lifecycle] is at least at [minActiveState].
 */
@Composable
fun <T> Flow<T>.CollectWithLifecycleEffect(
  vararg keys: Any?,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  collector: (T) -> Unit,
) {
  val flow = this
  val currentCollector by rememberUpdatedState(collector)

  LaunchedEffectInImmediateMain(flow, lifecycleOwner, minActiveState, *keys) {
    lifecycleOwner.repeatOnLifecycle(minActiveState) {
      flow.collect { currentCollector(it) }
    }
  }
}

@Composable
@NonRestartableComposable
@Suppress("ArrayReturn")
private fun LaunchedEffectInImmediateMain(
  vararg keys: Any?,
  block: suspend CoroutineScope.() -> Unit,
) {
  remember(*keys) { LaunchedEffectImpl(block) }
}

private class LaunchedEffectImpl(
  private val task: suspend CoroutineScope.() -> Unit,
) : RememberObserver {
  private val scope = CoroutineScope(Dispatchers.Main.immediate)
  private var job: Job? = null

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch(block = task)
  }

  override fun onForgotten() {
    job?.cancel()
    job = null
  }

  override fun onAbandoned() {
    job?.cancel()
    job = null
  }
}
