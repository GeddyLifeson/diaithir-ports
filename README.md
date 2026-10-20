# Diaithir Compat

On NeoForge's recipe-JSON event it tries every recipe as shipped; if one fails to decode, it upgrades a copy from the 1.21.1 JSON shapes and keeps it only if the copy then decodes. It runs first, so Almost Unified, JAOPCA and KubeJS see 26.1 JSON. It cannot break a working recipe.

It also keeps natural spawns inside geographic ranges on the Tellus Earth dimension. Ranges are datapack files `data/<namespace>/earth_ranges/*.json`: an entity id, an optional species (`species` name or `variant` number, for Untamed Wilds species) and a list of `{"lat": [min, max], "lon": [min, max]}` boxes. When a mob joins the level with the natural or chunk-generation spawn reason, its block position is converted to latitude and longitude with Tellus' own `EarthProjection` at the active preset's world scale, and the spawn is refused outside the boxes. Summons, eggs, spawners, breeding and loaded entities are never touched, entities with no entry are unaffected, and if Tellus is not loaded every spawn is allowed. The shipped files cover Untamed Wilds' mammals, from the regions in the Age I fauna table and today's native ranges (`diaithir-compat/tools/gen_earth_ranges.py` writes them).

Part of the Diaithir modpack for NeoForge 26.1.2. Source: https://github.com/GeddyLifeson/diaithir-ports/tree/diaithircompat. Licence: MIT.
