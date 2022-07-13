import SwiftUI
import shared
import Combine

@MainActor
class IOSGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published var state: GithubSearchState
  let eventPublisher: AnyPublisher<GithubSearchSingleEvent, Never>

  init(vm: GithubSearchViewModel) {
    self.vm = vm

    
    self.eventPublisher = vm.eventFlow.asNonNullPublisher()
      .assertNoFailure()
      .eraseToAnyPublisher()
    
    self.state = vm.stateFlow.value as! GithubSearchState
    vm.stateFlow.subscribeNonNullFlow(
      scope: vm.viewModelScope,
      onValue: { [weak self] in self?.state = $0 }
    )
  }

  @discardableResult
  func dispatch(action: GithubSearchAction) -> Bool {
    self.vm.dispatch(action: action)
  }

  deinit {
    vm.clear()
  }
}



struct ContentView: View {
  let greet = Greeting().greeting()

  @ObservedObject var vm = IOSGithubSearchViewModel(
    vm: DIContainer.shared.get(for: GithubSearchViewModel.self)
  )

  var body: some View {
    Text("Hello \(greet) \(self.vm.state)")
      .onAppear {
        self.vm.dispatch(action: GithubSearchActionSearch(term: "kmm"))
      }
      .onReceive(self.vm.eventPublisher) { event in
        print("Event --- \(event)")
      }
  }
}

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
