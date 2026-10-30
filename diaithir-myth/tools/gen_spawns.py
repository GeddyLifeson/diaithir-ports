#!/usr/bin/env python3
"""Writes the spawn data of Diaithir Myth Beasts from data/diaithir_myth/myth/creatures.json:

  data/diaithir_myth/spawn_regions/<creature>.json      lat/lon boxes of the creature's court region and its minimum Age
  data/diaithir_myth/tags/worldgen/biome/habitat/*.json biome tags per habitat word (coasts, mountains, deserts ...)
  data/diaithir_myth/neoforge/biome_modifier/<creature>.json  one add_spawns modifier per creature, on its habitat tag

Court regions come from the 'region' text of /mnt/project-files/mythology/out/courts.json. Boxes are degrees
(lat -90..90, lon -180..180); a box whose lon minimum is above its maximum wraps the antimeridian. They are
generous on purpose (sea margins included), because the game world is Earth at 1:5 scale and a creature that
never meets its own sea would be pointless. Run: python3 tools/gen_spawns.py
"""
import json, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'src/main/resources')
NS = 'diaithir_myth'

# court id -> (courts.json region text, boxes as (lat_min, lat_max, lon_min, lon_max))
COURT_BOXES = {
    'hellenic': ('Greece, Aegean, Anatolian coast', [(34.5, 41.8, 19.3, 29.5)]),
    'italic': ('Italy, Etruria, Latium', [(36.4, 46.6, 6.6, 18.7)]),
    'minoan': ('Crete and the Aegean', [(34.0, 36.0, 23.0, 27.0), (35.5, 40.2, 23.3, 28.0)]),
    'anatolian': ('Anatolia', [(35.8, 42.2, 26.0, 44.8)]),
    'canaanite': ('Levant and Phoenicia', [(29.5, 37.1, 33.5, 38.5)]),
    'nilotic': ('Nile valley', [(22.0, 31.8, 25.0, 35.0), (14.0, 22.0, 30.0, 35.0)]),
}

# habitat word -> biome tags (vanilla and the NeoForge c: convention tags; c: ones are optional)
HABITAT = {
    'woods': ['#minecraft:is_forest', '#minecraft:is_taiga'],
    'groves': ['#minecraft:is_forest', '#minecraft:is_hill', '#minecraft:is_savanna'],
    'mountains': ['#minecraft:is_mountain', '#minecraft:is_hill'],
    'peaks': ['#minecraft:is_mountain'],
    'hills': ['#minecraft:is_hill', '#c:is_plains', '#minecraft:is_savanna'],
    'volcano': ['#minecraft:is_mountain', '#minecraft:is_badlands', '#minecraft:is_hill'],
    'gates': ['#minecraft:is_mountain', '#minecraft:is_hill', '#c:is_stony_shores'],
    'labyrinths': ['#minecraft:is_hill', '#minecraft:is_mountain', '#minecraft:is_badlands', '#c:is_plateau'],
    'cliffs': ['#minecraft:is_mountain', '#c:is_stony_shores', '#minecraft:is_beach', '#minecraft:is_hill'],
    'sea': ['#minecraft:is_beach', '#c:is_stony_shores', '#minecraft:is_river', '#minecraft:is_ocean'],
    'temples': ['#c:is_plains', '#minecraft:is_hill', '#c:is_desert', '#minecraft:is_savanna'],
    'desert': ['#c:is_desert', '#c:is_sandy', '#minecraft:is_badlands', '#minecraft:is_savanna'],
    'dunes': ['#c:is_desert', '#c:is_sandy', '#minecraft:is_badlands'],
    'reeds': ['#minecraft:is_river', '#c:is_swamp', '#minecraft:is_beach', '#c:is_desert', '#c:is_plains'],
}


def main():
    creatures = json.load(open(os.path.join(RES, 'data/%s/myth/creatures.json' % NS)))['creatures']
    for d in ('spawn_regions', 'tags/worldgen/biome/habitat', 'neoforge/biome_modifier'):
        os.makedirs(os.path.join(RES, 'data', NS, d), exist_ok=True)
    for old in ('tags/worldgen/biome/spawns.json', 'neoforge/biome_modifier/spawns.json'):
        p = os.path.join(RES, 'data', NS, old)
        if os.path.exists(p):
            os.remove(p)
    for h, tags in HABITAT.items():
        values = [t if t.startswith('#minecraft:') else {'id': t, 'required': False} for t in tags]
        json.dump({'replace': False, 'values': values},
                  open(os.path.join(RES, 'data', NS, 'tags/worldgen/biome/habitat/%s.json' % h), 'w'), indent=2)
    for c in creatures:
        region, boxes = COURT_BOXES[c['court']]
        assert c['habitat'] in HABITAT, c['habitat']
        json.dump({'creature': '%s:%s' % (NS, c['id']), 'court': c['court'], 'region': region, 'min_age': c['age'],
                   'boxes': [{'lat': [b[0], b[1]], 'lon': [b[2], b[3]]} for b in boxes]},
                  open(os.path.join(RES, 'data', NS, 'spawn_regions/%s.json' % c['id']), 'w'), indent=2)
        json.dump({'type': 'neoforge:add_spawns', 'biomes': '#%s:habitat/%s' % (NS, c['habitat']),
                   'spawners': [{'type': '%s:%s' % (NS, c['id']), 'weight': c['weight'], 'minCount': 1, 'maxCount': 1 if c['boss'] else 2}]},
                  open(os.path.join(RES, 'data', NS, 'neoforge/biome_modifier/%s.json' % c['id']), 'w'), indent=2)
    print('wrote spawn data for', len(creatures), 'creatures')


if __name__ == '__main__':
    main()
