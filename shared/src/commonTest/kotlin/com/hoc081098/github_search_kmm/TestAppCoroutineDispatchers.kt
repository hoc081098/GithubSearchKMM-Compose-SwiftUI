package com.hoc081098.github_search_kmm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

@Suppress("unused")
class TestAppCoroutineDispatchers(
  val testCoroutineDispatcher: TestDispatcher = StandardTestDispatcher(),
) : AppCoroutineDispatchers {
  override val main: CoroutineDispatcher get() = testCoroutineDispatcher
  override val io: CoroutineDispatcher get() = testCoroutineDispatcher
  override val default: CoroutineDispatcher get() = testCoroutineDispatcher
  override val unconfined: CoroutineDispatcher get() = testCoroutineDispatcher
  override val immediateMain: CoroutineDispatcher get() = testCoroutineDispatcher
}
