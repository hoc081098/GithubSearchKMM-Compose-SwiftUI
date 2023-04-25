package com.hoc081098.flowredux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * It is a function which takes a stream of actions and returns a stream of actions. Actions in, actions out
 * (concept borrowed from redux-observable.js.or - so called epics).
 */
public fun interface SideEffect<Action, State> {
  /**
   * @param actionFlow Input action. Every SideEffect should be responsible to handle a single Action
   * (i.e using [kotlinx.coroutines.flow.filter] or [kotlinx.coroutines.flow.filterIsInstance] operator)
   * @param stateFlow Allow getting the latest state of the state machine or reacting to state changes.
   * @param coroutineScope The scope of [FlowReduxStore]. It can be used to start a coroutine or
   * share [actionFlow] via [kotlinx.coroutines.flow.shareIn] operator.
   * @return A Flow of actions. It can be empty if no action should be dispatched.
   */
  public operator fun invoke(
    actionFlow: Flow<Action>,
    stateFlow: StateFlow<State>,
    coroutineScope: CoroutineScope
  ): Flow<Action>
}

/**
 * Create a [SideEffect] that maps all actions to [Output]s and send them to a [Channel].
 * The result [Channel] will be closed when the [SideEffect] is cancelled (when calling [FlowReduxStore.close]).
 *
 * @param capacity The capacity of the [Channel].
 * @param transformActionToOutput A function that maps an [Action] to an [Output].
 * If the function returns `null`, the [Action] will be ignored.
 * Otherwise, the [Action] will be mapped to an [Output] and sent to the [Channel].
 * @return A [Pair] of the [SideEffect] and a [Flow] of [Output]s.
 */
public fun <Action, State, Output : Any> allActionsToOutputChannelSideEffect(
  capacity: Int = Channel.UNLIMITED,
  transformActionToOutput: (Action) -> Output?,
): Pair<SideEffect<Action, State>, ReceiveChannel<Output>> {
  val actionChannel = Channel<Output>(capacity)

  val sideEffect = SideEffect<Action, State> { actionFlow, _, coroutineScope ->
    actionFlow
      .mapNotNull(transformActionToOutput)
      .onEach(actionChannel::send)
      .onCompletion { actionChannel.close() }
      .launchIn(coroutineScope)

    emptyFlow()
  }

  return sideEffect to actionChannel
}
