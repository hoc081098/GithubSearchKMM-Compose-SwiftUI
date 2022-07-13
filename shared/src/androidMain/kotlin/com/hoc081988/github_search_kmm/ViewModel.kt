package com.hoc081988.github_search_kmm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope as androidXViewModelScope
import kotlinx.coroutines.CoroutineScope

actual abstract class ViewModel : ViewModel() {
  protected actual val viewModelScope: CoroutineScope = androidXViewModelScope
  actual override fun onCleared() = super.onCleared()
}
