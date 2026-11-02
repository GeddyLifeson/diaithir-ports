// Seasonal crop growth. Vanilla crops fire NeoForge's CropGrowEvent.Pre on every random-tick growth attempt; we read the
// temperature at the crop (global.TEMP_AT: outdoor season and altitude, or the room it sits in) and thin or stop growth
// outside the crop's band in global.CLIMATE.crops. Nothing runs per tick; one cached lookup per growth attempt.
(function () {
  let C = global.CLIMATE
  let Pre = Java.loadClass('net.neoforged.neoforge.event.level.block.CropGrowEvent$Pre')
  let Result = Java.loadClass('net.neoforged.neoforge.event.level.block.CropGrowEvent$Pre$Result')
  let cache = {}
  let cacheSize = 0
  let CACHE_TICKS = 100

  function tempHere(level, pos) {
    let now = level.getLevelData().getGameTime()
    let key = String(level.dimension) + '|' + (pos.getX() >> 3) + '|' + (pos.getY() >> 2) + '|' + (pos.getZ() >> 3)
    let hit = cache[key]
    if (hit && now - hit[0] < CACHE_TICKS) return hit[1]
    let t = global.TEMP_AT(level, pos.getX(), pos.getY(), pos.getZ())
    if (cacheSize > 4000) { cache = {}; cacheSize = 0 }
    cache[key] = [now, t]
    cacheSize++
    return t
  }

  // 0 = no growth, 1 = full speed
  function growthFactor(band, t) {
    if (t <= band[0] || t >= band[2]) return 0
    if (t < band[1]) return 0.15 + 0.85 * (t - band[0]) / (band[1] - band[0])
    return 0.15 + 0.85 * (band[2] - t) / (band[2] - band[1])
  }

  NativeEvents.onEvent(Pre, event => {
    let level = event.getLevel()
    if (level.isClientSide()) return
    let band = C.crops[String(event.getState().getBlock().id)]
    if (!band) return
    let t = tempHere(level, event.getPos())
    if (t === null || t === undefined) return
    let f = growthFactor(band, Number(t))
    if (f <= 0 || (f < 1 && Math.random() > f)) event.setResult(Result.DO_NOT_GROW)
  })
})()
