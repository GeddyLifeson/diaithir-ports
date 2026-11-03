# Diaithir Fauna

Every real animal species of Earth and its deep past, on shared body plans (MIT). One entity type and one model per plan (71), species are data in `species.json` (size, mass, diet, habitat, real range, extinction date, skin). Natural spawns pick a species by Tellus Earth range, biome habitat and the Era year. Fossils (12 rock blocks, fragment, cleaned fossil, preparation bench) give a taxon by depth (age), position (region) and rock.

Commands (op): `/fauna summon <species>`, `/fauna fossil <rock>`, `/fauna study <species>` (mark a fossil taxon studied), `/fauna revive` (revival with the held cleaned fossil, at your feet), `/fauna census` (counts, habitat mismatches, mspt, heap).

## Rebuild the data
The catalogue lives in `/mnt/project-files/animals-fossils` (see SCHEMA.md there).

    python3 tools/plan_gen.py                          # body plans
    python3 tools/skin_gen.py ART_DIR OUT_DIR          # skins (needs the creature art kit)
    python3 tools/build_species.py OUT_DIR             # species.json, lang, spawn modifiers, skins (256-colour)
    python3 tools/spawn_modifiers.py                   # biome spawn weights and group sizes per plan (weight 8, herds from the herd flag)
    python3 tools/gen_fossil_assets.py                 # fossil blocks, items, worldgen, revival assets
    python3 tools/era_feed.py                          # era-index feed

## Butchery
`python3 tools/gen_butchery.py` maps every species (fossils too) to a carcass class (plan archetype x mass tier) in `data/diaithirfauna/butchery/fauna.json` (read by diaithir-butchery) and `butchery_species.json`. With diaithir-butchery loaded a kill drops one carcass of that class; without it, vanilla drops.

## Ages
Wild species spawn from Age I. Fossils are dug in Age III (Cenozoic), IV (Mesozoic), V (Paleozoic).

## Revival
Extinct species come back only in the far future. Craft a revival matrix (amethyst, diamond, ender eye) and a revival chamber. Hold a cleaned fossil you have studied in the main hand and the matrix in the other, then use the fossil on the chamber (or run `/fauna revive`). It consumes the fossil and one cost item (server config `costItem`, default `minecraft:nether_star`, `costCount` 1) and spawns the species with the entity tag `Revived`, persistent. The matrix is not consumed. Water species need the chamber's top block to be water.

Gate by DNA tier: tier 1 needs Age VII, tiers 2-3 Age VIII (Future), tiers 4-5 Age IX (Rodos). The Age is the larger of the server config `age` and the score of the fake player `#age` in the objective `diaithirfauna_age`; the Age scripts set it (`/scoreboard players set #age diaithirfauna_age 7`, also from KubeJS with `server.runCommandSilent`). Default 0: nothing is revivable. Create the objective once with `/scoreboard objectives add diaithirfauna_age dummy`.

## Species bees (clean-room stand-in for Productive Bees)
Productive Bees is all rights reserved, so this is our own design, from the public idea only. Vanilla bees carry a species (attachment `bee_species`). Wild bees pick a surface species (meadow, sandy, muddy) when they appear; two parents of a crossing pair breed the crossed species (coal = meadow + sandy, iron = coal + muddy, and so on up to diamond; the table is `data/diaithirfauna/bees/species.json`), other pairings give a parent's species. A Bee Jar catches a bee (use on it) and lets it out (use on the ground). The Apiary holds up to three jars of bees and, with at least three flowers within five blocks, by day and out of the rain, makes one comb per bee every three minutes. Empty hand takes the combs, sneaking also takes the bees out in jars. Combs are smelted (or blasted for double) or crafted into the resource; all combs are in tag `c:combs`. Age gates in `age_gate_bees.json`, Codex text in `library-sync/diaithirfauna-bees.json`, `id_migrations_bees.json` for old worlds. `tools/gen_bee_assets.py` (S-id claimed) writes assets and data.

