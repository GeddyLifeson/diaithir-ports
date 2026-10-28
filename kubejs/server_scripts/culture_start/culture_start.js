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
    // forceload so the chunk generates, then wait for it before reading the surface: an ungenerated chunk reports the build limit (y=384)
    run(`execute in minecraft:overworld run forceload add ${x} ${z}`)
    let land = () => {
      run(`execute in minecraft:overworld positioned ${x} 0 ${z} positioned over world_surface run tp ${uuid} ~0.5 ~ ~0.5`)
      run(`execute as ${uuid} at @s run spawnpoint @s ~ ~ ~`)
    }
    let later = (ticks, f) => { if (server.scheduleInTicks) server.scheduleInTicks(ticks, f); else f() }
    later(60, () => land())
    later(160, () => { // second pass in case the chunk was slow, then release it
      land()
      run(`culture set ${who} ${e.value}`) // tooltips 2.0.0 (diaithir-ports #25) stores the culture on the player
      run(`execute in minecraft:overworld run forceload remove ${x} ${z}`)
      console.info('[culture] ' + who + ' begins at ' + (s.site || e.value) + ' ' + x + ' ' + z)
    })
    run(`culture set ${who} ${e.value}`)
  })
})
