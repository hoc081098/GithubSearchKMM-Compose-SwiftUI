package com.hoc081098.github_search_kmm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * An interface that provides properties for accessing commonly used [CoroutineDispatcher]s. This differs from the
 * [Dispatchers] object in that it has consistent properties across all platforms and since [AppCoroutineDispatchers] is
 * an interface, it can easily be mocked and tested, and different implementations can easily be made to adapt to
 * different scenarios.
 *
 * Each supported platform contains an implementation of this [AppCoroutineDispatchers] interface.
 *
 * Note that not all platforms natively support all of the [CoroutineDispatcher] types (ex: only JVM supports
 * Dispatchers.IO), so fallbacks are provided when they aren't available for the default implementations.
 */
interface AppCoroutineDispatchers {
  /**
   * The companion object for the [CoroutineDispatchers] interface. This is provided so that it's possible to create
   * extension functions and properties on the companion object.
   */
  companion object

  /**
   * The main [CoroutineDispatcher] that is usually used for UI work. Default implementations of this interface,
   * refer to [Dispatchers.Main] for this value when it is available.
   *
   * Note that this isn't available on all platforms, so when one isn't present, this falls back to the [default]
   * [CoroutineDispatcher] in the default implementation.
   *
   * Default implementation [CoroutineDispatcher]:
   * Android - Main
   * iOS - Custom Main implementation or Default
   */
  val main: CoroutineDispatcher

  /**
   * The [CoroutineDispatcher] that is usually used for input/output, or intensive operations. Default
   * implementations of this interface, refer to Dispatchers.IO for this value when it is available.
   *
   * Note that this isn't available on all platforms, so when one isn't present, this falls back to the [default]
   * [CoroutineDispatcher] in the default implementation.
   *
   * Default implementation [CoroutineDispatcher]:
   * Android - IO
   * iOS - Default
   */
  val io: CoroutineDispatcher

  /**
   * The [CoroutineDispatcher] that is the default that is used by all standard builders like launch and async if no
   * other [CoroutineDispatcher] is provided or in their context. Default implementations of this interface, refer to
   * [Dispatchers.Default] for this value.
   *
   * Default implementation [CoroutineDispatcher]:
   * Android - Default
   * iOS - Default
   */
  val default: CoroutineDispatcher

  /**
   * The [CoroutineDispatcher] that is not confined to any specific thread. Default implementations of this
   * interface refer to [Dispatchers.Unconfined] for this value.
   *
   * Default implementation [CoroutineDispatcher]:
   * Android - Unconfined
   * iOS - Unconfined
   */
  val unconfined: CoroutineDispatcher

  /**
   * Returns dispatcher that executes coroutines immediately when it is already in the right context
   * (e.g. current looper is the same as this handler's looper) without an additional [re-dispatch][CoroutineDispatcher.dispatch].
   *
   * Immediate dispatcher is safe from stack overflows and in case of nested invocations forms event-loop similar to [Dispatchers.Unconfined].
   * The event loop is an advanced topic and its implications can be found in [Dispatchers.Unconfined] documentation.
   * The formed event-loop is shared with [Unconfined] and other immediate dispatchers, potentially overlapping tasks between them.
   *
   * Example of usage:
   * ```
   * suspend fun updateUiElement(val text: String) {
   *   /*
   *    * If it is known that updateUiElement can be invoked both from the Main thread and from other threads,
   *    * `immediate` dispatcher is used as a performance optimization to avoid unnecessary dispatch.
   *    *
   *    * In that case, when `updateUiElement` is invoked from the Main thread, `uiElement.text` will be
   *    * invoked immediately without any dispatching, otherwise, the `Dispatchers.Main` dispatch cycle will be triggered.
   *    */
   *   withContext(immediateMain) {
   *     uiElement.text = text
   *   }
   *   // Do context-independent logic such as logging
   * }
   * ```
   *
   * Method may throw [UnsupportedOperationException] if immediate dispatching is not supported by current dispatcher,
   * please refer to specific dispatcher documentation.
   *
   * [Dispatchers.Main] supports immediate execution for Android, JavaFx and Swing platforms.
   */
  val immediateMain: CoroutineDispatcher
}
