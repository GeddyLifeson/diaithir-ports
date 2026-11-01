// Places a new character at the homeland of the culture they picked (engine StoryEvent "culture_start", key=player UUID, value=culture slug).
const CULTURE_SPAWNS = (() => {
  try { return JsonIO.read('kubejs/data/diaithir/culture_spawns.json') || {} }
  catch (e) { console.warn('[culture] spawns unreadable: ' + e); return {} }
})()

NativeEvents.onEvent(Java.loadClass('net.diaithir.ages.mc.StoryEvent'), e => {
  if (String(e.kind) !== 'culture_start') return
  let s = CULTURE_SPAWNS[String(e.value)]
  if (!s) { console.warn('[culture] no spawn for ' + e.value); return }
  let server = Java.loadClass('net.neoforged.neoforge.server.ServerLifecycleHooks').getCurrentServer()
  let uuid = String(e.key)
  server.execute(() => {
    let x = Math.floor(s.x), z = Math.floor(s.z)
    let run = c => server.runCommandSilent(c)
    let pl = server.getPlayerList().getPlayer(Java.loadClass('java.util.UUID').fromString(uuid))
    let who = pl ? String(pl.getGameProfile().name()) : uuid // the tooltips culture command takes a name
    // forceload a 7x7 chunk patch so it generates, then wait for it before reading the ground: an ungenerated chunk reports the build limit (y=384)
    run(`execute in minecraft:overworld run forceload add ${x - 48} ${z - 48} ${x + 48} ${z + 48}`)
    const Heightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
    // the nearest open spot: no canopy above (leaf-free height equals leaf height) and dry; the culture's own place is tried first, then rings outwards
    let spot = () => {
      let level = server.overworld(), best = null
      for (let r = 0; r <= 48 && !best; r += 4) {
        for (let k = 0; k < (r === 0 ? 1 : 12) && !best; k++) {
          let a = k / 12 * 2 * Math.PI, px = Math.floor(x + r * Math.cos(a)), pz = Math.floor(z + r * Math.sin(a))
          let top = level.getHeight(Heightmap.MOTION_BLOCKING, px, pz), ground = level.getHeight(Heightmap.MOTION_BLOCKING_NO_LEAVES, px, pz)
          let below = level.getBlockState(new (Java.loadClass('net.minecraft.core.BlockPos'))(px, ground - 1, pz))
          if (top === ground && below.getFluidState().isEmpty() && ground > -62) best = [px, ground, pz]
        }
      }
      return best || [x, level.getHeight(Heightmap.MOTION_BLOCKING_NO_LEAVES, x, z), z]
    }
    let land = () => {
      let p = spot()
      run(`execute in minecraft:overworld run tp ${uuid} ${p[0] + 0.5} ${p[1]} ${p[2] + 0.5}`)
      run(`execute as ${uuid} at @s run spawnpoint @s ~ ~ ~`)
      global.diaPlaceGiver(server, uuid, String(e.value))
    }
    let later = (ticks, f) => { if (server.scheduleInTicks) server.scheduleInTicks(ticks, f); else f() }
    later(100, () => land())
    later(200, () => { // second pass in case the chunk was slow, then release it
      land()
      run(`culture set ${who} ${e.value}`) // tooltips 2.0.0 (diaithir-ports #25) stores the culture on the player
      run(`execute in minecraft:overworld run forceload remove ${x - 48} ${z - 48} ${x + 48} ${z + 48}`)
      console.info('[culture] ' + who + ' begins at ' + (s.site || e.value) + ' ' + x + ' ' + z)
    })
    run(`culture set ${who} ${e.value}`)
  })
})
