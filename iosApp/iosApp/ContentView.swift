import SwiftUI
import shared

struct ContentView: View {
  let greet = Greeting().greeting()
  let searchRepoItemsUseCase = DIContainer.shared.get(for: SearchRepoItemsUseCase.self)
  var vm: GithubSearchViewModel? = nil

  var body: some View {
    Text(greet)
      .onAppear {
        self.vm = DIContainer.shared.get(for: GithubSearchViewModel.self)

      searchRepoItemsUseCase.invoke(term: "kmm", page: 1) { either, _ in
        either!.fold(
          ifLeft: { error in
            print("searchRepoItemsUseCase: error=\(error!)")
            return ()
          },
          ifRight: {
            let items = $0 as! [RepoItem]
            print("searchRepoItemsUseCase: items=\(items)")
            return ()
          }
        )
      }

    }
  }
}

struct ContentView_Previews: PreviewProvider {
  static var previews: some View {
    ContentView()
  }
}
