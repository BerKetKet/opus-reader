import sys, json, argparse
import numpy as np
from PIL import Image, ImageDraw
from measure import load, bg_color, bands
ap = argparse.ArgumentParser()
ap.add_argument('ref'); ap.add_argument('app'); ap.add_argument('out')
ap.add_argument('--mask', action='append', default=[], help='x0,y0,x1,y1 in px; repeatable')
ap.add_argument('--tol', type=int, default=8)
ap.add_argument('--status', type=int, default=90, help='top px to mask (status bar)')
ap.add_argument('--bottom', type=int, default=0, help='bottom px to mask (gesture bar)')
a = ap.parse_args()
r, p = load(a.ref), load(a.app)
assert r.shape == p.shape, f'size mismatch {r.shape} vs {p.shape}'
keep = np.ones(r.shape[:2], bool)
keep[:a.status] = False
if a.bottom: keep[-a.bottom:] = False
for m in a.mask:
    x0, y0, x1, y1 = map(int, m.split(',')); keep[y0:y1, x0:x1] = False
d = np.abs(r.astype(int) - p.astype(int)).max(axis=2)
bad = (d > a.tol) & keep
rb, pb = bands(r, bg_color(r)), bands(p, bg_color(p))
offs = []
for (s, e) in rb:
    if s < a.status or (a.bottom and e >= r.shape[0] - a.bottom):
        continue
    if not keep[s:e+1].any():
        continue
    c = (s + e) / 2
    if pb:
        j = min(pb, key=lambda q: abs((q[0] + q[1]) / 2 - c)); offs.append(dict(ref=[s, e], app=list(j), dy_top=j[0] - s, dh=(j[1] - j[0]) - (e - s)))
    else: offs.append(dict(ref=[s, e], app=None))
heat = np.zeros_like(r); heat[bad] = (255, 0, 60); heat[~bad] = (r[~bad] * 0.35).astype(np.uint8)
sheet = Image.new('RGB', (r.shape[1] * 3 + 20, r.shape[0]), (80, 0, 80))
for i, im in enumerate((r, p, heat)): sheet.paste(Image.fromarray(im), (i * (r.shape[1] + 10), 0))
sheet.save(a.out + '_diff.png')
res = dict(pct_bad=round(100 * bad.sum() / keep.sum(), 2), bands_ref=len(rb), bands_app=len(pb),
           worst_offsets=sorted(offs, key=lambda o: -abs(o.get('dy_top', 999) if o['app'] else 999))[:8])
print(json.dumps(res, indent=1))
