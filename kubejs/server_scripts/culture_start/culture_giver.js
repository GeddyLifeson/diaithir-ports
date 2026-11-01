// The quest giver every start has: a villager of the player's own culture standing beside the spawn point, dressed in that culture's kit.
// Right-click to take the culture's first main quest and its Omniverse entry quest (texts from culture_givers.json, built by tools/culture_start/givers.py).
const GIVERS = (() => {
  try { return JsonIO.read('kubejs/data/diaithir/culture_givers.json') || {} }
  catch (e) { console.warn('[culture] givers unreadable: ' + e); return {} }
})()

const roleName = r => String(r).replace(/_/g, ' ').replace(/^./, c => c.toUpperCase())

NativeEvents.onEvent(Java.loadClass('net.diaithir.ages.mc.StoryEvent'), e => {
  if (String(e.kind) !== 'culture_start') return
  let g = GIVERS[String(e.value)]
  if (!g) { console.warn('[culture] no giver for ' + e.value); return }
  let server = Java.loadClass('net.neoforged.neoforge.server.ServerLifecycleHooks').getCurrentServer()
  let uuid = String(e.key), slug = String(e.value)
  let later = (ticks, f) => { if (server.scheduleInTicks) server.scheduleInTicks(ticks, f); else f() }
  later(260, () => server.execute(() => { // after the second landing pass in culture_start.js
    let pl = server.getPlayerList().getPlayer(Java.loadClass('java.util.UUID').fromString(uuid))
    if (!pl) return
    let level = pl.level
    server.runCommandSilent(`kill @e[tag=dia_giver_${uuid}]`)
    let npc = level.createEntity(g.female ? 'mca:female_villager' : 'mca:male_villager') || level.createEntity('minecraft:villager')
    if (!npc) { console.warn('[culture] cannot make a giver for ' + slug); return }
    npc.setPosition(pl.x + 2, pl.y, pl.z)
    npc.setCustomName(Text.of(`${roleName(g.role)} of ${g.name}`))
    npc.setCustomNameVisible(true)
    npc.setNoAi(true); npc.setInvulnerable(true); npc.setPersistenceRequired()
    npc.addTag('dia_giver'); npc.addTag('dia_giver_' + uuid); npc.addTag('dia_culture_' + slug)
    npc.spawn()
    try { // the culture's own clothes (diaithir-compat); without it the villager keeps MCA's look
      Java.loadClass('net.diaithir.compat.SitePeople').dressAs(npc, slug, !!g.female, 'ruler')
    } catch (err) { console.warn('[culture] giver of ' + slug + ' not dressed: ' + err) }
    console.info('[culture] giver placed for ' + slug)
  }))
})

ItemEvents.entityInteracted(e => {
  let t = e.target
  if (!t || !t.tags.contains('dia_giver') || String(e.hand) !== 'MAIN_HAND') return
  let slug = ''
  t.tags.forEach(x => { if (String(x).startsWith('dia_culture_')) slug = String(x).slice(12) }) // no spread: this Rhino rejects [...set]
  let g = GIVERS[slug]
  if (!g) return
  let taken = e.player.persistentData
  g.quests.forEach((q, i) => {
    let key = 'dia_quest_' + q.id
    if (taken.getBooleanOr(key, false)) { if (i === 0) e.player.tell(Text.gray('You have already taken this quest.')); return }
    taken.putBoolean(key, true)
    e.player.tell(Text.gold(i === 0 ? 'New quest: ' : 'Also: ').append(Text.white(q.text)))
    e.server.runCommandSilent(`tag ${e.player.username} add ${key}`) // read by the Codex tracker and the quest goals
  })
  e.cancel()
})
