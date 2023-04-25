package com.hoc081098.flowredux

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@Suppress("NOTHING_TO_INLINE")
object L {
  inline operator fun <T> invoke() = emptyList<T>()
  inline operator fun <T> get(vararg elements: T): List<T> = elements.asList()
}

@ExperimentalCoroutinesApi
private fun TestScope.createScope() = CoroutineScope(
  UnconfinedTestDispatcher(
    testScheduler
  )
)

fun <Action : Any, State> CoroutineScope.createTestFlowReduxStore(
  initialState: State,
  sideEffects: List<SideEffect<Action, State>>,
  reducer: Reducer<Action, State>,
): Pair<FlowReduxStore<Action, State>, Flow<Action>> {
  val (effect, channel) = allActionsToOutputChannelSideEffect<Action, State, Action> { it }

  val store = createFlowReduxStore(
    initialState = initialState,
    sideEffects = sideEffects + effect,
    reducer = reducer
  )

  return store to channel.receiveAsFlow()
}

@FlowPreview
@ExperimentalCoroutinesApi
class FlowReduxStoreTest {
  @Test
  fun `initial state is emitted even without actions as input`() = runTest {
    val scope = createScope()

    var reducerInvocations = 0
    scope.createTestFlowReduxStore<Int, Int>(
      initialState = 0,
      sideEffects = L(),
      reducer = { state, _ ->
        reducerInvocations++
        state + 1
      }
    )
      .first
      .stateFlow
      .take(1)
      .testWithTestCoroutineScheduler {
        assertEquals(0, awaitItem())
        awaitComplete()
      }
    advanceUntilIdle()
    runCurrent()

    assertEquals(0, reducerInvocations)
    scope.cancel()
  }

  @Test
  fun `store without side effects just runs reducer`() = runTest {
    val scope = createScope()

    val (store, actionFlow) = scope.createTestFlowReduxStore<String, String>(
      initialState = "",
      sideEffects = L(),
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<String>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      actionFlow.toList(allActions)
    }

    scope.launch {
      delay(100)
      store.dispatch("1")
      store.dispatch("2")
    }

    store.stateFlow.take(3).testWithTestCoroutineScheduler {
      assertEquals("", awaitItem())
      assertEquals("1", awaitItem())
      assertEquals("12", awaitItem())
      awaitComplete()
    }
    advanceUntilIdle()
    runCurrent()

    assertContentEquals(
      L["1", "2"],
      allActions
    )
    scope.cancel()
  }

  @Test
  fun `store with empty side effect that emits nothing`() = runTest {
    val scope = createScope()
    val sideEffect1Actions = mutableListOf<String>()

    val (store, actionFlow) = scope.createTestFlowReduxStore<String, String>(
      initialState = "",
      sideEffects = L[
        SideEffect { actions, _, _ ->
          actions.flatMapConcat {
            sideEffect1Actions += it
            emptyFlow()
          }
        }
      ],
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<String>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      actionFlow.toList(allActions)
    }

    scope.launch {
      delay(100)
      store.dispatch("1")
      store.dispatch("2")
    }

    store.stateFlow.take(3).testWithTestCoroutineScheduler {
      assertEquals("", awaitItem())
      assertEquals("1", awaitItem())
      assertEquals("12", awaitItem())
      awaitComplete()
    }
    advanceUntilIdle()
    runCurrent()

    assertContentEquals(
      L["1", "2"],
      allActions
    )
    assertContentEquals(
      sideEffect1Actions,
      allActions
    )
    scope.cancel()
  }

