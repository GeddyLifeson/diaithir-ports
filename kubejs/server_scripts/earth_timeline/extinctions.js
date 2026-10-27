// Species die out on their real dates. global.etNS.EXTINCTIONS (extinctions_data.js, from world/earth/timeline) holds
// [fadeStartLc, extinctLc, [species ids]]: natural spawns thin out linearly over the fade years, then stop. Animals already
// in the world and tamed or spawned by command are left alone. flood.js stores the engine's year under global.etNS.etYearKey.
// The data file loads after this one (alphabetical), so the table is read on the first spawn, not at load.
//
// A species id is not always an entity type. Untamed Wilds has one entity type per family (untamedwilds:rhino) and a
// variant number per species (wooly = 5, from data/untamedwilds/entities/rhino.json), so 'untamedwilds:rhino_wooly' is
// looked up in VARIANTS. The variant is only chosen in Mob.finalizeSpawn, after the FinalizeSpawn event (checkSpawn), so
// checkSpawn only remembers which new natural spawns to judge and the verdict is given when the entity joins the level
// (spawned), when the variant is set. Ids not listed in VARIANTS are taken as entity types (diaithirbestiary:*) and are
// judged straight in checkSpawn. Animals Untamed Wilds places during chunk generation (FaunaSpawn calls finalizeSpawn
// itself, no event) are not affected; only spawns that go through NeoForge's FinalizeSpawn event with a natural reason are.
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

  let byType = null // entity type id -> { whole: row | null, variants: { n: row } }
  let pending = {} // uuid -> true, new natural spawns of a type that needs a variant check
  let pendingCount = 0

  function table() {
    if (byType) return byType
    byType = {}
    ;(global.etNS.EXTINCTIONS || []).forEach(row => row[2].forEach(id => {
      let v = VARIANTS[id]
      let type = v ? v[0] : id
      let e = byType[type] || (byType[type] = { whole: null, variants: {} })
      if (v) e.variants[v[1]] = row
      else e.whole = row
    }))
    warnMissing()
    return byType
  }

  // A type that is not an entity type in this pack would silently do nothing; say so once in the log.
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

  function rollFails(row, lc) {
    return Math.random() >= survival(row, lc)
  }

  EntityEvents.checkSpawn(event => {
    let e = table()[String(event.entity.type)]
    if (!e || !REASONS[String(event.type)]) return
    let lc = event.server.persistentData.getIntOr(EXT_KEY, -1)
    if (lc < 0) return
    if (e.whole && rollFails(e.whole, lc)) { event.cancel(); return }
    if (pendingCount > 256) { pending = {}; pendingCount = 0 }
    if (Object.keys(e.variants).length) { pending[String(event.entity.uuid)] = lc; pendingCount++ }
  })

  EntityEvents.spawned(event => {
    if (!pendingCount) return
    let key = String(event.entity.uuid)
    let lc = pending[key]
    if (lc === undefined) return
    delete pending[key]
    pendingCount--
    let e = byType && byType[String(event.entity.type)]
    let row = e && e.variants[Number(event.entity.getVariant())]
    if (row && rollFails(row, lc)) event.cancel()
  })
})()
