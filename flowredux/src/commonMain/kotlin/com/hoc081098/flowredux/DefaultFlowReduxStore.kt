package com.hoc081098.flowredux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

internal class DefaultFlowReduxStore<Action, State>(
  override val coroutineScope: CoroutineScope,
  initialState: State,
  sideEffects: List<SideEffect<State, Action>>,
  reducer: Reducer<State, Action>
) : FlowReduxStore<Action, State> {
  private val _stateFlow = MutableStateFlow(initialState)
  private val _actionChannel = Channel<Action>(Channel.UNLIMITED)
  private val _actionSharedFlow = MutableSharedFlow<Action>(Channel.UNLIMITED)

  init {
    val getState = GetState { _stateFlow.value }
    val loopbacks = Array(sideEffects.size) { Channel<Action>() }

    val actionFlow = buildList(capacity = sideEffects.size + 1) {
      sideEffects.forEachIndexed { index, sideEffect ->
        add(
          sideEffect(
            loopbacks[index].consumeAsFlow(),
            getState,
            coroutineScope,
          )
        )
      }
      add(_actionChannel.consumeAsFlow())
    }.merge()

    actionFlow
      .onEach { action ->
        _stateFlow.value = reducer(_stateFlow.value, action)

        loopbacks.sendAll(action)
        check(_actionSharedFlow.tryEmit(action)) { "Cannot send $action" }
      }
      .launchIn(coroutineScope)
  }

  override val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()
  override val actionSharedFlow: SharedFlow<Action> = _actionSharedFlow.asSharedFlow()
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
