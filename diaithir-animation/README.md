# Diaithir Animation (diaithir-animation)

MIT, original poses. Player animations on Player Animation Library 1.2.8 (MIT, required): melee swings and combos, tool swings, bows, crossbows, shields, staff casting and every gun action. Folds into `diaithir-entity-animation` later; no extra jar for the pack.

## How it works
- Clips are data: `assets/diaithiranimation/player_animations/{melee,tools,guns,casting}.json`, written by `tools/gen_clips.py` (Global-Repo S6874). 383 clips (own-gun reload clips come from diaithir-firearms guns.json: one per gun, as long as its server reload; classes now include musket).
- Which weapon plays which class comes from item tags: `data/diaithiranimation/tags/item/anim/<class>` and `anim/gun/<class>`. The generator fills them from `ANIMATIONS_COVERAGE.csv` (owner 2) plus the Komrag tags (`#diaithircombat:weapons/*`) and the diaithir-firearms ids; all entries are optional. To animate another mod's weapon, add it to a tag in a datapack.
- Classes: sword, axe, spear, dagger, mace, staff, pickaxe, shovel, hoe, shears, shield, bow, crossbow; guns: pistol, smg, rifle, sniper, shotgun, lmg, launcher, energy.
- The client reads every player (local and remote) each tick: draw on switch, hold/aim (sneak)/sprint loops for guns, 1-2-3 combo on each swing (resets after 25 ticks), bow nock/draw/release, crossbow load/aim/fire, shield raise/lower on a second layer (stacks with a main-hand clip), staff cast raise/channel/release.
- Server API (`Anim.gun(player, "fire"|"dry"|"reload"|"inspect")`, `Anim.play`, `Anim.cast`) sends a visual-only packet to everyone tracking the player. Own guns have a reload clip exactly as long as the server reload.
- Inspect key: H (rebindable), guns only.

## Body layer (vanilla body and first person)
Clean-room replacement for Not Enough Animations and the firstperson poses (ARR mods), on a PAL layer `body` (priority 1000) under the weapon layers.
- 87 clips in `player_animations/body.json`, written by `tools/gen_body.py` (Global-Repo S6875): `act.*` body states and one-shots (crawl, swim, tread, wade, climb/ladder/vine/scaffold, glide, fall, jump, land soft/hard, ride horse/boat/cart/mount, sit, sleep, stand up, hurt, die, respawn, lean walk/run/sprint/sneak, punch, hold torch/map, interact, containers, craft, brew, enchant, trade, mine, place, equip armor, drop, pick up, carry, push/pull, emotes) and `use.<kind>.<phase>` held-item actions.
- Held-item kinds from item tags `data/diaithiranimation/tags/item/anim/body/<kind>` (throwable, food, drink, horn, spyglass, map, tool, mount, wings, scroll, rod); vanilla consumable animations are the fallback. The generator fills the tags from `ANIMATIONS_COVERAGE.csv` owner 1.
- `BodyClient` reads every player (local and remote) each tick and starts a clip only on a state change; priorities: death, sleep, riding, gliding, using an item, climbing, swimming, crawling, treading water, falling, sneak/sprint/walk lean, held map/torch. `BodyServer` sends visual-only one-shots for events with no client signal (container open, craft, brew, enchant, trade, drop, pick up, place, respawn) as `Net.Play "body.<name>"`.
- First person: while a clip that uses the arms plays (use.*, hold, interact, mine, place) the third-person arms and held item are shown, otherwise vanilla first person. 3D skin layers (skinlayers3d replacement) are not in this PR.

## Wiring
- Komrag (diaithir-combat) needs no change: it already swings the vanilla arm on the server, which the client animates.
- diaithir-firearms: `AnimHook` (soft MethodHandle, no-op without this mod) calls fire, dry and reload. Separate PR stacked on #158.

## Spells
Class-skill poses are Spell Engine's own PAL clips; all 34 distinct animation ids used by the wizards, archers, skill tree and rpg_series spells exist in the shipped jars (checked by script). Paladins and rogues jars not checked yet.

## Not done
Visual check of first person (enabled: third-person arms and items show while a clip plays), spell hit effects and projectile entity animations (VFX lanes), tank turret/driver (no tank exists yet), visual tuning of poses on a real client (angles follow vanilla ModelPart convention).
