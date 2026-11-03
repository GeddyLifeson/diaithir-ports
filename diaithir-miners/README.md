# Diaithir Miners

Factorio-style ore patches and miners for NeoForge 26.1.2. Part of the `diaithir-miners` consolidated module (Foundry feature set, `plans/FACTORIO_TECH.md` section 1). Licence: MIT; both sources are MIT and their licences are kept in `LICENSE-AdvancedOreDeposits` and `LICENSE-FactoriOres`.

**Ported from**
- Advanced Ore Deposits 1.0.1 (samllea1, MIT, MC 1.21.1 NeoForge): patch worldgen, finite deposit tiles, raw chunks, chunk-to-ingot recipes. Moved to 26.1 APIs (Identifier, ValueInput/ValueOutput, new registry and loot formats). Dropped: Jade and Create hooks, zinc (Create is not in the pack), chunk force-loading and automation-drop plumbing, wrench removal.
- FactoriOres (Seraphaestus, MIT, MC 1.16 Forge): burner and electric miners, range 1 and 2, 200 ticks per cycle, depth 10, "mine the fullest tile first". Re-written onto the deposit tiles instead of FactoriOres' own ore blocks. Not ported: mechanical (Create) and creative miners, sulfur, lixiviant and fluid ores, Ponder, JEI, TOP.

**How it plays**
- Patches generate in any non-ocean overworld biome as 16 to 64 block blobs of deposit tiles. Each tile holds a count (4096 at the rim to 32768 in the middle of a big patch).
- Break a tile by hand: you get one raw chunk and one count is used; the tile stays until its count is zero. 8 chunks in a ring craft back to the ore item. Explosions do nothing.
- Burner Miner (fuel, right-click with fuel) and Electric Miner (20 FE per tick) sit above a patch. Empty hand takes the output; hoppers and item pipes extract from slot 0 and insert fuel into the burner. Redstone stops them.
- No GUI yet (scripted `selftest` covers behaviour).

**Art.** All textures are drawn from scratch by `tools/gen_assets.py` at 64 px in the Industrial Factory style (dirty steel, hazard rim, faceted ore heaps). No pixel from either source mod or from Factorio is used. Run it again to regenerate everything.

**Test.** `/diaithirminers selftest` (ops) builds a yard above spawn, runs a burner miner against a 3-count tile, and checks the plan generator. Dev: put a file `selftest.flag` in the server run folder and it runs once at start. Result 2026-11-02: PASS (3 chunks, tile removed when empty, 35 patches planned in 80x80 chunks). Not yet tested: worldgen placement in the real Tellus world, client rendering of the textures.

**Ids.** Block and item ids are now `diaithirminers:*`. `data/diaithirminers/id_migration.json` lists the old ids; neither source was in the pack so no world holds them.
