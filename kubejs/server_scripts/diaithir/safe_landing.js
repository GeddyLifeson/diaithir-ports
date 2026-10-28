// Safe landing. KubeJS 8 / MC 26.1, no jar.
//  1. Every login and every respawn: log "[safe_landing] <name> y=A -> y=B", give a few seconds of Slow Falling and Resistance,
//     revive a saved character at 0 health, and set a player who is airborne more than 3 blocks up down on the surface once the
//     chunk under them is loaded (creative, spectator and flying players are left alone).
//  The world spawn is not changed here (spawn placement belongs to the culture-start work).
(function () {
  let Heightmap = null
  function surfaceY(level, x, z) {
    if (!Heightmap) Heightmap = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')
    return level.getHeight(Heightmap.MOTION_BLOCKING_NO_LEAVES, Math.floor(x), Math.floor(z))
  }
  function tag(player, what) { console.info('[safe_landing] ' + player.username + ' ' + what) }

  function protect(player) {
    try {
      player.potionEffects.add('minecraft:slow_falling', 20 * 30, 0, false, false)
      player.potionEffects.add('minecraft:resistance', 20 * 15, 4, false, false)
    } catch (e) { console.warn('[safe_landing] effects: ' + e) }
  }

  function settle(player, triesLeft, y0) {
    try {
      let level = player.level
      let x = player.x, z = player.z, y = player.y
      let ground = surfaceY(level, x, z)
      if (ground <= level.getMinY() + 1) { // chunk not generated yet
        if (triesLeft > 0) player.server.scheduleInTicks(20, () => settle(player, triesLeft - 1, y0))
        else tag(player, 'y=' + Math.floor(y0) + ' -> y=' + Math.floor(y) + ' (surface unknown, left alone)')
        return
      }
      let skip = player.isCreative() || player.isSpectator() || (player.abilities && player.abilities.flying)
      if (!skip && y > ground + 3) {
        player.server.runCommandSilent('execute as ' + player.username + ' run tp @s ' + x.toFixed(2) + ' ' + ground + ' ' + z.toFixed(2))
        tag(player, 'y=' + Math.floor(y0) + ' -> y=' + ground + ' (set down)')
      } else {
        tag(player, 'y=' + Math.floor(y0) + ' -> y=' + Math.floor(y) + ' (surface ' + ground + ', nothing to do)')
      }
    } catch (e) { console.warn('[safe_landing] settle ' + e) }
  }

  function arrive(event) {
    let player = event.player
    try {
      let y0 = player.y
      tag(player, 'arrived at y=' + Math.floor(y0) + ' health=' + player.health)
      if (player.health <= 0) {
        try { player.setHealth(player.maxHealth); player.deathTime = 0; tag(player, 'was at 0 health, restored') } catch (e) { console.warn('[safe_landing] revive: ' + e) }
      }
      protect(player)
      player.server.scheduleInTicks(20, () => settle(player, 40, y0))
    } catch (e) { console.warn('[safe_landing] arrive ' + e) }
  }
  PlayerEvents.loggedIn(arrive)
  PlayerEvents.respawned(arrive)
})()
