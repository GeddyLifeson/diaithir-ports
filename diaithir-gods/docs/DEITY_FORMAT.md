# Deity datapack format

All files live in `data/<namespace>/deity/<kind>/...` and are plain JSON. The format is the one used by upstream RPG Gods;
the only changes for 26.1 are the item stack syntax (see "Items") and NeoForge's own loot modifier layout.
A deity is identified by `<namespace>:<path>`. Offerings, sacrifices and perks refer to it as described below.

## Items

Wherever an item is expected you can write either an id string (`"minecraft:bone"`) or an object
`{"id": "minecraft:potion", "count": 1, "components": {...}}`.

## deity/deity/<name>.json

| Field | Type | Default | Meaning |
|---|---|---|---|
| name | id | required | Deity id, normally the same as the file id |
| icon | item | none | Icon shown in the favor GUI |
| unlocked | bool | true | Known to every player from the start |
| enabled | bool | true | Deity can be used |
| minlevel | int | -10 | Lowest favor level |
| maxlevel | int | 10 | Highest favor level |

## deity/altar/<name>.json

The file name is the altar id; `name` gives the deity it belongs to. Fields: `enabled`, `name`, `female`, `slim`, `light`,
`items` (`head`, `chest`, `legs`, `feet`, `mainhand`, `offhand` as items, `block` as a block id,
`armor_locked`, `hands_locked`, `block_locked`), `material`, `pose` (parts `head`, `body`, `left_arm`, `right_arm`,
`left_leg`, `right_leg` with radian `x`, `y`, `z`), `pose_locked`. See `deity/altar/stone_figure.json`.

## deity/offering/<deity path>/<name>.json

The deity is the path text before the first `/`.

Fields: `item`, `item_tag` (custom data to match, SNBT string), `favor`, `maxuses` (16), `restocks` (-1),
`cooldown` (12000 ticks), `trade` (item given back), `trade_tag`, `minlevel`, `maxlevel` (levels at which the trade is allowed),
`function` (a mcfunction id run on acceptance), `function_text` (text shown for the function).

## deity/sacrifice/<deity path>/<name>.json

Fields: `entity` (entity type id), `favor`, `maxuses`, `restocks`, `cooldown`, `condition`, `function`, `function_text`.

## deity/perk/<name>.json

Blessings and curses. Fields:

- `icon`: `{item, color, hidden, fancy}`
- `condition`: `{type, data, tag}`. Types: patron, biome, day, night, random_tick, mainhand_item, structure, dimension, effect_start,
  entity_hurt_player, entity_killed_player, player_hurt_entity, player_killed_entity, player_interact_entity,
  player_interact_block, player_ride_entity, player_crouching, ritual, unlocked, enter_combat, near_altar, level_up, level_down.
- `range`: `{deity, minlevel, maxlevel}`. This ties the perk to a deity.
- `action`: `{type, data, id, tag, item, favor, multiplier, affinity, patron, hidden}`. Types: function, potion, summon, item,
  favor, affinity, arrow_damage, arrow_effect, arrow_count, offspring, crop_growth, crop_harvest, autosmelt, unsmelt,
  special_price, durability, damage, patron, unlock, add_decay, xp.
  A `potion` action uses `tag` like `{"Potion": "minecraft:regeneration", "Amplifier": 0, "Duration": 200, "ShowParticles": true}`.
- `chance`, `cooldown_category`, `cooldown`, `positive`.

## Patron and affinity

Patron: `{deity, penalty, decay, perk_chance}`. Affinity: `{type, entity}`. They are referenced from perk actions.

## Loot modifiers (NeoForge)

NeoForge 26.1 loads every file in `data/<ns>/loot_modifiers/` directly. There is no `global_loot_modifiers.json` list any more.
This mod ships `diaithir_gods:autosmelt_or_cobble` and `diaithir_gods:crop_multiplier`.

## Language

Deity display names come from `deity.<ns>.<path>` style keys in `assets/<ns>/lang/en_us.json`; the generated set uses
coined names, and the `.title` key holds the domain text.
