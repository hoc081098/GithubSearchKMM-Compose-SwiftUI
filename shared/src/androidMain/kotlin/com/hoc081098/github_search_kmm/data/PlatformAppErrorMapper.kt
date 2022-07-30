package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.domain.model.AppError
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

internal actual class PlatformAppErrorMapper @Inject constructor() : (Throwable) -> AppError? {
  override fun invoke(t: Throwable): AppError? {
    return when (t) {
      is AppError -> t
      is IOException -> when (t) {
        is UnknownHostException, is SocketException -> AppError.ApiException.NetworkException(t)
        is SocketTimeoutException -> AppError.ApiException.TimeoutException(t)
        else -> null
      }
      else -> null
    }
  }
}
