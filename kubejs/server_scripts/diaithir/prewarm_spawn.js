// Loads the area a new player is placed in right after the server has started, so the first join does not have to
// generate it. Since 26.1 the server prepares a joining player's spawn (7 x 7 chunks, PrepareSpawnTask) while the client
// sits in the configuration phase, and the connection times out after 30 s without a keep-alive reply. On Tellus worlds the
// first generation of that area (254-section chunks, density function compile, cave setup) takes longer than that on a
// small host, so the first player to join after a fresh start was dropped with "Timed out" and only the second join worked.
// This asks for the same chunks with the same ticket the join uses, without blocking the server thread; they are written
// to disk when they unload, so later boots find them already generated.
ServerEvents.loaded(event => {
  try {
    let tickets = Java.loadClass('net.minecraft.server.level.TicketType')
    let chunkPosClass = Java.loadClass('net.minecraft.world.level.ChunkPos')
    let level = event.server.overworld()
    let p = level.getRespawnData().pos()
    if (typeof p.pos === 'function') p = p.pos() // GlobalPos -> BlockPos
    let pos = new chunkPosClass(p.getX() >> 4, p.getZ() >> 4)
    let t0 = Date.now()
    level.getChunkSource().addTicketAndLoadWithRadius(tickets.PLAYER_SPAWN, pos, 3)
      .thenRun(() => console.info('[prewarm] spawn area around chunk ' + pos.x + ',' + pos.z + ' ready in ' + (Date.now() - t0) + ' ms'))
    console.info('[prewarm] loading spawn area around chunk ' + pos.x + ',' + pos.z)
  } catch (e) {
    console.warn('[prewarm] could not load the spawn area: ' + e)
  }
})
