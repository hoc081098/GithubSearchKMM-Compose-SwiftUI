//
//  GithubRepoItemsFirstPage.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/16/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct GithubRepoItemsFirstPage: View {
  let isLoading: Bool
  let error: AppError?
  let items: [RepoItem]
  let hasTerm: Bool
  let hasReachedMax: Bool

  let endOfListReached: () -> Void
  let onRetry: () -> Void

  var body: some View {
    if isLoading {
      ProgressView("Loading...")
    }
    else if let error = error {
      ErrorMessageAndButton(
        error: error,
        onRetry: onRetry
      )
    }
    else if items.isEmpty {
      GithubEmptySearch(
        hasTerm: hasTerm
      )
    }
    else {
      GithubRepoItemsList(
        items: items,
        isLoading: false,
        error: nil,
        hasReachedMax: hasReachedMax,
        endOfListReached: endOfListReached,
        onRetry: onRetry
      )
    }
  }
}
