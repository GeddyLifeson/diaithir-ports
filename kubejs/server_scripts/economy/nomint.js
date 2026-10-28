// Nobody mints coin. No recipe in the pack, from any mod, makes a currency item: Lightman's mint and its coin, block and pile recipes,
// and our own culture coins (data/diaithir/nomint.json lists the patterns; tools/economy/no_mint.py fails the build if a recipe outside
// them makes one). Coins come only from a store paying for goods, or from handing the culture's own staple to the keeper at the peg (/coin).
ServerEvents.recipes(event => {
  let cfg = econRead('kubejs/data/diaithir/nomint.json') || { outputs: [], types: [] }
  // KubeJS wraps a regex output filter lazily and throws "Ingredients can't be empty" for one that matches no item (a mod that is not
  // installed), which ends the whole event; so match the registry here and remove by exact item id
  let ids = Item.getTypeList().map(String)
  cfg.outputs.forEach(re => { let rx = new RegExp(re); ids.filter(id => rx.test(id)).forEach(id => event.remove({ output: id })) })
  cfg.types.forEach(t => event.remove({ type: t }))
})
