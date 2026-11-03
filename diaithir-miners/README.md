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

**Test.** `/diaithirminers selftest` (ops) builds a yard at the surface of the spawn chunk and checks: a burner miner against a 3-count tile, an electric miner fed through the public energy capability (a generator pushing 1000 FE every two seconds; it does nothing unpowered, mines when fed), and real patch generation. Dev: put a file `selftest.flag` in the server run folder and it runs once at start, then stops the server. Result 2026-11-03, NeoForge 26.1.2.109 dev server with the Tellus 0.8.3-diaithir.16 jar and `level-type=tellus:earth`: PASS. Electric miner: 0 chunks unpowered, 3 chunks powered. Worldgen: 25x25 chunks around spawn held 1030 deposit tiles, all with counts (4096 at the rim). The candidates and rejects were counted too: 1502 placed, 9414 rejected for no valid surface, 85 for biome.

Test limits: the Diaithir Age I preset needs Regions Unexplored and Biomes O' Plenty, which are not in this dev server, so the test used the plain Earth preset with empty stand-in biomes for the missing mods. The power source was a direct capability insert, not a generator mod. Not yet checked: how the textures look on a client.

**Ids.** Block and item ids are now `diaithirminers:*`. `data/diaithirminers/id_migration.json` lists the old ids; neither source was in the pack so no world holds them.
