//
//  IOSGithubSearchViewModel.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 7/18/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import Combine
import shared
import sharedSwift

@MainActor
class IOSGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published private(set) var state: GithubSearchState
  @Published private(set) var term: String = ""
  let eventPublisher: AnyPublisher<GithubSearchSingleEventKs, Never>

  init(vm: GithubSearchViewModel) {
    self.vm = vm

    self.eventPublisher = vm.eventFlow.asNonNullPublisher()
      .assertNoFailure()
      .map(GithubSearchSingleEventKs.init)
      .eraseToAnyPublisher()

    self.state = vm.stateFlow.value
    vm.stateFlow.subscribe(
      scope: vm.viewModelScope,
      onValue: { [weak self] in self?.state = $0 }
    )

    self.vm
      .termStateFlow
      .asNonNullPublisher(NSString.self)
      .assertNoFailure()
      .map { $0 as String }
      .assign(to: &$term)
  }

  @discardableResult
  func dispatch(action: GithubSearchAction) -> Bool {
    self.vm.dispatch(action: action)
  }

  deinit {
    vm.clear()
    Napier.d("\(self)::deinit isCleared=\(vm.isCleared())")
  }
}
