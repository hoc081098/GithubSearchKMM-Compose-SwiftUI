# GithubSearchKMM

Github Repos Search - Kotlin Multiplatform Mobile using Jetpack Compose, SwiftUI,
 FlowRedux, Coroutines Flow, Dagger Hilt, Koin Dependency Injection

[![Android Build CI](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/build.yml)
[![iOS Build CI](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/ios-build.yml/badge.svg)](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/ios-build.yml)
[![Validate Gradle Wrapper](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/hoc081098/GithubSearchKMM/actions/workflows/gradle-wrapper-validation.yml)

Minimal **Kotlin Multiplatform** project with SwiftUI, Jetpack Compose.
 - Android (Jetpack compose)
 - iOS (SwiftUI)

### Modern Development
 - Kotlin Multiplatform
 - Jetpack Compose
 - Kotlin Coroutines & Flows
 - Dagger Hilt
 - SwiftUI
 - Koin Dependency Injection
 - FlowRedux State Management

## Tech Stacks
 - Functional & Reactive programming with **Kotlin Coroutines with Flow**
 - **Clean Architecture** with **MVI** (Uni-directional data flow)
 - **Multiplatform ViewModel**
 - **Multiplatform FlowRedux** State Management
 - [**Λrrow** - Functional companion to Kotlin's Standard Library](https://arrow-kt.io/)
   - [Either](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/)
   - [Monad Comprehensions](https://arrow-kt.io/docs/patterns/monad_comprehensions/)
   - [Option](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-option/)
   - [parZip](https://arrow-kt.io/docs/apidocs/arrow-fx-coroutines/arrow.fx.coroutines/par-zip.html)
 - Dependency injection
   - iOS: [**Koin**](https://insert-koin.io/)
   - Android: [**Dagger Hilt**](https://dagger.dev/hilt/)
 - Declarative UI
   - iOS: [**SwiftUI**](https://developer.apple.com/xcode/swiftui/)
   - Android: [**Jetpack Compose**](https://developer.android.com/jetpack/compose)
 - [Ktor client library](https://ktor.io/docs/getting-started-ktor-client-multiplatform-mobile.html) for networking
 - [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON serialization/deserialization.
 - [Napier](https://github.com/AAkira/Napier) for Multiplatform Logging.
 - [FlowExt](https://github.com/hoc081098/FlowExt).
 - [MOKO KSwift](https://github.com/icerockdev/moko-kswift) is a gradle plugin for generation Swift-friendly API for Kotlin/Native framework.
 - [kotlinx.collections.immutable](https://github.com/Kotlin/kotlinx.collections.immutable): immutable collection interfaces and implementation prototypes for Kotlin..

## Overall Architecture

### What is shared?
 - **domain**: Domain models, UseCases, Repositories.
 - **presentation**: ViewModels, ViewState, ViewSingleEvent, ViewAction.
 - **data**: Repository Implementations, Remote Data Source, Local Data Source.
 - **utils**: Utilities, Logging Library

### Unidirectional data flow - FlowRedux

 - My implementation. **Credits: [freeletics/FlowRedux](https://github.com/freeletics/FlowRedux)**
 - See more docs and concepts at [freeletics/RxRedux](https://github.com/freeletics/RxRedux)

<p align="center">
    <img src="https://raw.githubusercontent.com/freeletics/RxRedux/master/docs/rxredux.png" width="600" alt="RxRedux In a Nutshell"/>
</p>

```kotlin
public sealed interface FlowReduxStore<Action, State> {
  public val coroutineScope: CoroutineScope

  public val stateFlow: StateFlow<State>

  /** Get streams of actions.
   *
   * This [Flow] includes dispatched [Action]s (via [dispatch] function)
   * and [Action]s returned from [SideEffect]s.
   */
  public val actionSharedFlow: SharedFlow<Action>

  /**
   * @return false if cannot dispatch action ([coroutineScope] was cancelled).
   */
  public fun dispatch(action: Action): Boolean
}
```

### Multiplatform ViewModel
```kotlin
open class GithubSearchViewModel(
  searchRepoItemsUseCase: SearchRepoItemsUseCase,
) : ViewModel() {
  private val store = viewModelScope.createFlowReduxStore(
    initialState = GithubSearchState.initial(),
    sideEffects = GithubSearchSideEffects(
      searchRepoItemsUseCase = searchRepoItemsUseCase,
    ).sideEffects,
    reducer = { state, action -> action.reduce(state) }
  )
  private val eventChannel = store.actionSharedFlow
    .mapNotNull { it.toGithubSearchSingleEventOrNull() }
    .buffer(Channel.UNLIMITED)
    .produceIn(viewModelScope)

  fun dispatch(action: GithubSearchAction) = store.dispatch(action)
  val stateFlow: StateFlow<GithubSearchState> by store::stateFlow
  val eventFlow: Flow<GithubSearchSingleEvent> get() = eventChannel.receiveAsFlow()
}
```

### Plaform ViewModel

#### Android

Extends `GithubSearchViewModel` to use `Dagger Constructor Injection`.

```kotlin
@HiltViewModel
class DaggerGithubSearchViewModel @Inject constructor(searchRepoItemsUseCase: SearchRepoItemsUseCase) :
  GithubSearchViewModel(searchRepoItemsUseCase)
```

#### iOS

Conform to `ObservableObject` and use `@Published` property wrapper.

```swift
import Foundation
import Combine
import shared
import sharedSwift

@MainActor
class IOSGithubSearchViewModel: ObservableObject {
  private let vm: GithubSearchViewModel

  @Published private(set) var state: GithubSearchState
  let eventPublisher: AnyPublisher<GithubSearchSingleEventKs, Never>

  init(vm: GithubSearchViewModel) {
    self.vm = vm

    self.eventPublisher = vm.eventFlow.asNonNullPublisher()
      .assertNoFailure()
      .map(GithubSearchSingleEventKs.init)
      .eraseToAnyPublisher()

    self.state = vm.stateFlow.typedValue()
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
    Napier.d("\(self)::deinit")
    vm.clear()
  }
}
```

## Download APK

- [Download latest debug APK here](https://nightly.link/hoc081098/ComicReaderApp_MVI_Coroutine_RxKotlin_Jetpack/workflows/build/master/app-debug.zip)

# Building & Develop

- `Android Studio Chipmunk | 2021.2.1` (note: **Java 11 is now the minimum version required**).
- `XCode 13.2` or later (due to use of new Swift 5.5 concurrency APIs).
- Clone project: `git clone https://github.com/hoc081098/GithubSearchKMM.git`
- Android: open project by `Android Studio` and run as usual.
- iOS
  ```shell
  cd GithubSearchKMM

  # generate podspec files for cocopods intergration. with integration will be generated swift files for cocoapod
  ./gradlew kSwiftsharedPodspec

  # go to ios dir
  cd iosApp

  # install pods
  pod install

  # now we can open xcworkspace and build ios project
  # or open via XCode GUI
  open iosApp.xcworkspace
  ```

  There's a *Build Phase* script that will do the magic. 🧞 <br>
  <kbd>Cmd</kbd> + <kbd>B</kbd> to build
  <br>
  <kbd>Cmd</kbd> + <kbd>R</kbd> to run.

# Screenshots

|                         |                         |                         |                         |
|        :---:            |          :---:          |        :---:            |          :---:          |
| ![](screenshots/1.jpeg) | ![](screenshots/2.jpeg) | ![](screenshots/3.jpeg) | ![](screenshots/4.jpeg) |
| ![](screenshots/5.jpeg) | ![](screenshots/6.jpeg) | ![](screenshots/7.jpeg) | ![](screenshots/8.jpeg) |
| ![](screenshots/9.jpeg) | ![](screenshots/10.png) | ![](screenshots/11.png) | ![](screenshots/12.png) |
| ![](screenshots/13.png) | ![](screenshots/14.png) | ![](screenshots/15.png) |                         |

# LOC

```sh
--------------------------------------------------------------------------------
 Language             Files        Lines        Blank      Comment         Code
--------------------------------------------------------------------------------
 Kotlin                 165        15406         1777          644        12985
 XML                     95         5181          464           81         4636
 Prolog                   7          127           18            0          109
 JSON                     2          103            0            0          103
 Markdown                 2          109           24            0           85
 Batch                    1           89           21            0           68
--------------------------------------------------------------------------------
 Total                  272        21015         2304          725        17986
--------------------------------------------------------------------------------
```
