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

    return NavigationView {
      VStack {
        TextField("Search...", text: $term)
          .onChange(of: term) {
            self.vm.dispatch(action: GithubSearchActionSearch(term: $0))
          }
          .padding()
        
        ZStack(alignment: .center) {
          
          if state.isFirstPage {
            if state.isLoading {
              ProgressView("Loading...")
            }
            else if let error = state.error {
              ErrorMessageAndButton(
                error: error,
                onRetry: {
                  self.vm.dispatch(action: GithubSearchActionRetry.shared)
                }
              )
            }
            else if state.items.isEmpty {
              EmptySearch(
                hasTerm: !state.term
                  .trimmingCharacters(in: .whitespacesAndNewlines)
                  .isEmpty
              )
            }
            else {
              GithubRepoItemsList(
                items: state.items,
                isLoading: false,
                error: nil,
                endOfListReached: {
                  self.vm.dispatch(action: GithubSearchActionLoadNextPage.shared)
                },
                onRetry: {
                  self.vm.dispatch(action: GithubSearchActionRetry.shared)
                }
              )
            }
          }
          else if state.items.isEmpty {
            EmptySearch(
              hasTerm: !state.term
                .trimmingCharacters(in: .whitespacesAndNewlines)
                .isEmpty
            )
          } else {
            GithubRepoItemsList(
              items: state.items,

              isLoading: state.isLoading,
              error: state.error,
              endOfListReached: {
                self.vm.dispatch(action: GithubSearchActionLoadNextPage.shared)
              },
              onRetry: {
                self.vm.dispatch(action: GithubSearchActionRetry.shared)
              }
            )
          }
        }
        .frame(maxHeight: .infinity)
        
      }
        .navigationTitle("Github search KMM")
    }.navigationViewStyle(.stack)
  }
}

struct EmptySearch: View {
  let hasTerm: Bool

  var body: some View {
    if hasTerm {
      Text("Empty results")
        .font(.title3)
        .multilineTextAlignment(.center)
        .padding(10)
    } else {
      Text("Search github repositories...")
        .font(.title3)
        .multilineTextAlignment(.center)
        .padding(10)
    }
  }
}

struct ErrorMessageAndButton: View {
  let error: AppError
  let onRetry: () -> Void
  var font: Font? = nil

  var body: some View {
    VStack(alignment: .center) {
      Text(error.readableMessage)
        .font(font ?? .title3)
        .multilineTextAlignment(.center)
        .padding(10)

      Button("Retry", action: onRetry)
        .buttonStyle(PlainButtonStyle.plain)

      Spacer().frame(height: 10)
    }.frame(maxWidth: .infinity)
  }
}

struct GithubRepoItemsList: View {
  let items: [RepoItem]
  let isLoading: Bool
  let error: AppError?

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
      } else if !items.isEmpty {
        Rectangle()
          .size(width: 0, height: 0)
          .onAppear(perform: endOfListReached)
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
