#!/usr/bin/env python3
"""Generates every data/asset file of diaithir-firearms from the tables below (clean-room, MIT).
Usage: python3 -I tools/gen.py <module dir>.  Writes src/main/resources/{guns.json, assets, data}, age_gates.json,
names_map.json (English only; Diaithiris names come from the names pipeline), id_migrations.json, library/firearms_library.json.
Textures are drawn here with PIL (16x16, flat shapes, no source art)."""
import json, os, re, sys
from PIL import Image, ImageDraw

M = sys.argv[1] if len(sys.argv) > 1 else '.'
R = f'{M}/src/main/resources'
NS = 'diaithirfirearms'

# id, English, class, ammo, damage (hp), cooldown ticks, mag, reload ticks, spread deg, range m, pellets, auto, age, recoil deg, volume
GUNS = [
 ('sidearm_light', 'Light Sidearm', 'pistol', 'ammo_9mm', 5, 4, 15, 40, 1.5, 40, 1, False, 'VI', 1.2),
 ('sidearm_heavy', 'Heavy Sidearm', 'pistol', 'ammo_45', 6.5, 5, 8, 44, 1.5, 40, 1, False, 'VI', 1.8),
 ('magnum', 'Magnum Revolver', 'pistol', 'ammo_magnum', 10, 12, 6, 60, 1.0, 55, 1, False, 'VI', 4.0),
 ('smg_compact', 'Compact Submachine Gun', 'smg', 'ammo_9mm', 4, 2, 30, 50, 3.0, 30, 1, True, 'VII', 1.0),
 ('carbine', 'Carbine', 'rifle', 'ammo_556', 6, 3, 30, 56, 2.0, 70, 1, True, 'VII', 1.4),
 ('assault_rifle', 'Assault Rifle', 'rifle', 'ammo_762', 8, 4, 30, 60, 2.2, 75, 1, True, 'VII', 2.0),
 ('marksman_rifle', 'Marksman Rifle', 'rifle', 'ammo_762', 12, 8, 20, 64, 0.6, 110, 1, False, 'VII', 2.6),
 ('sniper_rifle', 'Sniper Rifle', 'sniper', 'ammo_762', 22, 30, 5, 70, 0.1, 160, 1, False, 'VII', 5.0),
 ('antimaterial_rifle', 'Anti-Materiel Rifle', 'sniper', 'ammo_50bmg', 34, 40, 5, 90, 0.15, 200, 1, False, 'RODOS', 8.0),
 ('pump_shotgun', 'Pump Shotgun', 'shotgun', 'ammo_12g', 2.5, 20, 6, 70, 5.5, 25, 8, False, 'VI', 5.0),
 ('combat_shotgun', 'Combat Shotgun', 'shotgun', 'ammo_12g', 2.5, 10, 8, 80, 5.0, 25, 8, False, 'VII', 4.0),
 ('auto_shotgun', 'Automatic Shotgun', 'shotgun', 'ammo_12g', 2.2, 5, 20, 90, 6.0, 22, 8, True, 'VII', 3.0),
 ('light_mg', 'Light Machine Gun', 'lmg', 'ammo_556', 5.5, 3, 100, 120, 3.5, 70, 1, True, 'VII', 1.6),
 ('minigun', 'Rotary Minigun', 'lmg', 'ammo_762', 6, 1, 200, 140, 4.5, 60, 1, True, 'RODOS', 0.8),
 ('grenade_launcher', 'Grenade Launcher', 'launcher', 'grenade_40mm', 12, 24, 6, 90, 1.0, 60, 1, False, 'VII', 4.0),
 ('rocket_launcher', 'Rocket Launcher', 'launcher', 'rocket', 25, 50, 1, 100, 0.5, 90, 1, False, 'RODOS', 6.0),
 ('energy_rifle', 'Energy Rifle', 'energy', 'energy_cell', 9, 6, 24, 60, 0.5, 90, 1, True, 'RODOS', 0.6),
]
AMMO = [('ammo_9mm', '9 mm Round', 'VI'), ('ammo_45', '.45 Round', 'VI'), ('ammo_magnum', 'Magnum Round', 'VI'),
        ('ammo_556', '5.56 mm Round', 'VII'), ('ammo_762', '7.62 mm Round', 'VII'), ('ammo_12g', '12 Gauge Shell', 'VI'),
        ('ammo_50bmg', '.50 Round', 'RODOS'), ('grenade_40mm', '40 mm Grenade', 'VII'), ('rocket', 'Rocket', 'RODOS'),
        ('energy_cell', 'Energy Cell', 'RODOS')]
