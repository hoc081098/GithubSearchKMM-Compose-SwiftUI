import SwiftUI
import shared

@main
struct iOSApp: App {
  init() {
    KoinHelper.shared.doInitKoin { _ in }
  }
  
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
