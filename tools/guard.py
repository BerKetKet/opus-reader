import re, sys, pathlib
root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
bad = []
def add(kind, p, n=None, txt=''): bad.append((kind, str(p.relative_to(root)), n, txt.strip()[:90]))
mains = [p for p in root.rglob('src/main') if p.is_dir() and 'build' not in p.parts]
for m in mains:
    for p in m.rglob('*.kt'):
        s = str(p); feat = '/feature/' in s
        for i, line in enumerate(p.read_text(errors='ignore').splitlines(), 1):
            if re.search(r'\bisFixture\b|\bfixtureId\b', line) and 'debug' not in s: add('fixture flag in production code', p, i, line)
            if 'ContentScale.FillBounds' in line: add('FillBounds (deforms images)', p, i, line)
            if re.search(r'R\.drawable\.fixture_', line): add('fixture image in main code', p, i, line)
            if re.search(r'offset\((x|y) *= *-|clipToBounds\(\)|requiredHeight\(|translation(X|Y) *=|absoluteOffset', line): add('layout hack', p, i, line)
            if feat and re.search(r'\.refPx\b', line): add('refPx in feature code (belongs in :design)', p, i, line)
            if feat and re.search(r'Spacer\(.*height\(\s*[0-9.]+\.dp', line): add('hard-coded spacer in feature code', p, i, line)
            if feat and re.search(r':\s*\([^)]*\)\s*->\s*Unit\s*=\s*\{\s*\}', line): add('default no-op callback', p, i, line)
            if feat and p.name.endswith('Screen.kt') and re.search(r'^import .*\.data\.', line): add('feature screen imports :data', p, i, line)
    for p in (m / 'res').rglob('*') if (m / 'res').exists() else []:
        if p.suffix.lower() in ('.png', '.webp', '.jpg', '.jpeg') and 'mipmap' not in str(p): add('raster image in main/res', p)
pat = re.compile(r'kindle\.com|protonmail|gebregiorgis|bereket|@gmail\.com', re.I)
for p in list(root.glob('**/*.kt')) + list(root.glob('docs/**/*.md')):
    if 'build' in p.parts: continue
    for i, line in enumerate(p.read_text(errors='ignore').splitlines(), 1):
        if pat.search(line): add('personal data', p, i, line)
by = {}
for k, *_ in bad: by[k] = by.get(k, 0) + 1
for k, n in sorted(by.items()): print(f'{n:4d}  {k}')
if '-v' in sys.argv:
    for b in bad: print(b)
print('GUARD FAIL' if bad else 'GUARD OK')
sys.exit(1 if bad else 0)
