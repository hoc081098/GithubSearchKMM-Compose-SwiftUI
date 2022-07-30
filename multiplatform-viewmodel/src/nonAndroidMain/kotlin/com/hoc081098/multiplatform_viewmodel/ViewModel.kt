package com.hoc081098.multiplatform_viewmodel

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Suppress("NOTHING_TO_INLINE")
private inline val viewModelScopeDispatcher: CoroutineDispatcher
  get() = runCatching { Dispatchers.Main.immediate }
    .recoverCatching { Dispatchers.Main }
    .getOrDefault(Dispatchers.Default)

actual abstract class ViewModel actual constructor() {
  private val cleared = atomic(false)

  protected actual val viewModelScope: CoroutineScope =
    CoroutineScope(SupervisorJob() + viewModelScopeDispatcher)

  /**
   * This method will be called when this ViewModel is no longer used and will be destroyed.
   * It is useful when ViewModel observes some data and you need to clear this subscription to
   * prevent a leak of this ViewModel.
   */
  protected actual fun onCleared() {}

  /**
   * Closes the [viewModelScope] and cancels all its coroutines.
   *
   * When using it on iOS you'll want to make sure that you call [clear] on your ViewModel
   * on deinit to properly cancel the CoroutineScope
   */
  @Suppress("unused") // Called by platform code
  fun clear() {
    if (cleared.compareAndSet(expect = false, update = true)) {
      viewModelScope.cancel()
      onCleared()
    }
  }
}
