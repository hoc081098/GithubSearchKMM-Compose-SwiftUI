import SwiftUI
import shared
import Combine
import sharedSwift

@MainActor
class IOSGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published var state: GithubSearchState
  let eventPublisher: AnyPublisher<GithubSearchSingleEventKs, Never>

  init(vm: GithubSearchViewModel) {
    self.vm = vm

    self.eventPublisher = vm.eventFlow.asNonNullPublisher()
      .assertNoFailure()
      .map(GithubSearchSingleEventKs.init)
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

  @State private var term: String = ""

  var body: some View {
    let state = self.vm.state
    let hasTerm = !state.term
      .trimmingCharacters(in: .whitespacesAndNewlines)
      .isEmpty

    return NavigationView {
      VStack {
        HStack {
          Image(systemName: "magnifyingglass")

          TextField("Search...", text: $term)
            .onChange(of: term) { self.vm.dispatch(action: GithubSearchActionSearch(term: $0)) }
            .font(.title2)

          Button(action: { term = "" }) {
            Image(systemName: "xmark.circle.fill")
              .opacity(term == "" ? 0 : 1)
          }.foregroundColor(.secondary)
        }.padding()

        if hasTerm {
          Text("Search results for '\(state.term)'")
            .font(.subheadline)
        }

        ZStack(alignment: .center) {
          if state.isFirstPage {
            GithubRepoItemsFirstPage(
              isLoading: state.isLoading,
              error: state.error,
              items: state.items,
              hasTerm: hasTerm,
              hasReachedMax: state.hasReachedMax,
              endOfListReached: {
                self.vm.dispatch(action: GithubSearchActionLoadNextPage.shared)
              },
              onRetry: {
                self.vm.dispatch(action: GithubSearchActionRetry.shared)
              }
            )
          }
          else if state.items.isEmpty {
            GithubEmptySearch(
              hasTerm: hasTerm
            )
          } else {
            GithubRepoItemsList(
              items: state.items,

              isLoading: state.isLoading,
              error: state.error,
              hasReachedMax: state.hasReachedMax,
              endOfListReached: {
                self.vm.dispatch(action: GithubSearchActionLoadNextPage.shared)
              },
              onRetry: {
                self.vm.dispatch(action: GithubSearchActionRetry.shared)
              }
            )
          }
        }.frame(maxHeight: .infinity)

      }.navigationTitle("Github search KMM")

    }.navigationViewStyle(.stack)
      .onReceive(self.vm.eventPublisher) { event in
      switch event {
      case .searchFailure(let e):
        print(e.appError)
      }
    }
  }
}

extension RepoItem: Identifiable { }

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
