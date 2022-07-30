package com.hoc081098.github_search_kmm

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

internal class AndroidAppCoroutineDispatchers @Inject constructor() : AppCoroutineDispatchers {
  override val main: CoroutineDispatcher get() = Dispatchers.Main
  override val io: CoroutineDispatcher get() = Dispatchers.IO
  override val default: CoroutineDispatcher get() = Dispatchers.Default
  override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
  override val immediateMain: MainCoroutineDispatcher get() = Dispatchers.Main.immediate
}
