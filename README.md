# Morning Routine (Lockscreen Routine Overlay)

An Android application built with Kotlin and Jetpack Compose that places an interactive morning routine diagram directly over the system lockscreen.

The layout is inspired by hand-drawn sketches from Excalidraw, and the original concept board was generated using Gemini.

---

## Downloads

Official release builds are signed and published through GitHub Actions.

You can download the latest installable APK from the **[Releases](https://github.com/del0x3/morning-plan/releases)** tab.

Direct link to the latest APK:  
[app-release.apk (v1.1.0)](https://github.com/del0x3/morning-plan/releases/latest/download/app-release.apk)

---

## Visual Comparison & Demo

| Gemini Reference Board | Clean Lockscreen (English) | Completed Tasks |
| :---: | :---: | :---: |
| ![Gemini Reference](assets/reference_gemini.jpg) | ![Fresh Lockscreen](assets/screenshot_fresh.png) | ![Checked Tasks](assets/screenshot_checked.png) |

### Demonstration Video
A video recording captured on a physical Samsung Galaxy device demonstrating the hold-to-fill interaction and swipe-to-unlock gesture:
[Watch demo.mp4](assets/demo.mp4)

---

## How It Works

1. **Background Monitoring:** `LockscreenService` runs as a sticky foreground service monitoring device display power states (`ACTION_SCREEN_OFF` and `ACTION_SCREEN_ON`).
2. **Lockscreen Overlay:** When the phone display turns on, `LockscreenActivity` presents the routine diagram immediately on top of the keyguard using `showWhenLocked`, `turnScreenOn`, and full immersive flags that hide the system clock and status bars.
3. **Hold-to-Fill Confirmation:**
   - To prevent accidental misclicks when waking up, each task requires holding the card for 650ms.
   - An animated green progress fill sweeps across the card while held, accompanied by haptic feedback, before locking into a checkmark (`✓`).
   - Once completed, tasks remain locked against single taps. To reset a completed task, long-press it.
4. **Unlocking:** Swiping up dismisses the activity and seamlessly prompts the standard system PIN or biometric fingerprint prompt.

---

## Technical Stack

- Kotlin 2.0+
- Jetpack Compose (Material 3)
- Android Gradle Plugin 8.7+
- Android SDK 35 (Android 15 / 14 support)
- Kalam handwritten typography
- Canvas-based custom arrow and sketchy card renderer
- SharedPreferences for persistent daily progress

---

## Building from Source

Prerequisites:
- Android SDK (API 35, Build-tools 35.0.0)
- JDK 17

Build the release APK:
```bash
./gradlew assembleRelease
```

Output path:
`app/build/outputs/apk/release/app-release.apk`

Install directly via ADB:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## Setup on Samsung Devices (One UI)

1. Launch the app once to register and start the foreground service.
2. Grant "Display over other apps" (Settings -> Apps -> Special app access -> Display over other apps -> Morning Routine).
3. Set Battery usage to "Unrestricted" (Settings -> Apps -> Morning Routine -> Battery -> Unrestricted) so One UI does not pause the service in deep sleep.

---

## Roadmap

- Direct import of board layouts from Excalidraw JSON files and live URLs.
- Dynamic node and arrow creation directly in the app.
- Multi-platform expansion: release and native support for all devices, including tablets, desktops (macOS, Windows, Linux), and other mobile ecosystems.
