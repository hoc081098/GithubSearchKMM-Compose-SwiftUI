//
//  GithubRepoItemsList.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 7/16/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared

struct GithubRepoItemsList: View {
  let items: [RepoItem]
  let isLoading: Bool
  let error: AppError?
  let hasReachedMax: Bool

  let endOfListReached: () -> Void
  let onRetry: () -> Void

  var body: some View {
    List {
      ForEach(items) { item in
        Text(item.name)
      }


      if isLoading {
        HStack(alignment: .center) {
          ProgressView("Loading...")
        }.frame(maxWidth: .infinity)
      } else if let error = error {
        ErrorMessageAndButton(
          error: error,
          onRetry: onRetry,
          font: .subheadline
        )
      } else if !items.isEmpty, !hasReachedMax {
        Rectangle()
          .size(width: 0, height: 0)
          .onAppear(perform: endOfListReached)
      }
    }
  }
}

