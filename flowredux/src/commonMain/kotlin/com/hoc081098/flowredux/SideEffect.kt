package com.hoc081098.flowredux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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
