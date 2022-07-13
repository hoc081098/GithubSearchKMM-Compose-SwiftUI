package com.hoc081098.flowredux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * It is a function which takes a stream of actions and returns a stream of actions. Actions in, actions out
 * (concept borrowed from redux-observable.js.or - so called epics).
 *
 * @param actions Input action. Every SideEffect should be responsible to handle a single Action
 * (i.e using filter or ofType operator)
 * @param state [GetState] to get the latest state of the state machine
 */
public fun interface SideEffect<S, A> {
  public operator fun invoke(
    actions: Flow<A>,
    getState: GetState<S>,
    coroutineScope: CoroutineScope
  ): Flow<A>
}
