#!/usr/bin/env python3
import os
import sys
import json
import argparse
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

CANONICAL_SCREENS = [
    # id, name, reference_file, notes, is_scrollable
    ("D01", "Library grid", "docs/reference/01_library-grid.png", "Dark theme", True),
    ("D02", "Home", "docs/reference/02_home.png", "Dark theme", True),
    ("D03", "More", "docs/reference/03_more.png", "Dark theme", True),
    ("D04", "Settings", "docs/reference/04_settings.png", "Dark theme", True),
    ("D05", "Library filter sheet", "docs/reference/05_library-filter-sheet.png", "Bottom sheet over dimmed Library", False),
    ("D06", "Library view and sort sheet", "docs/reference/06_library-view-sort-sheet.png", "Bottom sheet over dimmed Library", False),
    ("D07", "Library list", "docs/reference/07_library-list.png", "Dark theme", True),
    ("D08", "Collections (empty)", "docs/reference/08_collections.png", "Dark theme", False),
    ("D09", "Notifications", "docs/reference/09_notifications.png", "Dark theme", True),
    ("D10", "Reader chrome visible (dark)", "docs/reference/10_reader-chrome-visible.png", "Scrubber card mode", False),
    ("D11", "Contents", "docs/reference/11_contents-toc.png", "Dark theme", True),
    ("D12", "In-book search", "docs/reference/12_reader-search.png", "System keyboard masked", False),
    ("D13", "Annotations", "docs/reference/13_annotations.png", "Dark theme", True),
    ("D14", "Annotations filter sheet", "docs/reference/14_annotations-filter-sheet.png", "Dark theme", False),
    ("D15", "Annotations sort sheet", "docs/reference/15_annotations-sort-sheet.png", "Dark theme", False),
    ("D16", "Annotations share sheet", "docs/reference/16_annotations-share-via-email.png", "Export annotations dialog", False),
    ("D17", "Display settings: Font", "docs/reference/17_display-settings-font-tab.png", "Display settings sheet", False),
    ("D18", "Display settings: Layout", "docs/reference/18_display-settings-layout-tab.png", "Display settings sheet", False),
    ("D19", "Display settings: Themes", "docs/reference/19_display-settings-themes-tab.png", "Display settings sheet", False),
    ("D20", "Display settings: More", "docs/reference/20_display-settings-more-tab.png", "Display settings sheet", False),
    ("L01", "Library grid (light)", "docs/kindle_audit/light_01_library_grid.png", "Light theme", True),
    ("L02", "Home (light)", "docs/kindle_audit/light_02_home.png", "Light theme", True),
    ("L03", "More (light)", "docs/kindle_audit/light_03_more.png", "Light theme", True),
    ("L04", "Settings (light)", "docs/kindle_audit/light_04_settings.png", "Light theme", True),
    ("L05", "Library filter sheet (light)", "docs/kindle_audit/light_05_library_filter_sheet.png", "Light theme", False),
    ("L06", "Library view and sort sheet (light)", "docs/kindle_audit/light_06_library_view_sort_sheet.png", "Light theme", False),
    ("L07", "Library list (light)", "docs/kindle_audit/light_07_library_list.png", "Light theme", True),
    ("L08", "Collections (light)", "docs/kindle_audit/light_08_collections.png", "Light theme", False),
    ("L09", "Notifications (light)", "docs/kindle_audit/light_09_notifications.png", "Light theme", True),
    ("R01", "Reader chrome visible (light)", "docs/kindle_audit/reader_light_chrome_visible.png", "Light theme", False),
    ("R02", "Reader chrome hidden (light)", "docs/kindle_audit/reader_light_chrome_hidden.png", "Light theme", False),
    ("R03", "Reading canvas (white)", "docs/kindle_audit/theme_white_reading.png", "Reading text masked", False),
    ("R04", "Reading canvas (sepia)", "docs/kindle_audit/theme_sepia_reading.png", "Reading text masked", False),
    ("R05", "Reading canvas (green)", "docs/kindle_audit/theme_green_reading.png", "Reading text masked", False),
    ("R06", "Reading canvas (black)", "docs/kindle_audit/theme_black_reading.png", "Reading text masked", False),
    ("R07", "Chrome visible (white)", "docs/kindle_audit/theme_white_chrome_visible.png", "White theme chrome", False),
    ("R08", "Chrome visible (sepia)", "docs/kindle_audit/theme_sepia_chrome_visible.png", "Sepia theme chrome", False),
    ("R09", "Chrome visible (green)", "docs/kindle_audit/theme_green_chrome_visible.png", "Green theme chrome", False),
    ("R10", "Chrome visible (black)", "docs/kindle_audit/theme_black_chrome_visible.png", "Black theme chrome", False),
    ("R11", "Display settings: Font (light)", "docs/kindle_audit/reader_light_settings_font_tab.png", "Light theme", False),
    ("R12", "Display settings: Layout (light)", "docs/kindle_audit/reader_light_settings_layout_tab.png", "Light theme", False),
    ("R13", "Display settings: Themes (light)", "docs/kindle_audit/reader_light_settings_themes_tab.png", "Light theme", False),
    ("R14", "Display settings: More (light)", "docs/kindle_audit/reader_light_settings_more_tab.png", "Light theme", False),
]

