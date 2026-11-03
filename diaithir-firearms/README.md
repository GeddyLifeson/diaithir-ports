# Diaithir Firearms (diaithir-firearms)

Clean-room, MIT. Replaces the all-rights-reserved Vic's Point Blank in the pack. No code, model, texture, sound or config of Point Blank was read or used; the 147 old item ids are only mapped to the nearest new archetype (`id_migrations.json`, names from the pack's item list).

## What it adds
- 17 guns by class, not by brand: light and heavy sidearm, magnum, compact SMG, carbine, assault rifle, marksman rifle, sniper rifle, anti-materiel rifle, pump / combat / automatic shotgun, light machine gun, rotary minigun, grenade launcher, rocket launcher, energy rifle.
- 10 ammunition items (9 mm, .45, magnum, 5.56, 7.62, 12 gauge, .50, 40 mm grenade, rocket, energy cell), 6 attachments (red dot, 4x and 8x scopes, suppressor, muzzle brake, foregrip), 4 parts (gunmetal nugget, ingot, mesh; gun internals).
- All numbers live in `src/main/resources/guns.json`; `tools/gen.py` writes it with every texture (drawn with PIL, 16x16), model, recipe, tag, lang, Age gate, name map, library entry and the id migration table.

## Roster (every mass-produced design)
`roster/ROSTER.md` lists 248 real firearms, one per design (most iconic version, no variants), from the 1288 hand cannon to 2019. Source rows are `roster/b*.csv` (Wikipedia title per row) and `roster/calibers.csv`; `tools/gen_roster.py` (run after `gen.py`) derives stats from class, caliber muzzle energy and real rate of fire. Ages by year: before 1861 V, to 1945 VI, after VII. Each design is made at the stonecutter from its class gun (muskets from an iron ingot). Stats are approximate; Diaithiris names and the quest lane are pending.

## Rules (server authoritative)
- Hold use to fire (auto guns keep firing, semi-auto needs a fresh press). Hitscan with spread, falloff past half range, 1.5x headshot, pellets for shotguns, explosions for launchers (follow the mob griefing rule). The client only sends "reload" (key R).
- Magazine is an item component; reload takes the gun's reload time, uses ammo from the inventory, is cancelled by changing slot. Creative players need no ammo.
- Attachments: sneak + use with the attachment in the off hand fits it (one per kind); sneak + use with an empty off hand removes the last one. Sights zoom the view while sneaking, suppressor quietens the shot and trims damage 10 %, muzzle brake cuts recoil, foregrip tightens spread.
- Age: guns are crafting-gated by `age_gates.json` (VI sidearms and pump shotgun, VII rifles and SMG and launcher, Rodos .50, minigun, rocket and energy). A player below a gun's Age (scoreboard `age`, default 9 when absent) has double spread and 1.5x reload.
- Units: damage in hp, range in m, cooldown in ticks (20 = 1 s); tooltip shows rounds per minute.
- Recipes take `#c:ingots/gunmetal` and vanilla items; gunmetal blends iron, copper and coal. Ammo uses gunpowder (paper for shells, TNT for grenades, firework rocket for rockets, redstone for cells).

## Sounds, names, Codex
- 12 sounds are synthesised by `tools/gen_sounds.py` (numpy waveforms, ffmpeg to .ogg; no samples, so no licence). Freesound was tried first and returned 403 from the cloud. They have not been listened to, only generated and registered; expect to retune them by ear on the PC.
- Names: `names_map.json` has English plus Diaithiris kennings (words from LEXICON.json, grammar from GRAMMAR.md, run through `diaithiris-archaic/tools/archaic.py`). Items are named in English with the Diaithiris name as a grey italic tooltip line (`names.json`).
- Codex: `library/firearms_library.json` (arms_library format) and `codex/firearms_handbook.json` (handbook chapters).

## Not done yet
No underbarrel slot (the old underbarrel launchers map to the grenade launcher), no weapon printer / workstation blocks (their ids map to the smithing table), recoil is a view kick only, no first-person animation or gun models (items use flat sprites), quests for the guns wait for the quest lane.

## Test
`/firearmsselftest` (op): see TEST.md.
