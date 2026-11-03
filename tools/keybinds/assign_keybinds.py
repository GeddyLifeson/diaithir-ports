#!/usr/bin/env python3
"""Generate pack options.txt key defaults that remove conflicts.
Usage: assign_keybinds.py KEYMAP_CSV OUT_OPTIONS_TXT
Priority order keeps vanilla and the first mapping (sorted by PRIORITY then name) on its
default; later mappings that overlap get the next free ALT/CTRL/SHIFT+key. Names with an unresolved
key are skipped (listed on stderr)."""
import csv, sys, itertools, importlib.util, os
here = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location('ck', os.path.join(here, 'check_keybinds.py'))
src = open(os.path.join(here, 'check_keybinds.py')).read().replace('\nmain()\n', '\n')
ns = {}; exec(compile(src, 'ck', 'exec'), ns)
VANILLA, ctxset = ns['VANILLA'], ns['ctxset']
PRIORITY = ['voicechat', 'neoorigins', 'diaithir', 'alexscaves', 'iceandfire', 'Vampirism', 'toolbelt']
LOW = ['key.diaithir_', 'key.diaithir.']
def prio(n, jar):
    for i, p in enumerate(PRIORITY):
        if p.lower() in (n + jar).lower(): return i
    return len(PRIORITY)
allow = set()
for l in open(os.path.join(here, 'allowlist.txt')):
    l = l.split('#')[0].strip()
    if '|' in l: allow.add(frozenset(l.split('|')))
rows = []
for r in csv.DictReader(open(sys.argv[1])):
    n = r['name']
    if n in ('?', 'key') or r['default_key'] in ('UNKNOWN', '?'): continue
    rows.append((n, r['default_key'], r['modifier'], ctxset(r['context']), r['jar']))
rows = sorted({r[0]: r for r in rows}.values(), key=lambda r: (prio(r[0], r[4]), r[0]))
used = {(k, 'NONE') for k in VANILLA.values()} | {(r[1], r[2]) for r in rows}
placed = [(n, k, 'NONE', {'game'}) for n, k in VANILLA.items()]
pool = [(k, m) for m in ('ALT', 'CONTROL', 'SHIFT') for k in 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789']
out = {}
def conflict(n, k, m, c):
    return any(p[1] == k and p[2] == m and p[3] & c and p[0] != n and frozenset((p[0], n)) not in allow for p in placed)
for n, k, m, c, jar in rows:
    if conflict(n, k, m, c):
        nk = next(((pk, pm) for pk, pm in pool if (pk, pm) not in used and not conflict(n, pk, pm, c)), None)
        if nk is None: print('NO FREE KEY for', n, file=sys.stderr); continue
        used.add(nk); k, m = nk; out[n] = nk
    placed.append((n, k, m, c))
JK = {'LSHIFT': 'left.shift', 'LCTRL': 'left.control', 'LALT': 'left.alt'}
with open(sys.argv[2], 'w') as f:
    for n, (k, m) in sorted(out.items()):
        kk = JK.get(k, k.lower())
        f.write(f'key_{n}:key.keyboard.{kk}' + (f':{m}' if m != 'NONE' else '') + '\n')
print(len(out), 'remapped')
