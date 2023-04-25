package com.hoc081098.flowredux

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job

// TODO: Consider using AutoCloseable since Kotlin 1.8.20
public sealed interface FlowReduxStore<Action, State> {
  /**
   * The state of this store.
   */
  public val stateFlow: StateFlow<State>

  /**
   * @return false if cannot dispatch action (this store was closed).
   */
  public fun dispatch(action: Action): Boolean

  /**
   * Call this method to close this store.
   * A closed store will not accept any action anymore, thus state will not change anymore.
   * All [SideEffect]s will be cancelled.
   */
  public fun close()

  /**
   * After calling [close] method, this function will return true.
   *
   * @return true if this store was closed.
   */
  public fun isClosed(): Boolean
}

/**
 * Create a [FlowReduxStore] with [sideEffects] and [reducer].
 *
 * The [FlowReduxStore] will be closed when the [CoroutineScope] is cancelled.
 * That requires the [CoroutineScope] has a [Job] in its context.
 * And you don't need to call [FlowReduxStore.close] manually.
 *
 * @receiver The [CoroutineScope] of the state machine. This scope must have a [Job] in its context.
 * @param initialState The initial state of the state machine.
 * @param sideEffects A list of [SideEffect]s.
 * @param reducer A [Reducer] function.
 */
public fun <Action, State> CoroutineScope.createFlowReduxStore(
  initialState: State,
  sideEffects: List<SideEffect<Action, State>>,
  reducer: Reducer<Action, State>,
): FlowReduxStore<Action, State> {
  val store = DefaultFlowReduxStore(
    coroutineContext = coroutineContext,
    initialState = initialState,
    sideEffects = sideEffects,
    reducer = reducer
  )
  coroutineContext.job.invokeOnCompletion {
    store.close()
  }
  return store
}

@Suppress("FunctionName") // Factory function
public fun <Action, State> FlowReduxStore(
  coroutineContext: CoroutineContext,
  initialState: State,
  sideEffects: List<SideEffect<Action, State>>,
  reducer: Reducer<Action, State>,
): FlowReduxStore<Action, State> = DefaultFlowReduxStore(
  coroutineContext = coroutineContext,
  initialState = initialState,
  sideEffects = sideEffects,
  reducer = reducer
)
