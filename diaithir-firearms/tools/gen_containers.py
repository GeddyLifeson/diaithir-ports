#!/usr/bin/env python3
"""S6918 Munitions container wiring (contract: Global-Repo plans/MUNITIONS_CONTAINERS.md). Run AFTER gen.py and gen_roster.py:
python3 -I tools/gen_containers.py .
Writes src/main/resources/container_kinds.json (gun class -> container kinds in preference order, read by Defs) and the item tags
diaithircrafting:uses_container/<kind> and diaithircrafting:ammo_class/<class> listing every gun."""
import json, os, sys

M = sys.argv[1] if len(sys.argv) > 1 else '.'
R = f'{M}/src/main/resources'
KINDS = {'pistol': ['magazine', 'speedloader'], 'smg': ['magazine', 'drum'], 'rifle': ['magazine', 'clip'], 'sniper': ['magazine'],
         'lmg': ['belt', 'magazine', 'drum'], 'energy': ['cell'], 'shotgun': [], 'musket': [], 'launcher': []}

def w(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w').write(json.dumps(data, indent=1, ensure_ascii=False))

guns = json.load(open(f'{R}/guns.json'))['guns']
classes = {g['class'] for g in guns}
assert classes <= set(KINDS), classes - set(KINDS)
w(f'{R}/container_kinds.json', KINDS)
tags = f'{R}/data/diaithircrafting/tags/item'
by_kind = {}
for g in guns:
    for k in KINDS[g['class']]:
        by_kind.setdefault(k, []).append(f"diaithirfirearms:{g['id']}")
for k, v in by_kind.items():
    w(f'{tags}/uses_container/{k}.json', {'values': v})
for c in sorted(classes):
    w(f'{tags}/ammo_class/{c}.json', {'values': [f"diaithirfirearms:{g['id']}" for g in guns if g['class'] == c]})
print('kinds', {k: len(v) for k, v in by_kind.items()}, 'classes', len(classes))
