// Species die out on their real dates. global.etNS.EXTINCTIONS (extinctions_data.js, from world/earth/timeline) holds
// [fadeStartLc, extinctLc, [entity ids]]: natural spawns thin out linearly over the fade years, then stop. Animals already
// in the world and tamed or spawned by command are left alone. flood.js stores the engine's year under global.etNS.etYearKey.
// The data file loads after this one (alphabetical), so the table is read on the first spawn, not at load.
(function () {
  const EXT_KEY = 'earth_timeline_lc'
  let byEntity = null

  function table() {
    if (byEntity) return byEntity
    byEntity = {}
    ;(global.etNS.EXTINCTIONS || []).forEach(x => x[2].forEach(id => { byEntity[id] = x }))
    return byEntity
  }

  function survival(row, lc) {
    if (lc >= row[1]) return 0
    if (lc <= row[0]) return 1
    return (row[1] - lc) / (row[1] - row[0])
  }

  EntityEvents.checkSpawn(event => {
    let row = table()[String(event.entity.type)]
    if (!row) return
    let lc = event.server.persistentData.getIntOr(EXT_KEY, -1)
    if (lc < 0) return
    if (Math.random() >= survival(row, lc)) event.cancel()
  })
})()
