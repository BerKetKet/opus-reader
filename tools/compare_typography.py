#!/usr/bin/env python3
"""
tools/compare_typography.py

Compares observed UI text elements and bands in Opus Reader app capture
against target Kindle reference screenshots.
Outputs exact pixel and dp/sp discrepancies in element positions (y),
dimensions/heights (h), and vertical spacing.
"""

import sys
import os
import argparse
import collections
import numpy as np
from PIL import Image

D = 2.625

def hex_color(c):
    return '#%02X%02X%02X' % tuple(c[:3])

def extract_bands(img_np, y0=0, y1=None, x_margin=45, tol=16, minh=2):
    if y1 is None:
        y1 = img_np.shape[0]
    
    sub = img_np[y0:y1]
    h_sub, w_sub = sub.shape[:2]
    
    sample = np.concatenate([
        sub[2:min(15, h_sub-2), x_margin:w_sub-x_margin],
        sub[-min(15, h_sub-2):-2, x_margin:w_sub-x_margin]
    ], axis=0) if h_sub > 30 else sub[:, x_margin:w_sub-x_margin]
    
    bg = collections.Counter(map(tuple, sample[::2, ::2, :3].reshape(-1, 3))).most_common(1)[0][0]
    bg_np = np.array(bg)
    
    active_cols = sub[:, x_margin:w_sub-x_margin, :3]
    diff = np.abs(active_cols.astype(int) - bg_np).sum(axis=2) > tol
    m = diff.any(axis=1)
    
    bands = []
    s = None
    for y, is_fg in enumerate(m):
        if is_fg and s is None:
            s = y
        elif not is_fg and s is not None:
            if y - s >= minh:
                bands.append((s + y0, y - 1 + y0))
            s = None
    if s is not None and (len(m) - s) >= minh:
        bands.append((s + y0, len(m) - 1 + y0))
        
    band_details = []
    for b_y0, b_y1 in bands:
        band_slice = img_np[b_y0:b_y1+1, :, :3]
        m_band = (np.abs(band_slice.astype(int) - bg_np).sum(axis=2) > tol)
        xs = np.where(m_band.any(axis=0))[0]
        x_min = int(xs.min()) if len(xs) > 0 else 0
        x_max = int(xs.max()) if len(xs) > 0 else w_sub - 1
        h_px = b_y1 - b_y0 + 1
        colors = collections.Counter(map(tuple, band_slice[m_band][::3])).most_common(2)
        
        band_details.append({
            'y0': b_y0,
            'y1': b_y1,
            'h_px': h_px,
            'h_dp': round(h_px / D, 1),
            'x0': x_min,
            'x1': x_max,
            'colors': [hex_color(c) for c, _ in colors]
        })
        
    return band_details, hex_color(bg)

def align_bands_dp(ref_bands, app_bands):
    """Align ref and app bands using dynamic programming with penalty for y and h distance"""
    n, m = len(ref_bands), len(app_bands)
    dp = np.zeros((n + 1, m + 1))
    
    # Gap penalty
    gap_penalty = 80
    for i in range(n + 1):
        dp[i][0] = i * gap_penalty
    for j in range(m + 1):
        dp[0][j] = j * gap_penalty
        
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            rb = ref_bands[i - 1]
            ab = app_bands[j - 1]
            dist_y = abs(rb['y0'] - ab['y0'])
            dist_h = abs(rb['h_px'] - ab['h_px'])
            cost = dist_y * 0.5 + dist_h * 1.5
            
            dp[i][j] = min(
                dp[i - 1][j - 1] + cost,
                dp[i - 1][j] + gap_penalty,
                dp[i][j - 1] + gap_penalty
            )
            
    # Traceback
    i, j = n, m
    pairs = []
    while i > 0 or j > 0:
        if i > 0 and j > 0:
            rb = ref_bands[i - 1]
            ab = app_bands[j - 1]
            cost = abs(rb['y0'] - ab['y0']) * 0.5 + abs(rb['h_px'] - ab['h_px']) * 1.5
            if np.isclose(dp[i][j], dp[i - 1][j - 1] + cost):
                pairs.append((i - 1, j - 1))
                i -= 1
                j -= 1
                continue
        if i > 0 and np.isclose(dp[i][j], dp[i - 1][j] + gap_penalty):
            pairs.append((i - 1, None))
            i -= 1
        else:
            pairs.append((None, j - 1))
            j -= 1
            
    pairs.reverse()
    return pairs

