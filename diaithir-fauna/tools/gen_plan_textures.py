#!/usr/bin/env python3
"""Writes textures/entity/plan/<plan>.png (the renderer's fallback skin) by copying the first textured species of each plan."""
import json, os, shutil
R = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
T = os.path.join(R, 'assets/diaithirfauna/textures/entity')
plans = json.load(open(os.path.join(R, 'data/diaithirfauna/plans/index.json')))
first = {}
for s in json.load(open(os.path.join(R, 'data/diaithirfauna/species.json')))['species']:
    if os.path.exists(f"{T}/{s['id']}.png"): first.setdefault(s['plan'], s['id'])
os.makedirs(f'{T}/plan', exist_ok=True)
for p in plans: shutil.copy(f'{T}/{first[p]}.png', f'{T}/plan/{p}.png')
print(len(plans), 'plan textures')
