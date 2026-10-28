// Registers the generated cuisine items, crops and cookware blocks from global.CUISINE (written by tools/cuisine/build.py).
const CUISINE_BOX = { pot: [3, 0, 3, 13, 8, 13], flat: [2, 0, 2, 14, 3, 14], tall: [4, 0, 4, 12, 14, 12], full: null }

StartupEvents.registry('item', event => {
  const C = global.CUISINE
  for (let [id, name, hunger, sat] of C.items) {
    let b = event.create(id).displayName(C.english[id] || name)
    if (hunger) b.food(hunger, sat)
  }
  for (let [id, name, hunger, sat] of C.dishes) event.create(id).displayName(C.english[id] || name).food(hunger, sat)
})

StartupEvents.registry('block', event => {
  const C = global.CUISINE
  for (let [produce, seed, climate, name] of C.crops) {
    if (!seed) continue // a Productive Farming crop: its own mod registers the seed block
    event.create(seed, 'crop').displayName(name).age(3).crop(produce)
  }
  for (let s of C.stations) {
    let b = event.create('diaithir:st_' + s.id).displayName(C.english['diaithir:st_' + s.id] || s.name).texture('diaithir:block/st_' + s.id).hardness(1.5).resistance(3)
    let box = CUISINE_BOX[s.shape]
    if (box) b.notSolid().box(box[0], box[1], box[2], box[3], box[4], box[5])
    let rows = Math.ceil(s.slots / 3)
    b.blockEntity(info => {
      info.inventory('in', [], 3, rows)
      info.rightClickOpensInventory('in')
      info.serverTicking()
      info.tickFrequency(20)
    })
  }
})
