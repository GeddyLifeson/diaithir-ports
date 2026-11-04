# Keelwright (package inside diaithir-ships, ids prefixed keelwright_)

Grid-built, crewed ships (own Diaithir design). Plan: SHIPBUILDING.md in the project plans folder.
Builds on VS2 mass and force APIs from diaithir-ships, not on Eureka internals.

Step 1 (this PR): `data/parts.json` part catalog and `tools/check_parts.py` validator with a thrust-to-mass check per hull class.
Run: `python3 diaithir-ships/keelwright/tools/check_parts.py`

Create: Propulsion fold (survey part 1, MIT): four new parts in `data/parts.json` and `data/thruster_fuels.json`.
- `ducted_propeller` (drive, Age V, sea/air, 60 kN, 5 PU/tick shaft power), `fluid_thruster` (drive, Age VII, air/space, 160 kN, burns fuel from the table), `magnetic_clamp` (new kind `dock`, Age VI, pulls or pushes against another clamp within 32 m, for docking and cargo), `gyro_wheel` (new kind `attitude`, Age VII, turns the hull without thrust).
- `thruster_fuels.json` merges upstream's 16 per-mod fuel files into 9 rows keyed by shared `c:` fluid tags (same fuel from CDG, Immersive Petroleum, TFMG and Immersive Engineering becomes one row), with thrust and burn multipliers.
- `tools/check_parts.py` validates the new kinds and the fuel table; run it: 30 parts, 9 fuels, 0 errors, thrust/mass inside the class ranges.
Not done: Java blocks for the four parts (the catalog is the single source and the blocks read it), VS2 force application, art, names, recipes, Codex pages. Upstream Create: Propulsion targets Create 6 on 1.21.1, which has no 26.1.2 build, so nothing is ported as code.
