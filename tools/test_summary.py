import sys, glob, pathlib
import xml.etree.ElementTree as ET
root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
unit = [p for p in root.glob('**/build/test-results/**/*.xml')]
dev = [p for p in root.glob('**/build/outputs/androidTest-results/**/*.xml')]
def read(files):
    rows = []
    for p in files:
        try: s = ET.parse(p).getroot()
        except Exception: continue
        suites = [s] if s.tag == 'testsuite' else list(s.iter('testsuite'))
        for t in suites:
            rows.append((str(p.relative_to(root)), t.get('name'), int(t.get('tests', 0)), int(t.get('failures', 0)) + int(t.get('errors', 0)), int(t.get('skipped', 0))))
    return rows
ok = True
for label, files in (('UNIT', unit), ('DEVICE', dev)):
    rows = read(files)
    tot = sum(r[2] for r in rows); bad = sum(r[3] for r in rows); skip = sum(r[4] for r in rows)
    for r in rows: print(f'{label}  {r[2]:3d} tests {r[3]:2d} failed {r[4]:2d} skipped  {r[1]}')
    print(f'{label} TOTAL: {tot} tests, {bad} failed, {skip} skipped')
    if tot == 0 or bad: ok = False; print(f'{label} FAIL: no tests ran or failures present')
print('TEST SUMMARY OK' if ok else 'TEST SUMMARY FAIL')
sys.exit(0 if ok else 1)
