// The sea follows the calendar. The year from the engine goes to Tellus (/diaithir_era), which floods any chunk generated
// from then on. Chunks made earlier are caught up here as they load, and for the chunks round each player when the sea
// rises: new water is placed only into air, down from the new surface (flood_core.js), so player blocks are never replaced.
// Claimed chunks (FTB Chunks) are left as they are. Queue-driven, a few chunks a tick.
// KubeJS 8: everything is wrapped, files share only `global`, and the server comes from ServerEvents.loaded (no Utils.server).
// /et_year <lc> (op) does what the engine's year event does, for testing.
(function () {
  const YEAR_KEY = 'earth_timeline_lc'
  const FLOOD_KEY = 'earth_timeline_flood'
  const VERTICAL_SCALE = 5.0 // metres per block of the Age I preset (world_scale 5)
  const CHUNKS_PER_TICK = 4
  const RETRIES = 40 // ticks a queued chunk may wait to finish loading
  const NEAR_CHUNKS = 10 // radius round a player caught up when the sea rises

  const ERA = (() => { try { return Java.loadClass('com.yucareux.tellus.era.EraRuntime') } catch (e) { return null } })()
  const POS = Java.loadClass('net.minecraft.core.BlockPos')
  const WATER = Java.loadClass('net.minecraft.world.level.block.Blocks').WATER.defaultBlockState()
  const QUEUE = [] // [cx, cz] pairs waiting to be caught up
  const QUEUED = {}

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

  function queue(cx, cz) {
    let k = cx + ',' + cz
    if (QUEUED[k]) return
    QUEUED[k] = true
    QUEUE.push([cx, cz, 0])
  }

  function queueAroundPlayers(server) {
    server.players.forEach(p => {
      if (!isOverworld(p.level)) return
      let cx = Math.floor(p.x) >> 4, cz = Math.floor(p.z) >> 4
      for (let x = cx - NEAR_CHUNKS; x <= cx + NEAR_CHUNKS; x++)
        for (let z = cz - NEAR_CHUNKS; z <= cz + NEAR_CHUNKS; z++) queue(x, z)
    })
  }

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
        queue(c.x, c.z)
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
          ctx.source.sendSystemMessage(Component.of(`earth_timeline year ${lc}, flood ${ERA ? ERA.floodBlocks(VERTICAL_SCALE) : '?'} blocks, ${QUEUE.length} chunks queued`))
          return 1
        })))
    })
  } catch (e) { console.warn('[earth_timeline] /et_year not registered: ' + e) }

  ServerEvents.tick(event => {
    let s = event.server
    if (!global.etNS.etServer) global.etNS.etServer = s
    if (!QUEUE.length) return
    let flood = s.persistentData.getIntOr(FLOOD_KEY, 0)
    let L = s.overworld()
    for (let n = 0; n < CHUNKS_PER_TICK && QUEUE.length; n++) {
      let [cx, cz, tries] = QUEUE.shift()
      if (flood > 0 && !L.hasChunk(cx, cz) && tries < RETRIES) { // a chunk just read from disk is not in the level yet
        QUEUE.push([cx, cz, tries + 1])
        continue
      }
      delete QUEUED[cx + ',' + cz]
      if (flood <= 0 || !L.hasChunk(cx, cz) || claimed(s, cx, cz)) continue
      let isAir = (x, y, z) => L.getBlockState(new POS(x, y, z)).isAir()
      let setWater = (x, y, z) => L.setBlock(new POS(x, y, z), WATER, 18) // 2 sync + 16 no neighbour updates
      for (let x = cx << 4; x < (cx << 4) + 16; x++)
        for (let z = cz << 4; z < (cz << 4) + 16; z++) global.etNS.etFloodColumn(isAir, setWater, x, z, flood)
    }
  })
})()
