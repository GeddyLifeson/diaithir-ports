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

Not built: bee entity textures per species (bees look vanilla; the species shows on the jar and in the apiary status), wild nests and hives in the world, hopper/pipe output from the apiary, genetic traits (productivity, endurance), bee-in-world species names on hover.
