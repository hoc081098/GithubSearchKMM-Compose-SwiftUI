package com.hoc081098.github_search_kmm.utils

inline fun <K, V> Map<K, V>.diff(other: Map<K, V>): List<Triple<K, V?, V?>> = entries
  .subtract(other.entries)
  .map { (k, v) -> Triple(k, this[k], other[k]) }
