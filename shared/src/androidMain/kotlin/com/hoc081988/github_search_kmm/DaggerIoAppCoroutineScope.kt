package com.hoc081988.github_search_kmm

import javax.inject.Inject

internal class DaggerIoAppCoroutineScope @Inject constructor(appCoroutineDispatchers: AppCoroutineDispatchers) :
  IoAppCoroutineScopeImpl(appCoroutineDispatchers)
