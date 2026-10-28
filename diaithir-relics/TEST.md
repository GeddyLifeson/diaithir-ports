# Test results

Build: `./gradlew build` (JDK 25, NeoForge 26.1.2.116), jar `diaithir-relics-neoforge-26.1.2-1.0.0.jar` (111 KB).

Dedicated server (gradle runServer, empty default world, Curios 15.0.0 in run/mods, `pause-when-empty-seconds=-1`):
- Boots with no ERROR lines. 37 loot modifiers, 26 item definitions, tags and recipes parse. The only WARN lines are "empty ingredients" for 7 recipes that need `c:ingots/bronze` or `c:ingots/steel` (empty tags on a bare server; the pack's Mekanism/Tinkers fill them).
- `/relicsselftest` (forces the chunks, then runs a second later; results in the log): `relics ok=26 bad=0`. For every relic it builds the stack, checks the components (attribute modifiers and weapon, blocks_attacks and equippable for shields, use_cooldown where there is an active, durability), gives it to a NeoForge fake player beside bound zombies, fires the active once and triggers the passive once through the real event path (a melee blow through LivingIncomingDamageEvent, a blow taken, the once-a-second tick, a Ring of Fate death event) and checks the visible result (fire, poison, slow, arcs, backstab 5.9 vs 3.9 damage, tide bonus, healing, absorption, glowing, and so on).
- Curios: with Curios loaded a ring put in a real `ring` slot (on an armour stand, via `data/curios/curios/entities/diaithir_relics.json`) is found. Without Curios the same 26 pass and trinkets work from the off hand.
- rcon give-all: `item replace entity` of all 26 ids into an armour stand's main hand and off hand (52 replaced) and `summon item` of all 26 (26 summoned), readback shows the ids.
- Static check: every item definition, model, parent and texture exists; every lang key exists.

Not tested: a real client (icons, spear in-hand model, shield off-hand model), a real player's blocking (the shield half of the powers is called with the shield up because a fake player cannot hold a block), the Sunwarmth/Moonsight/Tide-in-rain paths depend on sky and time, crafting with real bronze/steel ingots, loot actually dropping from structure chests (tables load; odds 10 to 20 percent per chest).

## Full pack (batch 1) note
mist_bell "no veil" was NeoOrigins cancelling all damage to a player without an origin (every wearer-side check passed or failed with no damage landing). The test now marks the fake player picker-abandoned (reflection), resets item cooldowns (fake player does not tick), and strips Mob Champions / level buffs from the target mobs. Full pack: ok=26 bad=0 in 6 runs on one boot.