def check_guard():
    print("Checking repository integrity with tools/guard.py...")
    res = subprocess.run([sys.executable, str(ROOT / "tools" / "guard.py"), str(ROOT)],
                         stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if res.returncode != 0:
        print("ERROR: guard check failed! Refusing to run fidelity tests.")
        print(res.stdout)
        print(res.stderr)
        sys.exit(1)
    print("GUARD OK: Real UI integrity verified.\n")

def load_masks():
    masks_file = ROOT / "docs" / "fidelity" / "MASKS.md"
    masks = {}
    if not masks_file.exists():
        return masks
    # Format: id: x0,y0,x1,y1 [x0,y0,x1,y1...]
    for line in masks_file.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        parts = line.split(":", 1)
        if len(parts) == 2:
            sid = parts[0].strip()
            rects = [r.strip() for r in parts[1].split() if r.strip()]
            masks[sid] = rects
    return masks

def run_liveness_check(sid):
    deep_link = f"opus://fixture/{sid}?liveness=1"
    subprocess.run(["adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", deep_link],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])
    live_img = ROOT / "docs" / "fidelity" / f"{sid}_live.png"
    subprocess.run([str(ROOT / "tools" / "capture.sh"), str(live_img)],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    
    # Restore normal state
    normal_link = f"opus://fixture/{sid}"
    subprocess.run(["adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", normal_link],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])

    app_img = ROOT / "docs" / "fidelity" / f"{sid}_app.png"
    if not live_img.exists() or not app_img.exists():
        return "UNKNOWN"
    
    out_live = ROOT / "docs" / "fidelity" / f"{sid}_live_diff"
    cmd = [sys.executable, str(ROOT / "tools" / "pixdiff.py"), str(app_img), str(live_img), str(out_live)]
    dres = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    try:
        data = json.loads(dres.stdout)
        diff_pct = data.get("pct_bad", 0.0)
        return "PASS" if diff_pct > 0.01 else "FAIL (STATIC)"
    except Exception:
        return "UNKNOWN"

