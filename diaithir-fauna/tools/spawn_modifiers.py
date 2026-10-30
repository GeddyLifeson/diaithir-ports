#!/usr/bin/env python3
"""Biome modifiers that add each plan to the overworld spawn lists, from species.json: weight 8 per plan (most plans do not fit a given biome, so the rest together roughly match the vanilla animals),
group size from the living species of the plan (herd flag and mass). Run on its own after editing species.json."""
import json, os, statistics

D = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources/data/diaithirfauna')

def group(members, flags):
    herd = flags.index('herd'); pack = flags.index('pack')
    kg = statistics.median(s['kg'] for s in members)
    frac = sum(1 for s in members if s['flags'] >> herd & 1) / len(members)
    pk = sum(1 for s in members if s['flags'] >> pack & 1) / len(members)
    carn = sum(1 for s in members if s['diet'] in ('carn', 'pisc')) / len(members)
    if frac >= 0.3: return (3, 6) if kg < 500 else (2, 4)
    if pk >= 0.3: return 2, 4
    if carn >= 0.6 and kg > 20: return 1, 1
    if kg < 1: return 2, 5
    return (1, 3) if kg < 100 else (1, 2)

def write(names=None):
    d = json.load(open(os.path.join(D, 'species.json')))
    bm = os.path.join(D, 'neoforge/biome_modifier'); os.makedirs(bm, exist_ok=True)
    for n in names or json.load(open(os.path.join(D, 'plans/index.json'))):
        live = [s for s in d['species'] if s['plan'] == n and not s.get('fossil')]
        f = os.path.join(bm, n + '.json')
        if not live:   # plans of fossil taxa only (revival, fossils) never spawn wild
            if os.path.exists(f): os.remove(f)
            continue
        lo, hi = group(live, d['flags'])
        json.dump({'type': 'neoforge:add_spawns', 'biomes': '#minecraft:is_overworld',
                   'spawners': {'type': f'diaithirfauna:{n}', 'weight': 8, 'minCount': lo, 'maxCount': hi}}, open(f, 'w'))

if __name__ == '__main__':
    write()
