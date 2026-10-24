# GhostOps TV OS (Tier 1 — Universal Smart TV Takeover)

[![Android TV](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Google%20TV-3b82f6.svg)](https://ghostops.live)
[![Zero Root](https://img.shields.io/badge/Root-Zero%20Root%20Required-10b981.svg)](https://ghostops.live)
[![Resolution](https://img.shields.io/badge/Display-4K%20HDR%20%2F%20OLED-8b5cf6.svg)](https://ghostops.live)
[![GhostOps Stream](https://img.shields.io/badge/Cinema-GhostOps%204K%20API-06b6d4.svg)](https://ghostops.live)

**GhostOps TV OS** is a native, zero-root Smart TV operating takeover system designed to transform any retail Android TV or Google TV (Sony, TCL, Xiaomi, Hisense, OnePlus, Nvidia Shield, Chromecast) into an ad-free, high-performance cinema hub.

---

## 🌟 Key Capabilities

1. **Ad-Free OLED Black Interface**:
   - Deep pitch-black background (`#07070b`) tailored for OLED and Mini-LED displays.
   - Iridescent glowing GhostOps logo with ambient sheen and high-visibility status bar.
   - Smooth 1.08x physical card scaling with dynamic glass highlights on D-pad focus.

2. **Integrated GhostOps Cinema Rails**:
   - **Trending Now**: Live 4K blockbuster movies & trending series.
   - **Critically Acclaimed Masterpieces**: Handpicked cinematic epics with dynamic backdrop spotlighting.
   - **Anime Universe**: Curated top-ranked anime series in 4K remasters.
   - **Spotlight Hero Banner**: Instant movie synopsis, runtime, maturity rating, and single-click "Watch in 4K" playback.

3. **Native Media3 / ExoPlayer 4K Player**:
   - Zero webview overhead: uses Android's native hardware-accelerated media decoders (HEVC, AVC, AV1, VP9).
   - Multi-server failover extraction using GhostOps API (`https://ghostops.live/api/stream`).
   - Seamless remote D-pad controls:
     - `OK` / `Enter`: Play / Pause toggle
     - `◀` / `▶`: 10-second seek backward / forward with visual scrub HUD
     - `▼`: Instant server failover switch (Sirius 4K, DarkMatter, Quasar)
     - `▲`: Toggle video metadata HUD
     - `Back`: Instant return to TV Home

4. **All Installed TV Apps Dock**:
   - Auto-discovers and launches all installed Android TV apps (YouTube, Netflix, Prime Video, JioCinema, Hotstar, SmartTube, Spotify, VLC, Plex).
   - Fast D-pad navigation across all apps.

5. **Hardware TV Input Switcher**:
   - Native integration with `android.media.tv.TvInputManager`.
   - Dedicated "Inputs" pill in top status bar: instant switching between HDMI 1, HDMI 2, HDMI 3, HDMI 4, Composite AV, and Live TV Tuner.
   - TV hardware calibration and system sound/display controls 100% preserved.

6. **1-Click Wi-Fi ADB Setup & Debloater**:
   - Automated setup over local Wi-Fi without needing USB cables or root.
   - Debloats manufacturer sponsored ads and tracking (PatchWall, Mi Promotion, TCL Waterfall, Sony Samba TV).
   - Generates full system state backup before changing anything.
   - Instant 10-second factory restore script included.

---

## 🚀 Quick Start Guide

### Step 1: Enable Wi-Fi Debugging on your Smart TV
1. On your TV, navigate to **Settings** → **Device Preferences / System** → **About**.
2. Scroll to **Build** and press **OK 7 times** until you see *"You are now a developer!"*.
3. Go back to **Developer Options** and turn **ON**:
   - **USB Debugging**
   - **Wireless / Network Debugging** (if present)
4. Note your TV's IP address under **Settings** → **Network & Internet** (e.g. `192.168.1.150`).

### Step 2: Run the GhostOps TV Setup Wizard
On your computer (connected to the same Wi-Fi network as the TV):
```powershell
python tools/setup_ghostops_tv.py
```
1. Enter your TV's IP address when prompted.
2. Look at your TV screen and accept the popup prompt: *"Always allow from this computer"*.
3. The wizard will automatically:
   - Create a safety backup of your TV's state in `backups/`.
   - Debloat manufacturer ad rows.
   - Install `GhostOpsTV.apk`.
   - Set GhostOps TV as the default Home screen.
   - Capture and pull a verification screenshot into `shots/`.

---

## ⏪ Factory Restore / Safety Undo
If you ever want to return your TV to the original factory launcher:
```powershell
python tools/restore_stock_tv.py
```
This re-enables all manufacturer services, clears the default home activity, and restores your TV to factory stock state in under 10 seconds.

---

## 🛠️ Building the APK from Source

The project includes the full native Android TV Launcher source code in `GhostOpsLauncher/`.

To build the APK:
```powershell
cd GhostOpsLauncher
.\gradlew.bat assembleDebug
```
The output APK is compiled to:
```
builds/GhostOpsTV.apk
```

---

## 📁 Project Structure

```
GhostOps-TV-OS/
├── GhostOpsLauncher/               # Native Android TV Launcher project
│   ├── app/
│   │   ├── src/main/java/com/ghostops/tv/
│   │   │   ├── MainActivity.java           # Top bar, spotlight hero, rails, app dock
│   │   │   ├── api/
│   │   │   │   ├── GhostOpsClient.java     # Multi-server stream resolver & curated rails
│   │   │   │   └── MediaItem.java          # Media metadata model
│   │   │   ├── ui/
│   │   │   │   ├── CinemaRailAdapter.java  # Horizontal card carousel
│   │   │   │   ├── AppDockAdapter.java     # Installed TV apps dock
│   │   │   │   └── FocusScaleHelper.java   # 1.08x D-pad focus scaling engine
│   │   │   ├── player/
│   │   │   │   └── PlayerActivity.java     # Media3 4K HLS/DASH ExoPlayer
│   │   │   └── inputs/
│   │   │       └── TvInputHelper.java      # HDMI 1-4, AV, Tuner hardware switcher
│   │   └── build.gradle
├── tools/
│   ├── setup_ghostops_tv.py        # 1-Click Wi-Fi ADB takeover & debloater
│   └── restore_stock_tv.py         # Factory restore & safety undo
├── builds/
│   └── GhostOpsTV.apk              # Signed native TV launcher APK (~5.76 MB)
├── backups/                        # Pre-takeover system state backups
└── shots/                          # Verification screenshots pulled from TV
```
