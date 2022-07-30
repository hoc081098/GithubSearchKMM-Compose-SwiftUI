package com.hoc081098.github_search_kmm

import javax.inject.Inject

internal class DaggerMainAppCoroutineScopeImpl @Inject constructor(appCoroutineDispatchers: AppCoroutineDispatchers) :
  MainAppCoroutineScopeImpl(appCoroutineDispatchers)
