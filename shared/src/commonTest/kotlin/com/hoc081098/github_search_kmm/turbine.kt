package com.hoc081098.github_search_kmm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlin.time.Duration
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

suspend fun <T> Flow<T>.testWithTestCoroutineScheduler(
  createTestDispatcher: (TestCoroutineScheduler) -> TestDispatcher = { UnconfinedTestDispatcher(it) },
  timeout: Duration? = null,
  validate: suspend ReceiveTurbine<T>.() -> Unit,
) {
  val testScheduler = currentCoroutineContext()[TestCoroutineScheduler]

  if (testScheduler == null) {
    test(timeout = timeout, validate = validate)
  } else {
    flowOn(createTestDispatcher(testScheduler)).test(
      timeout = timeout,
      validate = validate
    )
  }
}
