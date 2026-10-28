// Moonstone and onyx came from Iron's Jewelry; the kept Jewelry mod has no such gems, so they live on as plain gems.
StartupEvents.registry('item', event => {
  event.create('moonstone').displayName('Moonstone')
  event.create('onyx').displayName('Onyx')
})
