package com.hoc081098.github_search_kmm.android.core_ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The main background for the app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Box].
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The background content.
 */
@Composable
fun AppBackground(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val backgroundTheme = LocalBackgroundTheme.current
  val color = backgroundTheme.color
  val tonalElevation = backgroundTheme.tonalElevation

  Surface(
    color = if (color == Color.Unspecified) Color.Transparent else color,
    tonalElevation = if (tonalElevation == Dp.Unspecified) 0.dp else tonalElevation,
    modifier = modifier.fillMaxSize(),
  ) {
    CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
      content()
    }
  }
}
