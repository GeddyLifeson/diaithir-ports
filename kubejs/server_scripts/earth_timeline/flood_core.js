// Pure flood rule, no game types, so node can test it (test_flood_core.js). Shared with flood.js through `global`, because
// KubeJS may load every script into its own scope and in any order; each file is wrapped so no name leaks between them.
(function () {
  const ET_DATUM_Y = 200 // sea surface block at the Age I datum (Tellus seaLevel); the sea stands at this + flood
  const ET_MAX_STEPS = 24 // deepest rise handled in one pass, and the deepest any column is searched

  // Fills downward from the new sea surface through air and stops at the first block that is not air, so a coast fills up to
  // the new level, an old sea is topped up to it, and a tree or a roof in the way is left dry. Returns how many it filled.
  function etFloodColumn(isAir, setWater, x, z, flood) {
    if (flood <= 0) return 0
    let top = ET_DATUM_Y + flood, filled = 0
    for (let y = top; y > top - Math.min(ET_MAX_STEPS, flood + 2) && isAir(x, y, z); y--) {
      setWater(x, y, z)
      filled++
    }
    return filled
  }

  if (typeof global !== "undefined" && global.etNS) global.etNS.etFloodColumn = etFloodColumn
  if (typeof module !== 'undefined') module.exports = { etFloodColumn, ET_DATUM_Y }
})()
