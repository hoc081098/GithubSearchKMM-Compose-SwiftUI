import SwiftUI
import shared
import Combine
import sharedSwift

struct ContentView: View {
  @StateObject var vm: IOSGithubSearchViewModel

  @State private var showingAlert = false
  @State private var event: GithubSearchSingleEventKs?

  var body: some View {
    let state = self.vm.state
    let hasSubmittedTerm = !state.term
      .trimmingCharacters(in: .whitespacesAndNewlines)
      .isEmpty

    let termBinding = Binding<String>(
      get: { self.vm.term },
      set: { self.vm.dispatch(action: GithubSearchActionSearch(term: $0)) }
    )

    return NavigationView {
      VStack {
        HStack {
          Image(systemName: "magnifyingglass")

          TextField("Search...", text: termBinding)
            .font(.title2)

          Button(action: { self.vm.dispatch(action: GithubSearchActionSearch(term: "")) }) {
            Image(systemName: "xmark.circle.fill")
              .opacity(self.vm.term.isEmpty ? 0 : 1)
          }.foregroundColor(.secondary)
        }.padding()

        if hasSubmittedTerm {
          Text("Search results for '\(state.term)'")
            .font(.subheadline)
        }

        ZStack(alignment: .center) {
          if state.isFirstPage {
            GithubRepoItemsFirstPage(
              isLoading: state.isLoading,
              error: state.error,
              items: state.items,
              hasTerm: hasSubmittedTerm,
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
              hasTerm: hasSubmittedTerm
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

class FakeRepoItemRepository: RepoItemRepository {
  func searchRepoItems(term: String, page: Int32) async throws -> Arrow_coreEither<AppError, NSArray> {
    let items: [RepoItem] = [
        .init(
        id: 0,
        fullName: "Fullname 0",
        language: "Kotlin",
        starCount: 0,
        name: "Name 0",
        repoDescription: "Description 0",
        languageColor: ArgbColor.Companion.shared.parse(hex: "#000000").getOrNull(),
        htmlUrl: "html.com",
        owner: .init(id: 1, username: "username", avatar: "avatar"),
        updatedAt: Kotlinx_datetimeInstant.Companion.shared.fromEpochMilliseconds(epochMilliseconds: 0)
      )
    ]

    return EitherExt.shared.right(value: items) as! Arrow_coreEither<AppError, NSArray>
  }
}

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    let vm = IOSGithubSearchViewModel.init(
      vm: .init(
        searchRepoItemsUseCase: .init(repoItemRepository: FakeRepoItemRepository()),
        savedStateHandle: .init()
      )
    )
    ContentView(vm: vm)
  }
}
