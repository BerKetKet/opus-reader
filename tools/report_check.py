import re, sys
path, ids = sys.argv[1], sys.argv[2:]
t = open(path).read()
m = re.search(r'^## Fidelity.*?(?=^## |\Z)', t, re.S | re.M)
if not m:
    print('REPORT FAIL: no "## Fidelity" section'); sys.exit(1)
sec = m.group(0); bad = []
for i in ids:
    if not any(i in l and re.search(r'\d+(\.\d+)?\s*%', l) for l in sec.splitlines()): bad.append(i)
if bad:
    print('REPORT FAIL: no fidelity number for', ', '.join(bad)); sys.exit(1)
print('REPORT OK')
