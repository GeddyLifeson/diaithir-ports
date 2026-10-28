// Tag unification: makes each canonical tag in global.diaithirItemFamilies / diaithirFluidFamilies
// include the alias tags other mods use for the same thing. Additive only; no recipes are rewritten.
// Families are declared in startup_scripts/diaithir/unify_families.js.

// KubeJS 8 TagKubeEvent has no getObjectIds(); getElementIds() lists the tags that exist, so an alias is linked only if it exists.
function diaithirExistingTags(event) {
  let have = {}
  event.getElementIds().forEach(id => { have[String(id)] = true })
  return have
}

ServerEvents.tags('item', event => {
  let have = diaithirExistingTags(event)
  Object.keys(global.diaithirItemFamilies).forEach(canon => {
    global.diaithirItemFamilies[canon].forEach(alias => {
      if (have[alias]) {
        event.add(canon, `#${alias}`)
        global.diaithirLinkedAliases[alias] = canon
      }
    })
  })
})

ServerEvents.tags('fluid', event => {
  let have = diaithirExistingTags(event)
  Object.keys(global.diaithirFluidFamilies).forEach(canon => {
    global.diaithirFluidFamilies[canon].forEach(alias => {
      if (have[alias]) event.add(canon, `#${alias}`)
    })
  })
})
