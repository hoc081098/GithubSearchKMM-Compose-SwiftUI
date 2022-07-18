import SwiftUI
import shared
import Combine
import sharedSwift

struct ContentView: View {
  let greet = Greeting().greeting()

  @ObservedObject var vm = IOSGithubSearchViewModel(vm: DIContainer.shared.get())

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
        Napier.e(error: e.appError.asError(), "searchFailure")
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
