// Species die out on their real dates. global.etNS.EXTINCTIONS (extinctions_data.js, from world/earth/timeline) holds
// [fadeStartLc, extinctLc, [species ids]]: new spawns thin out linearly over the fade years, then stop. Animals already
// in the world (loaded from disk), tamed ones and ones spawned by command, egg or spawner block are left alone.
// flood.js stores the engine's year under global.etNS.etYearKey. The data file loads after this one (alphabetical), so the
// table is read on the first spawn, not at load.
//
// A species id is not an entity type:
//  - Untamed Wilds has one entity type per family (untamedwilds:rhino) and a VARIANT number per species (wooly = 5, from
//    data/untamedwilds/entities/rhino.json), so 'untamedwilds:rhino_wooly' is looked up in VARIANTS.
//  - Diaithir Bestiary has a few entity types (diaithirbestiary:beast, flyer, ...) and a creature id ('kind') per species,
//    so 'diaithirbestiary:eachreidhlein' is matched against Creature.kind().
// Both the variant and the kind are only set while the mob is being spawned (Mob.finalizeSpawn, after the FinalizeSpawn
// event that checkSpawn sees), so the verdict is given when the entity joins the level, and only for entities that were
// not loaded from disk. checkSpawn is used for one thing: it sees the spawn reason, and a reason that is not natural
// exempts that entity. Spawns that bypass the event (Bestiary's own spawner, Untamed Wilds' chunk-generation fauna) have
// no reason and count as natural.
(function () {
  const EXT_KEY = 'earth_timeline_lc'
  // EntitySpawnReason names that count as "natural"; everything else (command, egg, breeding, bucket, spawner block) is exempt.
  const REASONS = { NATURAL: true, CHUNK_GENERATION: true }
  // species id -> [entity type, VARIANT value]
  const VARIANTS = {
    'untamedwilds:rhino_wooly': ['untamedwilds:rhino', 5],
    'untamedwilds:big_cat_cave_lion': ['untamedwilds:big_cat', 0],
    'untamedwilds:big_cat_smilodon_fatalis': ['untamedwilds:big_cat', 10],
    'untamedwilds:ground_sloth_paramylodon': ['untamedwilds:ground_sloth', 1],
    'untamedwilds:ground_sloth_megalonyx': ['untamedwilds:ground_sloth', 2],
    'untamedwilds:canid_dire_wolf': ['untamedwilds:canid', 1],
    'untamedwilds:bear_short_faced': ['untamedwilds:bear', 8],
    'untamedwilds:cervid_stag_moose': ['untamedwilds:cervid', 5],
    'untamedwilds:cervid_megaloceros': ['untamedwilds:cervid', 0],
    'untamedwilds:camel_western': ['untamedwilds:camel', 3],
    'untamedwilds:elephantid_woolly': ['untamedwilds:elephantid', 0],
    'untamedwilds:elephantid_columbian': ['untamedwilds:elephantid', 1],
    'untamedwilds:elephantid_mastodon': ['untamedwilds:elephantid', 2],
    'untamedwilds:bison_steppe': ['untamedwilds:bison', 2],
    'untamedwilds:bison_ancient': ['untamedwilds:bison', 3],
    'untamedwilds:bovid_aurochs': ['untamedwilds:bovid', 0]
  }
  const BESTIARY = 'diaithirbestiary:'

  let byType = null // entity type id -> { variants: { n: row } }
  let byKind = null // Bestiary creature id -> row
  let exempt = {} // uuid -> true: spawn with a non-natural reason, seen in checkSpawn, not yet joined
  let exemptCount = 0

  function table() {
    if (byType) return byType
    byType = {}
    byKind = {}
    ;(global.etNS.EXTINCTIONS || []).forEach(row => row[2].forEach(id => {
      let v = VARIANTS[id]
      if (v) {
        let e = byType[v[0]] || (byType[v[0]] = { variants: {} })
        e.variants[v[1]] = row
      } else if (id.indexOf(BESTIARY) === 0) byKind[id] = row
      else console.warn('[earth_timeline] extinction id has no mapping to an entity: ' + id)
    }))
    warnMissing()
    return byType
  }

  // A base type that is not an entity type in this pack would silently do nothing; say so once in the log.
  function warnMissing() {
    try {
      let have = {}
      Java.loadClass('net.minecraft.core.registries.BuiltInRegistries').ENTITY_TYPE.keySet().forEach(k => { have[String(k)] = true })
      Object.keys(byType).forEach(id => { if (!have[id]) console.warn('[earth_timeline] extinction entity not in this pack: ' + id) })
    } catch (e) { console.warn('[earth_timeline] could not check extinction entity ids: ' + e) }
  }

  function survival(row, lc) {
    if (lc >= row[1]) return 0
    if (lc <= row[0]) return 1
    return (row[1] - lc) / (row[1] - row[0])
  }

  // The row for this entity, or null if it is not an extinct-able species.
  function rowFor(entity) {
    let t = String(entity.type)
    let e = table()[t]
    if (e) return e.variants[Number(entity.getVariant())] || null
    if (t.indexOf(BESTIARY) === 0 && typeof entity.kind === 'function') return byKind[String(entity.kind())] || null
    return null
  }

  function tamed(entity) {
    try {
      if (typeof entity.owner === 'function') return entity.owner() != null // Bestiary Creature: owner UUID
      if (typeof entity.isTame === 'function') return !!entity.isTame() // Untamed Wilds (TamableAnimal)
    } catch (e) { /* not a tameable mob */ }
    return false
  }

  EntityEvents.checkSpawn(event => {
    if (REASONS[String(event.type)]) return
    if (exemptCount > 256) { exempt = {}; exemptCount = 0 }
    exempt[String(event.entity.uuid)] = true
    exemptCount++
  })

  NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent'), event => {
    if (event.loadedFromDisk() || event.getLevel().isClientSide()) return
    let entity = event.getEntity()
    let key = String(entity.uuid)
    if (exemptCount && exempt[key]) { delete exempt[key]; exemptCount--; return }
    let lc = event.getLevel().getServer().persistentData.getIntOr(EXT_KEY, -1)
    if (lc < 0) return
    let row = rowFor(entity)
    if (!row) return
    if (tamed(entity)) return
    if (Math.random() >= survival(row, lc)) event.setCanceled(true)
  })
})()
