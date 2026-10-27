// Cold for the Spoiled mod. Spoiled ticks every food stack's timer (a pack every 30 ticks, a container by its block-entity modifier). We
// give back timer for the cold a stack sits in, so a root cellar, an ice pit or a frozen room slows rot as a fridge does.
// Rot follows RimWorld's curve: none at or below 0 C, full at 10 C and up, linear between. A room-temperature system plugs in as
// global.SPOIL_HOOK.tempAt = (level, x, y, z) => degrees C (or the climate scripts' global.TEMP_AT). The lower of temperature and
// surroundings applies. Containers are remembered the first time a player opens one.

const SPOIL_SCAN = 200 // ticks between pack scans
const SPOIL_REG_EVERY = 600 // ticks between container scans
const SPOIL_REG_CAP = 1200 // containers remembered (the saved string stays under the 64 KB NBT cap)
const SPOIL_REG_PER_SCAN = 40

const spoilBase = id => String(id).replace(/^[^:]*:/, '')

// RimWorld rot speed against temperature in C.
const spoilTempFactor = t => t <= 0 ? 0 : t >= 10 ? 1 : t / 10

// Share of the room-temperature rate that applies to a container. blockAt(dx, dy, dz) -> block id; roofed = earth or stone over it;
// waterNear = water within 2; temp = C or null.
const spoilColdFactor = (blockAt, roofed, waterNear, temp) => {
  let f = 1
  for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) for (let dz = -1; dz <= 1; dz++) {
    let m = global.SPOIL_COLD_NEAR[blockAt(dx, dy, dz)]
    if (m !== undefined && m < f) f = m
  }
  if (roofed && global.SPOIL_ROOT_CELLAR < f) f = global.SPOIL_ROOT_CELLAR
  if (roofed && waterNear && global.SPOIL_SPRINGHOUSE < f) f = global.SPOIL_SPRINGHOUSE
  if (temp !== null && temp !== undefined) f = Math.min(f, spoilTempFactor(temp))
  return f
}

// Spoiled updates a scan interval holds, and how many of them the keeping gives back (a negative count is rot sped up). mult is the share of
// the room-temperature rate that applies. A fraction is settled by chance.
const spoilRefund = (interval, mult, rand) => {
  let x = interval / global.SPOIL_UPDATE * (1 - mult)
  let base = Math.floor(x)
  return base + ((rand || Math.random)() < x - base ? 1 : 0)
}

// Butchery stations set a shelf life as a percent of normal on an item (diaithirbutchery:shelf, 400 = four times as long). The rate
// a stack rots at is the cold's share divided by that.
const spoilStackMult = (envMult, shelf) => envMult * 100 / (shelf > 0 ? shelf : 100)

// Degrees C at a cell from the room-temperature system, else null.
const spoilTempAt = (level, x, y, z) => {
  let f = global.SPOIL_HOOK.tempAt || global.TEMP_AT
  if (!f) return null
  try {
    let t = f(level, x, y, z)
    return t === null || t === undefined || isNaN(t) ? null : Number(t)
  } catch (e) { return null }
}

let SpoilHelper = null
try { SpoilHelper = Java.loadClass('com.mrbysco.spoiled.util.SpoilHelper') } catch (e) { console.log('Spoiled is not loaded; cold storage does nothing') }

// Spoiled 12.1.0 fills its recipe list only when the recipes are re-sent (/reload or a client join), so a dedicated server that has just
// started rots nothing. Fill it ourselves at start.
ServerEvents.loaded(event => {
  if (!SpoilHelper) return
  let K = Java.loadClass('com.mrbysco.spoiled.Constants')
  let R = Java.loadClass('com.mrbysco.spoiled.registration.SpoiledRecipes')
  K.SPOIL_RECIPES.clear()
  K.SPOIL_RECIPES.addAll(event.server.getRecipeManager().recipeMap().byType(R.SPOIL_RECIPE_TYPE.get()))
})

let ShelfType = null
const spoilShelf = stack => {
  if (ShelfType === null) {
    try {
      let reg = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
      let id = Java.loadClass('net.minecraft.resources.Identifier')
      ShelfType = reg.DATA_COMPONENT_TYPE.getValue(id.parse('diaithirbutchery:shelf')) || false
    } catch (e) { ShelfType = false }
  }
  if (!ShelfType) return 100
  let v = stack.get(ShelfType)
  return v === null || v === undefined ? 100 : Number(v)
}

