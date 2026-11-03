# Megamachines (inside diaithir-ships/keelwright)

Mega construction and extraction vehicles (12) and megastructures (7), Age gated. Design: MEGAMACHINES.md (Global-Repo PR 27, plans folder). `catalog.json` is the single source for blocks, balance, Codex and Age gates, same role as `../data/parts.json`.

- Dig rate must equal power x efficiency / specific energy of the medium (checked).
- Every vehicle keeps crew seats. Crewless running needs `autonomy_capable` (Age VII+ types) AND an autonomy module item; hook is the autonomy thread's per-entity API (#220), not an Age test.
- Ships: VS2 walkable decks. Output as pallets and containers. Crew wages from the employer treasury.

Check: `python3 -I tools/gen_megamachines.py /tmp/out` (rewrites nothing in the repo; compare with catalog.json).
Cab block (diaithir-ships `org.diaithir.mega`): `megamachines:cab` block entity holds the spec id and the autonomy-module flag; crewless only if catalog `autonomy_capable` AND the module (`diaithircarts:autonomy_module`, by id) is fitted, else a living crew member within 4 m (ship-aware). `autonomousCrew(job, cab)` is the wages hook. Selftest: `-Dmegamachines.selftest=true runServer` (13 checks pass). `data/megamachines/catalog.json` in ships is a copy of `catalog.json` here until the dig-head thread unifies the core.
