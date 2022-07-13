package com.hoc081098.flowredux

/**
 * The GetState is basically just a deferred way to get a state of a [reduxStore] at any given point in time.
 * So you have to call this method to get the state.
 */
public fun interface GetState<S> {
  public operator fun invoke(): S
}
