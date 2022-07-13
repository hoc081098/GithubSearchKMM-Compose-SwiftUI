package com.hoc081988.github_search_kmm

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
  protected val viewModelScope: CoroutineScope

  protected fun onCleared()
}
