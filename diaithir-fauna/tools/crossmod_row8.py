#!/usr/bin/env python3
"""Row 8 of crossmod/OPEN_ROWS.md: unified drop tags and per-Age natural-spawn gating for Ice and Fire, Untamed Wilds,
Vampirism, Aether II, Mutant Monsters and diaithir-fauna. Reads the mod jars (zip) when given, writes into ../kubejs/crossmod.
Usage: crossmod_row8.py [--jar iceandfire.jar --jar untamedwilds.jar --jar vampirism.jar] [--tech-ages tech_ages.json]
Without jars the tag layer is still written from the curated lists (items are 'required: false', so a missing mod is harmless)."""
import argparse, json, os, re, zipfile

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'kubejs', 'crossmod')
AGES = ['TELLING', 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'RODOS']

# ---- spawn table: entity id (or 'ns:*' namespace default) -> minimum Age. Exact ids beat the namespace default.
SPAWN = {
 'untamedwilds:*': 'I',          # wild fauna; real extinction dates are earth_timeline/extinctions.js
 'diaithirfauna:*': 'I',         # living species; fossils never spawn wild (Revival.java gates revival on diaithirfauna_age)
 # Ice and Fire: mythic fauna Age III, dragons Age IV (lightning V), the Dread realm undead IV
 'iceandfire:hippogryph': 'III', 'iceandfire:hippocampus': 'III', 'iceandfire:pixie': 'III', 'iceandfire:amphithere': 'III',
 'iceandfire:cockatrice': 'III', 'iceandfire:stymphalian_bird': 'III', 'iceandfire:siren': 'III', 'iceandfire:sea_serpent': 'III',
 'iceandfire:deathworm': 'III', 'iceandfire:cyclops': 'III', 'iceandfire:troll': 'III', 'iceandfire:gorgon': 'III',
 'iceandfire:hydra': 'III', 'iceandfire:ghost': 'III',
 'iceandfire:fire_dragon': 'IV', 'iceandfire:ice_dragon': 'IV', 'iceandfire:lightning_dragon': 'V',
 'iceandfire:dread_thrall': 'IV', 'iceandfire:dread_ghoul': 'IV', 'iceandfire:dread_beast': 'IV', 'iceandfire:dread_scuttler': 'IV',
 'iceandfire:dread_knight': 'IV', 'iceandfire:dread_horse': 'IV', 'iceandfire:dread_lich': 'IV', 'iceandfire:dread_queen': 'IV',
 'iceandfire:*': 'III',
 # Vampirism
 'vampirism:hunter': 'IV', 'vampirism:vampire': 'IV', 'vampirism:blinding_bat': 'IV', 'vampirism:ghost': 'IV',
 'vampirism:advanced_hunter': 'V', 'vampirism:advanced_vampire': 'V', 'vampirism:vampire_baron': 'V', 'vampirism:dracula': 'V',
 'vampirism:*': 'IV',
 # Mutant Monsters (jar not available here: namespace default only, ids unverified)
 'mutantmonsters:*': 'V',
 # Aether II: no Age gate here (the dimension gate is the portal); natural spawns allowed only inside the Aether dimension
 'aether_ii:*': 'I',
}
DIM_ONLY = {'aether_ii': 'aether_ii:'}   # entity namespace -> dimension id prefix it may naturally spawn in
NOT_NATURAL = ('converted_', 'minion', 'dummy', 'projectile', 'arrow', 'charge', 'multipart', 'skeleton')  # not spawned naturally; listed only for the table

# ---- tags: curated from the loot tables read in the jars
def opt(*ids): return [{'id': i, 'required': False} for i in ids]
DRAGON_COL = ['amethyst','black','blue','bronze','copper','electric','gray','green','red','sapphire','silver','white']
TAGS = {
 'scales': opt(*[f'iceandfire:dragonscales_{c}' for c in DRAGON_COL], 'iceandfire:shiny_scales',
               *[f'iceandfire:sea_serpent_scales_{c}' for c in ['blue','bronze','deepblue','green','purple','red','teal']]),
 'leathers': opt('iceandfire:troll_leather_forest','iceandfire:troll_leather_frost','iceandfire:troll_leather_mountain') + [{'id': '#c:leather', 'required': False}],
 'leather': opt('iceandfire:troll_leather_forest','iceandfire:troll_leather_frost','iceandfire:troll_leather_mountain'),
 'feathers': opt('iceandfire:amphithere_feather','iceandfire:stymphalian_bird_feather'),
 'eggs': opt(*[f'iceandfire:dragonegg_{c}' for c in DRAGON_COL], 'iceandfire:deathworm_egg', 'iceandfire:deathworm_egg_giant',
             *[f'untamedwilds:egg_{n}' for n in ['arowana','catfish','football_fish','giant_salamander','king_crab','newt','snake','softshell_turtle','spadefish','tarantula','tortoise','trevally']]),
}

def survey(jars):
    rows = {}
    for j in jars:
        z = zipfile.ZipFile(j)
        for n in z.namelist():
            m = re.match(r'data/([a-z_]+)/loot_table/entities/(.+)\.json$', n)
            if m:
                items = sorted(set(re.findall(r'"name":\s*"([a-z_]+:[a-z_0-9/]+)"', z.read(n).decode())))
                rows[f'{m.group(1)}:{m.group(2)}'] = items
    return rows

def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, 'w'), indent=1); open(path, 'a').write('\n')

