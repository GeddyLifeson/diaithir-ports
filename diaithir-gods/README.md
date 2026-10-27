# Diaithir Gods

A NeoForge 26.1.2 port of [RPG Gods](https://github.com/skyjay1/RPG-Gods) by skyjay1 (branch `main-1.19`), made for the Diaithir game pack.
Mod id and namespace: `diaithir_gods`. Java package: `rpggods` (unchanged from upstream so the diff stays reviewable).

RPG Gods adds data-driven deities: altars (statue entities), offerings, sacrifices, favor levels, blessings and curses (perks),
patrons, and affinities for tameable mobs. Everything about a deity is described in a datapack.
The datapack format is kept compatible with upstream. See [docs/DEITY_FORMAT.md](docs/DEITY_FORMAT.md).

## License and credits

- Code: GNU General Public License v3 (see `LICENSE`), as upstream. The original code is by skyjay1.
  This port changes it to compile and run on NeoForge 26.1.2 (new rendering, GUI, data layout and registry APIs).
- Art: clean-room. No upstream textures, models, sounds or language files are included. Every texture is drawn by
  `tools/gen_art.py` (Python, Pillow) in a simple Celtic stone, wood and bronze look. Language text is original wording.
- The upstream README credits select textures to Daniel Eddeland; none of those are used here.

## Content

- Altar (statue) item and entity, with the pose and equipment GUI.
- Offering brazier block, favor scroll item, favor GUI.
- Global loot modifiers (autosmelt or cobble, crop multiplier) used by perks.
- `/favor` command for operators, including `/favor selftest <deity>` which makes a fake player offer to a deity and prints the favor change.
- A generated court of 436 test deities in the `diaithir_gods` namespace (see below).

## Building

Needs JDK 25.

    export JAVA_HOME=/path/to/jdk-25
    ./gradlew build           # jar in build/libs/
    ./gradlew runServer       # dev server (run/eula.txt must contain eula=true)
    ./gradlew runClient -Pquickplay=<world folder name>   # optional: join a world in run/saves

## Regenerating art and deities

    python3 tools/gen_art.py                               # needs Pillow
    python3 tools/gen_deities.py [path/to/gods.json]       # default /mnt/project-files/mythology/out/gods.json

`gen_deities.py` reads the mythology list at run time and rewrites `data/diaithir_gods/deity/**` and
`assets/diaithir_gods/lang/en_us.json` (merged with `tools/lang_base.json`). Fields used: id, name, court, rank, domain,
emblem, offering, effect, age, living. The effect becomes a potion blessing, the offering word maps to vanilla items,
courts flagged `living` get no curses and no favor loss for killing creatures.
`deity/altar/stone_figure.json` is kept by hand.

## Not ported

- The sitting render offset for tamed mobs (affinity). Sitting still works, but the model is not lowered.
