package com.hoc081988.github_search_kmm.data

import com.hoc081988.github_search_kmm.domain.model.AppError

internal actual class PlatformAppErrorMapper : (Throwable) -> AppError? {
  override fun invoke(t: Throwable): AppError? {
    return when (t) {
      else -> null
    }
  }
}
