#!/usr/bin/env python3
"""
GhostOps TV OS - Factory Restore & Reversal Utility
Safely re-enables OEM launchers, re-enables manufacturer services,
and restores your Smart TV to its default factory state.
"""

import os
import sys
import subprocess
import shutil

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

def run_adb(cmd_list):
    cmd = [ADB_BIN] + cmd_list
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return res.stdout.strip(), res.stderr.strip(), res.returncode

def main():
    print("=" * 60)
    print("      GHOSTOPS TV OS - FACTORY STOCK RESTORE UTILITY")
    print("=" * 60)

    default_ip = os.environ.get("TV_IP", "")
    tv_ip = input(f"\nEnter TV IP Address [{default_ip}]: ").strip() or default_ip
    if not tv_ip:
        print("Error: TV IP is required!")
        sys.exit(1)

    port = "5555"
    if ":" in tv_ip:
        tv_ip, port = tv_ip.split(":", 1)

    print(f"Connecting to {tv_ip}:{port}...")
    run_adb(["connect", f"{tv_ip}:{port}"])

    out, _, _ = run_adb(["get-state"])
    if "device" not in out:
        print("Error: Could not connect to TV. Verify debugging is enabled.")
        sys.exit(1)

    print("\n[1/3] Clearing GhostOps TV default home status...")
    run_adb(["shell", "cmd", "package", "set-home-activity", "--clear"])

    print("[2/3] Re-enabling all manufacturer services & launchers...")
    out, _, _ = run_adb(["shell", "pm", "list", "packages", "-d"])
    disabled_pkgs = [p.replace("package:", "").strip() for p in out.splitlines()]

    restored = 0
    for pkg in disabled_pkgs:
        if "com.ghostops" in pkg: continue
        print(f"  Re-enabling: {pkg} ... ", end="")
        res, _, _ = run_adb(["shell", "pm", "enable", pkg])
        print("OK" if "enabled" in res.lower() or "new state" in res.lower() else res)
        restored += 1

    print(f"  Restored {restored} manufacturer packages.")

    print("\n[3/3] Optional: Uninstall GhostOps TV Launcher APK?")
    choice = input("Do you want to completely remove GhostOps TV APK? (y/N): ").strip().lower()
    if choice == "y":
        print("  Uninstalling com.ghostops.tv...")
        run_adb(["shell", "pm", "uninstall", "com.ghostops.tv"])
        print("  GhostOps TV uninstalled.")

    print("\n" + "=" * 60)
    print("  FACTORY RESTORE COMPLETE.")
    print("  Your Smart TV has been restored to standard factory launcher.")
    print("=" * 60 + "\n")

if __name__ == "__main__":
    main()
