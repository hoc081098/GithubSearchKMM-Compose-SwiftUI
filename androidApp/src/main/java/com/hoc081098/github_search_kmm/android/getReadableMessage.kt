package com.hoc081098.github_search_kmm.android

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.hoc081098.github_search_kmm.domain.model.AppError

@Composable
@ReadOnlyComposable
fun AppError.getReadableMessage(): String = getReadableMessage(LocalContext.current)

fun AppError.getReadableMessage(context: Context): String = when (this) {
  is AppError.ApiException.NetworkException -> context.getString(R.string.network_error_message)
  is AppError.ApiException.ServerException -> context.getString(R.string.server_error_message)
  is AppError.ApiException.TimeoutException -> context.getString(R.string.timeout_error_message)
  is AppError.ApiException.UnknownException -> context.getString(R.string.unknown_error_message)
  is AppError.LocalStorageException.DatabaseException -> context.getString(R.string.database_error_message)
  is AppError.LocalStorageException.FileException -> context.getString(R.string.file_error_message)
  is AppError.LocalStorageException.UnknownException -> context.getString(R.string.unknown_error_message)
  is AppError.UnknownException -> context.getString(R.string.unknown_error_message)
}
