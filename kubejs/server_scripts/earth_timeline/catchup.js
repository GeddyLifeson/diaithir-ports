// Vegetation and farmland catch-up in chunks that were generated before the calendar moved. Both are OFF by default and
// switched on by flag: `kubejs/config/earth_timeline.json` {"vegetation_catchup": true, "farmland": true}, or at run time by
// /et_flag vegetation_catchup|farmland on|off (op), which wins over the file.
//  - vegetation_catchup: in patches Tellus now reads as cleared by people (EraRuntime.woodlandClearedAt), natural trees come
//    out (catchup_core.js: trunks standing on ground with natural leaves near, and non-persistent leaves). Nothing else.
//  - farmland: on flat grass in HYDE cropland (EraRuntime.croplandAt), every third block along x is tilled with wheat.
// Never touches FTB-claimed chunks, never a column whose top is not natural. Needs flood.js (server, claims) and
// startup_scripts/earth_timeline_ns.js. Not run in a game client yet.
(function () {
  const FLAGS = ['vegetation_catchup', 'farmland']
  const PER_TICK = 1
  const RETRIES = 40
  const NEAR = 6
  const SCALE = 5.0
  const QUEUE = []
  const QUEUED = {}
  const POS = Java.loadClass('net.minecraft.core.BlockPos')
  const HEIGHTMAP = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types').WORLD_SURFACE
  const BLOCKS = Java.loadClass('net.minecraft.world.level.block.Blocks')
  const LEAVES = Java.loadClass('net.minecraft.world.level.block.LeavesBlock')
  const PILLAR = Java.loadClass('net.minecraft.world.level.block.RotatedPillarBlock')
  const CROP = Java.loadClass('net.minecraft.world.level.block.CropBlock')
  const INTEGER = Java.loadClass('java.lang.Integer')
  const AIR = BLOCKS.AIR.defaultBlockState()
  const GROUND = [BLOCKS.GRASS_BLOCK, BLOCKS.DIRT, BLOCKS.PODZOL, BLOCKS.COARSE_DIRT, BLOCKS.ROOTED_DIRT, BLOCKS.MUD, BLOCKS.MOSS_BLOCK]
  const FARMLAND = BLOCKS.FARMLAND.defaultBlockState()
  const WHEAT = BLOCKS.WHEAT

  let fileFlags = {}

  function flag(server, name) {
    let key = 'earth_timeline_flag_' + name
    if (server.persistentData.contains(key)) return !!server.persistentData.getBoolean(key)
    return !!fileFlags[name]
  }

  ServerEvents.loaded(event => {
    try {
      let data = JsonIO.read('kubejs/config/earth_timeline.json')
      if (data) FLAGS.forEach(f => { fileFlags[f] = !!data[f] })
    } catch (e) { console.warn('[earth_timeline] no config file: catch-up flags stay off') }
  })

  function queue(cx, cz) {
    let k = cx + ',' + cz
    if (QUEUED[k]) return
    QUEUED[k] = true
    QUEUE.push([cx, cz, 0])
  }

  function anyOn(server) { return FLAGS.some(f => flag(server, f)) }

  // Queue the chunks round the players when the 250-year step moves (EraRuntime.yearKey), as melt.js does.
  const YEAR_KEY = 'earth_timeline_catchup_key'
  global.etNS.etCatchupOnYear = function (server) {
    let era = global.etNS.etEra
    if (!era || !anyOn(server)) return
    let key = String(era.yearKey())
    if (key === String(server.persistentData.getString(YEAR_KEY))) return
    server.persistentData.putString(YEAR_KEY, key)
    server.players.forEach(p => {
      if (!global.etNS.etIsOverworld(p.level)) return
      let cx = Math.floor(p.x) >> 4, cz = Math.floor(p.z) >> 4
      for (let x = cx - NEAR; x <= cx + NEAR; x++)
        for (let z = cz - NEAR; z <= cz + NEAR; z++) queue(x, z)
    })
  }

  try {
    NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load'), e => {
      let level = e.getLevel()
      let s = global.etNS.etServer
      if (level.isClientSide() || !s || !global.etNS.etIsOverworld(level) || !global.etNS.etEra) return
      if (!anyOn(s) || s.persistentData.getIntOr(global.etNS.etYearKey, -1) < 0) return
      let c = e.getChunk().getPos()
      queue(c.x, c.z)
    })
  } catch (e) { console.warn('[earth_timeline] chunk load event unavailable: catch-up only round players') }

  try {
    ServerEvents.commandRegistry(event => {
      const { commands: Commands, arguments: Arguments } = event
      event.register(Commands.literal('et_flag').requires(s => s.permissions().hasPermission(Java.loadClass('net.minecraft.server.permissions.Permissions').COMMANDS_GAMEMASTER))
        .then(Commands.argument('name', Arguments.STRING.create(event))
          .then(Commands.argument('on', Arguments.BOOLEAN.create(event)).executes(ctx => {
            let name = Arguments.STRING.getResult(ctx, 'name')
            let on = Arguments.BOOLEAN.getResult(ctx, 'on')
            if (FLAGS.indexOf(name) < 0) { ctx.source.sendSystemMessage(Component.of('flags: ' + FLAGS.join(', '))); return 0 }
            ctx.source.server.persistentData.putBoolean('earth_timeline_flag_' + name, on)
            ctx.source.sendSystemMessage(Component.of(`earth_timeline ${name} ${on ? 'on' : 'off'}`))
            return 1
          }))))
    })
  } catch (e) { console.warn('[earth_timeline] /et_flag not registered: ' + e) }

  ServerEvents.tick(event => {
    let era = global.etNS.etEra
    if (!QUEUE.length || !era) return
    let s = event.server
    let L = s.overworld()
    for (let n = 0; n < PER_TICK && QUEUE.length; n++) {
      let [cx, cz, tries] = QUEUE.shift()
      if (!L.hasChunk(cx, cz) && tries < RETRIES) { QUEUE.push([cx, cz, tries + 1]); continue }
      delete QUEUED[cx + ',' + cz]
      if (!L.hasChunk(cx, cz) || global.etNS.etClaimed(s, cx, cz)) continue
      let trees = flag(s, 'vegetation_catchup'), fields = flag(s, 'farmland')
      if (!trees && !fields) continue
      try { processChunk(L, era, cx, cz, trees, fields) } catch (e) { console.warn('[earth_timeline] catch-up failed in chunk ' + cx + ',' + cz + ': ' + e) }
    }
  })

  function processChunk(L, era, cx, cz, trees, fields) {
    let pos = (x, y, z) => new POS(x, y, z)
    let top = (x, z) => L.getHeight(HEIGHTMAP, x, z) - 1
    let kindAt = (x, y, z) => {
      let st = L.getBlockState(pos(x, y, z))
      let b = st.getBlock()
      if (st.isAir()) return 'air'
      if (b instanceof LEAVES) return st.getValue(LEAVES.PERSISTENT) ? 'other' : 'leaf'
      if (b instanceof PILLAR && b !== BLOCKS.HAY_BLOCK && b !== BLOCKS.BONE_BLOCK && String(b.descriptionId).indexOf('log') >= 0) return 'log'
      if (GROUND.indexOf(b) >= 0) return 'ground'
      return 'other'
    }
    let set = (x, y, z, kind) => L.setBlock(pos(x, y, z), AIR, 18)
    let x0 = cx << 4, z0 = cz << 4
    if (trees) {
      let cleared = []
      for (let x = x0; x < x0 + 16; x++)
        for (let z = z0; z < z0 + 16; z++) if (era.woodlandClearedAt(x, z, SCALE)) cleared.push([x, z])
      // trunks first (they look for leaves), then the leaves
      cleared.forEach(([x, z]) => global.etNS.etClearTreeColumn(kindAt, set, x, z, top(x, z), 'trunk'))
      cleared.forEach(([x, z]) => global.etNS.etClearTreeColumn(kindAt, set, x, z, top(x, z), 'leaf'))
    }
    if (fields) {
      for (let x = x0; x < x0 + 16; x++)
        for (let z = z0; z < z0 + 16; z++) {
          if (!era.croplandAt(x, z, SCALE)) continue
          if (!global.etNS.etFurrowAt(x, z, (hx, hz) => top(hx, hz))) continue
          let y = top(x, z)
          if (L.getBlockState(pos(x, y, z)).getBlock() !== BLOCKS.GRASS_BLOCK) continue
          if (!L.getBlockState(pos(x, y + 1, z)).isAir()) continue
          L.setBlock(pos(x, y, z), FARMLAND, 18)
          L.setBlock(pos(x, y + 1, z), WHEAT.defaultBlockState().setValue(CROP.AGE, INTEGER.valueOf(Math.floor(Math.random() * 8))), 18)
        }
    }
  }
})()