  @Test
  fun `store with 2 side effects and they emit nothing`() = runTest {
    val scope = createScope()
    val sideEffect1Actions = mutableListOf<String>()
    val sideEffect2Actions = mutableListOf<String>()

    val (store, actionFlow) = scope.createTestFlowReduxStore<String, String>(
      initialState = "",
      sideEffects = L[
        SideEffect { actions, _, _ ->
          actions.flatMapConcat {
            sideEffect1Actions += it
            emptyFlow()
          }
        },
        SideEffect { actions, _, _ ->
          actions.flatMapConcat {
            sideEffect2Actions += it
            emptyFlow()
          }
        }
      ],
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<String>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      actionFlow.toList(allActions)
    }

    scope.launch {
      delay(100)
      store.dispatch("1")
      store.dispatch("2")
    }

    store.stateFlow.take(3).testWithTestCoroutineScheduler {
      assertEquals("", awaitItem())
      assertEquals("1", awaitItem())
      assertEquals("12", awaitItem())
      awaitComplete()
    }
    advanceUntilIdle()
    runCurrent()

    assertContentEquals(
      L["1", "2"],
      allActions
    )
    assertContentEquals(
      sideEffect1Actions,
      allActions
    )
    assertContentEquals(
      sideEffect2Actions,
      allActions
    )
    scope.cancel()
  }

  @Test
  fun `store with 2 simple side effects`() = runTest {
    val scope = createScope()
    val sideEffect1Actions = mutableListOf<Int>()
    val sideEffect2Actions = mutableListOf<Int>()

    val (store, actionFlow) = scope.createTestFlowReduxStore<Int, String>(
      initialState = "",
      sideEffects = L[
        SideEffect { actions, _, _ ->
          actions.flatMapConcat {
            sideEffect1Actions += it

            if (it < 6) {
              flowOf(6)
            } else {
              emptyFlow()
            }
          }
        },
        SideEffect { actions, _, _ ->
          actions.flatMapConcat {
            sideEffect2Actions += it

            if (it < 6) {
              flowOf(7)
            } else {
              emptyFlow()
            }
          }
        }
      ],
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<Int>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      actionFlow.toList(allActions)
    }

    scope.launch {
      delay(100)
      store.dispatch(1)
      delay(100)
      store.dispatch(2)
    }

    store.stateFlow.take(7).testWithTestCoroutineScheduler {
      // Initial State emission
      assertEquals("", awaitItem())

      // emission of 1
      assertEquals("1", awaitItem())
      assertEquals("16", awaitItem())
      assertEquals("167", awaitItem())

      // emission of 2
      assertEquals("1672", awaitItem())
      assertEquals("16726", awaitItem())
      assertEquals("167267", awaitItem())

      awaitComplete()
    }
    advanceUntilIdle()
    runCurrent()

    assertContentEquals(
      L[1, 6, 7, 2, 6, 7],
      allActions
    )
    assertContentEquals(
      L[1, 6, 7, 2, 6, 7],
      sideEffect1Actions
    )
    assertContentEquals(
      L[1, 6, 7, 2, 6, 7],
      sideEffect2Actions
    )
    scope.cancel()
  }

  @Test
  fun `store with 2 simple side effects no delay`() = runTest {
    val scope = createScope()
    val sideEffect1Actions = mutableListOf<Int>()
    val sideEffect2Actions = mutableListOf<Int>()

    val (store, actionFlow) = scope.createTestFlowReduxStore<Int, String>(
      initialState = "",
      sideEffects = L[
        SideEffect { actions, _, _ ->
          actions
            .buffer(Channel.UNLIMITED)
            .flatMapConcat {
              sideEffect1Actions += it

              if (it < 6) {
                flowOf(6)
              } else {
                emptyFlow()
              }
            }
        },
        SideEffect { actions, _, _ ->
          actions
            .buffer(Channel.UNLIMITED)
            .flatMapConcat {
              sideEffect2Actions += it

              if (it < 6) {
                flowOf(7)
              } else {
                emptyFlow()
              }
            }
        }
      ],
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<Int>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      actionFlow.toList(allActions)
    }

    store.stateFlow
      .onSubscription {
        store.dispatch(1)
        store.dispatch(2)
      }
      .take(7)
      .testWithTestCoroutineScheduler {
        // Initial State emission
        assertEquals("", awaitItem())

        // emission of 1
        assertEquals("1", awaitItem())

        // emission of 2
        assertEquals("12", awaitItem())

        // side effect 1 responses to 1
        assertEquals("126", awaitItem())

        // side effect 2 responses to 2
        assertEquals("1267", awaitItem())

        // side effect 2 responses to 1
        assertEquals("12676", awaitItem())

        // side effect 2 responses to 2
        assertEquals("126767", awaitItem())

        awaitComplete()
      }
    advanceUntilIdle()
    runCurrent()

    assertContentEquals(
      L[1, 2, 6, 7, 6, 7],
      allActions
    )
    assertContentEquals(
      L[1, 2, 6, 7, 6, 7],
      sideEffect1Actions
    )
    assertContentEquals(
      L[1, 2, 6, 7, 6, 7],
      sideEffect2Actions
    )
    scope.cancel()
  }

