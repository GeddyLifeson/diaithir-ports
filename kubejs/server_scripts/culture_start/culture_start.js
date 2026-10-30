// Places a new character at the homeland of the culture they picked (engine StoryEvent "culture_start", key=player UUID, value=culture slug).
const CULTURE_SPAWNS = (() => {
  try { return JsonIO.read('kubejs/data/diaithir/culture_spawns.json') || {} }
  catch (e) { console.warn('[culture] spawns unreadable: ' + e); return {} }
})()

const WORLD_BORDER = 59999968 // culture starts lie millions of blocks out: a narrower border kills players there ("left the confines of this world")
ServerEvents.loaded(e => {
  let b = e.server.overworld().getWorldBorder()
  if (b.getSize() < WORLD_BORDER) b.setSize(WORLD_BORDER)
})

// forceload so the chunk generates, then wait for it before reading the surface: an ungenerated chunk reports the build limit (y=384)
const placeAt = (server, uuid, x, z, done) => {
  let run = c => server.runCommandSilent(c)
  run(`execute in minecraft:overworld run forceload add ${x} ${z}`)
  let land = () => {
    run(`execute in minecraft:overworld positioned ${x} 0 ${z} positioned over world_surface run tp ${uuid} ~0.5 ~ ~0.5`)
    run(`execute as ${uuid} at @s run spawnpoint @s ~ ~ ~`)
  }
  let later = (ticks, f) => { if (server.scheduleInTicks) server.scheduleInTicks(ticks, f); else f() }
  later(60, () => land())
  later(160, () => { // second pass in case the chunk was slow, then release it
    land()
    run(`execute in minecraft:overworld run forceload remove ${x} ${z}`)
    if (done) done()
  })
}

NativeEvents.onEvent(Java.loadClass('net.diaithir.ages.mc.StoryEvent'), e => {
  if (String(e.kind) !== 'culture_start') return
  let s = CULTURE_SPAWNS[String(e.value)]
  if (!s) { console.warn('[culture] no spawn for ' + e.value); return }
  let server = Java.loadClass('net.neoforged.neoforge.server.ServerLifecycleHooks').getCurrentServer()
  let uuid = String(e.key)
  server.execute(() => {
    let x = Math.floor(s.x), z = Math.floor(s.z)
    let pl = server.getPlayerList().getPlayer(Java.loadClass('java.util.UUID').fromString(uuid))
    let who = pl ? String(pl.getGameProfile().name()) : uuid // the tooltips culture command takes a name
    server.persistentData.putString('culture_home_' + uuid, x + ',' + z)
    server.runCommandSilent(`culture set ${who} ${e.value}`) // tooltips 2.0.0 (diaithir-ports #25) stores the culture on the player
    placeAt(server, uuid, x, z, () => {
      server.runCommandSilent(`culture set ${who} ${e.value}`) // again once the teleport has settled, as before
      console.info('[culture] ' + who + ' begins at ' + (s.site || e.value) + ' ' + x + ' ' + z)
    })
  })
})

// A death without a bed sends the player to the world spawn; send them back to their culture start instead.
PlayerEvents.respawned(e => {
  let home = String(e.server.persistentData.getString('culture_home_' + e.player.uuid))
  if (!home) return
  let ws = e.server.overworld().getRespawnData().pos()
  if (typeof ws.pos === 'function') ws = ws.pos() // GlobalPos -> BlockPos
  if (Math.abs(e.player.x - ws.getX()) > 512 || Math.abs(e.player.z - ws.getZ()) > 512) return // bed or anchor: leave it
  let xz = home.split(',')
  placeAt(e.server, String(e.player.uuid), parseInt(xz[0]), parseInt(xz[1]))
})
