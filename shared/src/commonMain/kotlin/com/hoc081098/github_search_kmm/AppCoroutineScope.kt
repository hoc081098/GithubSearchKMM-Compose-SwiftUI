package com.hoc081098.github_search_kmm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

interface AppCoroutineScope : CoroutineScope

internal open class IoAppCoroutineScopeImpl(
  appCoroutineDispatchers: AppCoroutineDispatchers,
) : AppCoroutineScope {
  override val coroutineContext = SupervisorJob() + appCoroutineDispatchers.io

  override fun toString() = "DefaultAppCoroutineScope(coroutineContext=$coroutineContext)"
}

internal open class MainAppCoroutineScopeImpl(
  appCoroutineDispatchers: AppCoroutineDispatchers,
) : AppCoroutineScope {
  override val coroutineContext = SupervisorJob() + appCoroutineDispatchers.main

  override fun toString() = "DefaultAppCoroutineScope(coroutineContext=$coroutineContext)"
}
