// The sea follows the calendar. The year from the engine goes to Tellus (/diaithir_era), which floods any chunk generated
// from then on. Chunks made earlier are caught up here as they load, and for the chunks round each player when the sea
// rises: new water is placed only into air, down from the new surface (flood_core.js), so player blocks are never replaced.
// Claimed chunks (FTB Chunks) are left as they are. Queue-driven and throttled (queue_core.js): a few chunks and a few ms a
// tick, only chunks that are already loaded and near a player; nothing here ever loads a chunk.
// KubeJS 8: everything is wrapped, files share only `global`, and the server comes from ServerEvents.loaded (no Utils.server).
// /et_year <lc> (op) does what the engine's year event does, for testing.
(function () {
  const YEAR_KEY = 'earth_timeline_lc'
  const FLOOD_KEY = 'earth_timeline_flood'
  const VERTICAL_SCALE = 5.0 // metres per block of the Age I preset (world_scale 5)
  const CHUNKS_PER_TICK = 2
  const BUDGET_MS = 4 // work stops for the tick once this much time is spent
  const QUEUE_MAX = 256 // a longer queue drops its oldest chunks; they come back through the load event
  const RETRIES = 200 // scans a queued chunk may wait to finish loading (a chunk read from disk can be slow to become full)
  const SWEEP_TICKS = 1200 // every so often the chunks round each player are checked again, so a chunk reloaded later is caught up too
  const NEAR_CHUNKS = 10 // radius round a player caught up when the sea rises
  const WORK_RADIUS = 12 // a queued chunk further than this from every player is dropped

  const ERA = (() => { try { return Java.loadClass('com.yucareux.tellus.era.EraRuntime') } catch (e) { return null } })()
  const POS = Java.loadClass('net.minecraft.core.BlockPos')
  const WATER = Java.loadClass('net.minecraft.world.level.block.Blocks').WATER.defaultBlockState()
  let Q = null // the throttled queue (queue_core.js shares etMakeQueue through global; made on first use, file order is not fixed)
  let sweepClock = 0

  global.etNS.etYearKey = YEAR_KEY
  global.etNS.etEra = ERA

  function isOverworld(level) {
    let d
    try { d = level.dimension } catch (e) { d = null }
    if (typeof d === 'function') { try { d = level.dimension() } catch (e) { d = null } }
    return String(d).indexOf('minecraft:overworld') >= 0
  }
  global.etNS.etIsOverworld = isOverworld

  function claimed(server, cx, cz) {
    try {
      let chunks = Java.loadClass('dev.ftb.mods.ftbchunks.api.FTBChunksAPI').api().manager
      let ChunkDimPos = Java.loadClass('dev.ftb.mods.ftblibrary.math.ChunkDimPos')
      return !!chunks.getChunk(new ChunkDimPos(server.overworld().dimension, cx, cz))
    } catch (e) { return false }
  }
  global.etNS.etClaimed = claimed

  function queue() {
    if (!Q && global.etNS.etMakeQueue) Q = global.etNS.etMakeQueue({ max: QUEUE_MAX, retries: RETRIES, scan: 32 })
    return Q
  }

  // True when (cx, cz) lies within `radius` chunks of a player in the overworld. Shared with melt.js and catchup.js.
  function nearPlayer(server, cx, cz, radius) {
    let near = false
    server.players.forEach(p => {
      if (near || !isOverworld(p.level)) return
      if (Math.abs((Math.floor(p.x) >> 4) - cx) <= radius && Math.abs((Math.floor(p.z) >> 4) - cz) <= radius) near = true
    })
    return near
  }
  global.etNS.etNearPlayer = nearPlayer

  // Queues the chunks round each player that are loaded right now; one that loads later arrives through the load event.
  function queueLoadedAround(server, q, radius) {
    if (!q) return
    let L = server.overworld()
    server.players.forEach(p => {
      if (!isOverworld(p.level)) return
      let cx = Math.floor(p.x) >> 4, cz = Math.floor(p.z) >> 4
      for (let x = cx - radius; x <= cx + radius; x++)
        for (let z = cz - radius; z <= cz + radius; z++) if (L.hasChunk(x, z)) q.add(x, z)
    })
  }
  global.etNS.etQueueLoadedAround = queueLoadedAround

  function queueAroundPlayers(server) { queueLoadedAround(server, queue(), NEAR_CHUNKS) }

  function setYear(server, lc) {
    server.persistentData.putInt(YEAR_KEY, lc)
    if (!ERA) return
    ERA.setYear(lc) // direct call: the command runs a tick late, so floodBlocks below would read the previous year
    if (typeof global.etNS.etMeltOnYear === 'function') global.etNS.etMeltOnYear(server) // ice sheets thin with the year (melt.js)
    if (typeof global.etNS.etCatchupOnYear === 'function') global.etNS.etCatchupOnYear(server) // vegetation and farmland, flagged off by default (catchup.js)
    let flood = ERA.floodBlocks(VERTICAL_SCALE)
    if (flood !== server.persistentData.getIntOr(FLOOD_KEY, 0)) {
      server.persistentData.putInt(FLOOD_KEY, flood)
      queueAroundPlayers(server)
    }
  }
  global.etNS.etSetYear = setYear

  ServerEvents.loaded(event => {
    global.etNS.etServer = event.server
    // Tellus forgets the year on a restart; the saved one is sent again.
    let lc = event.server.persistentData.getIntOr(YEAR_KEY, -1)
    if (lc > 0) setYear(event.server, lc)
  })

  try {
    NativeEvents.onEvent(Java.loadClass('net.diaithir.ages.mc.StoryEvent'), e => {
      if (String(e.kind) === 'year' && global.etNS.etServer) setYear(global.etNS.etServer, parseInt(String(e.value)))
    })
  } catch (e) { console.warn('[earth_timeline] no Ages engine: the sea stays where Tellus put it') }

  try {
    NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load'), e => {
      let level = e.getLevel()
      if (level.isClientSide() || !isOverworld(level) || !global.etNS.etServer) return
      if (global.etNS.etServer.persistentData.getIntOr(FLOOD_KEY, 0) > 0) {
        let c = e.getChunk().getPos()
        let q = queue()
        if (q && nearPlayer(global.etNS.etServer, c.x, c.z, WORK_RADIUS)) q.add(c.x, c.z)
      }
    })
  } catch (e) { console.warn('[earth_timeline] chunk load event unavailable: only chunks round players are caught up') }

  try {
    ServerEvents.commandRegistry(event => {
      const { commands: Commands, arguments: Arguments } = event
      event.register(Commands.literal('et_year').requires(s => s.permissions().hasPermission(Java.loadClass('net.minecraft.server.permissions.Permissions').COMMANDS_GAMEMASTER))
        .then(Commands.argument('lc', Arguments.INTEGER.create(event)).executes(ctx => {
          let lc = Arguments.INTEGER.getResult(ctx, 'lc')
          setYear(ctx.source.server, lc)
          ctx.source.sendSystemMessage(Component.of(`earth_timeline year ${lc}, flood ${ERA ? ERA.floodBlocks(VERTICAL_SCALE) : '?'} blocks, ${queue() ? queue().size() : 0} chunks queued`))
          return 1
        })))
    })
  } catch (e) { console.warn('[earth_timeline] /et_year not registered: ' + e) }

  ServerEvents.tick(event => {
    let s = event.server
    if (!global.etNS.etServer) global.etNS.etServer = s
    let q = queue()
    if (!q || !q.size()) return
    let flood = s.persistentData.getIntOr(FLOOD_KEY, 0)
    if (flood <= 0) { q.clear(); return }
    let L = s.overworld()
    q.run({
      now: () => Date.now(),
      loaded: (cx, cz) => L.hasChunk(cx, cz),
      near: (cx, cz) => nearPlayer(s, cx, cz, WORK_RADIUS),
      work: (cx, cz) => {
        if (claimed(s, cx, cz)) return
        let isAir = (x, y, z) => L.getBlockState(new POS(x, y, z)).isAir()
        let setWater = (x, y, z) => L.setBlock(new POS(x, y, z), WATER, 18) // 2 sync + 16 no neighbour updates
        for (let x = cx << 4; x < (cx << 4) + 16; x++)
          for (let z = cz << 4; z < (cz << 4) + 16; z++) global.etNS.etFloodColumn(isAir, setWater, x, z, flood)
      }
    }, CHUNKS_PER_TICK, BUDGET_MS)
  })

  // Idempotent: a column already at the new level is left alone (its top block is no longer air). A chunk that was
  // unloaded when the year changed and comes back later is caught by the load event, and by this sweep if that event is missed.
  ServerEvents.tick(event => {
    if (++sweepClock < SWEEP_TICKS) return
    sweepClock = 0
    let s = event.server
    let q = queue()
    if (!q || q.size() || s.persistentData.getIntOr(FLOOD_KEY, 0) <= 0) return
    queueAroundPlayers(s)
  })
})()