# attachments: id, English, kind, value, age
ATT = [('sight_red_dot', 'Red Dot Sight', 'sight', 1.25, 'VII'), ('sight_x4', 'Four-Power Scope', 'sight', 4.0, 'VII'),
       ('sight_x8', 'Eight-Power Scope', 'sight', 8.0, 'VII'), ('suppressor', 'Suppressor', 'muzzle_quiet', 0.85, 'VII'),
       ('muzzle_brake', 'Muzzle Brake', 'muzzle_recoil', 0.6, 'VII'), ('foregrip', 'Foregrip', 'grip', 0.7, 'VII')]
PARTS = [('gunmetal_nugget', 'Gunmetal Nugget', 'VI'), ('gunmetal_ingot', 'Gunmetal Ingot', 'VI'), ('gunmetal_mesh', 'Gunmetal Mesh', 'VI'),
         ('gun_internals', 'Gun Internals', 'VI')]

# Point Blank id -> new id. First matching rule wins; everything else listed in UNMAPPED at the end of the run.
RULES = [
 (r'.*_?suppressor.*|xm7_suppressor', 'suppressor'), (r'.*muzzlebrake|.*compensator', 'muzzle_brake'),
 (r'.*grip.*|glockstock|m9_stock', 'foregrip'),
 (r'hamr|hi_red.*|hawk_scope|wolf_scope|specter|spear.*|drake_scope|eaglescope|acog|precision_scope|moa.*|srs', 'sight_x4'),
 (r'aimpoint.*|holographic.*|operatorreflex|delta|rspec|tti_viper|cantedrail', 'sight_red_dot'),
 (r'glock17|m9|p30l', 'sidearm_light'), (r'm1911a1|usp45', 'sidearm_heavy'), (r'deserteagle|rhino', 'magnum'),
 (r'mp5|mp7.*|tmp|p90|vector|ump45|glock18|ar57|m950|fn40.*', 'smg_compact'),
 (r'm4a1.*|m4sopmodii.*|hk416|ro635|g36[ck]|star15.*|m16a3kit|xm7|scarl.*|aug.*', 'carbine'),
 (r'ak47|ak12|ak74|an94|g3|g41|m16a1|uar10|xm29|ak_romgrip', 'assault_rifle'),
 (r'mk14ebr|sl8|wa2000|xm3', 'marksman_rifle'), (r'l96a1.*|c14', 'sniper_rifle'), (r'gm6lynx|ballista', 'antimaterial_rifle'),
 (r'm870|m590|citoricxs|hs12', 'pump_shotgun'), (r'm1014|spas12|m870modshotgun', 'combat_shotgun'), (r'aa12', 'auto_shotgun'),
 (r'lamg|m249|mk48', 'light_mg'), (r'm134minigun', 'minigun'),
 (r'm32mgl|m203launcher|gp25|grenade', 'grenade_launcher'), (r'javelin|at4|smaw', 'rocket_launcher'),
 (r'plasmagun|ulg99cannon', 'energy_rifle'),
 (r'ammo9mm|ammo46|ammo57|bullet|ammodefault|ammocreative', 'ammo_9mm'), (r'ammo45acp|ammo50ae', 'ammo_45'), (r'ammo357', 'ammo_magnum'),
 (r'ammo556|ammo545|ammo68', 'ammo_556'), (r'ammo762.*|ammo338lapua', 'ammo_762'), (r'ammo12gauge', 'ammo_12g'),
 (r'ammo50bmg', 'ammo_50bmg'), (r'grenade20mm|grenade40mm', 'grenade_40mm'), (r'.*rocket|rocket', 'rocket'), (r'ammolasercharge', 'energy_cell'),
 (r'gunmetal_ingot|gunmetal_nugget|gunmetal_mesh', None), (r'guninternals|motor|processor', 'gun_internals'),
 (r'printer|workstation', 'minecraft:smithing_table'),
]
# Explicit fixes that the regexes above cannot express (a grenade/rocket item vs launcher of the same prefix).
FIX = {'grenade': 'grenade_40mm', 'm32mgl': 'grenade_launcher', 'javelin_rocket': 'rocket', 'at4_rocket': 'rocket', 'smaw_rocket': 'rocket',
       'srs': 'sight_x8', 'precision_scope': 'sight_x8', 'ak_suppressor': 'suppressor', 'm4a1_asiimov': 'carbine', 'l96a1_hyperbeast': 'sniper_rifle',
       'm4sopmodii_bartender': 'carbine', 'test20': 'smg_compact', 'spear': 'sight_x4', 'spearblack': 'sight_x4', 'fn40': 'grenade_launcher',
       'm203launcher': 'grenade_launcher', 'gp25': 'grenade_launcher', 'm870modshotgun': 'combat_shotgun', 'ulg99cannon': 'energy_rifle'}

