import re, sys
path, ids = sys.argv[1], sys.argv[2:]
t = open(path).read()
def section(name):
    m = re.search(r'^## ' + name + r'.*?(?=^## |\Z)', t, re.S | re.M)
    return m.group(0) if m else None
errs = []
fid = section('Fidelity')
if fid is None: errs.append('no "## Fidelity" section')
else:
    for i in ids:
        if not any(i in l and re.search(r'\d+(\.\d+)?\s*%', l) for l in fid.splitlines()): errs.append('no fidelity number for ' + i)
    for i in ids:
        if not any(i in l and re.search(r'T:(PASS|FAIL)', l) for l in fid.splitlines()): errs.append('no threshold verdict for ' + i + ' (write T:PASS or T:FAIL in its row, judged against the AGENTS.md thresholds)')
for s in ('Inferred', 'Missing'):
    if section(s) is None: errs.append(f'no "## {s}" section (write "None" if empty)')
if errs:
    print('REPORT FAIL:'); [print(' -', e) for e in errs]; sys.exit(1)
print('REPORT OK')
