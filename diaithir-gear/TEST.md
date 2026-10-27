# Test results

Build: `./gradlew build` (JDK 25, NeoForge 26.1.2.116) -> `diaithir-gear-neoforge-26.1.2-1.0.0.jar`.

Dedicated server (gradle runServer, rcon), fresh world, with: Apotheosis 9.1.0 + Apothic Attributes/Enchanting + Placebo, Silent Gear 4.2.3 + Silent Lib + Silent Gems 5.1.4, Point Blank 2.2.0 + GeckoLib, Spell Engine/Power + Runes + Paladins/Rogues/Wizards (3.1.3), Artifacts 15.1.3, Jewelry 2.5.1, Iron's Jewelry + Lib, Curios 15, the Diaithir musketmod port.
- Boots clean: no ERROR or WARN from diaithirgear; Apotheosis logs `Registered 27 apotheosis:gems` (21 own + our 6, so the gem JSON validates).
- `/gearselftest` ok=116 bad=0 (run twice on the same server and once on a fresh world):
  - parity table 58 guns, multipliers 0.25 to 2.64; 57 of 58 gun items exist in the registry (`pointblank:usp45` has data but no registered item in this jar).
  - real damage on an iron golem through the damage event: empty hand 5.0, `pointblank:ak47` in hand 7.23 (table x1.446).
  - tags: `c:gems/ruby` holds jewelry:ruby and silentgems:ruby, `diaithirgear:gems/socketable` 34 members, `diaithir:ammo/bullets` reaches musketmod:cartridge and the Point Blank calibres through nested tags.
  - holy, shadow, arcane with a fake player: 0 to 4 armour pieces give tier -1,-1,0,0,1 (tiers at 2 and 4 pieces), nothing below the family Age (scoreboard `age`), the attribute modifier exists at the tier and is removed with the armour.
- Curios reflection resolves (`CuriosApi`, `findCurios`, `SlotResult.stack`).

Not tested: a real player or client (held gun firing real hitscan bullets, headshot and falloff on top of the multiplier), wayfarer/gemwork counts with real Curios slots filled, a real Age scoreboard progression, balance feel of the numbers, Apotheosis gem generation in loot and the gem cutting table with the six new gems, translation keys (names pending the compute job), grenade and rocket modes (untouched by design), non-player shooters.
