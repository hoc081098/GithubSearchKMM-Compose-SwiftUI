package com.hoc081098.github_search_kmm.utils

internal expect class WeakReference<T : Any> constructor(reference: T) {
  fun get(): T?
}
