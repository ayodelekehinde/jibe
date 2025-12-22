# Jibe

![Badge-License](https://img.shields.io/badge/License-MIT-blue.svg)
![Badge-Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Desktop-green.svg)
![Badge-Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-purple.svg)
![Badge-Compose](https://img.shields.io/badge/Compose-Multiplatform-blue.svg)

**Jibe** is a modern, cross-platform companion application built with **Kotlin Multiplatform** and **Compose Multiplatform**. It enables seamless peer-to-peer connection between devices (Android and Desktop) functionality.

## Features

-   **Cross-Platform Support**: Runs natively on Android, macOS, Windows, and Linux.
-   **Peer-to-Peer Connection**: Connect devices directly over the local network via TCP sockets.
-   **Extensible Plugin System**: capabilities are modular (e.g., Clipboard sharing).
-   **Modern UI**: Built entirely with Jetpack Compose / Compose Multiplatform.
-   **System Tray Integration**: Unobtrusive desktop experience with tray support.

## Tech Stack

-   **Language**: [Kotlin](https://kotlinlang.org/)
-   **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
-   **Networking**: [Ktor Network](https://ktor.io/) (Raw Sockets)
-   **Concurrency**: Kotlin Coroutines & Flows
-   **Build System**: Gradle Kotlin DSL

## Getting Started

### Prerequisites

-   JDK 17 or higher
-   Android Studio or IntelliJ IDEA
-   Android SDK (for Android builds)

### Installation

**Clone the repository:**
```bash
git clone https://github.com/yourusername/jibe.git
cd jibe
```

### Build & Run

#### Android
Run the app on a connected Android device or emulator:
```bash
./gradlew :composeApp:installDebug
```
Or build the APK:
```bash
./gradlew :composeApp:assembleDebug
```

#### Desktop (JVM)
Run the desktop application:
```bash
./gradlew :composeApp:run
```
Package for distribution (creates DMG/MSI/DEB):
```bash
./gradlew :composeApp:packageReleaseDistributionForCurrentOS
```

## Architecture

The project follows a Clean Architecture approach adapted for Multiplatform:

-   **`commonMain`**: Contains the core business logic, networking code, and shared UI.
-   **`androidMain`**: Android-specific implementations (activities, permissions).
-   **`jvmMain`**: Desktop-specific implementations (system tray, window management).

### Key Components
-   **`ConnectionManager`**: Handles TCP socket connections and state management.
-   **`PluginManager`**: Manage features like clipboard sync.
-   **`KtorTransport`**: Implementation of the custom handshake protocol.

## Contributing

Contributions are welcome! Please follow these steps:

1.  Fork the repository.
2.  Create a feature branch: `git checkout -b feature/my-feature`.
3.  Commit your changes: `git commit -m 'Add some feature'`.
4.  Push to the branch: `git push origin feature/my-feature`.
5.  Open a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.