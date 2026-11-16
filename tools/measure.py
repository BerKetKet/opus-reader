import sys, json, collections
import numpy as np
from PIL import Image
D = 2.625
def load(p): return np.array(Image.open(p).convert('RGB'))
def bg_color(a):
    return collections.Counter(map(tuple, a[::4, ::4].reshape(-1, 3))).most_common(1)[0][0]
def bands(a, bg, tol=18, minh=2, x0=0, x1=None):
    sub = a[:, x0:x1]
    m = (np.abs(sub.astype(int) - np.array(bg)).sum(axis=2) > tol).any(axis=1)
    out, s = [], None
    for y, v in enumerate(m):
        if v and s is None: s = y
        if not v and s is not None:
            if y - s >= minh: out.append((s, y - 1))
            s = None
    if s is not None: out.append((s, len(m) - 1))
    return out
def hex_(c): return '#%02X%02X%02X' % tuple(c)
def band_info(a, b, bg):
    y0, y1 = b
    sub = a[y0:y1 + 1]
    m = (np.abs(sub.astype(int) - np.array(bg)).sum(axis=2) > 18)
    xs = np.where(m.any(axis=0))[0]
    cols = collections.Counter(map(tuple, sub[m][::3])).most_common(2)
    return dict(y=[y0, y1], h=y1 - y0 + 1, x=[int(xs.min()), int(xs.max())],
                y_dp=[round(y0 / D, 1), round((y1 + 1) / D, 1)], h_dp=round((y1 - y0 + 1) / D, 1),
                colors=[hex_(c) for c, _ in cols])
def run(p):
    a = load(p); bg = bg_color(a)
    return dict(file=p, size=[a.shape[1], a.shape[0]], page_bg=hex_(bg),
                bands=[band_info(a, b, bg) for b in bands(a, bg)])
if __name__ == '__main__':
    for p in sys.argv[1:]: print(json.dumps(run(p), indent=1))
