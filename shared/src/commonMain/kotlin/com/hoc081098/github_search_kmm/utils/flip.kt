package com.hoc081098.github_search_kmm.utils

/**
 * A function that flips arguments order of a binary function.
 * @return A function with the same behavior as the input, but with arguments flipped.
 */
inline fun <A, B, C> flip(crossinline function: (A, B) -> C): (B, A) -> C = { b, a -> function(a, b) }
