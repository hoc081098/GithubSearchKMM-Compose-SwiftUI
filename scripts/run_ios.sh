./gradlew :shared:podGenIOS --parallel
./gradlew kSwiftsharedPodspec
cd iosApp || exit
pod install
open iosApp.xcworkspace
