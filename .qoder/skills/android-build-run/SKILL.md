---
name: android-build-run
description: Build and run JellyTunes Android TV app on emulator. Use when the user asks to compile, build, run, install the app, or launch the emulator.
---

# Android Build & Run

Build and run JellyTunes on Android TV emulator.

## Environment

| Variable | Path |
|----------|------|
| JAVA_HOME | `D:\Android\jdk-17.0.9` |
| ANDROID_HOME | `D:\Android\sdk` |
| Gradle | `D:\Android\gradle-8.5` |

## Quick Start

### 1. Check/Start Emulator

List available emulators:
```bash
D:\Android\sdk\emulator\emulator.exe -list-avds
```

Start emulator (use `JellyTunes_TV` for this project):
```bash
D:\Android\sdk\emulator\emulator.exe -avd JellyTunes_TV -no-snapshot-load
```

### 2. Verify Device Connection

```bash
D:\Android\sdk\platform-tools\adb.exe devices
```

Expected output:
```
List of devices attached
emulator-5554    device
```

### 3. Build and Install

Run via PowerShell:
```powershell
$env:JAVA_HOME='D:\Android\jdk-17.0.9'
$env:ANDROID_HOME='D:\Android\sdk'
Set-Location 'D:\Projects\JellyTunes'
& 'D:\Android\gradle-8.5\bin\gradle.bat' installDebug
```

Or as one-liner:
```powershell
powershell -Command "$env:JAVA_HOME='D:\Android\jdk-17.0.9'; $env:ANDROID_HOME='D:\Android\sdk'; Set-Location 'D:\Projects\JellyTunes'; & 'D:\Android\gradle-8.5\bin\gradle.bat' installDebug"
```

### 4. Launch App

```bash
D:\Android\sdk\platform-tools\adb.exe shell am start -n com.jellytunes.tv/.MainActivity
```

## Common Tasks

| Task | Command |
|------|---------|
| Build debug APK | `gradle.bat assembleDebug` |
| Build release APK | `gradle.bat assembleRelease` |
| Install debug | `gradle.bat installDebug` |
| Uninstall | `adb.exe uninstall com.jellytunes.tv` |
| View logs | `adb.exe logcat -s JellyTunes` |
| Clear app data | `adb.exe shell pm clear com.jellytunes.tv` |

## Troubleshooting

### Emulator already running
If you see "Running multiple emulators with the same AVD", the emulator is already running. Skip to step 2.

### Device not found
Wait 15-30 seconds after starting emulator, then check `adb devices` again.

### Build cache issues
```powershell
& 'D:\Android\gradle-8.5\bin\gradle.bat' clean
```
