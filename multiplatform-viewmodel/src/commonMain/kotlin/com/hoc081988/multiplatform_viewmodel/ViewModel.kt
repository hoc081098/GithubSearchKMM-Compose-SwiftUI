package com.hoc081988.multiplatform_viewmodel

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
  protected val viewModelScope: CoroutineScope

  protected fun onCleared()
}
