package com.hoc081098.github_search_kmm.data

import arrow.core.nonFatalOrThrow
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.AppError.UnknownException
import io.github.aakira.napier.Napier
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.util.cio.ChannelReadException

interface AppErrorMapper : (Throwable) -> AppError

internal expect class PlatformAppErrorMapper : (Throwable) -> AppError?

internal open class AppErrorMapperImpl(
  private val platformAppErrorMapper: PlatformAppErrorMapper,
) : AppErrorMapper {
  override fun invoke(throwable: Throwable): AppError {
    Napier.d("AppErrorMapperImpl.map $throwable")

    val t = throwable.nonFatalOrThrow()
    platformAppErrorMapper(t)?.let {
      Napier.d("platformAppErrorMapper.map -> $it")
      return it
    }

    return when (t) {
      is AppError -> t
      is ResponseException -> AppError.ApiException.ServerException(
        statusCode = t.response.status.value,
        cause = t
      )
      is HttpRequestTimeoutException,
      is ConnectTimeoutException,
      is SocketTimeoutException -> AppError.ApiException.TimeoutException(t)
      is ChannelReadException -> AppError.ApiException.NetworkException(t)
      else -> UnknownException(t)
    }
  }
}
