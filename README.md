# Mines

A native Android Mines game built with Kotlin and Jetpack Compose.

This project implements a dark, casino-inspired Mines gameplay loop with a clean MVVM architecture and a pure game engine that is easy to extend.

## Features

- 5x5 Mines board gameplay
- Adjustable bet and mine count
- Cash-out flow with multiplier tracking
- Fake balance demo state for local play
- Jetpack Compose UI with Material 3 theming
- Unit-tested game engine logic

## Tech Stack

- Kotlin
- Jetpack Compose
- Android Gradle Plugin
- JUnit 4

## Project Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/minesgame/
│   │   │   ├── data/
│   │   │   ├── ui/
│   │   │   └── MainActivity.kt
│   │   └── res/
│   └── test/java/com/minesgame/data/engine/
└── build.gradle.kts
```

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

## Running the app

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync.
4. Choose an emulator or connected device.
5. Run the `app` configuration.

## Building from the command line

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Notes

This version is a demo implementation with local game state and a backend seam designed for future real-money or server-authoritative integration.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
