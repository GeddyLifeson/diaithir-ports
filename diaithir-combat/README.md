# Komrag (diaithir-combat)

Clean-room, MIT. Replaces the all-rights-reserved Better Combat and Combat Roll in the pack. No code or assets of those mods were read or used; behaviour was designed from scratch.

Server-authoritative. The client only sends "I swung" and "roll this way"; reach, arc, damage, stamina, parry and i-frames are decided on the server.

## Weapon categories (data driven)
`data/diaithircombat/categories/<id>.json` per class, and the members come from the item tag `diaithircombat:weapons/<id>`.
Classes: dagger, sword, rapier, greatsword, axe, battleaxe, mace, warhammer, spear, polearm, staff (melee) and bow, longbow (ranged).
Spartan Weaponry (`claude/port-spartan-weaponry`) and diaithir-arms (`claude/diaithir-arms`) items are in the tags as optional entries, so absent mods are harmless. To add an item, add it to a tag in a datapack. To retune a class, drop a replacement JSON into `config/diaithircombat/categories/<id>.json`.

Melee JSON: `pattern` (slash, overhead, heavy, sweep = arc; thrust, stab = narrow line that pierces), `range`, `arc`, `width` (thrust), `max_targets`, `knockback`, `combo_window`, `parry_window` (ticks; sneak to parry), `combo[]` of `{damage, cooldown, stamina, arc_mult, range_mult, knockback_mult, pattern}`. Damage is the player's attack damage (the item's attribute) times the step multiplier; enchantment effects still apply.
Ranged JSON: `draw_ticks` (20 = vanilla), `drop_reduction` (share of arrow gravity cancelled).

## Mechanics
- Stamina-lite: strokes and rolls spend it, it regenerates after a rest. A swing with none left does 60% damage.
- Parry: raise a shield (or sneak holding a dagger, rapier or staff) and a frontal hit inside the window is cancelled, the attacker is slowed, your next stroke is instant.
- Dodge roll: key V (rebindable). Direction from movement keys, 8 ticks of invulnerability (not against void or starvation), cooldown and stamina cost.
- Age scaling: reads scoreboard objective `age` (1 to 9). Cooldowns, combo windows, bow draw and roll distance improve per Age (config `age.speedPerAge`).
- Bows: draw time and arrow drop tuned per class through the arrow-loose event.

## Config
`diaithircombat-common.toml`: enabled, damageMult, rangeMult, stamina.*, age.*, parry.*, dodge.*, ranged.*.

## Commands
`/combatselftest` (op): see TEST.md.

## Animation
Uses vanilla arm swing driven by the server swing packet. PlayerAnimationLibMerged keyframe animations are not wired yet; the swing packet and the combo index are the hook points (`Engine.swing` returns the step) for adding them. Remove `bettercombat-neoforge` and `combat_roll-neoforge` from the pack when this goes in.

## Library gear (Panscriptum weapons and armor)
Every WEAPON and ARMOR entry of the Panscriptum (99,823) is the same few items plus an `entry_id` component: `lib_<class>` for the 13 weapon classes (sword, bow and so on) and `lib_head/chest/legs/feet/shield/trinket`. `data/diaithircombat/library_gear/gear.tsv.gz` holds id, tier, Age gate, class and name per entry; class comes from keywords in the entry's own text (tools/gear_gen.py), nothing is invented. Stats come from the Age tier (attack damage, armor, toughness, durability, set on the stack by `LibraryGear.stack(entryId)`); the Age gate is the planet's `opens_age` + tier - 1 and a piece above the player's Age does not swing or stays out of the armor slots. Weapons join the combat tags, so the existing combo, parry and bow engine applies. Firearm and energy-launcher entries use the longbow class. Textures: tools/gear_art.py, 64 px, two layers, the second tinted per entry. `/libgear find|give|count|test` (op). Worn armor uses 9 grey plate layers (one per Age) dyed per entry; bows have pull frames. Not done: recipes and loot (progression graph owns those; call `LibraryGear.stack`).

### Recipes, loot and progression (gear_recipes.py, S6841)
- 171 recipes, now Age station recipes (`diaithir_stations/station_recipe/lib_<class>_a<N>.json`, class/slot x Age gate 1-9; no crafting-grid recipes, `foundry/tools/recipes_to_stations.py` converts what `tools/gear_recipes.py` writes). Materials follow the progression graph's Age chain (flint, copper, bronze, iron, steel, steel plate, hollowsteel, nanoalloy plate, netherite) and are dormant until the tag exists. The result carries `forge_age`; the first time the stack is held it becomes a real Library entry of that class and Age on the holder's planet, so all 99,823 entries are reachable by crafting.
- Loot function `diaithircombat:library_gear` (kind, classes, extra Ages) picks an entry of the chest's planet (dimension `diaithir_planets:<id>`, else Terra) with gate from the planet's `opens_age` to +extra. Tables `diaithircombat:gear/<site class>` cover the 12 worldgen site classes; the worldgen module chests add them as a pool.
- `library_gear_nodes.json`: nine `combat.gear_aN` nodes in graph.json schema for the progression owner to merge.
- Age material tags `diaithircombat:gear_material/a1..a9` are unions of real pack items (flint, copper, #c:ingots/bronze, iron, #c:ingots/steel, #c:plates/steel, tempered ingot, netherite, Rodos alloy ingot). This module fills the empty `c:ingots/bronze|steel|tempered|rodos_alloy` and `c:plates/steel` tags with the real ids from the pack's kept mods (optional entries). Graph: nodes merged into plans/progression/graph.json plus `combat.tempered` and `combat.rodos_alloy`; checker passes. `/libgear pick <planet>` checks planet and Age matching.

## Survey part 2 additions (package `survey`, clean-room from behaviour, MIT)
- **Sword blocking** (`SwordBlocking`): every durable item named `*sword`, rapier, sabre, falchion and similar, in any namespace, gets the vanilla `blocks_attacks` component: hold use to raise it, take half of hits from a 180 degree front arc, durability cost like a shield. No mixin. Behaviour reference: Sword Blocking Mechanics (MPL-2.0, not copied).
- **Status build-up** (`Buildup`): hits fill a per-kind meter (bleeding for edged weapons, poison for spiders and bees, burning, freezing, shock, stagger for blunt weapons); it drains 5 per second and a full 100 fires a vanilla effect. Reference: Overhauled Damage (MIT, not copied).
- **Mounted charge** (`Lance`): a rider with a lance, spear, halberd or pike adds damage with closing speed (up to x3.5). Reference: Jousting (all rights reserved, not read).
Test: `/combatsurveytest`.
- `Styles`: crit_chance and crit_damage player attributes (Critical Strike behaviour) and weapon styles from Epic Fight's weapon categories: dagger backstab x1.5, rapier +10% crit, katana bleed, warhammer stagger, glaive/halberd cleave to a second foe. Clean-room.
