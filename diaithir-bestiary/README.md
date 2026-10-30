# Diaithir Bestiary

Creatures for the Diaithir modpack (NeoForge 26.1.2) that are written as datapack JSON, so a new creature needs no Java. Each creature picks one of five behaviour presets, gets a GeckoLib model, texture and animations, its own size and stats, and optional spawn rules, loot, taming, riding, fuel and boss settings.

Requires GeckoLib 5.5 or later. Optional: diaithir-ages, for the world's current Age. Licence: MIT (see LICENSE). No code was copied from other mods; see CREDITS.md.

## Presets

| Preset | Behaviour |
| --- | --- |
| `beast` | Walks and wanders, fights in melee. Hostile ones hunt players. Can be tamed and, if `rideable`, ridden by its owner. |
| `flyer` | Flies (no gravity, flying pathing). Tamable; once tamed its owner can ride it: look up or down to climb or dive, jump to rise. |
| `mech` | A walker any player climbs into with an empty hand and pilots. It never acts on its own. With `fuel`, it moves only while it has fuel (right click with the fuel item); fuel burns one tick per tick while someone is aboard. |
| `avatar` | A boss with a boss bar. At `phase_two_at` of its health it enters phase two: +50% damage, +30% speed, a red bar and a shockwave every 5 seconds that hurts and throws back players within 6 blocks. Never despawns. |
| `stalker` | Moves only while no player is looking at it (within 64 blocks, line of sight, about a 53 degree cone). While watched it cannot move, turn or attack, and its animation freezes. |

## Creature files

Put one file per creature at `data/<namespace>/diaithirbestiary/creature/<name>.json`. The creature's id is `<namespace>:<name>`. Creature files are a datapack registry: they load when the world starts and are sent to clients, so changes need a world restart, not `/reload`.

```json
{
  "preset": "flyer",
  "width": 2.2,
  "height": 1.8,
  "stats": { "health": 60, "armor": 6, "damage": 8, "speed": 0.3, "flying_speed": 0.6, "follow_range": 40 },
  "tame": { "item": "minecraft:blaze_rod", "chance": 0.25 },
  "spawn": { "biomes": "minecraft:is_mountain", "weight": 3 },
  "loot_table": "diaithirbestiary:entities/emberwyrm"
}
```

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| `preset` | string | required | `beast`, `flyer`, `mech`, `avatar` or `stalker`. |
| `name` | string | `entity.<namespace>.<name>` | Lang key of the display name. Add the name to your `assets/<namespace>/lang/en_us.json`. |
| `model` | id | `<namespace>:entity/<name>` | GeckoLib model, read from `assets/<namespace>/geckolib/models/entity/<name>.geo.json`. |
| `texture` | id | `<namespace>:textures/entity/<name>.png` | Full texture path. |
| `animation` | id | `<namespace>:entity/<name>` | GeckoLib animations, read from `assets/<namespace>/geckolib/animations/entity/<name>.animation.json`. |
| `width`, `height` | number | 0.9, 1.4 | Hitbox in blocks (0.1 to 16). Riders sit on top. |
| `stats.health` | number | 20 | Max health. |
| `stats.armor` | number | 0 | Armor points. |
| `stats.damage` | number | 3 | Melee damage. |
| `stats.speed` | number | 0.25 | Walking speed (a zombie is 0.23). |
| `stats.flying_speed` | number | 0.4 | Flying speed, for flying creatures. |
| `stats.follow_range` | number | 32 | How far it notices targets. |
| `stats.knockback_resistance` | number | 0 | 0 to 1. |
| `hostile` | bool | true, false for `mech` | Hunts players. A tamed creature is never hostile. |
| `rideable` | bool | true for `flyer` and `mech` | Its owner (or anyone, for a mech) can ride it. Ignored by `avatar` and `stalker`. |
| `flying` | bool | true for `flyer` | Flies. Any preset may set it, for example a flying avatar. |
| `tame.item` | item id | none | Right click with this item to try to tame it (one item used per try). |
| `tame.chance` | number | 0.33 | Chance per try. |
| `fuel.item` | item id | none | Mech only: the fuel item. Without `fuel` a mech needs none. |
| `fuel.ticks` | int | 1200 | Ticks of running per fuel item. |
| `boss.phase_two_at` | number | 0.5 | Avatar only: share of max health that starts phase two. |
| `boss.bar_color` | string | `purple` | Avatar only: `pink`, `blue`, `red`, `green`, `yellow`, `purple` or `white`. |
| `spawn.biomes` | biome tag | none | Without `spawn` the creature only appears by command. |
| `spawn.weight` | int | 10 | 1 to 100, see below. |
| `spawn.min_group`, `spawn.max_group` | int | 1, 1 | Group size. |
| `spawn.max_light` | int | 15 | Only spawns at this light level or darker (4 for a night horror). |
| `spawn.rare` | bool | false | A rare spawn counts a twentieth of its weight: weight 2 means a 0.1% chance per 10 second try instead of 2%. For bosses and derelict mechs. |
| `spawn.min_age` | Age | 1 | Only spawns naturally once the world's current Age is at least this: a numeral `"I"` to `"VII"`, `"Rodos"` (counts as 8), or a number 1 to 8. Summons ignore it. |
| `loot_table` | id | none | Loot table dropped on death, for example `data/<namespace>/loot_table/entities/<name>.json`. |

