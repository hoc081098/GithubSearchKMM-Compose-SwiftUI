./gradlew :shared:podGenIOS :shared:generateDummyFramework --parallel --stacktrace
cd iosApp || exit
pod install
open iosApp.xcworkspace
