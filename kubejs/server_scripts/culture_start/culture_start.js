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
    // forceload so the surface can be read, move the player onto it, make it their respawn point, then release the chunk
    let run = c => server.runCommandSilent(c)
    run(`execute in minecraft:overworld run forceload add ${x} ${z}`)
    run(`execute in minecraft:overworld positioned ${x} 0 ${z} positioned over world_surface run tp ${uuid} ~0.5 ~ ~0.5`)
    run(`execute as ${uuid} at @s run spawnpoint @s ~ ~ ~`)
    run(`culture set ${uuid} ${e.value}`) // tooltips 2.0.0 (diaithir-ports #25) stores the culture on the player
    run(`execute in minecraft:overworld run forceload remove ${x} ${z}`)
    console.info('[culture] ' + uuid + ' begins at ' + (s.site || e.value) + ' ' + x + ' ' + z)
  })
})
