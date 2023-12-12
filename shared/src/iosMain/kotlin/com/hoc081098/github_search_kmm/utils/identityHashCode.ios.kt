package com.hoc081098.github_search_kmm.utils

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode as nativeIdentityHashCode

@OptIn(ExperimentalNativeApi::class)
internal actual fun Any?.identityHashCode(): Int = nativeIdentityHashCode()
