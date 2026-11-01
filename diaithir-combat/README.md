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
