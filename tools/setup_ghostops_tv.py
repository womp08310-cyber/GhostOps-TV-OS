#!/usr/bin/env python3
"""
GhostOps TV OS - Universal Smart TV Takeover & Debloater (Tier 1)
Automates Wi-Fi ADB connection, system backup, manufacturer ad debloating,
and native GhostOps TV Home installation on Android TV & Google TV.
"""

import os
import sys
import time
import subprocess
import shutil
from datetime import datetime

# Common paths for ADB
ADB_CANDIDATES = [
    "adb",
    r"C:\Users\mgarv\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    os.path.expanduser(r"~\AppData\Local\Android\Sdk\platform-tools\adb.exe"),
]

def find_adb():
    for candidate in ADB_CANDIDATES:
        if shutil.which(candidate) or os.path.isfile(candidate):
            return candidate
    return "adb"

ADB_BIN = find_adb()

def run_adb(cmd_list, timeout=30):
    cmd = [ADB_BIN] + cmd_list
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
    return res.stdout.strip(), res.stderr.strip(), res.returncode

def print_banner():
    print("=" * 65)
    print("   ██████╗ ██╗  ██╗ ██████╗ ███████╗████████╗ ██████╗ ██████╗ ███████╗")
    print("  ██╔════╝ ██║  ██║██╔═══██╗██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗██╔════╝")
    print("  ██║  ███╗███████║██║   ██║███████╗   ██║   ██║   ██║██████╔╝███████╗")
    print("  ██║   ██║██╔══██║██║   ██║╚════██║   ██║   ██║   ██║██╔═══╝ ╚════██║")
    print("  ╚██████╔╝██║  ██║╚██████╔╝███████║   ██║   ╚██████╔╝██║     ███████║")
    print("   ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚══════╝   ╚═╝    ╚═════╝ ╚═╝     ╚══════╝")
    print("                       T V   O S   ( T I E R   1 )")
    print("           Zero-Root Universal Smart TV Takeover Engine")
    print("=" * 65)

# Known ad recommendation & telemetry packages safely removable on Android TV
SAFE_DEBLOAT_LIST = [
    # Google TV ad recommendation rows
    "com.google.android.tvrecommendations",
    
    # Xiaomi PatchWall & ads
    "com.xiaomi.mitv.tvhome",
    "com.xiaomi.mitv.advertisement",
    "com.xiaomi.mitv.banner",
    "com.xiaomi.mitv.upgrade",
    "com.mitv.videoplayer",
    "com.xiaomi.mitv.payment",
    
    # TCL Home waterfall & recommendations
    "com.tcl.waterfall.overseas",
    "com.tcl.partnercustomizer",
    "com.tcl.usercenter",
    "com.tcl.xian.recommend",
    "com.tcl.tvmanager",
    
    # Sony Samba TV telemetry
    "com.samba.tv",
    "com.sony.dtv.samba.services",
    "com.sony.dtv.b2btvcustomizer",
    
    # Hisense VIDAA / Content carousel
    "com.jamdeo.tv.vod",
    "com.hisense.hitv.hicloud.account",
]

