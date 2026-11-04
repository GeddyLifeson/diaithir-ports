# Survey-mod port licence audit (megamachines)

Checked 2026-11-03 against the actual LICENSE file at the head of each upstream repo (raw text read, not site badges).

| Mod | Upstream | LICENSE file says | Verdict |
|---|---|---|---|
| RFTools Builder | McJtyMods/RFToolsBuilder (master) | MIT, (c) 2019 McJtyMods | Port allowed. Keep notice. Needs McJtyLib replacement (shim) |
| Industrial Foregoing (laser drill) | Buuz135/Industrial-Foregoing (master) | MIT, (c) 2017 Buuz135 | Port allowed for the laser drill logic. Keep notice |
| Create Aeronautics Physics Gantry | Creators-of-Aeronautics/Simulated-Project (main) | Code MIT; assets under aeronautics/simulated/offroad `resources/assets/` are All Rights Reserved | Code rewrite onto VS2 allowed; do NOT copy models, textures, lang or sounds |
| Northstar Redux | Astronauts-of-Create/Northstar-Redux (main, LICENSE.md) | MIT, (c) 2024 lightning2401, (c) 2025-2026 Redstonneur1256 | Port allowed; check asset folders before copying art |
| Galacticraft Legacy | TeamGalacticraft/Galacticraft-Legacy (master) | MIT, (c) 2022 Team Galacticraft | Port allowed; 1.12-era code, structure logic only |
| Intelligence: Quarry | IntelligenceModding/vanillaquarry (master; 26.1.2 branch has no LICENSE file) | "Copyright (c) 2022 All rights reserved." (Modrinth page says MIT, the repo file does not) | NOT portable. Clean-room only: the megamachines thread's bucket-wheel/Dig engine already covers a quarry |
| Create More Drill Heads | Forsteri123/CreateMoreDrillHeads (main, forge/1.20.1, forge/1.21.1) | "Copyright (c) 2023/2024 All rights reserved." | NOT portable. Clean-room only (drill-head tiers become `MegaSpec` heads) |
| Flan's Reloaded (tank tracks) | FlansMods/FlansMod, FlansModReloaded (LICENSE.txt) | Creative Commons Attribution-NonCommercial-ShareAlike 3.0 | NOT MIT and NonCommercial: do not port. Clean-room tracks on the mega chassis |

Result: 5 of 8 are MIT-code ports; 3 (Quarry, More Drill Heads, Flan's) are clean-room only despite the brief calling them MIT.
Rule applied: no new jars, MIT notices kept in LICENSES.md when code lands, namespace `diaithirmega`, no duplicate of the chassis/entities or bucket-wheel in #224.
Next ports, in order: RFTools Builder (shape/quarry card logic onto `Dig`), IF laser drill (focus/lens rules onto `MegaSpec`), VS2 gantry rewrite, then Northstar/Galacticraft structure data.
