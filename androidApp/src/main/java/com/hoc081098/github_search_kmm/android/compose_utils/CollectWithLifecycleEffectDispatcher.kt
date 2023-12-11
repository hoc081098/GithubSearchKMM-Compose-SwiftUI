package com.hoc081098.github_search_kmm.android.compose_utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Immutable
enum class CollectWithLifecycleEffectDispatcher {
  /**
   * Use [Dispatchers.Main][kotlinx.coroutines.MainCoroutineDispatcher].
   */
  Main,

  /**
   * Use [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
   */
  ImmediateMain,

  /**
   * Use [androidx.compose.runtime.Composer.applyCoroutineContext].
   * Under the hood, it uses Compose [androidx.compose.ui.platform.AndroidUiDispatcher].
   */
  Composer,
}

/**
 * Collect the given [Flow] in an effect that runs when [LifecycleOwner.lifecycle] is at least at [minActiveState].
 *
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.ImmediateMain], the effect will run in
 * [Dispatchers.Main.immediate][kotlinx.coroutines.MainCoroutineDispatcher.immediate].
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.Main], the effect will run in
 * [Dispatchers.Main][kotlinx.coroutines.MainCoroutineDispatcher].
 * - If [dispatcher] is [CollectWithLifecycleEffectDispatcher.Composer], the effect will run in
 * [androidx.compose.runtime.Composer.applyCoroutineContext].
 *
 * NOTE: When [dispatcher] or [collector] changes, the effect will **NOT** be restarted.
 * The latest [collector] will be used to receive values from the [Flow] ([rememberUpdatedState] is used).
 * If you want to restart the effect, you need to change [keys].
 *
 * @param keys Keys to be used to [remember] the effect.
 * @param lifecycleOwner The [LifecycleOwner] to be used to [repeatOnLifecycle].
 * @param minActiveState The minimum [Lifecycle.State] to be used to [repeatOnLifecycle].
 * @param dispatcher The dispatcher to be used to launch the [Flow].
 * @param collector The collector to be used to collect the [Flow].
 *
 * @see [LaunchedEffect]
 * @see [CollectWithLifecycleEffectDispatcher]
 */
@Composable
fun <T> Flow<T>.CollectWithLifecycleEffect(
  vararg keys: Any?,
  lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
  minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
  dispatcher: CollectWithLifecycleEffectDispatcher = CollectWithLifecycleEffectDispatcher.ImmediateMain,
  collector: (T) -> Unit,
) {
  val flow = this
  val collectorState = rememberUpdatedState(collector)

  val block: suspend CoroutineScope.() -> Unit = {
    lifecycleOwner.repeatOnLifecycle(minActiveState) {
      // NOTE: we don't use `flow.collect(collectState.value)` because it can use the old value
      flow.collect { collectorState.value(it) }
    }
  }

  when (dispatcher) {
    CollectWithLifecycleEffectDispatcher.ImmediateMain -> {
      LaunchedEffectInImmediateMain(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }

    CollectWithLifecycleEffectDispatcher.Main -> {
      LaunchedEffectInMain(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }

    CollectWithLifecycleEffectDispatcher.Composer -> {
      LaunchedEffect(flow, lifecycleOwner, minActiveState, *keys, block = block)
    }
  }
}

@Composable
@NonRestartableComposable
@Suppress("ArrayReturn")
private fun LaunchedEffectInImmediateMain(vararg keys: Any?, block: suspend CoroutineScope.() -> Unit) {
  remember(*keys) { LaunchedEffectImpl(block, Dispatchers.Main.immediate) }
}

@Composable
@NonRestartableComposable
@Suppress("ArrayReturn")
private fun LaunchedEffectInMain(vararg keys: Any?, block: suspend CoroutineScope.() -> Unit) {
  remember(*keys) { LaunchedEffectImpl(block, Dispatchers.Main) }
}

private class LaunchedEffectImpl(
  private val task: suspend CoroutineScope.() -> Unit,
  dispatcher: CoroutineDispatcher,
) : RememberObserver {
  private val scope = CoroutineScope(dispatcher)
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
