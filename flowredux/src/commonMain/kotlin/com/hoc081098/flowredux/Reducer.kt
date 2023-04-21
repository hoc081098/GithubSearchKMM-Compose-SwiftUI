package com.hoc081098.flowredux

/**
 * A simple type alias for a reducer function.
 * A Reducer takes a State and an Action as input and produces a state as output.
 *
 * If a reducer should not react on a Action, just return the old State.
 *
 * @param State The type of the state
 * @param Action The type of the Actions
 */
public fun interface Reducer<Action, State> {
  public operator fun invoke(state: State, action: Action): State
}
