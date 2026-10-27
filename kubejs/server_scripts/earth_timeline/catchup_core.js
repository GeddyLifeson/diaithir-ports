// Pure rules for the vegetation and farmland catch-up, no game types, so node can test them (test_catchup_core.js).
// Shared with catchup.js through global.etNS.
(function () {
  const MAX_DOWN = 40 // deepest a tree is searched below the surface

  // Takes the natural trees out of one column of a cleared patch, in two passes over the whole chunk: pass 'trunk' first
  // (it looks for the leaves), then pass 'leaf'. kindAt(x,y,z) is 'air', 'leaf' (natural, not persistent),
  // 'log', 'ground' (grass, dirt, podzol, ...) or 'other'; set(x,y,z,'air') removes. Leaves a player placed (persistent)
  // read 'other' and stay. A trunk (a log stack standing on ground) goes only when natural leaves hang within 3 blocks of
  // its top, so a log cabin or a lone log is never taken. Returns blocks removed.
  function etClearTreeColumn(kindAt, set, x, z, topY, pass) {
    let removed = 0
    let y = topY
    for (let n = 0; n < MAX_DOWN && y > topY - MAX_DOWN; n++, y--) {
      let k = kindAt(x, y, z)
      if (k === 'leaf' && pass === 'leaf') {
        set(x, y, z, 'air')
        removed++
      } else if (k === 'log' && pass === 'trunk') {
        let bottom = y
        while (kindAt(x, bottom - 1, z) === 'log') bottom--
        if (kindAt(x, bottom - 1, z) === 'ground' && leavesNear(kindAt, x, z, y)) {
          for (let t = bottom; t <= y; t++) {
            set(x, t, z, 'air')
            removed++
          }
        }
        y = bottom
      } else if (k === 'ground' || (k === 'log' && pass === 'leaf')) {
        break
      }
    }
    return removed
  }

  function leavesNear(kindAt, x, z, topLogY) {
    for (let dx = -3; dx <= 3; dx++)
      for (let dz = -3; dz <= 3; dz++)
        for (let dy = -2; dy <= 3; dy++)
          if (kindAt(x + dx, topLogY + dy, z + dz) === 'leaf') return true
    return false
  }

  // Farmland rows: every third block along x is a furrow, on flat grass with open air above. heightAt(x,z) is the surface
  // block's y. Returns true if (x, z) should be tilled.
  function etFurrowAt(x, z, heightAt) {
    if (((x % 3) + 3) % 3 !== 0) return false
    let h = heightAt(x, z)
    return heightAt(x - 1, z) === h && heightAt(x + 1, z) === h && heightAt(x, z - 1) === h && heightAt(x, z + 1) === h
  }

  if (typeof global !== 'undefined' && global.etNS) {
    global.etNS.etClearTreeColumn = etClearTreeColumn
    global.etNS.etFurrowAt = etFurrowAt
  }
  if (typeof module !== 'undefined') module.exports = { etClearTreeColumn, etFurrowAt }
})()
