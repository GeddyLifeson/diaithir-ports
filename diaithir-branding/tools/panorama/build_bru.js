// Builds the Brú (DiaithirRPG content/omniverse/kubejs/server_scripts/panscriptum/bru.js) in a dev world, for the title panorama.
//   node build_bru.js bru.js catalogue.json "run/saves/<world>/datapacks/bru"   then in game: /reload, /function bru:part0
// Panorama: start the client with -DMC_DEBUG_ENABLED=true -DMC_DEBUG_PANORAMA_SCREENSHOT=true, /gamemode spectator,
// /tp @s 0.5 157.6 0.5 0 0 (the Well, between Rings I and II), press Ctrl+F2, then quantize run/screenshots/panorama_*.png
// into assets/minecraft/textures/gui/title/background/.
const fs = require('fs'), path = require('path')
const stub = new Proxy(function () {}, { get: () => stub, apply: () => stub })
const ctx = { ServerEvents: stub, PlayerEvents: stub, BlockEvents: stub, ItemEvents: stub, EntityEvents: stub, ServerCommands: stub, Commands: stub, console }
const cat = JSON.parse(fs.readFileSync(process.argv[3]))
const src = fs.readFileSync(process.argv[2], 'utf8')
const fn = new Function(...Object.keys(ctx), 'pscData', src + '\nreturn { bruLayout, BRU, BRU_CUSTODES }')
const { bruLayout, BRU, BRU_CUSTODES } = fn(...Object.values(ctx), () => cat)
const L = bruLayout()
const rel = c => c.replace(/^(fill|setblock) (-?\d+) (-?\d+) (-?\d+)(?: (-?\d+) (-?\d+) (-?\d+))?/, (m, op, x1, y1, z1, x2, y2, z2) =>
  `${op} ${x1 - BRU.x} ${y1} ${z1 - BRU.z}` + (x2 !== undefined ? ` ${x2 - BRU.x} ${y2} ${z2 - BRU.z}` : ''))
const out = ['forceload add -80 -80 80 80', 'time set noon']

// Test shot only: the shelves get books (slot states), varied per column, as they will once Age I's books are shelved.
let seed = 7, rnd = () => (seed = (seed * 1103515245 + 12345) % 2147483648) / 2147483648
const books = c => c.replace(/chiseled_bookshelf\[facing=(\w+)\]/, (m, f) =>
  `chiseled_bookshelf[facing=${f},${[0, 1, 2, 3, 4, 5].map(i => `slot_${i}_occupied=${rnd() < 0.85}`).join(',')}]`)
L.cmds.forEach(c => { if (!c.includes('waystones:')) out.push(books(rel(c))) })
L.npcs.forEach(n => out.push(`summon minecraft:villager ${n[1]} ${n[2]} ${n[3]} {NoAI:1b,Invulnerable:1b,PersistenceRequired:1b,Silent:1b,CustomName:${JSON.stringify(BRU_CUSTODES[n[0]][0])},CustomNameVisible:1b,VillagerData:{profession:"minecraft:librarian",level:5,type:"minecraft:plains"}}`))
if (0) L.labels.forEach(l => out.push(`summon minecraft:text_display ${l[0] + 0.5} ${l[1]} ${l[2] + 0.5} {text:${JSON.stringify(l[3])},billboard:"center"}`))
const dir = path.join(process.argv[4], 'data/bru/function')
fs.mkdirSync(dir, { recursive: true })
const N = 20000
for (let i = 0; i * N < out.length; i++) {
  const part = out.slice(i * N, (i + 1) * N)
  if ((i + 1) * N < out.length) part.push(`schedule function bru:part${i + 1} 20t`)
  else part.push('say Bru built')
  fs.writeFileSync(path.join(dir, `part${i}.mcfunction`), part.join('\n') + '\n')
}
fs.writeFileSync(path.join(process.argv[4], 'pack.mcmeta'), JSON.stringify({ pack: { description: 'Bru test build', pack_format: 101, min_format: 101, max_format: 101 } }))
console.log(out.length, 'commands,', Math.ceil(out.length / N), 'parts; chamber', L.chamber)
