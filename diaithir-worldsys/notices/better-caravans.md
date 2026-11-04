# Better Caravans fold (package `caravans`)

Concept, trade pool and tuning follow **Better Caravans** 1.1.2 by Nikrecs (Modrinth `better-caravans`, CurseForge `better-caravans`).
Licence: MIT. The jar's neoforge.mods.toml says `license="MIT"`, Modrinth lists MIT, and the page says free to use in any modpack. No LICENSE file and no public source repository were found, so the grant rests on that metadata.

Taken: `caravan_trades/default.json` (12 weighted trades), copied as `data/diaithirworldsys/caravan_trades/default.json`. Code is new Java written from the described behaviour (the upstream classes were not copied): a wandering trader leading two leashed pack animals (camel in hot biomes, llama in cold, mule elsewhere), weighted trade pick, steady walking, 20 minute life, random spawn near players, `/caravan spawn|despawn|info`. Left out: mini-raids and rewards, caravan stops, discounts, pack-animal loot chests, advancements, translations. No art. No turn-based mechanics.