  @Test
  fun `canceling the flow of input actions also cancels all side effects`() = runTest {
    val scope = createScope()

    var sideEffect1Started = false
    var sideEffect2Started = false

    var sideEffect1Ended = false
    var sideEffect2Ended = false

    val store = scope.createFlowReduxStore<Int, String>(
      initialState = "",
      sideEffects = L[
        SideEffect { actions, _, _ ->
          actions
            .buffer(Channel.UNLIMITED)
            .map {
              sideEffect1Started = true
              println("sideEffect1 delay...")
              delay(2_000)
              sideEffect1Ended = true

              error("Should not reach here!")
            }
        },
        SideEffect { actions, _, _ ->
          actions
            .buffer(Channel.UNLIMITED)
            .map {
              sideEffect2Started = true
              println("sideEffect2 delay...")
              delay(2_000)
              sideEffect2Ended = true

              error("Should not reach here!")
            }
        }
      ],
      reducer = { state, action ->
        state + action
      }
    )

    launch {
      delay(200)
      println("Cancelling scope")
      scope.cancel()
    }

    store.stateFlow
      .onSubscription {
        store.dispatch(1)
      }
      .take(2)
      .testWithTestCoroutineScheduler {
        assertEquals("", awaitItem())
        assertEquals("1", awaitItem())
        awaitComplete()
      }

    assertTrue { sideEffect1Started && sideEffect2Started }
    assertFalse { sideEffect1Ended }
    assertFalse { sideEffect2Ended }
  }

  @Test
  fun `close and isClosed`() = runTest {
    val scope = createScope()
    val cancelled = CompletableDeferred<Unit>()

    val store = FlowReduxStore(
      coroutineContext = scope.coroutineContext,
      initialState = 0,
      sideEffects = L[
        SideEffect<Int, Int> { _, _, coroutineScope ->
          coroutineScope.coroutineContext.job.invokeOnCompletion { cancelled.complete(Unit) }
          emptyFlow()
        }
      ],
      reducer = { state, _ -> state + 1 }
    )

    assertTrue { store.dispatch(1) }
    assertFalse { store.isClosed() }

    store.close()

    assertFalse { store.dispatch(1) }
    assertTrue { store.isClosed() }

    cancelled.await()
  }

  @Test
  fun `empty logger`() = runTest {
    val l1: FlowReduxLogger<Int, String> = FlowReduxLogger.empty()
    val l2: FlowReduxLogger<String, Int> = FlowReduxLogger.empty()
    val l3: FlowReduxLogger<Int, Int> = FlowReduxLogger.empty()
    val l4: FlowReduxLogger<String, String> = FlowReduxLogger.empty()

    assertEquals(1, setOf(l1, l2, l3, l4).size)
  }
}

@ExperimentalCoroutinesApi
suspend fun <T> Flow<T>.testWithTestCoroutineScheduler(
  validate: suspend ReceiveTurbine<T>.() -> Unit,
) {
  val testScheduler = currentCoroutineContext()[TestCoroutineScheduler]

  if (testScheduler == null) {
    test(validate = validate)
  } else {
    flowOn(UnconfinedTestDispatcher(testScheduler)).test(validate = validate)
  }
}
