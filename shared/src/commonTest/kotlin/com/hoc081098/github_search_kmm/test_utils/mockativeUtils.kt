package com.hoc081098.github_search_kmm.test_utils

import io.mockative.ResultBuilder
import io.mockative.SuspendResultBuilder

// TODO: Check it following https://github.com/mockative/mockative/issues/73
fun <R> SuspendResultBuilder<R>.invokesWithArgs(block: suspend (arguments: Array<Any?>) -> R) = invokes(block)

// TODO: Check it following https://github.com/mockative/mockative/issues/73
infix fun <R> SuspendResultBuilder<R>.invokesWithoutArgs(block: suspend () -> R) = invokes(block)

// TODO: Check it following https://github.com/mockative/mockative/issues/73
fun <R> ResultBuilder<R>.invokesWithArgs(block: (arguments: Array<Any?>) -> R) = invokes(block)

// TODO: Check it following https://github.com/mockative/mockative/issues/73
fun <R> ResultBuilder<R>.invokesWithoutArgs(block: () -> R) = invokes(block)
