# GithubSearchKMM

Github Repos Search - Kotlin Multiplatform Mobile

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

## Download APK

- [Download latest debug APK here](https://nightly.link/hoc081098/ComicReaderApp_MVI_Coroutine_RxKotlin_Jetpack/workflows/build/master/app-debug.zip)
- [Download latest release APK here](https://nightly.link/hoc081098/ComicReaderApp_MVI_Coroutine_RxKotlin_Jetpack/actions/runs/1360316687/app-release.zip)

# Develop
- You must use **Android Studio Arctic Fox (2020.3.1)** (**note: Java 11 is now the minimum version required**)
- Clone: `git clone https://github.com/hoc081098/ComicReaderApp_MVI_Coroutine_RxKotlin.git`
- _Optional: **Delete `.idea` folder** if cannot open project_
- Open project by `Android Studio` and run as usual

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
