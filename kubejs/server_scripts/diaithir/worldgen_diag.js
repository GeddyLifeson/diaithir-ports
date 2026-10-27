// Boot diagnostics, log only. Writes lines starting "[diag]" to logs/kubejs/server.log (and latest.log) once per server load or /reload.
//  1. Feature order cycle (finding JJ-10): vanilla's FeatureSorter throws "Feature order cycle found, involved sources: [minecraft:birch_forest]"
//     when the feature lists of the biomes of one biome source cannot be put in a single order. This walks every biome, step by step,
//     and logs (a) a placed feature that appears twice in one biome's step list, which on its own makes a one-biome cycle (A, B, A), and
//     (b) per dimension, a real cycle across the biomes of that dimension's biome source (A before B in one, B before A in another)
//     with the biomes that cause each edge. Vanilla sorts per generator over getBiomeSource().possibleBiomes(), so a cycle between
//     biomes that never share a source (a Backrooms level and a Regions Unexplored biome) cannot fail anything and is not reported.
//  (Rhino re-runs a const declared in a loop body and throws "redeclaration", so loop locals use let.)
//  2. Which Sharpness definition won (finding JJ-12): the data pack in kubejs/data pins the Apothic Enchanting one.
//     Expected "[diag] sharpness exclusiveSet=empty".
function diagName(x) {
  let s = String(x)
  let m = s.match(/\/ ([^\]\s]+)\]$/)
  return m ? m[1] : s
}
function diagList(holderSet, placedReg) {
  let out = []
  holderSet.forEach(h => {
    let n = null
    // biome feature lists hold direct holders after biome modifiers ran, so the key is looked up from the value
    try { let k = h.unwrapKey(); if (k.isPresent()) n = diagName(k.get()) } catch (e) {}
    if (!n) { try { let id = placedReg.getKey(h.value()); if (id) n = String(id) } catch (e) {} }
    if (!n) n = 'inline:' + String(h.value()).slice(0, 240)
    out.push(n)
  })
  return out
}
function diagRegistry(access, key) {
  try { return access.lookupOrThrow(key) } catch (e) { return access.registryOrThrow(key) }
}
function diagAnalyse(biomes, label) {
  let graphs = [] // per step: { adj: Map<string, Set<string>>, owners: Map<string, string[]> }
  let dupes = 0
  biomes.forEach(b => {
    let steps = b.settings.features()
    for (let step = 0; step < steps.size(); step++) {
      let names = diagList(steps.get(step), b.placedReg)
      let g = graphs[step] || (graphs[step] = { adj: new Map(), owners: new Map() })
      let seen = new Map()
      names.forEach((n, i) => {
        if (!seen.has(n)) seen.set(n, [])
        seen.get(n).push(i)
      })
      seen.forEach((pos, n) => {
        if (pos.length > 1) {
          dupes++
          console.warn(`[diag] DUPLICATE feature in one biome step (${label}): biome=${b.id} step=${step} feature=${n} positions=${pos.join(',')} of ${names.length}`)
        }
      })
      for (let i = 0; i + 1 < names.length; i++) {
        let a = names[i], c = names[i + 1]
        if (a === c) continue
        if (!g.adj.has(a)) g.adj.set(a, new Set())
        g.adj.get(a).add(c)
        let k = a + ' > ' + c
        if (!g.owners.has(k)) g.owners.set(k, [])
        let o = g.owners.get(k)
        if (o.length < 4 && o.indexOf(b.id) < 0) o.push(b.id)
      }
    }
  })
  let cycles = 0
  graphs.forEach((g, step) => {
    if (!g) return
    let state = new Map() // 1 = on stack, 2 = done
    let stack = []
    let visit = start => {
      let work = [[start, Array.from(g.adj.get(start) || []), 0]]
      state.set(start, 1); stack.push(start)
      while (work.length) {
        let top = work[work.length - 1]
        if (top[2] >= top[1].length) { state.set(top[0], 2); stack.pop(); work.pop(); continue }
        let nxt = top[1][top[2]++]
        let st = state.get(nxt)
        if (st === 1) {
          if (cycles < 3) {
            let path = stack.slice(stack.indexOf(nxt)).concat([nxt])
            let edges = []
            for (let i = 0; i + 1 < path.length; i++) edges.push(`${path[i]} > ${path[i + 1]} [${(g.owners.get(path[i] + ' > ' + path[i + 1]) || []).join(', ')}]`)
            console.warn(`[diag] FEATURE ORDER CYCLE (${label}) step=${step}: ${edges.join(' ; ')}`)
          }
          cycles++
        } else if (!st) {
          state.set(nxt, 1); stack.push(nxt)
          work.push([nxt, Array.from(g.adj.get(nxt) || []), 0])
        }
      }
    }
    g.adj.forEach((v, k) => { if (!state.has(k)) visit(k) })
  })
  return { dupes: dupes, cycles: cycles }
}
function diagFeatureOrder(server) {
  let Registries = Java.loadClass('net.minecraft.core.registries.Registries')
  let placedReg = diagRegistry(server.registryAccess(), Registries.PLACED_FEATURE)
  let signatures = new Map()
  let levels = 0, bad = 0
  server.getAllLevels().forEach(level => {
    levels++
    let label = diagName(level.dimension)
    let biomes = []
    level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().forEach(h => {
      let id
      try { id = diagName(h.unwrapKey().get()) } catch (e) { id = 'inline' }
      biomes.push({ id: id, settings: h.value().getGenerationSettings(), placedReg: placedReg })
    })
    let sig = biomes.map(b => b.id).sort().join(',')
    if (signatures.has(sig)) { console.info(`[diag] ${label}: same ${biomes.length} biomes as ${signatures.get(sig)}, not repeated`); return }
    signatures.set(sig, label)
    let r = diagAnalyse(biomes, label)
    if (r.dupes || r.cycles) bad++
    console.info(`[diag] ${label}: ${biomes.length} biomes, ${r.dupes} duplicate feature(s) in a step, ${r.cycles} cycle edge(s)`)
  })
  console.info(`[diag] feature order done: ${levels} levels, ${signatures.size} distinct biome sets, ${bad} with a problem`)
}
function diagSharpness(server) {
  let Registries = Java.loadClass('net.minecraft.core.registries.Registries')
  let reg = diagRegistry(server.registryAccess(), Registries.ENCHANTMENT)
  reg.entrySet().forEach(en => {
    if (diagName(en.getKey()) !== 'minecraft:sharpness') return
    let e = en.getValue()
    let ex = String(e.exclusiveSet())
    console.info(`[diag] sharpness maxLevel=${e.getMaxLevel()} exclusiveSet=${ex.indexOf('[]') >= 0 || ex.indexOf('Direct{contents=[]}') >= 0 ? 'empty' : ex}`)
  })
}
ServerEvents.loaded(event => {
  try { diagFeatureOrder(event.server) } catch (e) { console.error('[diag] feature order failed: ' + e) }
  try { diagSharpness(event.server) } catch (e) { console.error('[diag] sharpness failed: ' + e) }
})