Test: `/fauna beetest` (owners) checks that every species can be bred from wild bees, outputs exist, crossing (both orders), the jar, the apiary capacity, combs with flowers and none without, and saving. Headless only.

Traits: every bee has genes "species/productivity/endurance" (0 to 3 each). Productivity makes combs 25 percent faster a level. Endurance 1 lets a bee work in rain, 2 also after dark. A child's traits are its parents' average, nudged now and then. Genes sync to the client, ride in the jar and the apiary, and are shown in the jar tooltip and apiary status.

Wild nests: `nest_meadow` (plains, forests), `nest_sandy` (desert, beach, badlands, savanna), `nest_muddy` (swamps, rivers) come up on the surface (worldgen files `nest_*`). By day a nest lets out bees of its species, up to three of that species within 24 blocks. Breaking a nest drops the nest.

Species colours: `client/SpeciesBeeRenderer` replaces the vanilla bee renderer and tints the body of the vanilla bee texture to the species colour, built in memory from the player's own vanilla textures when first needed (nothing derived from Mojang textures is shipped). Compiled and the server side is tested; the renderer has not been run in a client.

`tools/gen_bee_migrations.py` writes `id_migrations_bees.json` from Productive Bees' public language file (131 blocks: nests and hives, 2 items: cages to jars and combs where we have the same resource, 8 bee entities to a plain bee).

Not built: hopper or pipe output from the apiary, a trait-inspecting item, nests in the Nether or End.

## Library mobs (Panscriptum)
`data/diaithirfauna/library_mobs.tsv.gz` holds one row per Library MOB entry (id, name, body plan, Age tier, kg, scale, diet, flags, element), 59,351 of 59,431 (81 entries the sorter kept failing on). They use the 71 body plans, are kept out of `Species.ALL`, spawn tables and suggestions, and appear only through `/fauna libsummon <entry id>` or code (`Species.byId`). The Age tier raises health and damage (+40% per tier above 1, damage capped at 40). Names are the entry names; temper "hostile" sets the aggressive flag. Rebuild: `tools/libmob_extract.py JOB` (S6832), `tools/libmob_run.py JOB` (S6833), `tools/libmob_build.py JOB` (S6834). Stats come only from the entry's own text; no text is written.

### Library mob spawning
- **Planets:** `library_planets.tsv` (from /mnt/project-files/planets/planets.tsv) maps dimension to planet id and `opens_age`; an entry belongs to the planet named by the second part of its id. A creature of tier t appears from Age `opens_age + t - 1` (Age = `Revival.age`, at least 1).
- **Natural:** `LibrarySpawner` makes one attempt per player every 5 s, 24-48 blocks away, capped at 8 Library creatures within 64 blocks. They despawn when far. Test: `/fauna libspawntest <attempts> <age>`. Not yet per biome tag.
- **Structure spawners:** a spawner naming `diaithirfauna:<plan>` (any body plan, e.g. `theropod`, `giant_arthropod`, `primate_ape`, `lizard`) gets a hostile Library creature of that plan from the planet it stands in; in other dimensions it falls back to its usual species.

### Habitat and mod creatures
- Column 10 of `library_mobs.tsv.gz` is a habitat bitmask over the fauna habitat list (from the entry's own text, `tools/libmob_hab.py`, S6873); 0 means anywhere (25,849 entries). Natural spawns skip a creature whose habitat does not fit the biome: vanilla biomes use the tag logic, the planets' own biomes use `Habitat.planetMask` (name suffix for sea, river, shore and cave, otherwise temperature and height).
- `library_mob_bases.tsv` (`tools/libmob_bases.py`, S6879): 21 Library entries that exactly match an Alex's Mobs creature spawn as that creature when the mod is loaded, else as a fauna body plan. Matches with several candidates, entries with no match and mod creatures with no entry are left (see mobs-names/MATCH_PANSCRIPTUM.tsv).
