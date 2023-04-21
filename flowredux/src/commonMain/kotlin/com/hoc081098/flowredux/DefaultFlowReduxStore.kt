package com.hoc081098.flowredux

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job

internal class DefaultFlowReduxStore<Action, State>(
  coroutineContext: CoroutineContext,
  initialState: State,
  sideEffects: List<SideEffect<Action, State>>,
  reducer: Reducer<Action, State>
) : FlowReduxStore<Action, State> {
  private val coroutineScope = CoroutineScope(coroutineContext + Job())

  private val _stateFlow = MutableStateFlow(initialState)
  private val _actionChannel = Channel<Action>(Channel.UNLIMITED)

  override val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()

  init {
    val loopbacks = Array(sideEffects.size) { Channel<Action>() }

    val actionFlow = buildList(capacity = sideEffects.size + 1) {
      sideEffects.forEachIndexed { index, sideEffect ->
        add(
          sideEffect(
            loopbacks[index].consumeAsFlow(),
            stateFlow,
            coroutineScope,
          )
        )
      }
      add(_actionChannel.consumeAsFlow())
    }
      .merge()
      .buffer(Channel.UNLIMITED) // buffer all actions, we don't want to miss any action.

    actionFlow
      .onEach { action ->
        // update state
        _stateFlow.value = reducer(_stateFlow.value, action)

        // send action to loopbacks
        loopbacks.sendAll(action)
      }
      .launchIn(coroutineScope)
  }

  override fun close() = coroutineScope.cancel()

  override fun isClosed() = coroutineScope.coroutineContext.job.isCancelled

  override fun dispatch(action: Action): Boolean = _actionChannel
    .trySend(action)
    .isSuccess
}

private suspend fun <T> Array<Channel<T>>.sendAll(value: T) = coroutineScope {
  map { channel ->
    async { channel.send(value) }
  }.awaitAll()

  Unit
}
