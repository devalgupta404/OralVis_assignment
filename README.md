# OralVis - Oral Health Imaging System

A modern Android application built with Jetpack Compose for capturing, managing, and reviewing oral health images. This app provides a streamlined workflow for dental professionals to document patient oral conditions through high-quality image capture and organized session management.

## Features

- **📸 Camera Integration**: Real-time camera preview with high-quality image capture
- **🏥 Session Management**: Organize images by patient sessions with metadata
- **🔍 Search & Browse**: Easily find and review previous imaging sessions
- **👤 Patient Information**: Store patient details (name, age) for each session
- **🖼️ Full-Screen Viewer**: View captured images in full-screen mode
- **🌙 Dark Theme**: Modern Material 3 design with dark theme support
- **📱 Responsive UI**: Optimized for various screen sizes and orientations

## Demo Video
https://drive.google.com/drive/folders/1U9hyQiFfaDMKNurKF3y35j0UCtq5xmMQ?usp=drive_link

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Compose
- **Camera**: CameraX
- **Image Loading**: Coil
- **Navigation**: Navigation Compose
- **Theme**: Material 3 Design System

## Prerequisites

Before running this project, make sure you have:

- **Android Studio** (latest version recommended)
- **Android SDK** (API level 24 or higher)
- **Kotlin** (1.9.0 or higher)
- **Gradle** (8.0 or higher)
- **Android Device/Emulator** with API level 24+

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd oralvis
```

### 2. Open in Android Studio

- Launch Android Studio
- Select "Open an existing project"
- Navigate to the `oralvis` folder and open it

### 3. Sync Project

- Wait for Gradle sync to complete
- If prompted, update any dependencies

### 4. Run the App

- Connect an Android device or start an emulator
- Click the "Run" button (green play icon) in Android Studio
- Select your target device and click "OK"

### Alternative: Build APK

```bash
# For debug APK
./gradlew assembleDebug

# For release APK
./gradlew assembleRelease
```

The APK will be generated in:

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Project Structure

```
app/src/main/java/com/example/oralvis/
├── MainActivity.kt                 # Main activity with navigation setup
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt          # Welcome screen with session start
│   │   ├── CaptureScreen.kt       # Camera interface for image capture
│   │   ├── EndSessionScreen.kt    # Session review and patient info
│   │   └── SearchScreen.kt        # Browse and search previous sessions
│   └── theme/
│       ├── Color.kt               # Color definitions
│       ├── Theme.kt               # Material 3 theme setup
│       └── Type.kt                # Typography definitions
└── utils/
    └── MediaStoreUtils.kt         # Image storage and metadata utilities
```

## Dependencies

### Core Dependencies

```gradle
// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Camera
implementation("androidx.camera:camera-core:1.3.4")
implementation("androidx.camera:camera-camera2:1.3.4")
implementation("androidx.camera:camera-lifecycle:1.3.4")
implementation("androidx.camera:camera-view:1.3.4")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## Usage Guide

### Starting a New Session

1. Open the app and tap "Start New Session"
2. Grant camera permissions when prompted
3. Use the capture button to take oral images
4. Tap "End Session" when finished

### Adding Patient Information

1. After ending a session, you'll be prompted for patient details
2. Enter patient name (minimum 2 characters)
3. Enter patient age (1-120 years)
4. Tap "Save & Complete"

### Viewing Previous Sessions

1. Navigate to "Sessions" using the bottom navigation
2. Browse through your previous sessions
3. Use the search bar to find specific sessions or patients
4. Tap on any session to view captured images

### Viewing Images

1. In the session details screen, tap on any image
2. View the image in full-screen mode
3. Tap the close button to return to the session view

## Permissions

The app requires the following permissions:

- **Camera**: For capturing oral images
- **Storage**: For saving and accessing captured images
- **Media**: For organizing images in the device gallery

## Architecture Overview

This app follows modern Android development practices:

- **Single Activity Architecture**: Uses one MainActivity with Compose navigation
- **Composable UI**: All UI is built with Jetpack Compose
- **Material 3 Design**: Implements the latest Material Design guidelines
- **CameraX Integration**: Uses the modern CameraX library for camera functionality
- **MediaStore API**: Leverages Android's MediaStore for image storage

## Key Features Implementation

### Camera Integration

- Real-time preview using CameraX Preview use case
- High-quality image capture with ImageCapture use case
- Automatic lifecycle management
- Permission handling

### Image Storage

- Images saved to organized folder structure: `DCIM/OralVis/Sessions/{sessionId}/`
- Metadata stored using SharedPreferences
- Fallback storage in app's external files directory

### Session Management

- Unique session IDs generated using timestamps
- Patient metadata (name, age) stored per session
- Search functionality across sessions and patient names

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/your-repo/issues) page
2. Create a new issue with detailed information
3. Include device information and steps to reproduce

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Camera functionality powered by [CameraX](https://developer.android.com/training/camerax)
- UI design following [Material 3](https://m3.material.io/) guidelines
- Image loading handled by [Coil](https://coil-kt.github.io/coil/)

---

**Note**: This app is designed for educational and professional use. Always ensure proper patient consent and follow healthcare privacy regulations when using this application in clinical settings.
