// Spawn guard. KubeJS 8 / MC 26.1, no jar.
// No hostile mob and no nemesis stays within 64 blocks of the world spawn or of a culture's start site (culture_spawns.json).
//  1. A hostile mob (Enemy) or a nemesis (tag nemesis_<id>) that joins the overworld inside such a zone is refused: natural and
//     chunk-generation spawns, spawners, ambushes, and mobs loaded from disk (so whatever camped there is gone after a restart).
//  2. Every second, mobs of that kind already inside a zone are discarded: around the world spawn always (cheap: loaded chunks
//     only), around the start sites only when a player stands within 128 blocks of one. Logged as "[spawn_guard] removed ...".
//  3. Staff can still summon a mob there by giving it the tag zone_ok (/summon zombie ~ ~ ~ {Tags:["zone_ok"]}).
// The nemesis mod already keeps its ambushes 200 blocks from spawn; this removes one that was left standing there.
(function () {
  const R = 64
  const R2 = R * R
  const NEAR = 128
  const CELL = 1024 // grid cell for the start sites, bigger than NEAR so the 3 x 3 cells around a point hold every site in range
  const OK_TAG = 'zone_ok'
  let Mob = null
  let Enemy = null
  let AABB = null
  let grid = null // "cx,cz" -> [[x, z], ...]

  function classes() {
    if (Mob) return
    Mob = Java.loadClass('net.minecraft.world.entity.Mob')
    Enemy = Java.loadClass('net.minecraft.world.entity.monster.Enemy')
    AABB = Java.loadClass('net.minecraft.world.phys.AABB')
  }

  function sites() {
    if (grid) return grid
    grid = {}
    try {
      let data = JsonIO.read('kubejs/data/diaithir/culture_spawns.json') || {}
      Object.keys(data).forEach(k => {
        let s = data[k]
        if (!s || typeof s.x !== 'number' || typeof s.z !== 'number') return
        let key = Math.floor(s.x / CELL) + ',' + Math.floor(s.z / CELL)
        ;(grid[key] || (grid[key] = [])).push([s.x, s.z])
      })
    } catch (e) { console.warn('[spawn_guard] culture_spawns.json: ' + e) }
    return grid
  }

  function isOverworld(level) {
    return String(level.dimension).indexOf('minecraft:overworld') >= 0
  }

  function worldSpawn(level) {
    let p = level.getRespawnData().pos()
    if (typeof p.pos === 'function') p = p.pos() // GlobalPos -> BlockPos
    return [p.getX(), p.getZ()]
  }

  // centres of the zones within `range` blocks of (x, z)
  function centres(level, x, z, range, withSpawn) {
    let out = []
    let ws = worldSpawn(level)
    if (withSpawn !== false && Math.abs(ws[0] - x) <= range && Math.abs(ws[1] - z) <= range) out.push(ws)
    let g = sites()
    let cx = Math.floor(x / CELL), cz = Math.floor(z / CELL)
    for (let i = -1; i <= 1; i++) for (let j = -1; j <= 1; j++) {
      let cell = g[(cx + i) + ',' + (cz + j)]
      if (cell) cell.forEach(c => { if (Math.abs(c[0] - x) <= range && Math.abs(c[1] - z) <= range) out.push(c) })
    }
    return out
  }

  function inZone(level, x, z) {
    return centres(level, x, z, R, true).some(c => (c[0] - x) * (c[0] - x) + (c[1] - z) * (c[1] - z) <= R2)
  }

  function isNemesis(e) {
    let found = false
    e.entityTags().forEach(t => { if (String(t).indexOf('nemesis_') === 0) found = true })
    return found
  }

  function banned(e) {
    classes()
    if (!(e instanceof Mob)) return false
    if (e.entityTags().contains(OK_TAG)) return false
    return (e instanceof Enemy) || isNemesis(e)
  }

  function sweep(level, c, why) {
    classes()
    let box = new AABB(c[0] - R, level.getMinY(), c[1] - R, c[0] + R, level.getMaxY(), c[1] + R)
    let n = 0
    level.getEntitiesOfClass(Mob, box).forEach(e => {
      if (!banned(e)) return
      let dx = e.getX() - c[0], dz = e.getZ() - c[1]
      if (dx * dx + dz * dz > R2) return
      console.info('[spawn_guard] removed ' + e.type + (isNemesis(e) ? ' (nemesis)' : '') + ' at ' + Math.floor(e.getX()) + ' ' + Math.floor(e.getY()) + ' ' + Math.floor(e.getZ()) + ' (' + why + ')')
      e.discard()
      n++
    })
    return n
  }

  NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent'), event => {
    try {
      let level = event.getLevel()
      if (level.isClientSide()) return
      let e = event.getEntity()
      if (!banned(e) || !isOverworld(level)) return
      if (inZone(level, e.getX(), e.getZ())) event.setCanceled(true)
    } catch (err) { console.warn('[spawn_guard] join: ' + err) }
  })

  function sweepAll(server, why) {
    let level = server.overworld()
    sweep(level, worldSpawn(level), why)
    server.getPlayerList().getPlayers().forEach(p => {
      if (!isOverworld(p.level)) return
      centres(p.level, p.getX(), p.getZ(), NEAR + R, false).forEach(c => sweep(p.level, c, why))
    })
  }

  let ticks = 0
  ServerEvents.tick(event => {
    ticks++
    if (ticks % 20 !== 0) return
    try { sweepAll(event.server, 'tick') } catch (err) { console.warn('[spawn_guard] tick: ' + err) }
  })

  PlayerEvents.loggedIn(event => {
    try {
      let p = event.player
      if (!isOverworld(p.level)) return
      sweep(p.level, worldSpawn(p.level), 'login')
      centres(p.level, p.getX(), p.getZ(), NEAR + R, false).forEach(c => sweep(p.level, c, 'login'))
    } catch (err) { console.warn('[spawn_guard] login: ' + err) }
  })
})()
