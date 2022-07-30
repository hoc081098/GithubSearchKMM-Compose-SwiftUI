package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.domain.model.AppError
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.network.sockets.SocketTimeoutException
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet

internal actual class PlatformAppErrorMapper : (Throwable) -> AppError? {
  override fun invoke(t: Throwable): AppError? {
    return when (t) {
      is AppError -> t
      is SocketTimeoutException -> AppError.ApiException.TimeoutException(t)
      is DarwinHttpRequestException -> when {
        t.origin.domain == NSURLErrorDomain && t.origin.code in NETWORK_ERROR_CODES ->
          AppError.ApiException.NetworkException(t)
        else -> null
      }
      else -> null
    }
  }

  private companion object {
    private val NETWORK_ERROR_CODES = setOf(
      NSURLErrorNotConnectedToInternet,
      NSURLErrorNetworkConnectionLost
    )
  }
}
