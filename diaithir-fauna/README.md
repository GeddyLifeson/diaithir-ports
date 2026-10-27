# Diaithir Fauna

Every real animal species of Earth and its deep past, on shared body plans (MIT). One entity type and one model per plan (71), species are data in `species.json` (size, mass, diet, habitat, real range, extinction date, skin). Natural spawns pick a species by Tellus Earth range, biome habitat and the Era year. Fossils (12 rock blocks, fragment, cleaned fossil, preparation bench) give a taxon by depth (age), position (region) and rock.

Commands (op): `/fauna summon <species>`, `/fauna fossil <rock>`.

## Rebuild the data
The catalogue lives in `/mnt/project-files/animals-fossils` (see SCHEMA.md there).

    python3 tools/plan_gen.py                          # body plans
    python3 tools/skin_gen.py ART_DIR OUT_DIR          # skins (needs the creature art kit)
    python3 tools/build_species.py OUT_DIR             # species.json, lang, spawn modifiers, skins (256-colour)
    python3 tools/gen_fossil_assets.py                 # fossil blocks, items, worldgen
    python3 tools/era_feed.py                          # era-index feed

## Butchery
`python3 tools/gen_butchery.py` maps every species (fossils too) to a carcass class (plan archetype x mass tier) in `data/diaithirfauna/butchery/fauna.json` (read by diaithir-butchery) and `butchery_species.json`. With diaithir-butchery loaded a kill drops one carcass of that class; without it, vanilla drops.

## Ages
Wild species spawn from Age I. Fossils are dug in Age III (Cenozoic), IV (Mesozoic), V (Paleozoic). Revival (not built yet) by DNA tier: Age VII endgame tier 1, Future tiers 2-3, Rodos tiers 4-5.
