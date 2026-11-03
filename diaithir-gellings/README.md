# diaithir-gellings

Coloured slimes ("gellings") for the Diaithir pack: 11 kinds that spawn in the biome their colour suggests (ink at night, ember in the Nether, rare blush anywhere), each drops its own gel, nine gel craft into a bouncy gel block, and **gel rain** brings swarms down on players while it rains in the overworld (max 8 near a player).

Test commands (op): `/gellings spawn <kind>`, `/gellings swarm` (one rain drop pass).

## Origin and licence
Port of the idea behind **Terrarian Slimes** by lucaargolo (MPL-2.0, Fabric 1.16.5-1.19.2, https://github.com/lucaargolo/terrarian-slimes). Licence checked in the upstream `LICENSE` (copy: `LICENSE-MPL-2.0-upstream.txt`). The mod is a fresh NeoForge 26.1.2 implementation in Java; no upstream source file or asset is copied. Names are our own (no Re-Logic names), all art is procedural (`tools/gen_assets.py`), so this module is MPL-2.0 by credit and licence kept.
Not ported yet: boss slime, crown, throwables, glowsticks (follow-ups).

## Build and fold
`./gradlew build` (JDK 25). Meant to be folded into `diaithir-creatures` by DiaithirRPG `tools/distribution/fold_mods.py` (add `diaithirgellings`), not shipped as a standalone jar. Regenerate assets with `python3 tools/gen_assets.py` (needs Pillow).
