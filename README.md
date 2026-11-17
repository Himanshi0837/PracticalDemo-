# Movie Explorer & BLE Device Scanner

A modern native Android application built with Kotlin, Jetpack Compose, Clean Architecture + MVVM, Hilt Dependency Injection, and Kotlin Coroutines/Flow. The project features two modular features:
1. **Movie Explorer**: An API-driven movie discovery and tracking module integrated with The Movie Database (TMDB) REST API, featuring a 2-column grid layout, artist search, detailed bottom sheet, and offline local favourites persistence.
2. **BLE Device Scanner**: A Bluetooth Low Energy scanning and proximity tracking module with animated radar waves, 1D Kalman Filter signal smoothing, and a 180° proximity speedometer gauge with real-time distance estimation.

---

## Table of Contents
- [Architecture Explanation](#architecture-explanation)
- [Libraries & Dependencies](#libraries--dependencies)
- [API Configuration Instructions](#api-configuration-instructions)
- [Permissions Required](#permissions-required)
- [Project Setup Instructions](#project-setup-instructions)
- [Key Features](#key-features)
  - [Movie Explorer Module](#movie-explorer-module)
  - [BLE Tracker Module](#ble-tracker-module)
  - [App Theming & Design System](#app-theming--design-system)
- [Known Issues & Limitations](#known-issues--limitations)

---

## Architecture Explanation

The application adheres to **Google-recommended Clean Architecture** combined with the **MVVM (Model-View-ViewModel)** design pattern:

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Jetpack Compose Screens, ViewModels, UI State Flows  │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                      Domain Layer                      │
│   Domain Models, Business Rules, State Abstractions    │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                       Data Layer                       │
│   Repositories, TMDB Remote Service, Local Favourites  │
│   BleScanner, BleGattClient, Kalman Filter, Buffers    │
└────────────────────────────────────────────────────────┘
```

- **Separation of Concerns**: ViewModels manage pure UI state via Kotlin Coroutine `StateFlow` and have no dependencies on Android framework classes (like `BluetoothGatt` or `Activity`).
- **Repository Pattern**: `MovieRepository` coordinates network calls through `safeApiCall` and synchronizes with `FavouriteMoviesStore` for local data persistence.
- **BLE Coordination (`BleManager`)**: Orchestrates the `BleScanner` (advertisement discovery and Kalman RSSI smoothing) and `BleGattClient` (GATT connections and remote RSSI polling), exposing reactive state flows to the UI.
- **Dependency Injection**: Google Hilt injects singletons for network clients, Bluetooth coordinators, and repositories, providing clear module boundaries (`NetworkModule`, `BleModule`).

---

## Libraries & Dependencies

| Library | Version / BOM | Purpose |
| :--- | :--- | :--- |
| **Kotlin** | `2.0.21` | Modern, type-safe programming language |
| **Android Gradle Plugin** | `8.9.0` | Build system |
| **Jetpack Compose BOM** | `2024.09.00` | Declarative UI toolkit |
| **Compose Material 3** | BOM managed | Material Design 3 components and color schemes |
| **Compose Material Icons**| Extended | Icons for media, tracking, categories, and controls |
| **Hilt & Hilt Navigation**| `2.51.1` / `1.2.0` | Dependency injection across viewmodels and singletons |
| **Retrofit & Gson** | `2.11.0` | REST API networking and JSON serialization/deserialization |
| **OkHttp & Logging** | `4.12.0` | HTTP client with Bearer Token auth and logging interceptors |
| **Coil Compose** | `2.7.0` | Asynchronous image loading with crossfade and caching |
| **Kotlinx Coroutines** | `1.8.1` | Asynchronous programming, reactive `StateFlow` and `SharedFlow` |
| **AndroidX Core & Lifecycle**| Latest KTX | Lifecycle-aware state collection and back handlers |

---

## API Configuration Instructions

The Movie Explorer integrates with **The Movie Database (TMDB) v3 REST API**:
- **Base URL**: `https://api.themoviedb.org/3/`
- **Image CDN Base**:
  - Posters (w500): `https://image.tmdb.org/t/p/w500`
  - Backdrops (Original): `https://image.tmdb.org/t/p/original`

### Setting Up Credentials
The credentials are centrally managed in `core/common/Constants.kt`:
```kotlin
object Constants {
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_W500 = "https://image.tmdb.org/t/p/w500"
    const val TMDB_IMAGE_BASE_ORIGINAL = "https://image.tmdb.org/t/p/original"
    const val TMDB_API_KEY = "<YOUR_TMDB_API_KEY>"
    const val TMDB_ACCESS_TOKEN = "<YOUR_TMDB_BEARER_TOKEN>"
}
```

The `ApiClient` automatically injects the Bearer Access Token on every outgoing request via an OkHttp interceptor:
```kotlin
builder.addInterceptor { chain ->
    val request = chain.request().newBuilder()
        .header("Authorization", "Bearer ${Constants.TMDB_ACCESS_TOKEN}")
        .header("Accept", "application/json")
        .build()
    chain.proceed(request)
}
```

---

## Permissions Required

The application handles Android permissions across multiple API tiers (Android 8.0 through Android 14+):

### 1. Network Permissions
- `android.permission.INTERNET`: Required to communicate with the TMDB REST API and load poster images.
- `android.permission.ACCESS_NETWORK_STATE`: Required to monitor network connectivity status.

### 2. Bluetooth Permissions (Android 12+ / API 31+)
- `android.permission.BLUETOOTH_SCAN`: Required to scan for nearby BLE peripherals. Marked with `usesPermissionFlags="neverForLocation"` as scanning is not used to derive user physical location.
- `android.permission.BLUETOOTH_CONNECT`: Required to connect to discovered BLE peripherals, read GATT services, and query remote RSSI.

### 3. Legacy Bluetooth & Location Permissions (Android 11 and lower)
- `android.permission.BLUETOOTH`: Allows connections to paired Bluetooth devices.
- `android.permission.BLUETOOTH_ADMIN`: Allows discovering and pairing Bluetooth devices.
- `android.permission.ACCESS_FINE_LOCATION`: Required by Android 6.0–11 to receive BLE scan advertisement packets.
- `android.permission.ACCESS_COARSE_LOCATION`: Fallback location permission for Android 9 and lower.

The app automatically requests necessary permissions on runtime using `BlePermissionHelper` and degrades gracefully if permissions are denied.

---

## Project Setup Instructions

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer.
- **JDK**: Java 17 (recommended: OpenJDK 17).
- **Android SDK**:
  - `compileSdk`: 35
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk`: 35 (Android 15)

### Steps to Run
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/Exam.git
   cd Exam
   ```
2. **Open in Android Studio**:
   - Open Android Studio, select **Open**, and navigate to the `Exam` directory.
   - Wait for Gradle sync to complete.
3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`
4. **Deploy to Device**:
   - Connect a physical Android device via USB with USB Debugging enabled (BLE functionality requires physical Bluetooth hardware; emulator BLE capabilities are limited).
   - Press **Run** (`Shift + F10`) or install the APK via ADB:
     ```bash
     adb install -r app/build/outputs/apk/debug/app-debug.apk
     ```

---

## Key Features

### Movie Explorer Module
- **Category Filter Chips**: Instant switching between **Trending** (🔥), **Popular** (⭐), **Top Rated** (🏆), **Upcoming** (🎬), and **Favourites** (❤️).
- **2-Column Responsive Grid**: Displays movie posters, bold Montserrat titles, release years, language tags, ratings, and a favourite toggle button.
- **Search with Debounce**: 400ms debounced search with two search modes:
  - **Movies**: Direct title search (`/3/search/movie`).
  - **Artists / Actors**: Actor search (`/3/search/person`), fetching credits (`/3/person/{id}/movie_credits`) to display all movies featuring that actor.
- **Infinite Scroll Pagination**: Automatically triggers next page requests when reaching near the bottom of the grid.
- **Movie Details Modal**: Displays backdrop banners, floating posters, rating breakdown, formatted runtime (`2h 14m`), full synopsis, genre pills, director information, and a horizontal carousel of cast members with profile pictures.
- **Local Persistence**: Favourites are stored locally with `SharedPreferences` and `Gson`, remaining preserved across application restarts.

### BLE Tracker Module
- **Animated Radar Wave Graphic**: Custom Canvas drawing concentric rings and animated pulsing radar waves indicating active scanning.
- **Dual-Feed RSSI Tracking Strategy**: Solves the common BLE issue where peripherals drop idle GATT links by merging GATT polling with continuous background advertisement packet sniffing. The speedometer gauge needle and distance metrics **never freeze**.
- **Signal Smoothing & Distance Estimation**:
  - **1D Kalman Filter**: Mitigates multipath signal reflection and antenna noise.
  - **Moving Window Buffer**: Computes weighted rolling averages of filtered RSSI.
  - **Log-Distance Path Loss Model**: Converts dBm to estimated distance in meters.
- **180° Proximity Speedometer Gauge**: Custom Canvas speedometer with three zones (**Near**, **Closer**, **Far**), separator tick marks, dynamic label highlighting, and a spring-animated needle.
- **Pruning Protection**: Actively tracked devices are protected from being pruned by the out-of-range monitor while being viewed.
- **Graceful Disconnect & Reconnect**: Handles remote peer disconnection without crashes, allowing immediate one-tap reconnection.

### App Theming & Design System
- **Burgundy & Rose Gold Palette** (from `Color.kt`):
  - `PrimaryBlue` (`#21151D` - Deep Burgundy)
  - `SecondaryRose` (`#D47A91` - Rose Accent)
  - `PrimaryContainer` (`#F3E8EC` - Soft Rose)
  - `Wine` (`#39232F` - Deep Wine)
  - `RoseGold` (`#E8C6A8` - Rose Gold)
  - `AppBackground` (`#FAF6F7` - Soft Muted Background)
- **Status Bar Integration**: System status bar color matches `AppBackground` with light-appearance status bar icons (`isAppearanceLightStatusBars = true`).
- **Typography**: Bundled **Montserrat** font family (Regular, Medium, SemiBold, Bold) across all typography styles.
- **Floating Bottom Navigation Bar**: Custom pill-shaped floating navigation bar with spring width animation and smooth tab transitions.

---

## Known Issues & Limitations

1. **Android Emulator Limitations**:
   - The Android Studio emulator does not support real BLE scanning or GATT connections. A physical BLE-capable Android device is required for testing BLE functionality.
2. **Peripheral GATT Connection Timeouts**:
   - Certain commercial BLE peripherals (smartwatches, fitness bands, Apple/Google tags) drop unbonded idle GATT connections after 10–20 seconds to conserve battery. The application handles this gracefully using the dual-feed advertisement tracking strategy, keeping signal metrics active even if GATT terminates.
3. **Hardware MAC Address Masking (Android 12+)**:
   - On Android 12 and above, the platform randomizes or restricts access to the true hardware MAC address in scan results for non-system apps as part of privacy safeguards.
4. **Offline Movie Caching**:
   - Favourites and favourite movie details are completely persistent offline. However, browsing uncached category pages requires an active internet connection to communicate with TMDB servers.

