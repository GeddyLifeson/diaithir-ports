// The ice sheets follow the calendar. Chunks made before the sheets thinned are caught up as they load, and for the chunks
// round each player when the year moves on a step: each column's ice run is cut down to what Tellus now puts there
// (EraRuntime.iceBlocksAt), see melt_core.js. Only a column whose top block is ice is touched, so builds are safe, and
// claimed chunks (FTB Chunks) are skipped. Needs flood.js for the server, year and claim helpers (shared through `global`).
(function () {
  const ICE_KEY = 'earth_timeline_ice_key'
  const MELT_PER_TICK = 2
  const BUDGET_MS = 4 // work stops for the tick once this much time is spent (queue_core.js)
  const RETRIES = 100
  const MELT_NEAR = 8
  const WORK_RADIUS = 10 // a queued chunk further than this from every player is dropped; nothing here ever loads a chunk
  let Q = null
  const POS = Java.loadClass('net.minecraft.core.BlockPos')
  const HEIGHTMAP = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types').WORLD_SURFACE
  const BLOCKS = Java.loadClass('net.minecraft.world.level.block.Blocks')
  const STATES = {
    air: BLOCKS.AIR.defaultBlockState(),
    snow: BLOCKS.SNOW_BLOCK.defaultBlockState(),
    gravel: BLOCKS.GRAVEL.defaultBlockState()
  }

  function queue() {
    if (!Q && global.etNS.etMakeQueue) Q = global.etNS.etMakeQueue({ max: 256, retries: RETRIES, scan: 32 })
    return Q
  }

  // Called by flood.js each time the engine year arrives; the ice only changes when the 250-year step does.
  global.etNS.etMeltOnYear = function (server) {
    let era = global.etNS.etEra
    if (!era) return
    let key = String(era.yearKey())
    if (key === String(server.persistentData.getString(ICE_KEY))) return
    server.persistentData.putString(ICE_KEY, key)
    global.etNS.etQueueLoadedAround(server, queue(), MELT_NEAR)
  }

  try {
    NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load'), e => {
      let level = e.getLevel()
      if (level.isClientSide() || !global.etNS.etServer || !global.etNS.etIsOverworld(level) || !global.etNS.etEra) return
      if (global.etNS.etServer.persistentData.getIntOr(global.etNS.etYearKey, -1) < 0) return
      let c = e.getChunk().getPos()
      let q = queue()
      if (q && global.etNS.etNearPlayer(global.etNS.etServer, c.x, c.z, WORK_RADIUS)) q.add(c.x, c.z)
    })
  } catch (e) { console.warn('[earth_timeline] chunk load event unavailable: only chunks round players melt') }

  ServerEvents.tick(event => {
    let era = global.etNS.etEra
    let q = queue()
    if (!q || !q.size() || !era) return
    let s = event.server
    let L = s.overworld()
    q.run({
      now: () => Date.now(),
      loaded: (cx, cz) => L.hasChunk(cx, cz),
      near: (cx, cz) => global.etNS.etNearPlayer(s, cx, cz, WORK_RADIUS),
      work: (cx, cz) => {
        if (global.etNS.etClaimed(s, cx, cz)) return
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
        if (!probes.some(([dx, dz]) => isIce(x0 + dx, topOf(x0 + dx, z0 + dz), z0 + dz))) return
        for (let x = x0; x < x0 + 16; x++)
          for (let z = z0; z < z0 + 16; z++)
            global.etNS.etMeltColumn(isIce, set, x, z, topOf(x, z), era.iceBlocksAt(x, z))
      }
    }, MELT_PER_TICK, BUDGET_MS)
  })
})()