def w(path, data):
    p = f'{M}/{path}'; os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, 'w').write(data if isinstance(data, str) else json.dumps(data, indent=1, ensure_ascii=False))

def tex(name, fn):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im); fn(d)
    p = f'{R}/assets/{NS}/textures/item/{name}.png'; os.makedirs(os.path.dirname(p), exist_ok=True); im.save(p)

DARK, MID, LIGHT, WOOD, BRASS = (38, 40, 46, 255), (74, 78, 88, 255), (122, 128, 140, 255), (110, 74, 42, 255), (196, 150, 58, 255)
CYAN = (90, 220, 240, 255)

def draw_gun(cls):
    def f(d):
        L = {'pistol': 6, 'smg': 9, 'rifle': 13, 'sniper': 15, 'shotgun': 13, 'lmg': 14, 'launcher': 15, 'energy': 12}[cls]
        x0 = 8 - L // 2; y = 6
        d.rectangle([x0, y, x0 + L, y + 2], fill=MID)                 # body
        d.rectangle([x0 + L - 3, y - 1, x0 + L, y + 1], fill=DARK)    # barrel end
        d.rectangle([x0 + 1, y + 3, x0 + 3, y + 6], fill=DARK)        # grip
        d.rectangle([x0 + L // 2, y + 3, x0 + L // 2 + 1, y + 4], fill=LIGHT)
        if cls in ('rifle', 'sniper', 'shotgun'): d.rectangle([x0 - 2, y, x0, y + 2], fill=WOOD)
        if cls == 'sniper': d.rectangle([x0 + 4, y - 2, x0 + 8, y - 1], fill=LIGHT)
        if cls == 'lmg': d.rectangle([x0 + 5, y + 3, x0 + 9, y + 7], fill=BRASS)
        if cls == 'launcher': d.rectangle([x0, y - 1, x0 + L, y + 3], outline=DARK)
        if cls == 'energy': d.rectangle([x0 + 4, y, x0 + 9, y + 1], fill=CYAN)
        d.line([x0, y, x0 + L, y], fill=LIGHT)
    return f

def draw_ammo(aid):
    def f(d):
        if aid == 'ammo_12g': d.rectangle([6, 4, 9, 12], fill=(190, 40, 40, 255)); d.rectangle([6, 11, 9, 12], fill=BRASS)
        elif aid == 'grenade_40mm': d.ellipse([5, 4, 10, 12], fill=(80, 100, 60, 255)); d.rectangle([7, 2, 8, 4], fill=BRASS)
        elif aid == 'rocket': d.rectangle([7, 2, 9, 12], fill=(90, 90, 96, 255)); d.polygon([(7, 2), (9, 2), (8, 0)], fill=(190, 40, 40, 255))
        elif aid == 'energy_cell': d.rectangle([5, 3, 10, 12], fill=DARK); d.rectangle([6, 5, 9, 10], fill=CYAN)
        else:
            h = {'ammo_9mm': 6, 'ammo_45': 7, 'ammo_magnum': 8, 'ammo_556': 9, 'ammo_762': 10, 'ammo_50bmg': 12}[aid]
            d.rectangle([7, 12 - h, 8, 12], fill=BRASS); d.rectangle([7, 12 - h, 8, 13 - h + 1], fill=(160, 100, 50, 255))
    return f

def draw_att(aid):
    def f(d):
        if aid.startswith('sight'): d.rectangle([4, 7, 11, 9], fill=DARK); d.rectangle([5, 5, 10, 6], fill=MID); d.point((8, 8), fill=(220, 40, 40, 255))
        elif aid == 'suppressor': d.rectangle([3, 6, 12, 9], fill=MID); d.line([3, 7, 12, 7], fill=LIGHT)
        elif aid == 'muzzle_brake': d.rectangle([5, 6, 10, 9], fill=DARK); d.line([6, 6, 6, 9], fill=LIGHT); d.line([9, 6, 9, 9], fill=LIGHT)
        else: d.rectangle([6, 3, 9, 12], fill=DARK); d.rectangle([7, 3, 8, 12], fill=MID)
    return f

def draw_part(pid):
    def f(d):
        if pid == 'gunmetal_ingot': d.rectangle([3, 6, 12, 10], fill=MID); d.line([3, 6, 12, 6], fill=LIGHT)
        elif pid == 'gunmetal_nugget': d.ellipse([6, 6, 10, 10], fill=MID)
        elif pid == 'gunmetal_mesh':
            for i in range(3, 13, 3): d.line([i, 3, i, 12], fill=MID); d.line([3, i, 12, i], fill=MID)
        else: d.rectangle([4, 5, 11, 11], fill=DARK); d.ellipse([5, 6, 10, 10], fill=BRASS); d.point((7, 8), fill=DARK)
    return f

names, ages, items = {}, {}, []
def item(i, english, age, tex_fn, handheld=True):
    items.append(i); names[i] = {'english': english}; ages[f'{NS}:{i}'] = age
    w(f'src/main/resources/assets/{NS}/items/{i}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:item/{i}'}})
    w(f'src/main/resources/assets/{NS}/models/item/{i}.json', {'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{i}'}})
    tex(i, tex_fn)

