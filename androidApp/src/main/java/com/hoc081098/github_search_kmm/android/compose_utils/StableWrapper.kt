package com.hoc081098.github_search_kmm.android.compose_utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

@Stable
@JvmInline
value class StableWrapper<T>(val value: T)

@Stable
@Suppress("NOTHING_TO_INLINE")
inline operator fun <T> StableWrapper<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <T> rememberStableWrapperOf(value: T): StableWrapper<T> = remember(value) { StableWrapper(value) }
