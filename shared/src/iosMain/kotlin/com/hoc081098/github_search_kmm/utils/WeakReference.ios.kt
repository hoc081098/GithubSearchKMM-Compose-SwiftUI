package com.hoc081098.github_search_kmm.utils

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual class WeakReference<T : Any> actual constructor(reference: T) {
  private val ref = kotlin.native.ref.WeakReference(reference)

  actual fun get(): T? = ref.get()
}
