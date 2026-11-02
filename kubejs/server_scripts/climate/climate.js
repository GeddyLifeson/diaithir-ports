// Room temperature and seasons, in the way of RimWorld and Prison Architect: an enclosed room is its own little climate.
// Its temperature drifts toward ambient (outdoor, or ground temperature when buried) through its walls, and heaters and
// cold blocks push it. Open doors leak, an open ceiling makes it outdoors. Rooms are scanned lazily and stepped analytically,
// so nothing runs per tick. global.TEMP_AT(level, x, y, z) is the one call other scripts (spoilage, bridge) read.
//
// KubeJS 8: tables live in startup_scripts/climate/tables.js; this file only fills in the functions on global.CLIMATE.

(() => {
  let C = global.CLIMATE
  let Heightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
  let Props = Java.loadClass('net.minecraft.world.level.block.state.properties.BlockStateProperties')
  let Blocks = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries').BLOCK
  let SeasonsApi = (() => { try { return Java.loadClass('homeostaticseasons.api.HomeostaticSeasonsAPI') } catch (e) { return null } })()
  let SeasonsCfg = (() => { try { return Java.loadClass('homeostaticseasons.config.ConfigHandler$Common') } catch (e) { return null } })()
  let FILE = 'kubejs/data/climate_rooms.json'
  let DIRS = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]
  let SEASON_NAMES = ['early_spring', 'mid_spring', 'late_spring', 'early_summer', 'mid_summer', 'late_summer',
    'early_autumn', 'mid_autumn', 'late_autumn', 'early_winter', 'mid_winter', 'late_winter']

  let clamp = (v, lo, hi) => Math.max(lo, Math.min(hi, v))
  let gameTime = level => Number(level.getLevelData().getGameTime())
  let dayTime = level => Number(level.getDefaultClockTime())
  let dimId = level => String(level.dimension)
  let key = (x, y, z) => x + ',' + y + ',' + z

  // ---- seasons -----------------------------------------------------------------------------------------------------
  // Homeostatic Seasons runs one clock for the whole dimension. seasonAt adds the hemisphere: south of the equator the
  // sub-season is six steps on. Tellus keeps north at negative z (EarthProjection.latToBlockZ), 1 block = 5 m.
  // Tellus projects Earth with Web Mercator in z (checked: Alexandria z -731982 is 31.4 N, Creag Ruadh z -1541408 is 56.8 N),
  // so latitude is the inverse Mercator of the distance from the equator, not a linear scale.
  let latitudeAt = z => {
    let y = -(z - C.equatorZ) * C.metersPerBlock
    return (2 * Math.atan(Math.exp(y / 6378137)) - Math.PI / 2) * 180 / Math.PI
  }

  C.seasonAt = (level, x, z) => {
    let lat = latitudeAt(z)
    let south = lat < 0
    let sub = 0, frac = 0, yearDays = 36
    if (SeasonsApi) {
      let s = SeasonsApi.getCurrentSeason(level)
      sub = s.ordinal()
      let len = Number(s.getSeasonLength())
      frac = len > 0 ? clamp(1 - Number(SeasonsApi.getTimeUntilNextSeason(level)) / len, 0, 0.9999) : 0
      if (SeasonsCfg) yearDays = Number(SeasonsCfg.getTotalYearLength()) / 24000
    }
    if (!SeasonsApi) {  // no Homeostatic Seasons: run a plain 36-day year from the game clock so seasons still turn
      let yr = (gameTime(level) / (yearDays * 24000)) % 1
      sub = Math.floor(yr * 12)
      frac = yr * 12 - sub
    }
    if (south) sub = (sub + 6) % 12
    return { sub: sub, name: SEASON_NAMES[sub], frac: frac, year: (sub + frac) / 12, yearDays: yearDays,
      hemisphere: south ? 'south' : 'north', lat: lat }
  }

  // ---- outdoor and ground temperature ------------------------------------------------------------------------------
  // Surface height of the column; the pack's sea level is Y200, so a fixed y such as 64 is deep underground there.
  let surfaceY = (level, x, z) => {
    try { if (level.hasChunkAt(new BlockPos(x, 0, z))) return Number(level.getHeight(Heightmap.MOTION_BLOCKING, x, z)) } catch (e) { }
    return null
  }
  let fixedDimension = id => {
    if (C.dimensions[id] !== undefined) return C.dimensions[id]
    for (let p in C.dimensionPrefixes) if (id.startsWith(p)) return C.dimensionPrefixes[p]
    return null
  }

  // Tellus places real Earth, so latitude carries the climate: yearly mean 27 C at the equator, 20 at 30 degrees, 12 at 45,
  // 0 at 60 and -10 at 70. The biome's own temperature only nudges that by its distance from what the latitude expects
  // (a desert runs hot, a high plateau cold). Water biomes say nothing about air temperature, so oceans take the latitude mean.
  let latMean = lat => 27 - 0.0075 * lat * lat
  let isWater = (level, at) => {
    let name = String(level.getBiome(at).getRegisteredName())
    return name.indexOf('ocean') >= 0 || name.indexOf('river') >= 0 || name.indexOf('beach') >= 0
  }
  let meanOf = (level, at, t, z) => {
    let lat = Math.abs(latitudeAt(z))
    let m = latMean(lat)
    if (isWater(level, at)) return clamp(m, C.meanMin, C.meanMax)
    let tExp = clamp(1.2 - lat / 50, 0, 1.2)
    return clamp(m + clamp((t - tExp) * 5, -6, 6), C.meanMin, C.meanMax)
  }

  // Yearly mean plus the season swing, the day swing and the drop with height. Underground uses the mean only.
  let climateAt = (level, pos, withWeather, noDay) => {
    let fixed = fixedDimension(dimId(level))
    if (fixed !== null) return fixed
    let sy = surfaceY(level, pos.getX(), pos.getZ())
    let at = sy !== null && pos.getY() < sy ? new BlockPos(pos.getX(), sy, pos.getZ()) : pos
    let t = Number(level.getBiome(at).value().getBaseTemperature())
    let water = isWater(level, at)
    let T = meanOf(level, at, t, pos.getZ())
    if (!withWeather) return T
    let sea = Number(level.getSeaLevel())
    T -= Math.max(0, pos.getY() - sea) * C.lapsePerBlock * C.metersPerBlock
    let sn = C.seasonAt(level, pos.getX(), pos.getZ())
    let latFactor = clamp(Math.abs(sn.lat) / 40, 0.15, 1)
    let amp = (12 - 6 * clamp(t / 2, 0, 1)) * latFactor * (water ? 0.4 : 1)
    T += amp * Math.cos(2 * Math.PI * (sn.year - 0.375))
    if (noDay) return T
    let dayAmp = (2 + 4 * clamp((t - 0.8) / 1.2, 0, 1)) * (water ? 0.3 : 1)
    T += dayAmp * Math.sin(2 * Math.PI * (dayTime(level) - 2000) / 24000)
    return T
  }

  C.outdoorAt = (level, x, y, z) => climateAt(level, new BlockPos(x, y, z), true)

  // ---- scanning a room ---------------------------------------------------------------------------------------------
  let blockIds = new Map()
  let idOf = block => {
    let id = blockIds.get(block)
    if (id === undefined) { id = String(Blocks.getKey(block)); blockIds.set(block, id) }
    return id
  }
  let wallCache = new Map()
  let wallU = id => {
    let u = wallCache.get(id)
    if (u === undefined) {
      u = C.wallDefault
      for (let w of C.walls) if (w[0].test(id)) { u = w[1]; break }
      wallCache.set(id, u)
    }
    return u
  }
  let isLeaky = state => {
    if (state.hasProperty(Props.OPEN) && state.getValue(Props.OPEN)) return true
    return false
  }
  let passable = (level, pos, state) => state.isAir() || state.getCollisionShape(level, pos).isEmpty()

  // Flood fill from (x,y,z). Returns {open:true} for sky or oversize, else the room's volume, wall conductance and power.
  let scan = (level, sx, sy, sz) => {
    let seen = new Set()
    let queue = [[sx, sy, sz]]
    seen.add(key(sx, sy, sz))
    let pos = new BlockPos.MutableBlockPos()
    let room = { cells: [], G: 0, Gc: 0, Tc: 0, power: 0, minKey: null, cellCount: 0, ymax: sy, ymin: sy }
    let sources = new Set()
    let minX = sx, minY = sy, minZ = sz
    while (queue.length) {
      let c = queue.pop()
      pos.set(c[0], c[1], c[2])
      if (level.canSeeSky(pos)) return { open: true }
      if (room.cellCount >= C.maxCells) return { open: true }
      room.cellCount++
      room.cells.push(key(c[0], c[1], c[2]))
      if (c[1] > room.ymax) room.ymax = c[1]
      if (c[1] < room.ymin) room.ymin = c[1]
      if (c[1] < minY || (c[1] === minY && (c[0] < minX || (c[0] === minX && c[2] < minZ)))) { minX = c[0]; minY = c[1]; minZ = c[2] }
      for (let d of DIRS) {
        let nx = c[0] + d[0], ny = c[1] + d[1], nz = c[2] + d[2]
        let k = key(nx, ny, nz)
        pos.set(nx, ny, nz)
        if (!level.hasChunkAt(pos)) { room.G += C.wallDefault; continue }
        let st = level.getBlockState(pos)
        if (passable(level, pos, st)) {
          if (!seen.has(k)) { seen.add(k); queue.push([nx, ny, nz]) }
          let id = st.isAir() ? null : idOf(st.getBlock())
          if (id && !sources.has(k)) {
            let p = C.heat[id] || 0
            if (p) { sources.add(k); room.power += st.hasProperty(Props.LIT) && !st.getValue(Props.LIT) ? 0 : p }
          }
        } else {
          let id = idOf(st.getBlock())
          let u = isLeaky(st) ? C.openDoor : wallU(id)
          room.G += u
          if (C.cold[id] !== undefined) { room.Gc += u; room.Tc += u * C.cold[id] }
          if (!sources.has(k)) {
            let p = C.heat[id] || 0
            if (p) { sources.add(k); room.power += st.hasProperty(Props.LIT) && !st.getValue(Props.LIT) ? 0 : p }
          }
        }
      }
    }
    room.minKey = key(minX, minY, minZ)
    room.G = Math.max(room.G, 1)
    return room
  }

  // ---- room registry -----------------------------------------------------------------------------------------------
  let rooms = new Map()      // roomKey -> {T, at, scannedAt, V, G, power, ambient, cells, last}
  let cellRoom = new Map()   // dim|cell -> roomKey
  let saved = {}
  try { saved = JsonIO.read(FILE) || {} } catch (e) { saved = {} }

  let ambientFor = (level, room, cx, cy, cz) => {
    let fixed = fixedDimension(dimId(level))
    if (fixed !== null) return fixed
    let pos = new BlockPos(cx, cy, cz)
    let surface = surfaceY(level, cx, cz)
    if (surface === null) surface = cy
    let depth = surface - cy
    let f = clamp((depth - C.groundDepthStart) / (C.groundDepthFull - C.groundDepthStart), 0, 1)
    let outdoor = climateAt(level, pos, true)
    if (f <= 0) return outdoor
    let ground = climateAt(level, pos, false) + Math.max(0, depth - 20) / 10 * C.geothermalPer10
    return outdoor + (ground - outdoor) * f
  }

  let step = (room, now) => {
    let tau = C.cap * room.V / room.G
    let eq = (room.ambient * (room.G - room.Gc) + room.Tc + room.power) / room.G
    let dt = Math.max(0, now - room.at)
    room.T = eq + (room.T - eq) * Math.exp(-dt / tau)
    room.at = now
    room.eq = eq
    room.tau = tau
  }

  // The room report for a cell; null when the cell is open air. The block itself may be solid (a chest): the cell is then
  // the first passable neighbour, above first.
  C.roomAt = (level, x, y, z) => {
    let now = gameTime(level)
    let pos = new BlockPos.MutableBlockPos()
    let cx = x, cy = y, cz = z
    pos.set(cx, cy, cz)
    if (!level.hasChunkAt(pos)) return null
    let st = level.getBlockState(pos)
    if (!passable(level, pos, st)) {
      let found = false
      for (let d of [[0, 1, 0], [1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1], [0, -1, 0]]) {
        pos.set(x + d[0], y + d[1], z + d[2])
        if (!level.hasChunkAt(pos)) continue
        if (passable(level, pos, level.getBlockState(pos))) { cx = x + d[0]; cy = y + d[1]; cz = z + d[2]; found = true; break }
      }
      if (!found) return null
    }
    let dk = dimId(level) + '|' + key(cx, cy, cz)
    let rk = cellRoom.get(dk)
    let room = rk ? rooms.get(rk) : null
    if (room && now - room.scannedAt < C.rescanTicks) {
      if (room.open) return null
      room.last = now
      step(room, now)
      return room
    }
    let sc = scan(level, cx, cy, cz)
    if (sc.open) {
      let stub = { open: true, scannedAt: now, last: now }
      let rkey = dk
      rooms.set(rkey, stub)
      cellRoom.set(dk, rkey)
      return null
    }
    let rkey = dimId(level) + '|' + sc.minKey
    let old = rooms.get(rkey)
    let fresh = {
      key: rkey, V: sc.cellCount, G: sc.G, Gc: sc.Gc, Tc: sc.Tc, power: sc.power, scannedAt: now, last: now, open: false,
      ambient: ambientFor(level, sc, cx, cy, cz), at: now, T: null, minKey: sc.minKey
    }
    if (old && !old.open && old.T !== null) { fresh.T = old.T; fresh.at = old.at }
    else if (saved[rkey]) { fresh.T = saved[rkey][0]; fresh.at = saved[rkey][1] }
    else fresh.T = fresh.ambient
    rooms.set(rkey, fresh)
    for (let c of sc.cells) cellRoom.set(dimId(level) + '|' + c, rkey)
    cellRoom.set(dk, rkey)
    step(fresh, now)
    return fresh
  }

  C.tempAt = (level, x, y, z) => {
    let r = C.roomAt(level, x, y, z)
    if (r) return r.T
    return ambientFor(level, null, x, y, z)  // open air gives the outdoor figure, buried cells blend toward ground temperature
  }

  // Outdoor temperature at the surface of a column: what markets and the economy should read.
  C.surfaceAt = (level, x, z) => {
    let sy = surfaceY(level, x, z)
    return climateAt(level, new BlockPos(x, sy === null ? Number(level.getSeaLevel()) : sy, z), true, true)  // daily mean: no day/night swing
  }

  // Homeostatic reads the room through the diaithir-climate mod: its own outdoor figure plus how far the room sits from ambient.
  // Ambient already carries the ground effect, which Homeostatic handles itself, so only heaters, cold faces and lag count here.
  try {
    let Bridge = Java.loadClass('net.diaithir.climate.api.ClimateBridge')
    Bridge.setProvider((level, pos) => {
      let r = C.roomAt(level, pos.getX(), pos.getY(), pos.getZ())
      return r ? r.T - r.ambient : 0
    })
  } catch (e) {
    console.log('[climate] diaithir-climate mod not loaded, rooms do not reach body temperature: ' + e)
  }

  // Forget cells of rooms nobody has asked about for a while; save room temperatures so a restart does not reset them.
  let flush = server => {
    let now = gameTime(server.overworld())
    for (let [k, r] of rooms) {
      if (r.open) { if (now - r.last > 1200) rooms.delete(k); continue }
      if (now - r.last > 4800) { saved[k] = [r.T, r.at]; rooms.delete(k) }
    }
    for (let [k, rk] of cellRoom) if (!rooms.has(rk)) cellRoom.delete(k)
    for (let [k, r] of rooms) if (!r.open) saved[k] = [r.T, r.at]
    try { JsonIO.write(FILE, saved) } catch (e) { console.error('[climate] state not saved: ' + e) }
  }
  ServerEvents.tick(event => { if (event.server.tickCount % 6000 === 0) flush(event.server) })

  // ---- /climate ----------------------------------------------------------------------------------------------------
  let fmt = v => (Math.round(v * 10) / 10) + ' C'
  ServerEvents.commandRegistry(event => {
    let { commands: Commands } = event
    event.register(Commands.literal('climate').executes(ctx => {
      let p = ctx.source.player
      if (!p) return 0
      let level = p.level, x = Math.floor(p.x), y = Math.floor(p.y + 0.5), z = Math.floor(p.z)
      let sn = C.seasonAt(level, x, z)
      let out = C.outdoorAt(level, x, y, z)
      let r = C.roomAt(level, x, y, z)
      let line = 'Outside ' + fmt(out) + ', ' + sn.name.replace('_', ' ') + ' (' + sn.hemisphere + ', ' + Math.round(sn.frac * 100) + '%).'
      if (r) line += ' This room ' + fmt(r.T) + ' (settling to ' + fmt(r.eq) + '; ' + r.V + ' cells, walls ' + (Math.round(r.G * 10) / 10) + ', heat ' + Math.round(r.power) + ').'
      else line += ' You are in the open.'
      ctx.source.sendSystemMessage(Text.of(line))
      return 1
    }))
  })
})()
