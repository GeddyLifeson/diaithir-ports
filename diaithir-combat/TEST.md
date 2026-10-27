# Test results

Build: `./gradlew build` with JDK 25, NeoForge 26.1.2.116, jar `diaithir-combat-neoforge-26.1.2-1.0.0.jar`.

Dedicated server (gradle runServer, rcon) run twice: alone, and with the diaithir-arms and spartan-weaponry port jars in `mods/`.
- Boots with no ERROR line from the mod (the only ERROR is the vanilla flat-world `layers` property in my test server.properties).
- `/combatselftest` (fake player, armour stands, iron golem): ok=297 bad=0 with both item mods loaded. It checks, for every class and every combo step:
  reach (hit just inside, miss beyond), arc (hit inside, miss outside) or thrust width, `max_targets` pierce/sweep cap, real damage on a golem against
  base attack damage x step multiplier (enchantment effects included), the cooldown gate, exhausted swings, and cooldown shrinking with Age.
  Plus parry (inside window from the front; not after the window; not from behind), dodge (launch, i-frames, void bypasses them, cooldown, stamina cost and refusal) and bow draw/charge mapping.
- Tag resolution with the port jars: dagger 10, sword 47, rapier 5, greatsword 5, axe 22, battleaxe 10, mace 8, warhammer 10, spear 22, polearm 20, staff 5, bow 4, longbow 6 members. Alone, only the vanilla members resolve and the test falls back to a vanilla stand-in item.
- Spartan's own dagger/rapier traits (backstab, armour piercing) fire on top of our hit; the test allows up to x3.05 for those two classes.
- Damage table (age 1, iron-level base): see the `[combatselftest]` lines; e.g. sword 6.3/6.3/9.1, axe 9.9/12.6, mace 7.2/9.0, spear 4.0/4.0/4.8 (wood-tier stand-ins). Cooldown ticks age1/age7: sword 9/7, 14/10; greatsword 16/12, 24/18.
- `item replace` of spartanweaponry:iron_rapier, diaithirarms:steel_sword and composite_bow into a summoned armour stand: accepted (stand-in for `/give`, no player online).

Not tested: a real client (attack-key interception, V roll key, tooltip line, arm swing), real player Age from a scoreboard, bow draw feel and arrow drop in flight, real shield/sneak parry with a player, PlayerAnimationLib animations (not implemented).
