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

@MainActor
class IosGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published private(set) var state: GithubSearchState
  @Published private(set) var term: String = ""
  let eventPublisher: AnyPublisher<Skie.GithubSearchKMM__shared.GithubSearchSingleEvent.__Sealed, Never>

  init(
    vm: GithubSearchViewModel = DIContainer.shared.get(),
    immediateMainDispatcher: CoroutineDispatcher = DIContainer.shared
      .get(for: AppCoroutineDispatchers.self)
      .immediateMain
  ) {
    self.vm = vm
    
    self.eventPublisher = vm.eventFlow.asNonNullPublisher(
        GithubSearchSingleEvent.self,
        dispatcher: immediateMainDispatcher
      )
      .assertNoFailure()
      .map(onEnum(of:))
      .eraseToAnyPublisher()

    self.state = vm.stateFlow.value
    vm.stateFlow.subscribe(
      scope: vm.viewModelScope,
      onValue: { [weak self] in self?.state = $0 }
    )

    self.vm
      .termStateFlow
      .asNonNullPublisher(
        NSString.self,
        dispatcher: immediateMainDispatcher
      )
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
