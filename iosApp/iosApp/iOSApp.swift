import SwiftUI
import shared

@main
struct iOSApp: App {
  init() {
    DIContainer.shared.doInit { _ in
}
  }
  
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}

extension DIContainer {
  func get<T>(
    for type: T.Type = T.self,
    qualifier: Koin_coreQualifier? = nil,
    parameters: (() -> Koin_coreParametersHolder)? = nil
  ) -> T {
    self.get(
      type: type,
      qualifier: qualifier,
      parameters: parameters
    ) as! T
  }
}
