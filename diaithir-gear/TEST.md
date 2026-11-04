# Test results

Build: `./gradlew build` (JDK 25, NeoForge 26.1.2.116) -> `diaithir-gear-neoforge-26.1.2-1.0.0.jar`.

Dedicated server (gradle runServer, rcon), fresh world, with: Apotheosis 9.1.0 + Apothic Attributes/Enchanting + Placebo, Silent Gear 4.2.3 + Silent Lib + Silent Gems 5.1.4, Point Blank 2.2.0 + GeckoLib, Spell Engine/Power + Runes + Paladins/Rogues/Wizards (3.1.3), Artifacts 15.1.3, Jewelry 2.5.1, Iron's Jewelry + Lib, Curios 15, the Diaithir musketmod port.
- Boots clean: no ERROR or WARN from diaithirgear; Apotheosis logs `Registered 27 apotheosis:gems` (21 own + our 6, so the gem JSON validates).
- `/gearselftest` ok=117 bad=0 (run twice on the same server and once on a fresh world):
  - parity table 58 guns, multipliers 0.25 to 2.64; 57 of 58 gun items exist in the registry (`pointblank:usp45` has data but no registered item in this jar).
  - real damage on an iron golem through the damage event: empty hand 5.0, pointblank:ak47 in hand as melee 5.0 (unchanged; only hits whose call stack passes through Point Blank are scaled), bullet path (test hook) 7.23 (table x1.446). A real fired Point Blank bullet was not exercised.
  - tags: `c:gems/ruby` holds jewelry:ruby and silentgems:ruby, `diaithirgear:gems/socketable` 34 members, `diaithir:ammo/bullets` reaches musketmod:cartridge and the Point Blank calibres through nested tags.
  - holy, shadow, arcane with a fake player: 0 to 4 armour pieces give tier -1,-1,0,0,1 (tiers at 2 and 4 pieces), nothing below the family Age (scoreboard `age`), the attribute modifier exists at the tier and is removed with the armour.
- Curios reflection resolves (`CuriosApi`, `findCurios`, `SlotResult.stack`).

Not tested: a real player or client (held gun firing real hitscan bullets, headshot and falloff on top of the multiplier), wayfarer/gemwork counts with real Curios slots filled, a real Age scoreboard progression, balance feel of the numbers, Apotheosis gem generation in loot and the gem cutting table with the six new gems, translation keys (names pending the compute job), grenade and rocket modes (untouched by design), non-player shooters.


## Parts stations (cloud, headless)
`/workshoptest` ok=31 bad=0 on a dedicated NeoForge 26.1.2.116 server with only this mod (gradle runServer, rcon, fresh world): 14 materials and 25 blueprints load, damage rises bone < iron < steel < star lattice, honing raises damage, Age gate refuses iron at the default Age, a fake player builds a sword on the smithy (head and guard from iron, handle from planks, binding from leather), the finished stack carries its assembly, attack damage modifier, durability, model and name, 100 uses hone it to level 1, an off-hand flint adds Keen edge once, sneak returns the spent stack, a schematic blueprint is locked until learned, the assembly codec round-trips. The 17 base guns plus the 248 roster designs of ports #178 load as blueprints (265 guns, 273 blueprints with its guns.json); they are hidden here because `diaithir-firearms` is not loaded; their blueprints load.
Not tested: a real client (rendering of tinted layers, overlay text), a real Age scoreboard, firearms reading `diaithirgear.damage_mul` in the full pack, benches with hoppers.

With the three rosters the catalogue is 15 materials and 907 blueprints; items of other mods are hidden here until their module loads (checked: load, no data errors).

## Folded survey items (cloud, headless)
Survey part 2 folds: item magnets (CC0), repair kits (MIT), anvil repair (LGPL-2.1 package `fold.lgpl`). See `NOTICE-FOLDS.md`.
`/foldselftest` on a dedicated NeoForge 26.1.2.116 server with only this mod (gradle runServer, rcon, fresh world, `/forceload add 0 0 16 16` first so the test spot is entity ticking): magnet radii 4 and 8 m, a 4 m pull takes the near item and orb and leaves the far item and a 1-tick-old item, the 8 m pull takes the far item; copper kit pool 300 and netherite 5000, transfer moves damage point for point and stops at the pool; a damaged anvil becomes chipped then plain with iron ingots, a plain anvil passes, chance 0 fails but spends the ingot. `/gearselftest` still ok=71 bad=0. Boot has no ERROR lines from diaithirgear and all new recipes, advancement and item models load.
Not tested: a real client (item textures, tooltips), the sneak/hand flow of a repair kit by a real player, Age gate by scoreboard, magnets held in a real inventory over ticks (the pull function is tested, the tick hook is three lines).
