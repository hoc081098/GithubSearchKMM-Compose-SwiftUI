package com.hoc081098.flowredux

public fun interface FlowReduxLogger<Action, State> {
  /**
   * Called when the reducer is called with [action] and [prevState] to produce [nextState].
   * @param action The action that was dispatched.
   * @param prevState The previous state.
   * @param nextState The new state produced by the reducer.
   */
  public fun onReduced(action: Action, prevState: State, nextState: State)

  public companion object {
    /**
     * Returns an empty logger that does nothing.
     * Use this logger to disable logging.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <Action, State> empty(): FlowReduxLogger<Action, State> = Empty as FlowReduxLogger<Action, State>
  }
}

private object Empty : FlowReduxLogger<Any?, Any?> {
  override fun onReduced(action: Any?, prevState: Any?, nextState: Any?): Unit = Unit
}

public fun <Action, State> Reducer<Action, State>.withLogger(
  logger: FlowReduxLogger<Action, State>,
): Reducer<Action, State> = when (logger) {
  FlowReduxLogger.empty<Action, State>() -> this
  else -> Reducer { prevState, action ->
    val nextState = this(prevState, action)

    logger.onReduced(action, prevState, nextState)

    nextState
  }
}