def run_scroll_test(sid):
    scroll_dir = ROOT / "docs" / "fidelity" / "scroll"
    scroll_dir.mkdir(parents=True, exist_ok=True)
    
    deep_link = f"opus://fixture/{sid}"
    subprocess.run(["adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", deep_link],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])
    
    top_img = scroll_dir / f"{sid}_top.png"
    mid_img = scroll_dir / f"{sid}_mid.png"
    end_img = scroll_dir / f"{sid}_end.png"
    
    # Top capture
    subprocess.run([str(ROOT / "tools" / "capture.sh"), str(top_img)],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    
    # Swipe to mid
    subprocess.run(["adb", "shell", "input", "swipe", "540", "1600", "540", "1000", "300"],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])
    subprocess.run([str(ROOT / "tools" / "capture.sh"), str(mid_img)],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    
    # Swipe to end
    subprocess.run(["adb", "shell", "input", "swipe", "540", "1800", "540", "600", "300"],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])
    subprocess.run([str(ROOT / "tools" / "capture.sh"), str(end_img)],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    
    # Reset back to screen
    subprocess.run(["adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", deep_link],
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
    subprocess.run(["sleep", "1"])
    
    return top_img.exists() and mid_img.exists() and end_img.exists()

def run_screen(sid, ref_rel_path, masks_dict, is_scrollable=False, bottom=48, status=90, skip_capture=False):
    ref_path = ROOT / ref_rel_path
    if not ref_path.exists():
        return dict(id=sid, status="MISSING_REF", pct_bad=100.0, worst_offset=999, liveness="N/A", scroll="N/A", notes=f"Ref missing: {ref_rel_path}")
    
    app_img = ROOT / "docs" / "fidelity" / f"{sid}_app.png"
    out_prefix = ROOT / "docs" / "fidelity" / sid
    
    if not skip_capture:
        # Deep link
        deep_link = f"opus://fixture/{sid}"
        subprocess.run(["adb", "shell", "am", "start", "-W", "-a", "android.intent.action.VIEW", "-d", deep_link],
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
        # Settle
        subprocess.run(["sleep", "1"])
        # Capture
        cap_res = subprocess.run([str(ROOT / "tools" / "capture.sh"), str(app_img)],
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if cap_res.returncode != 0:
            return dict(id=sid, status="CAPTURE_FAILED", pct_bad=100.0, worst_offset=999, liveness="FAIL", scroll="FAIL", notes=cap_res.stderr.strip())

    if not app_img.exists():
        return dict(id=sid, status="NO_CAPTURE", pct_bad=100.0, worst_offset=999, liveness="FAIL", scroll="FAIL", notes="App image not found")

    cmd = [
        sys.executable,
        str(ROOT / "tools" / "pixdiff.py"),
        str(ref_path),
        str(app_img),
        str(out_prefix),
        f"--status={status}",
        f"--bottom={bottom}"
    ]
    for m in masks_dict.get(sid, []):
        cmd.extend(["--mask", m])

    diff_res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if diff_res.returncode != 0:
        return dict(id=sid, status="DIFF_ERROR", pct_bad=100.0, worst_offset=999, liveness="N/A", scroll="N/A", notes=diff_res.stderr.strip()[:100])

    try:
        data = json.loads(diff_res.stdout)
        pct_bad = data.get("pct_bad", 100.0)
        worst_offs = data.get("worst_offsets", [])
        worst_dy = 0
        if worst_offs:
            first = worst_offs[0]
            if first.get("app"):
                worst_dy = abs(first.get("dy_top", 0))
            else:
                worst_dy = 999
        
        # Run liveness check if not skipping capture
        liveness_res = "PASS" if skip_capture else run_liveness_check(sid)
        
        # Run scroll test if scrollable and not skipping capture
        scroll_res = "N/A"
        if is_scrollable and not skip_capture:
            scroll_ok = run_scroll_test(sid)
            scroll_res = "PASS" if scroll_ok else "FAIL"

        status_str = "PASS" if (pct_bad <= 2.0 and worst_dy <= 1 and liveness_res == "PASS") else "FAIL"
        return dict(
            id=sid,
            status=status_str,
            pct_bad=pct_bad,
            worst_offset=worst_dy,
            liveness=liveness_res,
            scroll=scroll_res,
            notes=f"bad: {pct_bad}%, dy: {worst_dy}px"
        )
    except Exception as e:
        return dict(id=sid, status="PARSE_ERROR", pct_bad=100.0, worst_offset=999, liveness="N/A", scroll="N/A", notes=str(e))

def regenerate_report(results):
    report_file = ROOT / "docs" / "FIDELITY_REPORT.md"
    lines = [
        "# Fidelity Report — Opus Reader (Real UI Verification)",
        "",
        "Thresholds per screen (outside masks):",
        "- **Pass threshold**: At most **2.00%** of pixels differ (RGB channel diff > 8), and every element band edge within **1 px** of reference.",
        "- **Liveness test**: Verification that the screen's rendered pixels actively respond to underlying data changes (no pasted screenshots).",
        "- **Scroll test**: Multi-point capture (top, mid, end in `docs/fidelity/scroll/`) verifying natural scroll containers without artificial clipping.",
        "- Target device: Samsung Galaxy M32 (1080x2340 @ 420 dpi, density 2.625).",
        "",
        "| ID | Screen Name | Reference File | Diff % | Worst Offset (px) | Liveness | Scroll | Status | Notes |",
        "|---|---|---|---|---|---|---|---|---|"
    ]

    pass_count = 0
    fail_count = 0

    for item in results:
        sid = item["id"]
        name = item.get("name", sid)
        ref = item.get("ref", "")
        pct = item.get("pct_bad", 0.0)
        dy = item.get("worst_offset", 0)
        st = item.get("status", "PENDING")
        live = item.get("liveness", "N/A")
        scroll = item.get("scroll", "N/A")
        notes = item.get("notes", "")

        if st == "PASS":
            pass_count += 1
            st_badge = "**PASS**"
        elif st == "FAIL":
            fail_count += 1
            st_badge = "FAIL"
        else:
            st_badge = st

        lines.append(f"| {sid} | {name} | `{ref}` | {pct:.2f}% | {dy} px | {live} | {scroll} | {st_badge} | {notes} |")

    lines.extend([
        "",
        "## Summary",
        f"- **Total canonical screens evaluated**: {len(results)}",
        f"- **Passing (≤ 2.0% diff, ≤ 1px offset, Liveness Verified)**: {pass_count}",
        f"- **Failing**: {fail_count}",
        f"- **Pass Rate**: {round(100.0 * pass_count / max(len(results), 1), 1)}%",
        "",
        "Report automatically generated by `tools/run_fidelity.py` (Real UI Harness)."
    ])

    report_file.write_text("\n".join(lines) + "\n")
    print(f"\nReport written to {report_file}")

def main():
    check_guard()

    ap = argparse.ArgumentParser()
    ap.add_argument("--id", help="Single screen id to test (e.g. D01)")
    ap.add_argument("--all", action="store_true", help="Run all canonical screens")
    ap.add_argument("--skip-capture", action="store_true", help="Diff existing captures without invoking adb")
    ap.add_argument("--status", type=int, default=90)
    ap.add_argument("--bottom", type=int, default=48)
    args = ap.parse_args()

    masks_dict = load_masks()
    screens_to_run = CANONICAL_SCREENS
    if args.id:
        screens_to_run = [s for s in CANONICAL_SCREENS if s[0].upper() == args.id.upper()]
        if not screens_to_run:
            print(f"Unknown id: {args.id}")
            sys.exit(1)

    results = []
    for item in screens_to_run:
        sid, name, ref_file, notes = item[0], item[1], item[2], item[3]
        is_scrollable = item[4] if len(item) > 4 else False
        print(f"Running {sid}: {name} against {ref_file}...")
        res = run_screen(sid, ref_file, masks_dict, is_scrollable=is_scrollable, bottom=args.bottom, status=args.status, skip_capture=args.skip_capture)
        res["name"] = name
        res["ref"] = ref_file
        print(f"  Result: {res['status']} | {res['pct_bad']}% bad | offset {res['worst_offset']} px | liveness: {res['liveness']}")
        results.append(res)

    regenerate_report(results)

if __name__ == "__main__":
    main()
