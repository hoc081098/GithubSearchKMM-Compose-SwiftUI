package com.hoc081098.github_search_kmm.domain.usecase

import arrow.core.left
import arrow.core.right
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.test_utils.genRepoItems
import com.hoc081098.github_search_kmm.test_utils.getOrThrow
import com.hoc081098.github_search_kmm.test_utils.invokesWithoutArgs
import com.hoc081098.github_search_kmm.test_utils.leftValueOrThrow
import io.mockative.Mock
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.mock
import io.mockative.once
import io.mockative.verifyNoUnmetExpectations
import io.mockative.verifyNoUnverifiedExpectations
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SearchRepoItemsUseCaseTest {
  @Mock
  private lateinit var repoItemRepository: RepoItemRepository
  private lateinit var searchRepoItemsUseCase: SearchRepoItemsUseCase

  @BeforeTest
  fun setup() {
    repoItemRepository = mock(RepoItemRepository::class)
    searchRepoItemsUseCase = SearchRepoItemsUseCase(repoItemRepository)
  }

  @AfterTest
  fun teardown() {
    verifyNoUnverifiedExpectations(repoItemRepository)
    verifyNoUnmetExpectations(repoItemRepository)
  }

  @Test
  fun `returns a Right WHEN RepoItemRepository returns a Right`() = runTest {
    val items = genRepoItems(0..10)
    val term = "term"
    val page = 1

    coEvery { repoItemRepository.searchRepoItems(term, page) }
      .invokesWithoutArgs { items.right() }

    val either = searchRepoItemsUseCase(term, page)

    assertEquals(
      items,
      either.getOrThrow
    )
    coVerify { repoItemRepository.searchRepoItems(term, page) }
      .wasInvoked(exactly = once)
  }

  @Test
  fun `returns a Left WHEN RepoItemRepository returns a Left`() = runTest {
    val error = AppError.ApiException.NetworkException(null)
    val term = "term"
    val page = 1

    coEvery { repoItemRepository.searchRepoItems(term, page) }
      .invokesWithoutArgs { error.left() }

    val either = searchRepoItemsUseCase(term, page)

    assertEquals(error, either.leftValueOrThrow)
    coVerify { repoItemRepository.searchRepoItems(term, page) }
      .wasInvoked(exactly = once)
  }
}
