#!/usr/bin/env python3
"""Generates every data and asset file of Diaithir Myth Beasts: creature table, models, skins, spawn egg icons,
loot tables, lang, spawn modifier and biome tag.

Creature stats and behaviours are adapted from Greek Fantasy by skyjay1 (GPLv3). No upstream model, texture, sound
or language file is used: the geometry, skins and names below are new. Names are coined Diaithiris names.

    python3 tools/gen_myth.py
"""
import json, os, sys, unicodedata
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import paint

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, 'src/main/resources')
NS = 'diaithir_myth'
CREATURES_JSON = '/mnt/project-files/mythology/out/creatures.json'


def ascii_id(s):
    return unicodedata.normalize('NFD', s).encode('ascii', 'ignore').decode().lower()


from models import *


# ----------------------------------------------------------------------------------------------------------
# creature table: json id -> (builder, upstream creature it adapts, stats, size, loot, sounds, abilities)
# stats: upstream health/attack/speed; beasts adapted from boss-grade upstream creatures are scaled and marked.
# ----------------------------------------------------------------------------------------------------------
S = 'minecraft:entity.'
SPEC = {
    'hellenic.goburdoiri': dict(model=m_goburdoiri, up='Satyr', hp=28, atk=2.5, spd=0.35, size=(0.6, 1.9), eggs=((104, 80, 58), (196, 186, 160)),
                                loot=[('leather', 0, 2), ('bone', 0, 1)], snd=('goat.ambient', 'goat.hurt', 'goat.death')),
    'hellenic.famuirkeid': dict(model=m_famuirkeid, up='Gigante', hp=100, atk=5.5, spd=0.22, kb=0.9, armor=5, size=(1.3, 3.0), eggs=((128, 120, 108), (98, 74, 50)),
                                loot=[('bone', 1, 4), ('stick', 1, 5)], snd=('iron_golem.step', 'iron_golem.hurt', 'iron_golem.death'), look=32, xp=20),
    'italic.maktirimatuir': dict(model=m_maktirimatuir, up='Centaur', hp=34, atk=4.0, spd=0.25, kb=0.5, armor=1, size=(1.3, 2.2), eggs=((108, 80, 58), (42, 36, 36)),
                                 loot=[('leather', 1, 3), ('arrow', 0, 3)], snd=('horse.ambient', 'horse.hurt', 'horse.death')),
    'italic.famuirknok': dict(model=m_famuirknok, up='Talos (bronze automaton)', hp=80, atk=6.0, spd=0.22, kb=0.8, armor=6, size=(1.1, 2.6), eggs=((140, 106, 66), (100, 124, 106)),
                              loot=[('copper_ingot', 1, 4), ('iron_nugget', 0, 4)], snd=('iron_golem.step', 'iron_golem.damage', 'iron_golem.death'), look=28, xp=18, scaled='Talos stats scaled to 0.3 hp'),
    'italic.dourasniechgleidid': dict(model=m_dourasniechgleidid, up='Cerberus', hp=190, atk=8.0, spd=0.3125, size=(1.9, 1.6), eggs=((56, 44, 38), (96, 48, 40)),
                                      loot=[('bone', 3, 8), ('emerald', 2, 5)], snd=('wolf.growl', 'wolf.hurt', 'wolf.death'), look=32, xp=60, ability='slam'),
    'minoan.tarbiumachuoirtach': dict(model=m_tarbiumachuoirtach, up='Cretan Minotaur', hp=114, atk=9.0, spd=0.31, size=(1.2, 2.8), eggs=((56, 44, 38), (112, 56, 46)),
                                      loot=[('leather', 2, 5), ('bone', 2, 5), ('emerald', 2, 5)], snd=('ravager.ambient', 'ravager.hurt', 'ravager.death'), look=32, xp=60, ability='charge'),
    'minoan.griubluchuirt': dict(model=m_griublochuirt, up='Pegasus', hp=28, atk=0.0, spd=0.25, fly=1.32, size=(1.3, 1.7), eggs=((206, 202, 192), (172, 164, 148)),
                                 loot=[('feather', 1, 3), ('leather', 0, 2)], snd=('horse.ambient', 'horse.hurt', 'horse.death')),
    'minoan.tarbmoir': dict(model=m_tarbmoir, up='Bronze Bull', hp=65, atk=5.0, spd=0.28, kb=0.5, size=(1.6, 1.7), eggs=((56, 44, 38), (140, 106, 66)),
                            loot=[('leather', 1, 3), ('beef', 1, 3), ('copper_ingot', 0, 2)], snd=('cow.ambient', 'cow.hurt', 'cow.death'), scaled='Bronze Bull stats scaled to 0.28 hp'),
    'anatolian.natuirkriegach': dict(model=m_natuirkriegach, up='Arachne', hp=80, atk=4.5, spd=0.285, size=(1.8, 2.0), eggs=((56, 44, 38), (190, 184, 168)),
                                     loot=[('string', 3, 8), ('spider_eye', 1, 3), ('emerald', 2, 4)], snd=('spider.ambient', 'spider.hurt', 'spider.death'), look=28, xp=50, ability='web'),
    'anatolian.liupurdsielg': dict(model=m_liupurdsielg, up='Nemean Lion', hp=60, atk=6.0, spd=0.28, kb=0.4, armor=4, size=(1.5, 1.4), eggs=((160, 124, 78), (72, 52, 40)),
                                   loot=[('leather', 1, 3), ('bone', 1, 3), ('beef', 0, 2)], snd=('ocelot.hurt', 'ocelot.hurt', 'ocelot.death'), look=24, xp=15, scaled='Nemean Lion stats scaled to 0.6 hp'),
    'anatolian.iuluirbeind': dict(model=m_iuluirbeind, up='Stymphalian bird', hp=16, atk=0.0, spd=0.26, fly=1.35, size=(0.8, 1.3), eggs=((132, 104, 62), (58, 50, 46)),
                                  loot=[('feather', 1, 3), ('copper_nugget', 0, 2)], snd=('parrot.ambient', 'parrot.hurt', 'parrot.death')),
    'canaanite.natuirmoir': dict(model=m_natuirmoir, up='Scylla', hp=140, atk=8.0, spd=0.16, kb=1.0, armor=8, size=(1.9, 2.3), eggs=((142, 160, 164), (70, 108, 108)),
                                 loot=[('prismarine_shard', 2, 6), ('ink_sac', 2, 6), ('emerald', 2, 5)], snd=('wolf.growl', 'wolf.hurt', 'wolf.death'), look=32, xp=60, ability='slam', scaled='Scylla speed raised from 0.08 to 0.16'),
    'canaanite.leomundkraeb': dict(model=m_leomundkraeb, up='Dryad', hp=30, atk=3.0, spd=0.28, armor=4, size=(0.6, 1.8), eggs=((96, 78, 56), (72, 104, 56)),
                                   loot=[('stick', 1, 3), ('apple', 0, 2), ('oak_sapling', 0, 1)], snd=('villager.ambient', 'villager.hurt', 'villager.death')),
    'canaanite.leomundcheistrigchatuir': dict(model=m_leomundcheistrigchatuir, up='Naiad', hp=24, atk=3.0, spd=0.25, armor=1, size=(0.6, 1.8), eggs=((142, 160, 164), (70, 108, 108)),
                                              loot=[('prismarine_crystals', 0, 2), ('kelp', 0, 3)], snd=('villager.ambient', 'villager.hurt', 'villager.death')),
    'nilotic.madadfasaigratad': dict(model=m_madadfasaigratad, up='Orthus', hp=24, atk=4.5, spd=0.29, size=(0.9, 1.1), eggs=((150, 124, 88), (74, 92, 62)),
                                     loot=[('bone', 1, 2), ('leather', 0, 1)], snd=('fox.ambient', 'fox.hurt', 'fox.death')),
    'nilotic.toimsechunleomund': dict(model=m_toimsechunleomund, up='Python', hp=70, atk=4.5, spd=0.31, kb=0.8, armor=2, size=(2.0, 1.0), eggs=((150, 128, 86), (112, 56, 46)),
                                      loot=[('leather', 3, 6), ('spider_eye', 1, 3), ('emerald', 2, 5)], snd=('spider.ambient', 'spider.hurt', 'spider.death'), look=32, xp=50, ability='venom'),
    'nilotic.natuirkrun': dict(model=m_natuirkrun, up='Cerastes', hp=24, atk=1.0, spd=0.31, size=(0.6, 0.5), eggs=((74, 92, 62), (200, 190, 164)),
                               loot=[('spider_eye', 0, 1), ('bone', 0, 1)], snd=('spider.ambient', 'spider.hurt', 'spider.death'), look=8, ability='poisonhit'),
    'nilotic.daelaggrianu': dict(model=m_daelaggrianu, up='Harpy', hp=24, atk=0.0, spd=0.22, fly=1.875, kb=0.3, size=(0.7, 1.6), eggs=((98, 76, 58), (176, 138, 106)),
                                 loot=[('feather', 1, 3), ('bone', 0, 1)], snd=('parrot.ambient', 'parrot.hurt', 'parrot.death')),
}
# avatar creatures that are peaceful until struck (the table's hostile flag decides who starts a fight)


