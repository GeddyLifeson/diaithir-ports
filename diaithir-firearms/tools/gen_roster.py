#!/usr/bin/env python3
"""Adds the real-world roster (roster/*.csv + roster/calibers.csv) to diaithir-firearms. Run AFTER gen.py:
python3 -I tools/gen.py . && python3 -I tools/gen_roster.py .
One entry per design (its most iconic version), no variants. Stats derive from class + caliber energy + real rpm/magazine."""
import csv, glob, hashlib, json, math, os, sys
from PIL import Image, ImageDraw

M = sys.argv[1] if len(sys.argv) > 1 else '.'
R = f'{M}/src/main/resources'
NS = 'diaithirfirearms'
rd = lambda p: json.load(open(f'{M}/{p}'))
def w(path, data):
    p = f'{M}/{path}'; os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, 'w').write(data if isinstance(data, str) else json.dumps(data, indent=1, ensure_ascii=False))

EXISTING_AMMO = {'ammo_9mm': 'VI', 'ammo_45': 'VI', 'ammo_magnum': 'VI', 'ammo_556': 'VII', 'ammo_762': 'VII', 'ammo_12g': 'VI', 'ammo_50bmg': 'RODOS',
                 'grenade_40mm': 'VII', 'rocket': 'RODOS', 'energy_cell': 'RODOS'}
# class: (spread deg, base range m, recoil deg, reload ticks, sound class, stonecut base, handheld length)
CLS = {'pistol': (1.5, 40, 1.5, 40, 'sidearm_light', 6), 'smg': (3.0, 30, 1.0, 50, 'smg_compact', 9), 'rifle': (2.0, 60, 2.0, 60, 'carbine', 13),
       'sniper': (0.15, 120, 5.0, 70, 'sniper_rifle', 15), 'shotgun': (5.5, 25, 4.0, 70, 'pump_shotgun', 13), 'lmg': (3.5, 60, 1.6, 120, 'light_mg', 14),
       'launcher': (1.0, 70, 5.0, 100, 'rocket_launcher', 15), 'musket': (3.5, 40, 4.0, 160, None, 15)}
def age(year): return 'V' if year < 1861 else 'VI' if year < 1946 else 'VII'
AGE_NO = {'V': 5, 'VI': 6, 'VII': 7, 'RODOS': 8}

cal = {}
for l in open(f'{M}/roster/calibers.csv'):
    if l.startswith('#') or not l.strip(): continue
    i, label, e, y, kind, ex = (l.rstrip('\n').split(',') + [''])[:6]
    cal[i] = dict(id=i if i in EXISTING_AMMO or i == 'grenade_40mm' or i == 'rocket' else 'ammo_' + i, label=label, e=float(e), year=int(y), kind=kind, existing=bool(ex))
rows = []
for f in sorted(glob.glob(f'{M}/roster/b*.csv')):
    for l in open(f):
        if l.startswith('#') or not l.strip(): continue
        p = l.rstrip('\n').split('|')
        rows.append(dict(id=p[0], name=p[1], country=p[2], year=int(p[3]), cls=p[4], cal=p[5], auto=p[6] == '1', mag=int(p[7]), rpm=float(p[8]), wiki=(p[9] if len(p) > 9 and p[9] else p[1])))
ids = [r['id'] for r in rows]; assert len(ids) == len(set(ids)), 'duplicate roster id'
G = rd('src/main/resources/guns.json')
base_ids = {g['id'] for g in G['guns'] if not g.get('roster')}
assert not (set(ids) & base_ids), set(ids) & base_ids
for r in rows: assert r['cal'] in cal, (r['id'], r['cal'])
assert 'age' in G['guns'][0]

def stats(r):
    sp, rg, rc, rl, _, _ = CLS[r['cls']][:6]; c = cal[r['cal']]; e = c['e']
    pellets = 8 if r['cls'] == 'shotgun' else 1
    dmg = round(math.sqrt(e / pellets) / (7.5 if pellets > 1 else 4.5), 1)
    if r['cls'] == 'launcher': dmg = 25.0
    rng = int(min(200, rg + (e / 60 if r['cls'] in ('rifle', 'sniper', 'lmg', 'musket') else 0))) if r['cls'] != 'launcher' else rg
    return {'id': r['id'], 'class': r['cls'], 'ammo': c['id'], 'damage': dmg, 'cooldown': max(1, round(1200 / r['rpm'])), 'mag': r['mag'],
            'reload': rl, 'spread': sp, 'range': rng, 'pellets': pellets, 'auto': r['auto'], 'age': age(r['year']),
            'recoil': round(rc * (e / 2000) ** 0.3, 1), 'roster': True}

