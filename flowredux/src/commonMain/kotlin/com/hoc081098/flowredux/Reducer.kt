package com.hoc081098.flowredux

/**
 * A simple type alias for a reducer function.
 * A Reducer takes a State and an Action as input and produces a state as output.
 *
 * If a reducer should not react on a Action, just return the old State.
 *
 * @param S The type of the state
 * @param A The type of the Actions
 */
public fun interface Reducer<S, A> {
  public operator fun invoke(state: S, action: A): S
}