for g in GUNS: item(g[0], g[1], g[12], draw_gun(g[2]))
for a, e, ag in AMMO: item(a, e, ag, draw_ammo(a), False)
for a, e, k, v, ag in ATT: item(a, e, ag, draw_att(a), False)
for p, e, ag in PARTS: item(p, e, ag, draw_part(p), False)

guns = [{'id': g[0], 'class': g[2], 'ammo': g[3], 'damage': g[4], 'cooldown': g[5], 'mag': g[6], 'reload': g[7], 'spread': g[8],
         'range': g[9], 'pellets': g[10], 'auto': g[11], 'age': g[12], 'recoil': g[13]} for g in GUNS]
w('src/main/resources/guns.json', {'guns': guns, 'attachments': [{'id': a[0], 'kind': a[2], 'value': a[3]} for a in ATT]})
w('src/main/resources/assets/%s/lang/en_us.json' % NS, {f'item.{NS}.{i}': names[i]['english'] for i in items} | {
    f'{NS}.reload': 'Reloading', f'{NS}.empty': 'No ammunition', f'{NS}.mag': 'Magazine', f'{NS}.age': 'Needs Age %s to handle well',
    f'{NS}.attached': 'Attached', f'{NS}.detached': 'Removed', f'key.{NS}.reload': 'Reload', f'key.categories.{NS}': 'Diaithir Firearms'})
w('names_map.json', names)
w('age_gates.json', {'_doc': 'Same shape as DiaithirRPG content/tech_ages.json. Firearms start at Age VI; energy weapons and heavy weapons wait for Rodos.',
                     'namespaces': {NS: 'VI'}, 'items': ages})

# tags and recipes
w(f'src/main/resources/data/{NS}/tags/item/ammo.json', {'values': [f'{NS}:{a[0]}' for a in AMMO]})
w(f'src/main/resources/data/{NS}/tags/item/guns.json', {'values': [f'{NS}:{g[0]}' for g in GUNS]})
w('src/main/resources/data/c/tags/item/ingots/gunmetal.json', {'values': [f'{NS}:gunmetal_ingot']})
w('src/main/resources/data/c/tags/item/nuggets/gunmetal.json', {'values': [f'{NS}:gunmetal_nugget']})
w('src/main/resources/data/c/tags/item/ammo.json', {'values': [f'#{NS}:ammo']})
def recipe(name, body):
    if 'pattern' in body:
        used=set(''.join(body['pattern'])); body['key']={k:v for k,v in body['key'].items() if k in used}
    w(f'src/main/resources/data/{NS}/recipe/{name}.json', body)
recipe('gunmetal_ingot', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['III', 'ICI', 'III'][:0] or ['NN', 'NN'],
                          'key': {'N': f'{NS}:gunmetal_nugget'}, 'result': {'id': f'{NS}:gunmetal_ingot', 'count': 1}})
recipe('gunmetal_nuggets', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [f'{NS}:gunmetal_ingot'],
                            'result': {'id': f'{NS}:gunmetal_nugget', 'count': 4}})