G['guns'] = [g for g in G['guns'] if not g.get('roster')] + [stats(r) for r in rows]
used = sorted({cal[r['cal']]['id'] for r in rows} - set(EXISTING_AMMO))
G['ammo'] = used
w('src/main/resources/guns.json', G)

# ---- textures / models / items
DARK, MID, LIGHT, WOOD, BRASS = (38, 40, 46, 255), (74, 78, 88, 255), (122, 128, 140, 255), (110, 74, 42, 255), (196, 150, 58, 255)
def hsh(s, n): return int(hashlib.md5(s.encode()).hexdigest(), 16) % n
def tex(name, fn):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); fn(ImageDraw.Draw(im))
    p = f'{R}/assets/{NS}/textures/item/{name}.png'; os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)
def draw_gun(r):
    cls = r['cls']; h = hsh(r['id'], 1 << 20)
    def f(d):
        L = CLS[cls][5]; x0 = 8 - L // 2; y = 6
        wood = [(110, 74, 42, 255), (84, 52, 30, 255), (140, 100, 60, 255), (60, 62, 56, 255)][h & 3]
        body = [MID, (58, 62, 70, 255), (90, 92, 98, 255), (70, 66, 56, 255)][(h >> 2) & 3]
        d.rectangle([x0, y, x0 + L, y + 2], fill=body)
        d.rectangle([x0 + L - 3, y - 1, x0 + L, y + 1], fill=DARK)
        d.rectangle([x0 + 1, y + 3, x0 + 3, y + 6 + ((h >> 4) & 1)], fill=wood if cls in ('pistol', 'musket') else DARK)
        d.rectangle([x0 + L // 2, y + 3, x0 + L // 2 + 1, y + 4], fill=LIGHT)
        if cls in ('rifle', 'sniper', 'shotgun', 'musket'): d.rectangle([x0 - 2, y, x0, y + 2 + ((h >> 5) & 1)], fill=wood)
        if cls == 'musket': d.rectangle([x0 + 4, y + 3, x0 + L - 4, y + 3], fill=wood)
        if cls == 'sniper' or (cls == 'rifle' and (h >> 6) & 3 == 0): d.rectangle([x0 + 4, y - 2, x0 + 8, y - 1], fill=LIGHT)
        if cls == 'lmg': d.rectangle([x0 + 5, y + 3, x0 + 9, y + 7], fill=BRASS)
        if cls == 'smg' and (h >> 8) & 1: d.rectangle([x0 + 4, y + 3, x0 + 5, y + 8], fill=DARK)
        if cls == 'launcher': d.rectangle([x0, y - 1, x0 + L, y + 3], outline=DARK)
        d.line([x0, y, x0 + L, y], fill=LIGHT)
    return f
def draw_ammo(c):
    hh = max(4, min(13, int(3 + math.log10(c['e'] + 10) * 2.4))); h = hsh(c['id'], 1 << 12)
    def f(d):
        col = BRASS if c['kind'] == 'cartridge' else (70, 70, 74, 255)
        if c['kind'] == 'powder': d.ellipse([5, 5, 10, 10], fill=col); d.rectangle([11, 4 + h % 3, 12, 11], fill=(190, 170, 120, 255))
        else:
            d.rectangle([7, 12 - hh, 8, 12], fill=col); d.rectangle([7, 12 - hh, 8, 13 - hh + 1], fill=[(160, 100, 50, 255), (170, 170, 176, 255), (120, 60, 40, 255)][h % 3])
            if h & 8: d.rectangle([6, 12, 9, 13], fill=col)
    return f
def item(i, tex_fn, handheld):
    w(f'src/main/resources/assets/{NS}/items/{i}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:item/{i}'}})
    w(f'src/main/resources/assets/{NS}/models/item/{i}.json', {'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{i}'}})
    tex(i, tex_fn)
for r in rows: item(r['id'], draw_gun(r), True)
ammo_age = {}
for r in rows:
    a = cal[r['cal']]['id']; ammo_age[a] = min(ammo_age.get(a, 9), AGE_NO[age(r['year'])])
