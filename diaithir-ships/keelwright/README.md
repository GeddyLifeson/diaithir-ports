# Keelwright (package inside diaithir-ships, ids prefixed keelwright_)

Grid-built, crewed ships (own Diaithir design). Plan: SHIPBUILDING.md in the project plans folder.
Builds on VS2 mass and force APIs from diaithir-ships, not on Eureka internals.

Step 1 (this PR): `data/parts.json` part catalog and `tools/check_parts.py` validator with a thrust-to-mass check per hull class.
Run: `python3 diaithir-ships/keelwright/tools/check_parts.py`
