#!/usr/bin/env python3
"""Catalogue -> mod data: species.json, plans/index.json, lang. Run after plan_gen.py.

    python3 build_species.py [SKIN_DIR]    (SKIN_DIR with <id>.png copies skins into assets/diaithirfauna/textures/entity)
"""
import json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalogue as C

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src/main/resources')
FLAGS = ['flies', 'flightless', 'nocturnal', 'diurnal', 'herd', 'pack', 'venomous', 'swims', 'aggressive', 'toxic', 'arboreal', 'burrow', 'migratory', 'domesticable', 'scav', 'fast']
HABS = 'forest jungle plains savanna taiga swamp desert badlands mountain icy river lake coast reef ocean_warm ocean_cold ocean_deep cave island tundra grassland wetland'.split()

def species():
    out = []
    for kind in ('mob', 'fossil'):
        for r in C.rows(kind):
            if not r['plan'].startswith('gen:'): continue
            notes = {t.strip().split(' ')[0].lower() for t in r['notes'].split(',')}
            fl = 0
            for i, f in enumerate(FLAGS):
                if f in notes: fl |= 1 << i
            e = dict(id=r['id'], plan=r['plan'][4:], scale=round(float(r['scale']), 3), kg=float(r['kg']), diet=r['diet'], flags=fl)
            if kind == 'mob':
                hab = 0
                for h in r['habitat'].split(';'):
                    if h in HABS: hab |= 1 << HABS.index(h)
                e['hab'] = hab
                if r['ext'] != '-': e['ext'] = int(r['ext'])
                e['boxes'] = [[b['lat'][0], b['lat'][1], b['lon'][0], b['lon'][1]] for b in C.boxes(r['range'])]
            else:
                e['fossil'] = 1
                e['ma'] = [float(r['ma_from']), float(r['ma_to'])]
            e['name'] = r['common']
            out.append(e)
    return out

if __name__ == '__main__':
    sp = species()
    d = os.path.join(RES, 'data/diaithirfauna')
    names = sorted(f[:-5] for f in os.listdir(os.path.join(d, 'plans')) if f != 'index.json')
    json.dump(names, open(os.path.join(d, 'plans/index.json'), 'w'))
    json.dump(dict(flags=FLAGS, habitats=HABS, species=sp), open(os.path.join(d, 'species.json'), 'w'), separators=(',', ':'))
    bm = os.path.join(d, 'neoforge/biome_modifier'); os.makedirs(bm, exist_ok=True)
    for n in names:
        json.dump({'type': 'neoforge:add_spawns', 'biomes': '#minecraft:is_overworld',
                   'spawners': {'type': f'diaithirfauna:{n}', 'weight': 6, 'minCount': 1, 'maxCount': 3}}, open(os.path.join(bm, n + '.json'), 'w'))
    lang = {f'entity.diaithirfauna.{p}': p.replace('_', ' ').title() for p in names}
    lang.update({f'species.diaithirfauna.{s["id"]}': s['name'] for s in sp})
    ld = os.path.join(RES, 'assets/diaithirfauna/lang'); os.makedirs(ld, exist_ok=True)
    json.dump(lang, open(os.path.join(ld, 'en_us.json'), 'w'), ensure_ascii=False, indent=0)
    if len(sys.argv) > 1:
        td = os.path.join(RES, 'assets/diaithirfauna/textures/entity'); os.makedirs(td, exist_ok=True)
        n = 0
        for f in os.listdir(sys.argv[1]):
            if f.endswith('.png'):
                from PIL import Image  # 256-colour palette: 4x smaller, mean error 3/255 on the painted skins
                Image.open(os.path.join(sys.argv[1], f)).convert('RGBA').quantize(256, method=Image.FASTOCTREE, dither=Image.NONE).save(os.path.join(td, f), optimize=True); n += 1
        print(n, 'skins copied')
    print(len(sp), 'species,', len(names), 'plans')