def compare_screen(ref_path, app_path, y0=0, y1=None):
    if not os.path.exists(ref_path):
        print(f"Error: Ref image not found: {ref_path}")
        return
    if not os.path.exists(app_path):
        print(f"Error: App image not found: {app_path}")
        return
        
    ref_img = np.array(Image.open(ref_path).convert('RGB'))
    app_img = np.array(Image.open(app_path).convert('RGB'))
    
    ref_bands, ref_bg = extract_bands(ref_img, y0=y0, y1=y1)
    app_bands, app_bg = extract_bands(app_img, y0=y0, y1=y1)
    
    pairs = align_bands_dp(ref_bands, app_bands)
    
    print(f"=== Typography & Element Band Analysis ===")
    print(f"Reference: {ref_path} (bg: {ref_bg})")
    print(f"App Capture: {app_path} (bg: {app_bg})")
    print(f"Region: y=[{y0}..{y1 if y1 else ref_img.shape[0]}]")
    print(f"{'-'*85}")
    print(f"{'Idx':<4} | {'REF y0..y1 (h_px, h_dp)':<24} | {'APP y0..y1 (h_px, h_dp)':<24} | {'Delta y':<8} | {'Delta h':<8}")
    print(f"{'-'*85}")
    
    for idx, (ri, aj) in enumerate(pairs):
        if ri is not None and aj is not None:
            rb = ref_bands[ri]
            ab = app_bands[aj]
            ref_str = f"y={rb['y0']}..{rb['y1']} ({rb['h_px']}px, {rb['h_dp']}dp)"
            app_str = f"y={ab['y0']}..{ab['y1']} ({ab['h_px']}px, {ab['h_dp']}dp)"
            dy = ab['y0'] - rb['y0']
            dh = ab['h_px'] - rb['h_px']
            dy_str = f"{dy:+d}px"
            dh_str = f"{dh:+d}px"
            print(f"{idx:<4} | {ref_str:<24} | {app_str:<24} | {dy_str:<8} | {dh_str:<8}")
        elif ri is not None:
            rb = ref_bands[ri]
            ref_str = f"y={rb['y0']}..{rb['y1']} ({rb['h_px']}px, {rb['h_dp']}dp)"
            print(f"{idx:<4} | {ref_str:<24} | {'MISSING IN APP':<24} | {'-':<8} | {'-':<8}")
        else:
            ab = app_bands[aj]
            app_str = f"y={ab['y0']}..{ab['y1']} ({ab['h_px']}px, {ab['h_dp']}dp)"
            print(f"{idx:<4} | {'EXTRA IN APP':<24} | {app_str:<24} | {'-':<8} | {'-':<8}")
            
    print(f"{'-'*85}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Compare text and UI element dimensions between Ref and App")
    parser.add_argument('--ref', help="Path to reference screenshot")
    parser.add_argument('--app', help="Path to app screenshot")
    parser.add_argument('--id', help="Canonical screen ID (e.g. D16)")
    parser.add_argument('--y0', type=int, default=0, help="Start Y coordinate")
    parser.add_argument('--y1', type=int, default=None, help="End Y coordinate")
    
    args = parser.parse_args()
    
    ref_path = args.ref
    app_path = args.app
    
    if args.id:
        if not app_path:
            app_path = f"docs/fidelity/{args.id}_app.png"
        if not ref_path:
            canon = "docs/CANONICAL_SCREENS.md"
            if os.path.exists(canon):
                with open(canon) as f:
                    for line in f:
                        if line.startswith(f"| {args.id} ") or f"| {args.id} |" in line:
                            parts = [p.strip() for p in line.split('|')]
                            for p in parts:
                                if p.endswith('.png'):
                                    ref_path = p if os.path.exists(p) else os.path.join("docs/reference", os.path.basename(p))
                                    break
    
    if not ref_path or not app_path:
        print("Usage: python3 tools/compare_typography.py --id <ID> [--y0 <Y>] [--y1 <Y>]")
        print("   or: python3 tools/compare_typography.py --ref <ref.png> --app <app.png>")
        sys.exit(1)
        
    compare_screen(ref_path, app_path, y0=args.y0, y1=args.y1)