JS_SPAWN = r'''// Written by diaithir-fauna/tools/crossmod_row8.py. Cancels NATURAL spawns of a creature mod's mobs before the Age that allows them.
// Pattern copied from earth_timeline/extinctions.js: checkSpawn only sees the spawn reason (command, egg, spawner, breeding are exempt),
// the verdict is given when the entity joins the level, never for entities loaded from disk. Spawns that bypass the event count as natural.
// Age: diaithir_ages AgesSavedData (as speuran/age_gate.js); falls back to the score of #age in diaithirfauna_age (0 Telling, 1..7 = I..VII, 9 Rodos).
// UNTESTED in game. Table: data/diaithirfauna/spawn_ages.json (same content, for other tools).
(function () {
  const AGES = __AGES__
  const MIN = __MIN__
  const DIM_ONLY = __DIM__
  const NATURAL = { NATURAL: true, CHUNK_GENERATION: true }
  let exempt = {}, exemptCount = 0, cache = { t: -1, v: 0 }

  function ageIndex(server, level) {
    let t = level.getGameTime ? Number(level.getGameTime()) : 0
    if (cache.t >= 0 && t - cache.t < 100 && t >= cache.t) return cache.v
    let v = -1
    try { v = AGES.indexOf(String(Java.loadClass('net.diaithir.ages.mc.AgesSavedData').get(server).world.age).toUpperCase()) } catch (e) { /* diaithir_ages absent */ }
    if (v < 0) {
      try {
        let sb = server.getScoreboard(), obj = sb.getObjective('diaithirfauna_age')
        let n = obj ? sb.getOrCreatePlayerScore(Java.loadClass('net.minecraft.world.scores.ScoreHolder').forNameOnly('#age'), obj).get() : 0
        v = n >= 9 ? 8 : Math.max(0, n)
      } catch (e) { v = AGES.length } // unknown: open everything rather than silence the world
    }
    cache = { t: t, v: v }
    return v
  }

  function required(id) {
    if (MIN[id]) return MIN[id]
    return MIN[id.split(':')[0] + ':*'] || null
  }

  EntityEvents.checkSpawn(event => {
    if (NATURAL[String(event.type)]) return
    if (exemptCount > 256) { exempt = {}; exemptCount = 0 }
    exempt[String(event.entity.uuid)] = true
    exemptCount++
  })

  NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent'), event => {
    if (event.loadedFromDisk() || event.getLevel().isClientSide()) return
    let entity = event.getEntity()
    let key = String(entity.uuid)
    if (exemptCount && exempt[key]) { delete exempt[key]; exemptCount--; return }
    let id = String(entity.type), ns = id.split(':')[0]
    let need = required(id)
    if (!need) return
    let level = event.getLevel()
    if (DIM_ONLY[ns] && String(level.dimension().location()).indexOf(DIM_ONLY[ns]) !== 0) { event.setCanceled(true); return }
    let server = level.getServer()
    if (ageIndex(server, level) < AGES.indexOf(need)) event.setCanceled(true)
  })
})()
'''

JS_DROPS = r'''// Written by diaithir-fauna/tools/crossmod_row8.py. Adds a standard drop only where the mob's own loot table has none (see NOTES.md).
// Bones (c:bones) for bony Ice and Fire / Vampirism mobs, and eggs (c:eggs) for ground birds of diaithir-fauna. UNTESTED in game.
const CM_BONES = ['iceandfire:hydra', 'iceandfire:sea_serpent', 'iceandfire:gorgon', 'iceandfire:siren', 'iceandfire:troll',
  'vampirism:vampire', 'vampirism:advanced_vampire', 'vampirism:vampire_baron', 'vampirism:hunter', 'vampirism:advanced_hunter']
const CM_EGG_PLANS = ['galliform', 'waterfowl', 'ratite', 'pigeon', 'seabird']
const cmHas = (event, id) => event.drops.stream().anyMatch(i => String(i.item.id) === id)
CM_BONES.forEach(t => EntityEvents.drops(t, event => {
  if (!cmHas(event, 'minecraft:bone') && Math.random() < 0.6) event.addDrop(Item.of('minecraft:bone', 1 + Math.floor(Math.random() * 2)))
}))
CM_EGG_PLANS.forEach(p => EntityEvents.drops('diaithirfauna:' + p, event => {
  if (!cmHas(event, 'minecraft:egg') && Math.random() < 0.25) event.addDrop(Item.of('minecraft:egg'))
}))
'''

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('--jar', action='append', default=[]); ap.add_argument('--tech-ages')
    a = ap.parse_args()
    for t, v in TAGS.items(): write(f'{OUT}/data/c/tags/item/{t}.json', {'replace': False, 'values': v})
    write(f'{OUT}/data/diaithirfauna/spawn_ages.json', {'ages': AGES, 'min_age': SPAWN, 'dimension_only': DIM_ONLY})
    sj = f'{OUT}/server_scripts/crossmod'; os.makedirs(sj, exist_ok=True)
    open(f'{sj}/row8_spawn_age.js', 'w').write(JS_SPAWN.replace('__AGES__', json.dumps(AGES)).replace('__MIN__', json.dumps(SPAWN)).replace('__DIM__', json.dumps(DIM_ONLY)))
    open(f'{sj}/row8_drops.js', 'w').write(JS_DROPS)
    if a.jar:
        rows = survey(a.jar); write(f'{OUT}/data/diaithirfauna/loot_survey.json', rows)
        print(len(rows), 'entity loot tables surveyed')
    if a.tech_ages:
        d = json.load(open(a.tech_ages)); known = set(d['namespaces']) | set(d['nontech'])
        print('namespaces not in tech_ages.json:', sorted({k.split(':')[0] for k in SPAWN} - known))
    print('wrote', OUT)

if __name__ == '__main__': main()