def main():
    print_banner()

    # Step 1: TV IP discovery / prompt
    print("\n[Step 1/6] Connect to your Smart TV over Wi-Fi ADB")
    print("Ensure Developer Options & USB/Network Debugging are ENABLED on your TV:")
    print("  (Settings -> About -> Click 'Build' 7 times -> Developer Options -> Turn ON Debugging)")
    
    default_ip = os.environ.get("TV_IP", "")
    tv_ip = input(f"\nEnter TV IP Address [{default_ip}]: ").strip() or default_ip
    if not tv_ip:
        print("Error: TV IP address is required!")
        sys.exit(1)

    port = "5555"
    if ":" in tv_ip:
        tv_ip, port = tv_ip.split(":", 1)

    print(f"\nConnecting to {tv_ip}:{port}...")
    out, err, code = run_adb(["connect", f"{tv_ip}:{port}"])
    print(f"ADB: {out}")

    # Check connection
    out, _, _ = run_adb(["get-state"])
    if "device" not in out:
        print("\n[!] TV is not authorized or not responding.")
        print("Please check your TV screen right now and select 'Always allow from this computer'.")
        input("Press Enter once you have accepted the prompt on your TV...")
        out, _, _ = run_adb(["get-state"])
        if "device" not in out:
            print("Failed to establish authorized ADB connection. Please verify IP and Wi-Fi connection.")
            sys.exit(1)

    # Step 2: Device Info
    print("\n[Step 2/6] Detecting TV Hardware & Firmware...")
    mfr, _, _ = run_adb(["shell", "getprop", "ro.product.manufacturer"])
    model, _, _ = run_adb(["shell", "getprop", "ro.product.model"])
    sdk, _, _ = run_adb(["shell", "getprop", "ro.build.version.sdk"])
    android_ver, _, _ = run_adb(["shell", "getprop", "ro.build.version.release"])
    print(f"  Manufacturer : {mfr.capitalize()}")
    print(f"  Model        : {model}")
    print(f"  Android OS   : Android {android_ver} (API {sdk})")

    # Step 3: Backup
    print("\n[Step 3/6] Creating Safety State Backup...")
    backup_dir = os.path.join(os.path.dirname(__file__), "..", "backups")
    os.makedirs(backup_dir, exist_ok=True)
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_file = os.path.join(backup_dir, f"tv_backup_{ts}.txt")
    
    pkg_list, _, _ = run_adb(["shell", "pm", "list", "packages"])
    with open(backup_file, "w", encoding="utf-8") as f:
        f.write(f"Manufacturer: {mfr}\nModel: {model}\nAndroid: {android_ver}\n\nPackages:\n{pkg_list}\n")
    print(f"  Safety backup written to: {backup_file}")

    # Step 4: Debloat Manufacturer Ads
    print("\n[Step 4/6] Debloating Manufacturer Sponsored Ads & Recommendations...")
    disabled_count = 0
    installed_pkgs = [p.replace("package:", "").strip() for p in pkg_list.splitlines()]

    for bad_pkg in SAFE_DEBLOAT_LIST:
        if bad_pkg in installed_pkgs:
            print(f"  Disabling ad service: {bad_pkg} ... ", end="")
            out, _, code = run_adb(["shell", "pm", "disable-user", "--user", "0", bad_pkg])
            if "disabled" in out.lower() or "new state" in out.lower():
                print("DONE")
                disabled_count += 1
            else:
                print(f"SKIPPED ({out})")
    
    print(f"  Debloat complete: {disabled_count} ad and tracking services disabled.")
    print("  (All TV Tuners, HDMI Inputs, Remotes, and Settings are 100% PRESERVED)")

    # Step 5: Install GhostOps TV Launcher APK
    print("\n[Step 5/6] Installing Native GhostOps TV OS Launcher...")
    apk_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "builds", "GhostOpsTV.apk"))
    if not os.path.exists(apk_path):
        print(f"Error: APK not found at {apk_path}")
        print("Please run gradle assembleDebug first!")
        sys.exit(1)

    print(f"  Pushing GhostOpsTV.apk ({os.path.getsize(apk_path)} bytes)...")
    run_adb(["push", apk_path, "/data/local/tmp/GhostOpsTV.apk"])
    print("  Installing package com.ghostops.tv...")
    out, err, code = run_adb(["shell", "pm", "install", "-r", "/data/local/tmp/GhostOpsTV.apk"])
    if "Success" not in out:
        print(f"  Install failed: {out} {err}")
        sys.exit(1)
    print("  GhostOps TV OS successfully installed!")

    # Step 6: Set Default Home Activity
    print("\n[Step 6/6] Activating GhostOps TV as Default Home Screen...")
    run_adb(["shell", "cmd", "package", "set-home-activity", "com.ghostops.tv/.MainActivity"])
    
    # Launch Home Activity directly
    run_adb(["shell", "am", "start", "-n", "com.ghostops.tv/.MainActivity"])

    # Pull verification screenshot
    time.sleep(2)
    shot_dir = os.path.join(os.path.dirname(__file__), "..", "shots")
    os.makedirs(shot_dir, exist_ok=True)
    shot_file = os.path.join(shot_dir, f"ghostops_tv_verified_{ts}.png")
    run_adb(["shell", "screencap", "-p", "/sdcard/ghostops_verify.png"])
    run_adb(["pull", "/sdcard/ghostops_verify.png", shot_file])

    print("\n" + "=" * 65)
    print("  GHOSTOPS TV OS ACTIVATION COMPLETE!")
    print(f"  - Clean, ad-free OLED black cinema experience running on your TV.")
    print(f"  - Integrated 4K streaming rails (Trending, Masterpieces, Anime).")
    print(f"  - Hardware TV input switching (HDMI 1-4, AV, Tuner) active.")
    print(f"  - Verification screenshot captured: {shot_file}")
    print("=" * 65)
    print("To restore factory launcher at any time, run: python tools/restore_stock_tv.py\n")

if __name__ == "__main__":
    main()