// Over `interval` ticks, gives each spoiling stack in an item handler back the timer its keeping earns: env is the cold's share, the
// stack's own shelf life (if a butchery station set one) comes on top.
const spoilRewind = (inv, interval, env) => {
  for (let i = 0; i < inv.getSlots(); i++) {
    let s = inv.getStackInSlot(i)
    if (s.isEmpty() || !SpoilHelper.isSpoiling(s)) continue
    let n = spoilRefund(interval, spoilStackMult(env, spoilShelf(s)))
    if (n === 0) continue
    let taken = inv.extractItem(i, s.getCount(), false) // handlers hand out copies and refuse direct writes: take the stack, change it, put it back
    SpoilHelper.setSpoilTime(taken, Math.max(0, SpoilHelper.getSpoilTime(taken) - n))
    inv.insertItem(i, taken, false)
  }
}

// ---- containers ----
const spoilReg = server => {
  try { return JSON.parse(server.persistentData.getStringOr('spoil_reg', '{}')) } catch (e) { return {} }
}
const spoilSaveReg = (server, reg) => {
  let n = 0
  for (let d in reg) n += Object.keys(reg[d]).length
  for (let d in reg) for (let k in reg[d]) if (n > SPOIL_REG_CAP) { delete reg[d][k]; n-- } // oldest-inserted first
  server.persistentData.putString('spoil_reg', JSON.stringify(reg))
}

const spoilContainer = (level, x, y, z) => {
  let block = level.getBlock(x, y, z)
  let inv = block.inventory
  if (!inv) return false
  let blockAt = (dx, dy, dz) => String(level.getBlock(x + dx, y + dy, z + dz).id)
  let roofed = true
  for (let dy = 1; dy <= 4; dy++) if (blockAt(0, dy, 0) === 'minecraft:air') { roofed = false; break }
  let water = false
  for (let dx = -2; dx <= 2 && !water; dx++) for (let dz = -2; dz <= 2; dz++) if (blockAt(dx, 0, dz) === 'minecraft:water') { water = true; break }
  let mult = spoilColdFactor(blockAt, roofed, water, spoilTempAt(level, x, y, z))
  spoilRewind(inv, SPOIL_REG_EVERY, mult)
  return true
}

const spoilRegister = (server, level, b) => {
  if (!b.inventory) return
  let reg = spoilReg(server)
  let d = String(level.dimension)
  reg[d] = reg[d] || {}
  let key = `${b.x},${b.y},${b.z}`
  if (reg[d][key] === undefined) { reg[d][key] = 1; spoilSaveReg(server, reg) }
}

// A container is registered when it is opened or placed; one a world generated stays unregistered (and rots at the plain rate) until first opened.
BlockEvents.rightClicked(event => spoilRegister(event.server, event.level, event.block))
BlockEvents.placed(event => spoilRegister(event.server, event.level, event.block))

ServerEvents.tick(event => {
  if (!SpoilHelper) return
  let server = event.server
  let t = server.tickCount
  if (t % SPOIL_SCAN === 0) {
    server.players.forEach(p => {
      let temp = spoilTempAt(p.level, Math.floor(p.x), Math.floor(p.y), Math.floor(p.z))
      spoilRewind(p.inventory, SPOIL_SCAN, temp === null ? 1 : spoilTempFactor(temp))
    })
  }
  if (t % SPOIL_REG_EVERY !== 0) return
  let reg = spoilReg(server), dirty = false, done = 0
  for (let dim in reg) {
    let level = server.getLevel(dim)
    if (!level) continue
    for (let key in reg[dim]) {
      if (done >= SPOIL_REG_PER_SCAN) break
      let c = key.split(',').map(Number)
      if (!level.hasChunkAt(new BlockPos(c[0], c[1], c[2]))) continue
      done++
      if (!spoilContainer(level, c[0], c[1], c[2])) { delete reg[dim][key]; dirty = true }
    }
  }
  if (dirty) spoilSaveReg(server, reg)
})

// Hides: beasts drop green hide, which rots until it is tanned.
EntityEvents.drops(event => {
  let id = String(event.entity.type)
  if (!/^minecraft:(cow|mooshroom|horse|donkey|mule|llama|trader_llama|camel)$/.test(id)) return
  event.drops.forEach(d => {
    if (String(d.item.id) === 'minecraft:leather') d.setItem(Item.of('diaithir:raw_hide', d.item.getCount()))
  })
})
