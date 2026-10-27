# Diaithir Myth Beasts

Eighteen mythic creatures for the Diaithir game pack (NeoForge 26.1.2, mod id `diaithir_myth`).

Creature stats, behaviours and presets are adapted from **Greek Fantasy** by **skyjay1**
(https://github.com/skyjay1/GreekFantasy, GPLv3). This mod is GPLv3 as well.
No upstream model, texture, sound or language file is used: all geometry, skins, egg icons and names are new,
and every creature carries a coined Diaithiris name.

## What is in it

* Beast, flyer and avatar presets. Beasts walk, fight or flee. Flyers hover and flee. Avatars are two-phase
  bosses with a boss bar: below 50% health they speed up (+35%), hit harder (+20%) and gain one extra attack
  (ground slam, charge, web snare or venom cloud, depending on the creature).
* Courts: hellenic, minoan, italic, anatolian, canaanite and nilotic. Living-tradition courts get nothing here.
  Creatures that already exist in the pack (marked `iceandfire:*` in the creature table) are skipped.
* Spawn eggs, loot tables, English lang and a biome modifier that adds the creatures (low weights) to every biome in the
  tag `diaithir_myth:spawns` (`data/diaithir_myth/tags/worldgen/biome/spawns.json`, which pulls in `#minecraft:is_overworld`).
  A pack can override that tag to narrow the biomes.
* `data/diaithir_myth/myth/creatures.json` is the single source of the creature table (stats, size, court, sounds);
  `data/diaithir_myth/myth/courts.json` lists each creature's court.

## Mapping to Greek Fantasy

See `docs/CREATURES.md`.

## Building

    python3 tools/gen_myth.py   # regenerates data and art (needs Pillow and numpy; reads the creature table)
    ./gradlew build

JDK 25 is required.
