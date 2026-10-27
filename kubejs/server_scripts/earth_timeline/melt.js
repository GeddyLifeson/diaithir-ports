// The ice sheets follow the calendar. Chunks made before the sheets thinned are caught up as they load, and for the chunks
// round each player when the year moves on a step: each column's ice run is cut down to what Tellus now puts there
// (EraRuntime.iceBlocksAt), see melt_core.js. Only a column whose top block is ice is touched, so builds are safe, and
// claimed chunks (FTB Chunks) are skipped. Needs flood.js for the server, year and claim helpers (shared through `global`).
(function () {
  const ICE_KEY = 'earth_timeline_ice_key'
  const MELT_PER_TICK = 2
  const RETRIES = 40
  const MELT_NEAR = 8
  const QUEUE = []
  const QUEUED = {}
  const POS = Java.loadClass('net.minecraft.core.BlockPos')
  const HEIGHTMAP = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types').WORLD_SURFACE
  const BLOCKS = Java.loadClass('net.minecraft.world.level.block.Blocks')
  const STATES = {
    air: BLOCKS.AIR.defaultBlockState(),
    snow: BLOCKS.SNOW_BLOCK.defaultBlockState(),
    gravel: BLOCKS.GRAVEL.defaultBlockState()
  }

  function queue(cx, cz) {
    let k = cx + ',' + cz
    if (QUEUED[k]) return
    QUEUED[k] = true
    QUEUE.push([cx, cz, 0])
  }

  // Called by flood.js each time the engine year arrives; the ice only changes when the 250-year step does.
  global.etNS.etMeltOnYear = function (server) {
    let era = global.etNS.etEra
    if (!era) return
    let key = String(era.yearKey())
    if (key === String(server.persistentData.getString(ICE_KEY))) return
    server.persistentData.putString(ICE_KEY, key)
    server.players.forEach(p => {
      if (!global.etNS.etIsOverworld(p.level)) return
      let cx = Math.floor(p.x) >> 4, cz = Math.floor(p.z) >> 4
      for (let x = cx - MELT_NEAR; x <= cx + MELT_NEAR; x++)
        for (let z = cz - MELT_NEAR; z <= cz + MELT_NEAR; z++) queue(x, z)
    })
  }

  try {
    NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load'), e => {
      let level = e.getLevel()
      if (level.isClientSide() || !global.etNS.etServer || !global.etNS.etIsOverworld(level) || !global.etNS.etEra) return
      if (global.etNS.etServer.persistentData.getIntOr(global.etNS.etYearKey, -1) < 0) return
      let c = e.getChunk().getPos()
      queue(c.x, c.z)
    })
  } catch (e) { console.warn('[earth_timeline] chunk load event unavailable: only chunks round players melt') }

  ServerEvents.tick(event => {
    let era = global.etNS.etEra
    if (!QUEUE.length || !era) return
    let s = event.server
    let L = s.overworld()
    for (let n = 0; n < MELT_PER_TICK && QUEUE.length; n++) {
      let [cx, cz, tries] = QUEUE.shift()
      if (!L.hasChunk(cx, cz) && tries < RETRIES) { // a chunk just read from disk is not in the level yet
        QUEUE.push([cx, cz, tries + 1])
        continue
      }
      delete QUEUED[cx + ',' + cz]
      if (!L.hasChunk(cx, cz) || global.etNS.etClaimed(s, cx, cz)) continue
      let isIce = (x, y, z) => {
        let b = L.getBlockState(new POS(x, y, z)).getBlock()
        return b === BLOCKS.PACKED_ICE || b === BLOCKS.SNOW_BLOCK
      }
      let set = (x, y, z, kind) => L.setBlock(new POS(x, y, z), STATES[kind], 18)
      let topOf = (x, z) => {
        let y = L.getHeight(HEIGHTMAP, x, z) - 1
        // vanilla snow layers lie over the cap
        for (let i = 0; i < 2 && L.getBlockState(new POS(x, y, z)).getBlock() === BLOCKS.SNOW; i++) y--
        return y
      }
      let x0 = cx << 4, z0 = cz << 4
      // a chunk with no ice at its corners and middle is skipped without scanning
      let probes = [[0, 0], [15, 0], [0, 15], [15, 15], [8, 8]]
      if (!probes.some(([dx, dz]) => isIce(x0 + dx, topOf(x0 + dx, z0 + dz), z0 + dz))) continue
      for (let x = x0; x < x0 + 16; x++)
        for (let z = z0; z < z0 + 16; z++)
          global.etNS.etMeltColumn(isIce, set, x, z, topOf(x, z), era.iceBlocksAt(x, z))
    }
  })
})()
