import SwiftUI
import shared

@main
struct iOSApp: App {
  init() {
    DIContainer.shared.doInit { _ in }
  }

  var body: some Scene {
    WindowGroup {
      ContentView()
    }
  }
}
