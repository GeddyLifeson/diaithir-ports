#!/usr/bin/env python3
"""One-off generator: Minecraft 26.1 client item definitions (assets/deeperdarker/items/*.json) and equipment assets.
Upstream's generated assets still use the 1.21 'overrides' model predicates, which 26.1 ignores.
Run from the module root: python3 tools/gen_assets_26_1.py
"""
import json, os, re, glob, shutil
from PIL import Image, ImageDraw

R = 'src/generated/resources/assets/deeperdarker'
M = 'src/main/resources/assets/deeperdarker'
MODEL = lambda m: {"type": "minecraft:model", "model": m}
os.makedirs(R + '/items', exist_ok=True)

def have_model(i):
    return os.path.exists(f'{R}/models/item/{i}.json') or os.path.exists(f'{M}/models/item/{i}.json')

def write(i, model):
    json.dump({"model": model}, open(f'{R}/items/{i}.json', 'w'), indent=2)

ids = set()
for f in glob.glob(R + '/models/item/*.json') + glob.glob(M + '/models/item/*.json'):
    ids.add(os.path.basename(f)[:-5])
# block items without an item model: use the block model
for f in glob.glob(R + '/models/block/*.json'):
    i = os.path.basename(f)[:-5]
    if i not in ids and os.path.exists(f'{R}/blockstates/{i}.json'): ids.add(i)
variants = {i for i in ids if re.search(r'_(linked|charging|charged|broken|\d\d)$', i) or '_trim' in i}

SPAWN = {  # mob -> (body, spots) original egg colours
 'anger_pot': ('#7a2e2e', '#d6a24a'), 'angler_fish': ('#1f3a5a', '#7fd0e8'), 'fear_pot': ('#3b2a5a', '#b9a3e3'),
 'sculk_centipede': ('#0f2f38', '#2fb6c9'), 'sculk_leech': ('#16303b', '#52d6c4'), 'sculk_snapper': ('#14323a', '#8fe0d0'),
 'shattered': ('#10242c', '#3fa7c4'), 'shriek_worm': ('#1a2f3a', '#e0f2f5'), 'sludge': ('#18343a', '#4fa89a'),
 'sorrow_pot': ('#23304f', '#7f9fd8'), 'stalker': ('#0c1f28', '#58c9e0'),
}
def egg(name, body, spots):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im)
    d.ellipse((3, 1, 12, 14), fill=body, outline='#0a0a0a')
    for xy in ((6, 5), (9, 8), (5, 10), (8, 3)): d.point(xy, fill=spots); d.point((xy[0] + 1, xy[1]), fill=spots)
    im.save(f'{M}/textures/item/{name}_spawn_egg.png')
for mob, (b, s) in SPAWN.items():
    egg(mob, b, s)
    json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"deeperdarker:item/{mob}_spawn_egg"}}, open(f'{R}/models/item/{mob}_spawn_egg.json', 'w'), indent=2)

for i in sorted(ids - variants):
    if i == 'ancient_compass':
        comp = json.load(open(f'{R}/models/item/ancient_compass.json'))['overrides']
        ent = [{"threshold": o['predicate']['minecraft:angle'], "model": MODEL(o['model'])} for o in comp]
        ent.sort(key=lambda e: e['threshold'])
        rd = lambda tgt: {"type": "minecraft:range_dispatch", "property": "minecraft:compass", "target": tgt, "scale": 1.0, "entries": ent}
        write(i, {"type": "minecraft:condition", "property": "minecraft:has_component", "component": "minecraft:lodestone_tracker",
                  "on_true": rd('lodestone'), "on_false": rd('none')})
    elif re.fullmatch(r'(.*_)?sculk_transmitter', i) and have_model(i + '_linked'):
        write(i, {"type": "minecraft:condition", "property": "minecraft:has_component", "component": "deeperdarker:transmitter",
                  "on_true": MODEL(f'deeperdarker:item/{i}_linked'), "on_false": MODEL(f'deeperdarker:item/{i}')})
    elif i == 'sonorous_staff':
        write(i, {"type": "minecraft:condition", "property": "minecraft:using_item",
                  "on_false": MODEL('deeperdarker:item/sonorous_staff'),
                  "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 1 / 128,
                              "fallback": MODEL('deeperdarker:item/sonorous_staff'),
                              "entries": [{"threshold": 0.001, "model": MODEL('deeperdarker:item/sonorous_staff_charging')},
                                          {"threshold": 1.0, "model": MODEL('deeperdarker:item/sonorous_staff_charged')}]}})
    elif i == 'soul_elytra':
        write(i, {"type": "minecraft:condition", "property": "minecraft:broken",
                  "on_false": MODEL('deeperdarker:item/soul_elytra'), "on_true": MODEL('deeperdarker:item/soul_elytra_broken')})
    elif i == 'gloomslate_pot':
        write(i, {"type": "minecraft:special", "base": "minecraft:item/decorated_pot", "model": {"type": "minecraft:decorated_pot"}})
    elif have_model(i):
        write(i, MODEL(f'deeperdarker:item/{i}'))
    else:
        write(i, MODEL(f'deeperdarker:block/{i}'))

# strip dead 1.21 'overrides' from model files, and fix the missing spawn egg parent
for f in glob.glob(R + '/models/item/*.json'):
    d = json.load(open(f))
    if 'overrides' in d:
        del d['overrides']; json.dump(d, open(f, 'w'), indent=2)

# equipment assets + move armour textures to the 26.1 location
for name in ('resonarium', 'warden'):
    os.makedirs(f'{R}/equipment', exist_ok=True)
    json.dump({"layers": {"humanoid": [{"texture": f"deeperdarker:{name}"}], "humanoid_leggings": [{"texture": f"deeperdarker:{name}"}]}},
              open(f'{R}/equipment/{name}.json', 'w'), indent=2)
    for src, sub in ((f'{M}/textures/models/armor/{name}_layer_1.png', 'humanoid'), (f'{M}/textures/models/armor/{name}_layer_2.png', 'humanoid_leggings')):
        if os.path.exists(src):
            os.makedirs(f'{M}/textures/entity/equipment/{sub}', exist_ok=True)
            shutil.move(src, f'{M}/textures/entity/equipment/{sub}/{name}.png')
print(len(os.listdir(R + '/items')), 'item definitions')
