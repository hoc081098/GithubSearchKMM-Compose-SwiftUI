import SwiftUI
import shared
import Combine

extension Kotlinx_coroutines_coreFlow {
  @discardableResult
  func subscribeNonNullFlow<T: AnyObject>(
    scope: Kotlinx_coroutines_coreCoroutineScope,
    onValue: @escaping (T) -> Void,
    onError: ((KotlinThrowable) -> Void)? = nil,
    onComplete: (() -> Void)? = nil
  ) -> Closeable {
    NonNullFlowWrapper<T>(flow: self)
      .subscribe(scope: scope) {
      onValue($0)

    } onError: { error in
      onError?(error)
    } onComplete: {
      onComplete?()
    }
  }
}


@MainActor
class IOSGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published var state: GithubSearchState

  init(
    vm: GithubSearchViewModel
  ) {
    self.vm = vm

    self.state = vm.stateFlow.value as! GithubSearchState
    vm.stateFlow.subscribeNonNullFlow(
      scope: vm.viewModelScope,
      onValue: { self.state = $0 }
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
  }
}

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
