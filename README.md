# Pedometer++ Native (iOS & Android)

A complete, high-performance, battery-efficient recreation of the iconic **Pedometer++** app for both **iOS** (Swift & SwiftUI) and **Android** (Kotlin & Jetpack Compose).

---

## 📱 Features

1. **Daily Step Tracking & Goals**:
   - Circular progress ring displaying live step count against customizable daily goals (default: 10,000 steps).
   - Distance (km/miles), Floors Climbed / Elevation gain, Active Calories, and Active Walking Time.
2. **Iconic Pedometer++ Bar Charts**:
   - Interactive 7-day, 30-day, and yearly bar charts color-coded by goal achievement (Green: Goal Met, Orange: In Progress, Gray: Rest Day).
   - Goal indicator line, average indicators, and day selection details.
3. **GPS Route & Outdoor Workouts**:
   - Real-time GPS workout tracking (Walk, Run, Hike) with live map route drawing, pace calculation, distance, and elevation splits.
4. **Badges, Streaks & Gamification**:
   - Goal streak tracker (consecutive days hit).
   - Lifetime milestone badges (10k Daily, 100k Steps, Marathon 42.2km, Everest 8,848m, etc.).
5. **Themes & Customization**:
   - Dark Mode, OLED Black, and Light Mode.
   - Custom accent colors (Pedometer Classic Green, Cyber Orange, Electric Blue, Neon Purple).
   - Metric / Imperial unit switching.
6. **Native Hardware Sensors & Battery Efficiency**:
   - **iOS**: Uses `CMPedometer` (Apple Motion Coprocessor) + `HealthKit` sync for zero battery drain.
   - **Android**: Uses hardware `Sensor.TYPE_STEP_COUNTER` + Foreground Service + Room DB + Accelerometer fallback.

---

## 📂 Project Structure

```
d:/pedometer/
├── ios/                      # iOS Native Xcode Project (Swift & SwiftUI)
│   ├── PedometerApp.xcodeproj
│   ├── PedometerApp/
│   │   ├── App/              # App entry point & App lifecycle
│   │   ├── Models/           # StepData, Workout, Badge, Goal models
│   │   ├── Services/         # PedometerManager (CMPedometer), HealthKitManager, LocationManager
│   │   ├── ViewModels/       # DashboardViewModel, WorkoutViewModel, BadgesViewModel, SettingsViewModel
│   │   ├── Views/            # SwiftUI Views (Dashboard, Charts, Workouts, Badges, Settings)
│   │   ├── Components/       # StepRingView, DailyBarChart, MetricCard, MapView
│   │   └── Resources/        # Assets.xcassets, Info.plist
├── android/                  # Android Native Studio Project (Kotlin & Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/pedometer/app/
│   │   │   │   ├── data/       # Room Database, DAOs, Entities, Preferences
│   │   │   │   ├── service/    # PedometerForegroundService, StepSensorManager, BootReceiver
│   │   │   │   ├── ui/         # Jetpack Compose Screens, Components, Theme
│   │   │   │   └── viewmodel/  # MainViewModel, WorkoutViewModel, BadgesViewModel
│   │   │   └── res/            # Drawables, strings, icons, themes
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
└── README.md
```

---

## 🚀 How to Run

### iOS (Xcode)
1. Open `ios/PedometerApp.xcodeproj` in **Xcode 15+**.
2. Select your target device or iOS Simulator (iOS 16+ or iOS 17+ recommended).
3. Ensure Motion & Fitness permissions are enabled in Simulator (`Debug -> Simulate Location / Motion`).
4. Click **Run** (`Cmd + R`).

### Android (Android Studio)
1. Open the `android/` directory in **Android Studio Hedgehog / Iguana / Jellyfish (2023.2+)**.
2. Allow Gradle sync to complete (JDK 17+).
3. Connect an Android device (API 26+) or launch an Android Virtual Device (AVD).
4. Click **Run** (`Shift + F10`).
