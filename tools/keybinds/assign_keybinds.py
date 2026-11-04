#!/usr/bin/env python3
"""Generate pack options.txt key defaults that remove conflicts.
Usage: assign_keybinds.py KEYMAP_CSV LIVE_OPTIONS_TXT OUT_OPTIONS_TXT
LIVE_OPTIONS_TXT = key_ lines of a real client's options.txt (dump_live_keys.py), the ground truth for every
registered mapping and its default. Contexts come from the static CSV (unknown = both game and gui).
Vanilla keeps its keys; then mappings in PRIORITY order; a mapping that overlaps an earlier one gets the next
free modifier+key from a pool that no default uses. Unbound mappings are left alone."""
import csv, sys, os, re
here = os.path.dirname(os.path.abspath(__file__))
ns = {}; exec(compile(open(os.path.join(here, 'check_keybinds.py')).read().replace('\nmain()\n', '\n'), 'ck', 'exec'), ns)
ctxset, LK, VNAMES = ns['ctxset'], ns['LK'], ns['VNAMES']
PRIORITY = ['voicechat', 'neoorigins', 'diaithir', 'alexscaves', 'iceandfire', 'vampirism', 'toolbelt']
allow = set()
for l in open(os.path.join(here, 'allowlist.txt')):
    l = l.split('#')[0].strip()
    if '|' in l: allow.add(frozenset(l.split('|')))
ctx = {r['name']: ctxset(r['context']) for r in csv.DictReader(open(sys.argv[1]))}
rows = []
for l in open(sys.argv[2]):
    n, _, v = l.strip()[4:].partition(':'); v, _, mod = v.partition(':')
    if 'unknown' in v: continue
    vanilla = bool((n[4:] in VNAMES or re.match(r'^key\.hotbar\.\d$', n)))
    c = {'game'} if vanilla or n == 'key.chat' else ctx.get(n, {'game', 'gui'})
    rows.append((n, v, mod.upper() or 'NONE', c, vanilla))
def prio(r):
    if r[4]: return (-1, r[0])
    for i, p in enumerate(PRIORITY):
        if p in r[0].lower(): return (i, r[0])
    return (len(PRIORITY), r[0])
rows.sort(key=prio)
used = {(r[1], r[2]) for r in rows}
keys = [f'key.keyboard.{c}' for c in 'abcdefghijklmnopqrstuvwxyz0123456789'] + [f'key.keyboard.f{i}' for i in (4, 6, 7, 8, 9, 10, 12)] \
     + [f'key.keyboard.keypad.{i}' for i in range(10)] + ['key.keyboard.home', 'key.keyboard.end', 'key.keyboard.insert', 'key.keyboard.delete', 'key.keyboard.page.up', 'key.keyboard.page.down'] \
     + [f'key.keyboard.{c}' for c in ('comma', 'period', 'slash', 'semicolon', 'apostrophe', 'left.bracket', 'right.bracket', 'backslash', 'minus', 'equal', 'grave.accent', 'up', 'down', 'left', 'right')]
pool = [(k, m) for k in keys for m in ('ALT', 'CONTROL', 'SHIFT', 'NONE')]
pool = [p for p in pool if not (p[1] == 'NONE' and not p[0].startswith('key.keyboard.keypad') and p[0] not in ('key.keyboard.home', 'key.keyboard.end', 'key.keyboard.insert', 'key.keyboard.delete', 'key.keyboard.page.up', 'key.keyboard.page.down') and not re.match(r'.*\.f\d+$', p[0]))]
placed = []; out = {}
def clash(n, k, m, c):
    return any(p[1] == k and p[2] == m and p[3] & c and frozenset((p[0], n)) not in allow for p in placed)
for n, k, m, c, v in rows:
    if not v and clash(n, k, m, c):
        nk = next(((pk, pm) for pk, pm in pool if (pk, pm) not in used and not clash(n, pk, pm, c)), None)
        if nk is None: print('NO FREE KEY for', n, file=sys.stderr); placed.append((n, k, m, c)); continue
        used.add(nk); k, m = nk; out[n] = nk
    placed.append((n, k, m, c))
with open(sys.argv[3], 'w') as f:
    for n, (k, m) in sorted(out.items()): f.write(f'key_{n}:{k}' + (f':{m}' if m != 'NONE' else '') + '\n')
print(len(out), 'remapped of', len(rows), 'bound mappings')