Natural spawning: every 10 seconds each player gets one try at a spot 24 to 48 blocks away on the surface (not on water). The creatures whose biome tag and light limit fit that spot add up their weights, and that sum is the percent chance (a `rare` creature counts a twentieth of its weight) that one of them (picked by weight) spawns there. The current Age comes from diaithir-ages (its saved story) when that mod is installed; without it the Age is I, so creatures with a `min_age` above I never spawn naturally. No try is made while 6 or more bestiary creatures are within 64 blocks of the player, while the `spawn_mobs` game rule is off, or (for hostile creatures) on Peaceful.

## Animations

The model's animation file should have `idle`, `walk` and `attack`; flying creatures also `fly`. `attack` plays when it lands a melee hit (and when an avatar enters phase two). Any other bones and animations in the file are up to you. Models are ordinary Blockbench GeckoLib entity models (Bedrock geometry 1.12.0).

## Commands

- `/bestiary list`: every loaded creature with its preset and name.
- `/bestiary summon <id> [pos]`: summons a creature (operators).
- `/bestiary selftest`: on a test world, spawns each creature of this mod's own datapack at x 3000, z 3000, lets them run two seconds and checks that each spawns with its size and health, ticks, takes damage, that mechs and flyers take a fake player as pilot and are steered by it (and refuse strangers before taming), mech fuel, the avatar's boss bar and phase two, the stalker's gaze rule, spawn candidates for its biome tag (allowed at its `min_age`, blocked one Age below), where the current Age comes from, and loot. Prints `bestiary selftest PASS` or the list of failures (server owners only).

## Example creatures

| Id | Preset | What it is |
| --- | --- | --- |
| `diaithirbestiary:bumblesnout` | beast | A round, friendly cartoon critter of the plains from Age III. Tame with sweet berries and ride it. |
| `diaithirbestiary:emberwyrm` | flyer | A red mountain dragon, from Age III. Tame with blaze rods, then fly it. |
| `diaithirbestiary:ironstrider` | mech | A two-legged walker that burns coal. Summon only. |
| `diaithirbestiary:aurelith` | avatar | Aurelith, Avatar of the Dawn: a haloed god avatar boss. Summon only. |
| `diaithirbestiary:hollowmourn` | stalker | A pale-faced forest horror that comes out in the dark from Age III and only moves when unseen. |

Their models, animations, 64x textures and glow masks are built by the procedural art toolkit in `tools/art` (needs numpy and Pillow; one module per creature in `tools/art/creatures`). Edit a creature module and run `python3 tools/art/build.py <name>` from this folder, or replace the files with Blockbench exports.

## Building

`./gradlew build` with JDK 25. GeckoLib comes from the Modrinth Maven.

## Creatures added in 0.3.0

All fourteen are written by `tools/gen_content.py` (which also writes their creature JSON, loot tables and names). Their models and 64x textures come from `tools/art` (Celtic knotwork and enamel for the gods, riveted industrial plate for the mechs, bone, burlap and gauze for the stalkers, soft cel shading for the toons). Glowing parts use `<id>_glowmask.png` through GeckoLib's auto glow layer. Replace the art with Blockbench exports at any time and keep the ids.

| Id | Preset | From Age | Where |
| --- | --- | --- | --- |
| `verdanai` Avatar of the Wild Green | avatar | III | forests, rare |
| `orsolune` Avatar of the Hushed Moon | avatar | IV | taiga at night, rare |
| `ashenvar` Avatar of the Ember Hour | avatar | IV | badlands, rare |
| `marenoth` Avatar of the Deep Tide | avatar | III | beaches, rare |
| `brassjack` | mech | V | derelict in deserts, rare; coal |
| `siegewarden` | mech | VI | derelict in badlands, rare; coal |
| `tinkerspider` | mech | V | derelict in deserts, rare; redstone |
| `pallidgaunt` | stalker | IV | forests in the dark |
| `marrowcrawl` | stalker | IV | swamps in the dark |
| `veilstitch` | stalker | III | plains in the dark |
| `puddlepip` | beast, friendly | IV | rivers, groups; tame with seeds |
| `grumblebun` | beast, friendly | V | forests; tame with carrots |
| `twirlfin` | flyer, friendly | V | plains; tame with cod, then fly it |
| `mossmop` | beast, friendly | IV | swamps; tame with wheat |

## Creatures added in 0.3.1

Two realistic Pleistocene beasts for the Earth timeline (Age I), both written by `tools/gen_content.py` with art in `tools/art/creatures`. Names are Diaithiris compounds.

| Id | Preset | From Age | Where |
| --- | --- | --- | --- |
| `dobhranfamhair` Dobrunfumuir (giant beaver, Castoroides ohioensis) | beast | I | swamps; tame with stripped oak logs |
| `eachreidhlein` Iechrèidlein (Mexican horse, Equus conversidens) | beast, rideable | I | plains, herds; tame with wheat |
