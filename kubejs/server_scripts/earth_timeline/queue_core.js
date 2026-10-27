// Throttled chunk work queue shared by flood.js, melt.js and catchup.js, no game types so node can test it
// (test_queue_core.js). The catch-up must never pull chunks into memory: it only touches chunks that are already loaded and
// near a player, spends at most a few ms and a few chunks per tick, and drops everything else (unloaded, out of range, over
// the cap), because a chunk that loads later arrives through the load event anyway.
(function () {
  // opts: { max: queue cap (oldest dropped), retries: how many scans a not-yet-ready chunk may wait, scan: entries looked at per tick }
  function etMakeQueue(opts) {
    const MAX = opts.max || 256
    const RETRIES = opts.retries || 100
    const SCAN = opts.scan || 32
    let items = []
    let queued = {}

    function key(cx, cz) { return cx + ',' + cz }

    return {
      size: () => items.length,
      add(cx, cz) {
        let k = key(cx, cz)
        if (queued[k]) return false
        if (items.length >= MAX) { let old = items.shift(); delete queued[key(old[0], old[1])] }
        queued[k] = true
        items.push([cx, cz, 0])
        return true
      },
      clear() { items = []; queued = {} },
      // ctx: { loaded(cx,cz), near(cx,cz), work(cx,cz), now() ms }. Handles at most `perTick` chunks and stops once `budgetMs`
      // is spent (always at least one if any is ready). Returns the number worked.
      run(ctx, perTick, budgetMs) {
        let start = ctx.now(), worked = 0, scanned = 0
        let n = items.length
        while (n-- > 0 && items.length && scanned < SCAN && worked < perTick) {
          let it = items.shift()
          scanned++
          let cx = it[0], cz = it[1]
          if (!ctx.near(cx, cz)) { delete queued[key(cx, cz)]; continue }
          if (!ctx.loaded(cx, cz)) {
            if (it[2] < RETRIES) { it[2]++; items.push(it) } else delete queued[key(cx, cz)]
            continue
          }
          delete queued[key(cx, cz)]
          ctx.work(cx, cz)
          worked++
          if (ctx.now() - start >= budgetMs) break
        }
        return worked
      }
    }
  }

  if (typeof global !== 'undefined' && global.etNS) global.etNS.etMakeQueue = etMakeQueue
  if (typeof module !== 'undefined') module.exports = { etMakeQueue }
})()
