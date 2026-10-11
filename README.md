# Morning Routine (Lockscreen Routine Overlay)

An Android application built with Kotlin and Jetpack Compose that places an interactive morning routine diagram directly over the system lockscreen.

The visual layout is inspired by hand-drawn sketches from Excalidraw, and the original concept board was generated using Gemini.

---

## Downloads

Prebuilt APKs are automatically compiled and published through GitHub Actions.

You can download the latest installable file from the **[Releases](https://github.com/del0x3/morning-plan/releases)** tab.

---

## Visual Comparison & Demo

| Gemini Reference Board | Clean Lockscreen (English) | Completed Tasks |
| :---: | :---: | :---: |
| ![Gemini Reference](assets/reference_gemini.jpg) | ![Fresh Lockscreen](assets/screenshot_fresh.png) | ![Checked Tasks](assets/screenshot_checked.png) |

### Demonstration Video
A video recording from a physical Samsung Galaxy device is included in the repository:
[Watch demo.mp4](assets/demo.mp4)

---

## How It Works

1. **Background Service:** `LockscreenService` runs as a foreground service monitoring display power state (`ACTION_SCREEN_OFF` and `ACTION_SCREEN_ON`).
2. **Lockscreen Overlay:** When the phone screen turns on, `LockscreenActivity` presents the routine diagram immediately on top of the keyguard using `showWhenLocked`, `turnScreenOn`, and full immersive flags that hide the system clock and status bars.
3. **Task Completion:**
   - Tapping an item marks it as completed: the card transitions to green and displays a checkmark.
   - Once marked complete, tasks cannot be accidentally unchecked with a single tap.
   - To reset all tasks for the next day, press and hold the reset control in the top-right corner for 3 seconds.
4. **Unlocking:** Swiping up dismisses the activity and seamlessly prompts the standard Samsung One UI PIN or fingerprint scanner.
5. **Localization:** Tap the language toggle in the top-left corner to switch between English and Russian.

---

## Technical Stack

- Kotlin 2.0+
- Jetpack Compose (Material 3)
- Android Gradle Plugin 8.7+
- Android SDK 35 (Android 15 / 14 support)
- Canvas-based custom arrow renderer
- SharedPreferences for persistent daily progress

---

## Building from Source

Prerequisites:
- Android SDK (API 35, Build-tools 35.0.0)
- JDK 17

Build the debug APK:
```bash
./gradlew assembleDebug
```

Output path:
`app/build/outputs/apk/debug/app-debug.apk`

Install directly via ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Setup on Samsung Devices (One UI)

1. Launch the app once to start the service.
2. Grant "Display over other apps" (Settings -> Apps -> Special app access -> Display over other apps -> Morning Routine).
3. Set Battery usage to "Unrestricted" (Settings -> Apps -> Morning Routine -> Battery -> Unrestricted) so One UI does not put the background service to sleep.

---

## Roadmap

- Direct import of board layouts from Excalidraw JSON files and live URLs.
- Dynamic node and arrow creation directly in the app.
- Cross-platform support for tablets, desktop, and other mobile ecosystems.
