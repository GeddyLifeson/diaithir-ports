#!/usr/bin/env python3
"""Assign diaithir-animation specs to the owner-4 rows (machines, blocks, block entities, vehicles) of ANIMATIONS_COVERAGE.csv.
Usage: python3 -I gen_coverage.py <rules.tsv> <coverage.csv>
Same first-match-wins globs as Rules.java. Rows that match get status 'route' and a concrete engine_route;
rows already past 'route' (wired, done) keep their status. Machines and block entities with no keyword get the generic
working-core PULSE; vehicles with no keyword get the generic vehicle set (seat, wheel, propeller by part name)."""
import csv, fnmatch, sys

rules_path, cov_path = sys.argv[1:3]
OWNER = '4-machines-blocks-vehicles'
rules = []
for line in open(rules_path, encoding='utf-8'):
    if line.startswith('#') or not line.strip():
        continue
    f = line.rstrip('\n').split('\t')
    rules.append((f[0], f[1], f[2], f[8]))

def match(ident):
    ns, _, path = ident.partition(':')
    for pat, kind, axis, cat in rules:
        pn, _, pp = pat.rpartition(':') if ':' in pat else ('', '', pat)
        if pn and not fnmatch.fnmatchcase(ns, pn):
            continue
        if fnmatch.fnmatchcase(path, pp):
            return pat, kind, axis
    return None

FALLBACK = {'machine': ('generic machine', 'PULSE', 'y'), 'block-entity': ('generic block entity', 'PULSE', 'y'),
            'vehicle': ('generic vehicle', 'SWAY', 'z')}
with open(cov_path, newline='', encoding='utf-8') as fh:
    rd = csv.DictReader(fh)
    cols = rd.fieldnames
    rows = list(rd)
n = fb = 0
for r in rows:
    if r['owner'] != OWNER or r['status'] not in ('todo', 'route'):
        continue
    m = match(r['thing'])
    if m is None and r['category'] in FALLBACK:
        m = FALLBACK[r['category']]; fb += 1
    if m is None:
        continue  # decor block with no moving part: stays todo until it gets a model-level rule
    r['engine_route'] = f'diaithir-animation {m[1]} axis {m[2]} (rule {m[0]})'
    r['status'] = 'route'
    n += 1
with open(cov_path, 'w', newline='', encoding='utf-8') as fh:
    w = csv.DictWriter(fh, cols, lineterminator='\n')
    w.writeheader(); w.writerows(rows)
print(f'{n} rows routed ({fb} by fallback)')