recipe('gunmetal_blend', {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ['#c:ingots/iron', '#c:ingots/copper', 'minecraft:coal'],
                          'result': {'id': f'{NS}:gunmetal_ingot', 'count': 2}})
recipe('gunmetal_mesh', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['N N', ' N ', 'N N'], 'key': {'N': f'{NS}:gunmetal_nugget'},
                         'result': {'id': f'{NS}:gunmetal_mesh', 'count': 1}})
recipe('gun_internals', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['MR', 'RM'],
                         'key': {'M': f'{NS}:gunmetal_mesh', 'R': 'minecraft:redstone'}, 'result': {'id': f'{NS}:gun_internals', 'count': 1}})
BODY = {'pistol': ('IM', ' I'), 'smg': ('III', 'MP '), 'rifle': ('IIM', 'PI '), 'sniper': ('IIM', 'PIM'), 'shotgun': ('IIM', 'PI '),
        'lmg': ('IIM', 'PIM'), 'launcher': ('III', 'PMI'), 'energy': ('IMD', 'PIM')}
for g in GUNS:
    cls = g[2]; top, bot = BODY[cls]
    # I = gunmetal ingot, M = mesh, P = internals, D = diamond (energy lens)
    recipe(g[0], {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'pattern': [top.ljust(3), bot.ljust(3)],
                  'key': {'I': '#c:ingots/gunmetal', 'M': f'{NS}:gunmetal_mesh', 'P': f'{NS}:gun_internals', 'D': 'minecraft:diamond'},
                  'result': {'id': f'{NS}:{g[0]}', 'count': 1}})
AM = {'ammo_9mm': 8, 'ammo_45': 8, 'ammo_magnum': 6, 'ammo_556': 8, 'ammo_762': 8, 'ammo_12g': 6, 'ammo_50bmg': 4, 'grenade_40mm': 2, 'rocket': 1, 'energy_cell': 2}
for a, n in AM.items():
    extra = {'ammo_12g': 'minecraft:paper', 'grenade_40mm': 'minecraft:tnt', 'rocket': 'minecraft:firework_rocket', 'energy_cell': 'minecraft:redstone'}.get(a, 'minecraft:gunpowder')
    ing = [f'{NS}:gunmetal_nugget', extra] + ([extra] if a in ('ammo_762', 'ammo_50bmg') else [])
    recipe(a, {'type': 'minecraft:crafting_shapeless', 'category': 'equipment', 'ingredients': ing, 'result': {'id': f'{NS}:{a}', 'count': n}})
ATTR = {'sight_red_dot': ['IGI', ' M '], 'sight_x4': ['GMG', 'IMI'], 'sight_x8': ['GMG', 'GMG'], 'suppressor': ['III', 'MMM'],
        'muzzle_brake': ['MIM', 'MIM'], 'foregrip': ['I  ', 'MI ']}
for a, pat in ATTR.items():
    recipe(a, {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'pattern': [p.ljust(3) for p in pat],
               'key': {'I': '#c:ingots/gunmetal', 'M': f'{NS}:gunmetal_mesh', 'G': 'minecraft:glass_pane'}, 'result': {'id': f'{NS}:{a}', 'count': 1}})

# id migrations from the pack's item list
ids = [l.split('\t')[0].split(':', 1)[1] for l in open('/mnt/project-files/pack_item_ids.tsv') if l.startswith('pointblank:')]
mig, unmapped = {}, []
for i in ids:
    t = FIX.get(i)
    if t is None:
        for pat, tgt in RULES:
            if re.fullmatch(pat, i): t = tgt; break
        else: unmapped.append(i); continue
    if i in ('gunmetal_ingot', 'gunmetal_nugget', 'gunmetal_mesh'): t = i
    if t is None: unmapped.append(i); continue
    mig[f'pointblank:{i}'] = t if ':' in t else f'{NS}:{t}'
w('id_migrations.json', {'_doc': 'Point Blank (all rights reserved) ids -> diaithir-firearms archetypes. Old guns become the nearest class; skins fold into the base gun; scopes by zoom tier. Applied by the consolidation id-migration step.',
                         'items': mig})
w('library/firearms_library.json', {'entries': [{'id': f'{NS}:{g[0]}', 'name': g[1], 'class': g[2], 'age': g[12], 'ammo': f'{NS}:{g[3]}',
                                                'damage_hp': g[4], 'magazine': g[6], 'range_m': g[9]} for g in GUNS]})
print(len(items), 'items;', len(mig), 'migrated;', 'unmapped:', unmapped)
