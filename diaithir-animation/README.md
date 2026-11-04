# diaithir-animation

Source package (no jar) for the machine, block and vehicle part of the animation module `diaithir-entity-animation`. Minecraft-free core: a `Controller` per instance (IDLE / WORKING / DONE with wind-up and wind-down), ten motion `Kind`s, and `Rules` that map any block, block entity or vehicle id to a `Spec` by first-match glob. `rules.tsv` is the single source of truth: the renderer glue and `tools/gen_coverage.py` both read it, so ANIMATIONS_COVERAGE.csv always matches what the game does.

Covers Foundry machines and operator seat, upgrade beacon, Keelwright helm/thrusters/sails, carts and rail, Immersive Vehicles/Aircraft (wheels, propellers, hatches, seats), doors, chests, levers, tanks, turrets, belts.

Test: `tools/run_tests.sh` (JDK 17+, no network). Next PR: NeoForge client glue (block entity renderer reading `Controller.pose()`, state from the block entity's working flag), then per-model part names.
## Body layer (vanilla body and first person)
Clean-room replacement for Not Enough Animations and the firstperson poses (ARR mods), on a PAL layer `body` (priority 1000) under the weapon layers.
- 87 clips in `player_animations/body.json`, written by `tools/gen_body.py` (Global-Repo S6875): `act.*` body states and one-shots (crawl, swim, tread, wade, climb/ladder/vine/scaffold, glide, fall, jump, land soft/hard, ride horse/boat/cart/mount, sit, sleep, stand up, hurt, die, respawn, lean walk/run/sprint/sneak, punch, hold torch/map, interact, containers, craft, brew, enchant, trade, mine, place, equip armor, drop, pick up, carry, push/pull, emotes) and `use.<kind>.<phase>` held-item actions.
- Held-item kinds from item tags `data/diaithiranimation/tags/item/anim/body/<kind>` (throwable, food, drink, horn, spyglass, map, tool, mount, wings, scroll, rod); vanilla consumable animations are the fallback. The generator fills the tags from `ANIMATIONS_COVERAGE.csv` owner 1.
- `BodyClient` reads every player (local and remote) each tick and starts a clip only on a state change; priorities: death, sleep, riding, gliding, using an item, climbing, swimming, crawling, treading water, falling, sneak/sprint/walk lean, held map/torch. `BodyServer` sends visual-only one-shots for events with no client signal (container open, craft, brew, enchant, trade, drop, pick up, place, respawn) as `Net.Play "body.<name>"`.
- First person: while a clip that uses the arms plays (use.*, hold, interact, mine, place) the third-person arms and held item are shown, otherwise vanilla first person. 3D skin layers (skinlayers3d replacement) are not in this PR.

## 3D skin layers
Clean-room replacement for 3D Skin Layers (ARR). `SkinVoxels` (pure geometry) turns each overlay box of the 64x64 skin (hat, jacket, sleeves, pants, wide and slim) into extruded pixel voxels: every opaque texel (alpha 128 or more) is a flat outer quad lifted 1 px (head) or 0.7 px (body) plus a side wall wherever the neighbour texel is transparent or the face ends. `SkinLayers` reads the skin's pixels once per texture (downloaded skins are DynamicTextures, default skins are resources), wraps the quads in a PlayerModel with the vanilla bones, so vanilla, PAL and body-layer animations move it, and draws it as a render layer. Vanilla's flat overlay is switched off for that frame through the render state's show* flags; beyond 20 blocks, with an unreadable skin, or on mannequins vanilla keeps drawing. Not covered: first-person hand sleeves, capes and items (unchanged). Client commands: `/skinlayers on|off` (toggle), `/skinlayerstest` (builds a wide and a slim synthetic skin through the downloaded-skin path and checks the quad counts), `/skinlayersbench N` (draws N extra voxel models per player and logs ms per frame every 5 s). Server command for tests: `/animplay <player> <clip>` (op only; `body.<name>` plays a body clip).

## Wiring
- Komrag (diaithir-combat) needs no change: it already swings the vanilla arm on the server, which the client animates.
- diaithir-firearms: `AnimHook` (soft MethodHandle, no-op without this mod) calls fire, dry and reload. Separate PR stacked on #158.

## Spells
Class-skill poses are Spell Engine's own PAL clips; all 34 distinct animation ids used by the wizards, archers, skill tree and rpg_series spells exist in the shipped jars (checked by script). Paladins and rogues jars not checked yet.

## Not done
Visual check of first person (enabled: third-person arms and items show while a clip plays), spell hit effects and projectile entity animations (VFX lanes), tank turret/driver (no tank exists yet), visual tuning of poses on a real client (angles follow vanilla ModelPart convention).
