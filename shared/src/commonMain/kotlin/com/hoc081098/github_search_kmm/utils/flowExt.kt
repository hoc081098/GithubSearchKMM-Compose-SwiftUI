package com.hoc081098.github_search_kmm.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

/**
 * Returns a flow that ignores all elements emitted by the original flow.
 * The returned flow completes normally when the original flow completes.
 * If the original flow fails, the returned flow fails with the same exception.
 */
fun <T> Flow<T>.ignoreElements(): Flow<Nothing> = flow { collect() }
