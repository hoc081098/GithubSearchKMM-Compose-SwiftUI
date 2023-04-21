package com.hoc081098.github_search_kmm.utils

/**
 * A function that flips arguments order and returns a new functions
 */
fun <A, B, C> ((A, B) -> C).flip(): (B, A) -> C = { b: B, a: A -> this(a, b) }
