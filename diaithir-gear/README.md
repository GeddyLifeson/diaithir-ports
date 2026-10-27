# Gléaus (diaithir-gear)

MIT, clean-room, data driven. NeoForge 26.1.2. One small module for the cross-mod gear rows.

| Row | What it does |
|---|---|
| 5 Bullet parity | Point Blank (all rights reserved, untouched) bullet hits are scaled per gun toward the Musket Mod: a hit of calibre C does `16 x sqrt(E_C / 1800 J)` (the musket ball is 16 at Age IV), reduced for fast fire by `clamp(sqrt(120 / rpm), 0.6, 1)`, so Age VII rounds beat the musket per hit without a damage-per-second jump. Table `data/diaithirgear/bullet_parity.json` (58 guns, regenerate with `tools/gen_parity.py <pointblank jar>`), override in `config/diaithirgear/bullet_parity.json`, global blend `parity.strength`. Calibre tags `diaithir:ammo/<calibre>`, `diaithir:ammo/cartridge`, `diaithir:ammo/bullets`. |
| 6 Gems | `c:gems/<stone>` now also lists the Jewelry gems; `diaithirgear:gems/socketable` is the one shared gem tag (Silent Gems, Jewelry, Iron's Jewelry). Six Apotheosis gems (`diaithirgear:peridot, garnet, opal, pearl, aquamarine, tanzanite`) give the same attribute as the Silent Gems trait of that stone at matching levels (`gem_affix_map.json`). |
| 7 Set bonuses | One bonus per family, tiers by pieces worn, applied as attribute modifiers every second, only at or above the family Age (scoreboard `age`). Families: holy (Paladins), shadow (Rogues), arcane (Wizards), all armour slots; wayfarer (Artifacts) and gemwork (Jewelry, Iron's Jewelry) count Curios items. Data `data/diaithirgear/set_bonuses/`, item tags `diaithirgear:sets/<family>`, override in `config/diaithirgear/set_bonuses`. |

`/gearselftest` runs the checks (see TEST.md). Age gates: `age_gates.json`. Display names and Library lines come from the compute job `gear_crossmod_names` (Diaithiris via tools/archaic.py); until it is merged the six gems show their translation key.
Tools: `tools/gen_parity.py`, `tools/gen_sets.py` (needs the pack item list and the Silent Gems jar; writes only our own data).