def build_model(creature_id, builder):
    m = builder()
    rects = []  # (part, boxidx, w, h)
    for p in m.parts:
        for bi, b in enumerate(p.boxes):
            x, y, z, w, h, d = b[:6]
            rects.append((p, bi, int(np.ceil(2 * d + 2 * w)), int(np.ceil(d + h))))
    # box sizes may be fractional; use ceil for packing and integer dims for the unwrap
    pos, W, H = paint.pack([(r[2], r[3]) for r in rects])
    arr = np.zeros((H, W, 3), np.float32)
    arr[:] = (80, 72, 64)
    rng = np.random.default_rng(sum(map(ord, creature_id)))
    jparts = []
    byname = {p.name: p for p in m.parts}
    idx = 0
    for p in m.parts:
        boxes = []
        for bi, b in enumerate(p.boxes):
            x, y, z, w, h, d, mat, eyes = b
            u, v = pos[idx]; idx += 1
            iw, ih, idp = w, h, d
            paint.paint_box(arr, u, v, iw, ih, idp, mat, rng, eyes)
            boxes.append([u, v, x - p.pivot[0], y - p.pivot[1], z - p.pivot[2], w, h, d])
        par = byname[p.parent].pivot if p.parent else (0, 0, 0)
        jparts.append(dict(name=p.name, parent=p.parent, pivot=[p.pivot[i] - par[i] for i in range(3)], rot=list(p.rot), boxes=boxes))
    img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), 'RGB')
    return dict(tex=[W, H], parts=jparts), img


