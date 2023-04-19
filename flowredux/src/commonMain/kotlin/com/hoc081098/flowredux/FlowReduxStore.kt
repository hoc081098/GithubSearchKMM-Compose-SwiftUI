package com.hoc081098.flowredux

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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

public fun <Action, State, Output> sendOutputFromActionSideEffect(
  capacity: Int = Channel.UNLIMITED,
  transformActionToOutput: (Action) -> Output?,
): Pair<SideEffect<State, Action>, Flow<Output>> {
  val actionChannel = Channel<Output>(capacity)

  val sideEffect = SideEffect<State, Action> { actionFlow, _, coroutineScope ->
    actionFlow
      .mapNotNull(transformActionToOutput)
      .onEach(actionChannel::send)
      .onCompletion { actionChannel.close() }
      .launchIn(coroutineScope)

    emptyFlow()
  }

  return sideEffect to actionChannel.receiveAsFlow()
}

@Suppress("FunctionName") // Factory function
public fun <Action, State> FlowReduxStore(
  coroutineContext: CoroutineContext,
  initialState: State,
  sideEffects: List<SideEffect<State, Action>>,
  reducer: Reducer<State, Action>,
): FlowReduxStore<Action, State> = DefaultFlowReduxStore(
  coroutineContext = coroutineContext,
  initialState = initialState,
  sideEffects = sideEffects,
  reducer = reducer
)
