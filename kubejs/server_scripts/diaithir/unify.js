// Tag unification: makes each canonical tag in global.diaithirItemFamilies / diaithirFluidFamilies
// include the alias tags other mods use for the same thing. Additive only; no recipes are rewritten.
// Families are declared in startup_scripts/diaithir/unify_families.js.

ServerEvents.tags('item', event => {
  Object.keys(global.diaithirItemFamilies).forEach(canon => {
    global.diaithirItemFamilies[canon].forEach(alias => {
      if (event.get(alias).getObjectIds().size() > 0) {
        event.add(canon, `#${alias}`)
        global.diaithirLinkedAliases[alias] = canon
      }
    })
  })
})

ServerEvents.tags('fluid', event => {
  Object.keys(global.diaithirFluidFamilies).forEach(canon => {
    global.diaithirFluidFamilies[canon].forEach(alias => {
      if (event.get(alias).getObjectIds().size() > 0) event.add(canon, `#${alias}`)
    })
  })
})
