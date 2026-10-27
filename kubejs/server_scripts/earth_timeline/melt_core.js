// Pure ice-melt rule, no game types, so node can test it (test_melt_core.js). Shared with melt.js through `global`.
(function () {
  const ET_MELT_MAX = 400 // deepest ice run measured in one column

  // A column's ice run starts at topY and goes down through packed ice and snow blocks (isIce). If it is thicker than the
  // year now allows, the top comes off: air, a snow cap on what is left, and gravel on the ground when all of it goes.
  // A column whose top is not ice (a player's build, a tree, bare land) is never touched. Returns blocks removed.
  function etMeltColumn(isIce, set, x, z, topY, desired) {
    if (!isIce(x, topY, z)) return 0
    let thick = 0
    while (thick < ET_MELT_MAX && isIce(x, topY - thick, z)) thick++
    desired = Math.max(0, desired)
    if (thick <= desired) return 0
    let remove = thick - desired
    for (let i = 0; i < remove; i++) set(x, topY - i, z, desired === 0 && i === remove - 1 ? 'gravel' : 'air')
    if (desired > 0) set(x, topY - remove, z, 'snow')
    return remove
  }

  if (typeof global !== "undefined" && global.etNS) global.etNS.etMeltColumn = etMeltColumn
  if (typeof module !== 'undefined') module.exports = { etMeltColumn }
})()
