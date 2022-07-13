package com.hoc081988.github_search_kmm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

interface ScopeProvider {
  fun scope(): CoroutineScope
}

internal class MainScopeProvider : ScopeProvider {
  override fun scope() = MainScope()
}
