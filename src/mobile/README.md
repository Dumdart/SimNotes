# SimNotes Mobile

Android and iOS client for SimNotes, built with Kotlin Multiplatform and Compose Multiplatform. Both apps use the UI and Kotlin code in `shared`.

Create notebooks and write or edit multiline notes. Checking a note moves it to that notebook’s **⋮ → View Done** menu. Choose **Restore** to return it to its original place. SimNotes saves changes in app-private storage and works offline from the first launch.

The shared interface bundles [Comic Neue](https://github.com/crozynski/comicneue) under the [SIL Open Font License](shared/src/commonMain/composeResources/files/comic_neue_ofl.txt).

## Project layout

| Directory | Purpose |
| --- | --- |
| `androidApp/` | Android application, manifest, and launcher resources |
| `iosApp/` | SwiftUI entry point, Xcode project, and iOS configuration |
| `shared/src/commonMain/` | Shared Compose UI, resources, and Kotlin logic |
| `shared/src/androidMain/` | Android platform implementation |
| `shared/src/iosMain/` | iOS platform implementation and Compose view controller |
| `shared/src/commonTest/` | Tests shared between platforms |
| `shared/src/androidHostTest/` | Android host tests that run on the JVM |
| `shared/src/iosTest/` | iOS tests |
| `gradle/` | Dependency version catalog, wrapper, and JVM configuration |

## Setup

Open `src/mobile` as the Gradle project in Android Studio. Run all commands below from this directory.

- Use JDK 21 for Gradle, as configured in `gradle/gradle-daemon-jvm.properties`.
- Install Android SDK Platform 37. The Android app requires API 29 or later.
- Let Android Studio configure the SDK path, or set `sdk.dir` in a local `local.properties` file. This file is ignored by Git.
- Building and testing iOS requires macOS and Xcode. The Xcode project targets iOS 18.2 or later; Kotlin targets are `iosArm64` and `iosSimulatorArm64`.

Use the included Gradle wrapper. Dependency and plugin versions are defined in `gradle/libs.versions.toml`.

## Run Android

Select the `androidApp` run configuration in Android Studio and choose an emulator or connected device.

To build a debug APK from PowerShell:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

The APK is written to `androidApp/build/outputs/apk/debug/`. To build and install it on a running emulator or connected device:

```powershell
.\gradlew.bat :androidApp:installDebug
```

On macOS or Linux, replace `.\gradlew.bat` with `./gradlew`.

## Run iOS

On a Mac, open `iosApp/iosApp.xcodeproj` in Xcode, select the `iosApp` scheme, choose an iPhone simulator, and run the app.

Xcode's build phase invokes `:shared:embedAndSignAppleFrameworkForXcode` to build and embed the shared `Shared` framework. For a physical device, configure your signing team through `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` and check the target's Signing & Capabilities settings.

## Tests

Run the Android host tests, including the common tests:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

Run the project checks:

```powershell
.\gradlew.bat check
```

On an Apple Silicon Mac, run the iOS simulator tests:

```sh
./gradlew :shared:iosSimulatorArm64Test
```

The common tests cover creating, editing, completing, and restoring notes. Each platform also has a test that reopens a persisted notebook file. Running the iOS simulator tests requires macOS and Xcode.

## Python API

The API is maintained separately in `../api`. See the [repository README](../../README.md) for API setup and tests.
