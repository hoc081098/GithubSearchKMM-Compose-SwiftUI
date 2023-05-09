package com.hoc081098.github_search_kmm.utils

@Suppress("NOTHING_TO_INLINE")
inline fun <K, V> Map<K, V>.diff(other: Map<K, V>): List<Triple<K, V?, V?>> = entries
  .subtract(other.entries)
  .map { (k) -> Triple(k, this[k], other[k]) }
