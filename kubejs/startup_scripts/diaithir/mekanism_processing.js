// AllTheOres and Allthemodium for 26.1.2 ship their Mekanism ore-processing recipes and textures but not the items and
// slurries those recipes use (both were built before Mekanism existed for 26.1). This registers them in each mod's namespace.

const MEK_PROCESSING_METALS = {
  alltheores: {
    aluminum: 0xd5dde0, copper: 0xe7834b, gold: 0xf7d34c, iridium: 0xd6d9e8, iron: 0xd8af93, lead: 0x5a5e75, nickel: 0xd4cf99,
    osmium: 0x9fb5c9, platinum: 0x8fd4ea, silver: 0xdfe8ef, tin: 0xc4d8e2, uranium: 0x7cb342, zinc: 0xb8c5b3
  },
  allthemodium: { allthemodium: 0xf2c84b, vibranium: 0x4ee6c4, unobtainium: 0xc35bdc }
}

if (Platform.isLoaded("mekanism")) {
  StartupEvents.registry("item", (event) => {
    for (let [mod, metals] of Object.entries(MEK_PROCESSING_METALS)) {
      Object.keys(metals).forEach((metal) => {
        const ids = [`${metal}_shard`, `${metal}_clump`, `${metal}_crystal`, `dirty_${metal}_dust`]
        ids.forEach((id) => {
          const type = id.startsWith("dirty_") ? "dirty_dust" : id.substring(metal.length + 1)
          event.create(`${mod}:${id}`).texture("layer0", `${mod}:item/${id}`).tag(`c:${type}s`).tag(`c:${type}s/${metal}`)
        })
      })
    }
  })

  StartupEvents.registry("mekanism:chemical", (event) => {
    let Chemical = Java.loadClass("mekanism.api.chemical.Chemical")
    let ChemicalBuilder = Java.loadClass("mekanism.api.chemical.ChemicalBuilder")
    for (let [mod, metals] of Object.entries(MEK_PROCESSING_METALS)) {
      for (let [metal, tint] of Object.entries(metals)) {
        event.createCustom(`${mod}:clean_${metal}`, () => Chemical(ChemicalBuilder.cleanSlurry().tint(tint | 0xFF000000)))
        event.createCustom(`${mod}:dirty_${metal}`, () => Chemical(ChemicalBuilder.dirtySlurry().tint(tint | 0xFF000000)))
      }
    }
  })
}
