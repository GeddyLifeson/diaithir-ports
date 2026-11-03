# Aircraft roster (Immersive Vehicles pack, inside diaithir-arms)

`ROSTER.csv`: one iconic mass-produced design per row, no variants (same rule as the guns). 60 rows: pioneer, WWI, interwar, WWII, jet, modern airliners, transports, trainers, utility, seaplanes, drone, 5 helicopters. `source` is the Wikipedia page for each row; `first_flight_year` drives Age gating (VI up to 1945, VII after; the tech-by-age doc puts aircraft in VI).

Status: roster only. Mod-first check: Immersive Aircraft (tech-batch-a) only covers a few generic craft, so the named designs are built clean-room as `assets/diaithiraircraft/` in this jar, same generator pattern as `tools/gen_tank.py`. Not started: models, textures, IV defs, Diaithiris names, library/codex. Fuel comes from the shared fuel system (cars thread), assembly from the shared assembly yard (airframe/hangar yard requested), guns hook into diaithir-firearms. Specs (mass kg, speed m/s, range m) to be filled from each source page, SI only.
