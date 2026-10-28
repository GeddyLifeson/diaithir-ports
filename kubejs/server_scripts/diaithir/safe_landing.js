// A player who joins or respawns at the world spawn must not die falling to the ground. The spawn point is stored with a
// fixed height (a respawned survival player was seen at y=231 over Creag Ruadh, around -251201 / -1541408, and died of
// fall damage). Two guards, neither needs a jar:
//  1. a few seconds of Slow Falling and Resistance, so a fall in the first moments cannot kill;
//  2. once the chunk under the player is loaded, a player standing in the air well above the surface is set down on it.
// Players who are underground (below the surface) are left alone, as are creative and spectator players and fliers.
(function () {
  let Heightmap = null
  function surfaceY(level, x, z) {
    if (!Heightmap) Heightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
    return level.getHeight(Heightmap.MOTION_BLOCKING_NO_LEAVES, Math.floor(x), Math.floor(z))
  }

  function protect(player) {
    try {
      player.potionEffects.add('minecraft:slow_falling', 20 * 30, 0, false, false)
      player.potionEffects.add('minecraft:resistance', 20 * 15, 4, false, false)
    } catch (e) { console.warn('[safe_landing] effects: ' + e) }
  }

  function settle(player, triesLeft) {
    try {
      if (!player || player.isRemoved() || player.abilities.flying || player.isCreative() || player.isSpectator()) return
      let level = player.level
      let x = player.x, z = player.z
      let ground = surfaceY(level, x, z)
      // getHeight returns the first free y above the surface; a loaded chunk under the world floor means not ready yet
      if (ground <= level.getMinY() + 1) {
        if (triesLeft > 0) player.server.scheduleInTicks(20, () => settle(player, triesLeft - 1))
        return
      }
      if (player.y > ground + 3) {
        let name = player.username
        player.server.runCommandSilent('execute as ' + name + ' run tp @s ' + x.toFixed(2) + ' ' + ground + ' ' + z.toFixed(2))
        console.info('[safe_landing] set ' + name + ' down from y=' + Math.floor(player.y) + ' to y=' + ground)
      }
    } catch (e) { console.warn('[safe_landing] ' + e) }
  }

  function arrive(event) {
    let player = event.player
    protect(player)
    // wait for the spawn chunks, then check again a few times while the area generates
    player.server.scheduleInTicks(20, () => settle(player, 30))
  }

  PlayerEvents.loggedIn(arrive)
  PlayerEvents.respawned(arrive)
})()
