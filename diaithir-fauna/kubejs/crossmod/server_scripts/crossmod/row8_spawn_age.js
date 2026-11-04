// Written by diaithir-fauna/tools/crossmod_row8.py. Cancels NATURAL spawns of a creature mod's mobs before the Age that allows them.
// Pattern copied from earth_timeline/extinctions.js: checkSpawn only sees the spawn reason (command, egg, spawner, breeding are exempt),
// the verdict is given when the entity joins the level, never for entities loaded from disk. Spawns that bypass the event count as natural.
// Age: diaithir_ages AgesSavedData (as speuran/age_gate.js); falls back to the score of #age in diaithirfauna_age (0 Telling, 1..7 = I..VII, 9 Rodos).
// UNTESTED in game. Table: data/diaithirfauna/spawn_ages.json (same content, for other tools).
(function () {
  const AGES = ["TELLING", "I", "II", "III", "IV", "V", "VI", "VII", "RODOS"]
  const MIN = {"untamedwilds:*": "I", "diaithirfauna:*": "I", "iceandfire:hippogryph": "III", "iceandfire:hippocampus": "III", "iceandfire:pixie": "III", "iceandfire:amphithere": "III", "iceandfire:cockatrice": "III", "iceandfire:stymphalian_bird": "III", "iceandfire:siren": "III", "iceandfire:sea_serpent": "III", "iceandfire:deathworm": "III", "iceandfire:cyclops": "III", "iceandfire:troll": "III", "iceandfire:gorgon": "III", "iceandfire:hydra": "III", "iceandfire:ghost": "III", "iceandfire:fire_dragon": "IV", "iceandfire:ice_dragon": "IV", "iceandfire:lightning_dragon": "V", "iceandfire:dread_thrall": "IV", "iceandfire:dread_ghoul": "IV", "iceandfire:dread_beast": "IV", "iceandfire:dread_scuttler": "IV", "iceandfire:dread_knight": "IV", "iceandfire:dread_horse": "IV", "iceandfire:dread_lich": "IV", "iceandfire:dread_queen": "IV", "iceandfire:*": "III", "vampirism:hunter": "IV", "vampirism:vampire": "IV", "vampirism:blinding_bat": "IV", "vampirism:ghost": "IV", "vampirism:advanced_hunter": "V", "vampirism:advanced_vampire": "V", "vampirism:vampire_baron": "V", "vampirism:dracula": "V", "vampirism:*": "IV", "mutantmonsters:*": "V", "aether_ii:*": "I"}
  const DIM_ONLY = {"aether_ii": "aether_ii:"}
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
