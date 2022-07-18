package com.hoc081988.github_search_kmm.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.hoc081988.github_search_kmm.domain.model.AppError

@Composable
@ReadOnlyComposable
fun AppError.getReadableMessage(): String {
  return when (this) {
    is AppError.ApiException.NetworkException -> stringResource(R.string.network_error_message)
    is AppError.ApiException.ServerException -> stringResource(R.string.server_error_message)
    is AppError.ApiException.TimeoutException -> stringResource(R.string.timeout_error_message)
    is AppError.ApiException.UnknownException -> stringResource(R.string.unknown_error_message)
    is AppError.LocalStorageException.DatabaseException -> stringResource(R.string.database_error_message)
    is AppError.LocalStorageException.FileException -> stringResource(R.string.file_error_message)
    is AppError.LocalStorageException.UnknownException -> stringResource(R.string.unknown_error_message)
    is AppError.UnknownException -> stringResource(R.string.unknown_error_message)
  }
}
