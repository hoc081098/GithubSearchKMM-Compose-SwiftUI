package com.hoc081098.flowredux

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
private fun TestScope.createScope() = CoroutineScope(testScheduler)

@ExperimentalCoroutinesApi
class FlowReduxStoreTest {
  @Test
  fun `initial state is emitted even without actions as input`() = runTest {
    val scope = createScope()

    var reducerInvocations = 0
    scope.createFlowReduxStore<Int, Int>(
      initialState = 0,
      sideEffects = listOf(),
      reducer = { state, _ ->
        reducerInvocations++
        state + 1
      }
    )
      .stateFlow
      .take(1)
      .testWithTestCoroutineScheduler {
        assertEquals(0, awaitItem())
        awaitComplete()
      }

    assertEquals(0, reducerInvocations)
    scope.cancel()
  }

  @Test
  fun `store without side effects just runs reducer`() = runTest {
    val scope = createScope()

    val store = scope.createFlowReduxStore<String, String>(
      initialState = "",
      sideEffects = listOf(),
      reducer = { state, action ->
        state + action
      }
    )
    val allActions = mutableListOf<String>()
    scope.launch(start = CoroutineStart.UNDISPATCHED) {
      store.actionSharedFlow.toList(allActions)
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

    assertContentEquals(
      listOf("1", "2"),
      allActions
    )
    scope.cancel()
  }
}

@ExperimentalCoroutinesApi
suspend fun <T> Flow<T>.testWithTestCoroutineScheduler(
  validate: suspend FlowTurbine<T>.() -> Unit,
) {
  val testScheduler = currentCoroutineContext()[TestCoroutineScheduler]

  if (testScheduler == null) {
    test(validate)
  } else {
    flowOn(UnconfinedTestDispatcher(testScheduler)).test(validate)
  }
}
