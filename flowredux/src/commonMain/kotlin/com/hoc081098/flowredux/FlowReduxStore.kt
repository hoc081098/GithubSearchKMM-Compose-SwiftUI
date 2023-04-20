package com.hoc081098.flowredux

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
  public override fun close()

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

/**
 * Create a [SideEffect] that maps all actions to [Output]s and send them to a [Channel].
 * @param capacity The capacity of the [Channel].
 * @param transformActionToOutput A function that maps an [Action] to an [Output].
 * If the function returns `null`, the [Action] will be ignored.
 * Otherwise, the [Action] will be mapped to an [Output] and sent to the [Channel].
 * @return A [Pair] of the [SideEffect] and a [Flow] of [Output]s.
 */
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