RN = {v: k for k, v in AGE_NO.items()}
for c in cal.values():
    if c['id'] in used: item(c['id'], draw_ammo(c), False)

# ---- lang, ages, tags, recipes, library
lang = rd(f'src/main/resources/assets/{NS}/lang/en_us.json')
lang = {k: v for k, v in lang.items() if not k.startswith('rosteritem.')}
for r in rows: lang[f'item.{NS}.{r["id"]}'] = r['name']
for c in cal.values():
    if c['id'] in used: lang[f'item.{NS}.{c["id"]}'] = c['label']
w(f'src/main/resources/assets/{NS}/lang/en_us.json', lang)
ag = rd('age_gates.json')
for r in rows: ag['items'][f'{NS}:{r["id"]}'] = age(r['year'])
for a in used: ag['items'][f'{NS}:{a}'] = RN[ammo_age[a]]
w('age_gates.json', ag)
guns_tag = rd(f'src/main/resources/data/{NS}/tags/item/guns.json'); ammo_tag = rd(f'src/main/resources/data/{NS}/tags/item/ammo.json')
for r in rows:
    if f'{NS}:{r["id"]}' not in guns_tag['values']: guns_tag['values'].append(f'{NS}:{r["id"]}')
for a in used:
    if f'{NS}:{a}' not in ammo_tag['values']: ammo_tag['values'].append(f'{NS}:{a}')
w(f'src/main/resources/data/{NS}/tags/item/guns.json', guns_tag); w(f'src/main/resources/data/{NS}/tags/item/ammo.json', ammo_tag)
def recipe(name, body): w(f'src/main/resources/data/{NS}/recipe/{name}.json', body)
for r in rows:   # stonecutter: one recipe per design, no pattern clashes between guns of a class
    src = CLS[r['cls']][4]
    ing = f'{NS}:{src}' if src else 'minecraft:iron_ingot'
    recipe(r['id'], {'type': 'minecraft:stonecutting', 'ingredient': ing, 'result': {'id': f'{NS}:{r["id"]}', 'count': 1}})
for a in used:
    c = next(x for x in cal.values() if x['id'] == a)
    if c['kind'] == 'powder': ing = ['minecraft:iron_nugget', 'minecraft:gunpowder', 'minecraft:paper']
    elif c['e'] > 8000: ing = [f'{NS}:gunmetal_nugget', 'minecraft:gunpowder', 'minecraft:gunpowder']
    else: ing = [f'{NS}:gunmetal_nugget', 'minecraft:gunpowder']
    recipe(a, {'type': 'minecraft:crafting_shapeless', 'category': 'equipment', 'ingredients': ing, 'result': {'id': f'{NS}:{a}', 'count': 8}})
lib = [e for e in rd('library/firearms_library.json') if not (len(e) > 5 and e[5] == 'roster')]
for r in rows:
    c = cal[r['cal']]; st = next(g for g in G['guns'] if g['id'] == r['id'])
    lib.append([r['name'], 'item', AGE_NO[st['age']], 'firearms', f"{r['name']} ({r['country']}, {r['year']}): {r['cls']} firing {c['label']}. {st['damage']:g} hp"
                f"{' x8 pellets' if st['pellets'] > 1 else ''} a shot, {r['rpm']:g} rounds a minute, {'automatic' if r['auto'] else 'one shot per pull'}, {r['mag']} in the magazine, "
                f"effective to {st['range']} m. Source: https://en.wikipedia.org/wiki/{r['wiki'].replace(' ', '_')}", 'roster'])
w('library/firearms_library.json', lib)
# roster listing
with open(f'{M}/roster/ROSTER.md', 'w') as o:
    o.write('# Mass-produced firearm roster (generated from roster/*.csv)\n\nOne entry per design, most iconic version, no variants. Stats approximate, derived from class, caliber muzzle energy and real rate of fire.\n\n| Gun | Country | Year | Class | Round | Age | Source |\n|---|---|---|---|---|---|---|\n')
    for r in sorted(rows, key=lambda r: (r['year'], r['name'])):
        o.write(f"| {r['name']} | {r['country']} | {r['year']} | {r['cls']} | {cal[r['cal']]['label']} | {age(r['year'])} | [wiki](https://en.wikipedia.org/wiki/{r['wiki'].replace(' ', '_')}) |\n")
print(len(rows), 'roster guns;', len(used), 'new ammo types')
