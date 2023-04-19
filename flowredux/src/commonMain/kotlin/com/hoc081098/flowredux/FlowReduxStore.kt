package com.hoc081098.flowredux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job

@OptIn(ExperimentalStdlibApi::class)
public sealed interface FlowReduxStore<Action, State> : AutoCloseable {
  public val stateFlow: StateFlow<State>

  /**
   * @return false if cannot dispatch action ([coroutineScope] was cancelled).
   */
  public fun dispatch(action: Action): Boolean
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
  sideEffects: List<SideEffect<State, Action>>,
  reducer: Reducer<State, Action>,
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
