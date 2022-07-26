import SwiftUI
import shared
import Combine
import sharedSwift

struct ContentView: View {
  @ObservedObject var vm = IOSGithubSearchViewModel(vm: DIContainer.shared.get())

  @State private var term: String = ""

  @State private var showingAlert = false
  @State private var event: GithubSearchSingleEventKs?

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

    }
      .navigationViewStyle(.stack)
      .onReceive(self.vm.eventPublisher) { event in
        self.event = event
        self.showingAlert = true
      }
      .alert(isPresented: $showingAlert, content: eventAlert)
  }

  private func eventAlert() -> Alert {
    switch self.event {
    case .reachedMaxItems:
      return Alert(
        title: Text("Reached max items"),
        message: Text("Loaded all items!"),
        dismissButton: .default(Text("OK"))
      )
    case .searchFailure(let e):
      return Alert(
        title: Text("Error"),
        message: Text(e.appError.readableMessage),
        dismissButton: .default(Text("OK"))
      )
    case nil:
      Napier.e("\(self).event is nil")

      return Alert(
        title: Text("Error"),
        message: Text("Unexpected error occurred!"),
        dismissButton: .default(Text("OK"))
      )
    }
  }

}

extension RepoItem: Identifiable { }

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