def lang_name(c):
    return c['name']


def main():
    data = json.load(open(CREATURES_JSON))
    byid = {ascii_id(c['id']): c for c in data}
    for d in ('assets/%s/textures/entity' % NS, 'assets/%s/textures/item' % NS, 'assets/%s/models/item' % NS, 'assets/%s/items' % NS,
              'assets/%s/lang' % NS, 'assets/%s/myth_models' % NS, 'data/%s/loot_table/entities' % NS, 'data/%s/neoforge/biome_modifier' % NS,
              'data/%s/tags/worldgen/biome' % NS, 'data/%s/myth' % NS):
        os.makedirs(os.path.join(RES, d), exist_ok=True)
    table, lang, spawners, courts = [], {}, [], {}
    for jid, sp in SPEC.items():
        c = byid[jid]
        assert c['court'] in ('hellenic', 'minoan', 'italic', 'anatolian', 'canaanite', 'nilotic') and c['existing'] == '-' and not c['living'], jid
        cid = ascii_id(jid.split('.', 1)[1])
        model, img = build_model(cid, sp['model'])
        json.dump(model, open(os.path.join(RES, 'assets/%s/myth_models/%s.json' % (NS, cid)), 'w'), separators=(',', ':'))
        img.save(os.path.join(RES, 'assets/%s/textures/entity/%s.png' % (NS, cid)))
        e1, e2 = sp['eggs']
        paint.egg_icon(e1, e2, sum(map(ord, cid))).save(os.path.join(RES, 'assets/%s/textures/item/%s_spawn_egg.png' % (NS, cid)))
        json.dump({'model': {'type': 'minecraft:model', 'model': '%s:item/%s_spawn_egg' % (NS, cid)}},
                  open(os.path.join(RES, 'assets/%s/items/%s_spawn_egg.json' % (NS, cid)), 'w'), indent=2)
        json.dump({'parent': 'minecraft:item/generated', 'textures': {'layer0': '%s:item/%s_spawn_egg' % (NS, cid)}},
                  open(os.path.join(RES, 'assets/%s/models/item/%s_spawn_egg.json' % (NS, cid)), 'w'), indent=2)
        pools = []
        for item, lo, hi in sp['loot']:
            pools.append({'rolls': 1.0, 'bonus_rolls': 0.0, 'entries': [{'type': 'minecraft:item', 'name': 'minecraft:' + item, 'functions': [
                {'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': float(lo), 'max': float(hi)}, 'add': False},
                {'function': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 1.0}}]}]})
        json.dump({'type': 'minecraft:entity', 'pools': pools}, open(os.path.join(RES, 'data/%s/loot_table/entities/%s.json' % (NS, cid)), 'w'), indent=2)
        avatar = c['preset'] == 'avatar'
        flyer = c['preset'] == 'flyer'
        weight = 1 if avatar else (2 if c['hostile'] else 3)
        entry = dict(id=cid, name=c['name'], court=c['court'], habitat=c['habitat'], preset=c['preset'], hostile=c['hostile'], age=c['age'],
                     upstream=sp['up'], health=sp['hp'], attack=sp['atk'], speed=sp['spd'], flying_speed=sp.get('fly', 0.0),
                     knockback_resistance=sp.get('kb', 0.0), armor=sp.get('armor', 0.0), width=sp['size'][0], height=sp['size'][1],
                     follow_range=sp.get('look', 16), xp=sp.get('xp', 5 if c['hostile'] else 2), ability=sp.get('ability', 'none'),
                     boss=avatar, flyer=flyer, weight=weight, egg_primary=(e1[0]<<16)|(e1[1]<<8)|e1[2], egg_secondary=(e2[0]<<16)|(e2[1]<<8)|e2[2],
                     sounds=dict(ambient='minecraft:entity.' + sp['snd'][0], hurt='minecraft:entity.' + sp['snd'][1], death='minecraft:entity.' + sp['snd'][2]))
        if 'scaled' in sp:
            entry['stat_note'] = sp['scaled']
        table.append(entry)
        courts[cid] = c['court']
        lang['entity.%s.%s' % (NS, cid)] = c['name']
        lang['item.%s.%s_spawn_egg' % (NS, cid)] = c['name'] + ' Spawn Egg'
        spawners.append({'type': '%s:%s' % (NS, cid), 'weight': weight, 'minCount': 1, 'maxCount': 1 if avatar else 2})
    lang['itemGroup.%s' % NS] = 'Diaithir Myth Beasts'
    lang['%s.boss.phase2' % NS] = 'The beast is enraged'
    json.dump(dict(version=1, creatures=table), open(os.path.join(RES, 'data/%s/myth/creatures.json' % NS), 'w'), indent=1, ensure_ascii=False)
    json.dump(dict(comment='court of each creature in this mod; living-tradition courts are not represented', courts=courts),
              open(os.path.join(RES, 'data/%s/myth/courts.json' % NS), 'w'), indent=1)
    json.dump(lang, open(os.path.join(RES, 'assets/%s/lang/en_us.json' % NS), 'w'), indent=2, ensure_ascii=False)
    json.dump({'type': 'neoforge:add_spawns', 'biomes': '#%s:spawns' % NS, 'spawners': spawners},
              open(os.path.join(RES, 'data/%s/neoforge/biome_modifier/spawns.json' % NS), 'w'), indent=2)
    json.dump({'replace': False, 'values': ['#minecraft:is_overworld']}, open(os.path.join(RES, 'data/%s/tags/worldgen/biome/spawns.json' % NS), 'w'), indent=2)
    print('wrote', len(table), 'creatures')


if __name__ == '__main__':
    main()
