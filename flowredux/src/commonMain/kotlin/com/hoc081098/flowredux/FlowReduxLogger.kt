package com.hoc081098.flowredux

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public interface FlowReduxLogger<Action, State> {
  public fun onReducer(action: Action, oldState: State, newState: State)
  public fun onNewState(state: State)

  public companion object {
    @Suppress("UNCHECKED_CAST")
    public fun <Action, State> empty(): FlowReduxLogger<Action, State> = Empty as FlowReduxLogger<Action, State>
  }
}

private object Empty : FlowReduxLogger<Any?, Any?> {
  override fun onReducer(action: Any?, oldState: Any?, newState: Any?): Unit = Unit
  override fun onNewState(state: Any?): Unit = Unit
}

public fun <Action, State> Reducer<Action, State>.withLogger(
  logger: FlowReduxLogger<Action, State>,
): Reducer<Action, State> = when (logger) {
  FlowReduxLogger.empty<Action, State>() -> this
  else -> Reducer { oldState, action ->
    val newState = this(oldState, action)

    logger.onReducer(action, oldState, newState)

    newState
  }
}

public fun <Action, State> loggerSideEffect(
  logger: FlowReduxLogger<Action, State>,
): SideEffect<Action, State>? = when (logger) {
  FlowReduxLogger.empty<Action, State>() -> null
  else -> SideEffect { _, stateFlow, scope ->
    stateFlow
      .onEach(logger::onNewState)
      .launchIn(scope)

    emptyFlow()
  }
}
