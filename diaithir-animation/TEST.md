# Test results

Build: JDK 25, NeoForge 26.1.2.116, PAL 1.2.8+mc.26.1 from Modrinth. `./gradlew compileJava` ok.

Dedicated server (gradle runServer + rcon), mod alone with PAL: boots with no error from the mod. `/animselftest, /animplay <clip> and /animgun <action> (op: send a clip to yourself for render tests)` ok=455 bad=0:
- all four clip files load with PAL's own loader (`UniversalAnimLoader`), every clip has length and bones;
- every class in `clips.json` has every listed clip; every gun class has hold, aim, fire, reload, dry, inspect, draw, sprint; all 17 own-gun reload clips exist;
- tag resolution: iron sword/axe/pickaxe/shovel/hoe, bow, crossbow, shield, shears resolve to the right class (vanilla items only; the firearms jar was not loaded).

Not tested: a real client (PAL layer registration, trigger and playback, pose look, first person, the H key, packets on a live connection), firearms ids in tags with the firearms jar loaded (hook compiles on #158), Komrag combo timing with real swings. Done in the coverage CSV means clip plus tag class exists and loads, not that the poses were seen in game.

## With diaithir-firearms (#158 + hook PR) loaded
Server with both jars and PAL: boots clean. `/firearmsselftest` then `/animselftest`: ok=461 bad=0 (own-gun ids resolve to the right classes and the own reload clip name), `clips_sent=16`, so the firearms hook reaches Anim from fire and reload. Packets were sent to a fake player; nothing was received by a real client.

## Spell Engine spells
Script check of the wizards, archers, skill_tree and spell_engine jars: all 34 animation ids used by their spells exist in the shipped player_animations. Paladins and rogues jars were not available to check.

## Body layer (headless server + single-mod client, cloud)
Dedicated server with PAL: boots clean, `/animselftest` ok=859 bad=0 (body.json loads with PAL's loader, all 87 clips have length and bones, every action and held-item phase points at a clip, sample vanilla items resolve to the right kind).
Single-mod client (gradle runClient under Xvfb, llvmpipe, joined the local server as Tester): the `body` PAL layer registers and plays on a real client. Driven by rcon, the log shows first plays of act.hurt (damage), act.fall (tp 150 up), act.tread (water), act.die and act.drop_item (server one-shot packet reached the client). Not tested: the other clips in motion, remote players seeing each other, first-person arms, and how the poses look (no screenshots; angles follow the vanilla ModelPart convention). "done" in ANIMATIONS_COVERAGE.csv means clip plus tag/state exists and loads, same standard as the weapon layer.
Run: `./gradlew runClient -PjoinArgs="--username Tester --quickPlayMultiplayer 127.0.0.1:25565"` with `runServer` (rcon in run/server.properties).

## Round 2 (effects, projectiles, seats)
`/animselftest` ok=1695 bad=0: new class reload clips, unjam for all 9 gun classes, seat and cannon clips, projectile look picks (fire, frost, default). Paladins and rogues (26.3 jars) spells: all 309 spell animation ids across every checked jar exist. The particle effects, helix and seat logic compile and load on a dedicated server but have not run on a client.
## 3D skin layers
Server: `/animselftest` ok=867 bad=0, including voxel counts (opaque hat 576 quads, single texel 1 outer + 4 walls, alpha 127 hole, alpha 128 opaque) and the winding of every quad. The first run caught a flipped normal on the top and bottom faces.
Client (single-mod, Xvfb, llvmpipe): at startup `[skinlayers] verify vs vanilla cube: 0 vertices missing, 0 faces wound differently` (the voxel builder at thickness 0 reproduces every position and uv of vanilla's own ModelPart.Cube). Joining as Tester built the voxel model for the default skin (`noor`, wide) and a third-person screenshot showed the extruded hair and jacket without artifacts. Second round (two headless clients, Tester and Tester2, same server, NeoForge dev clients under Xvfb):
- Slim skin: Tester2 got the default slim skin (`noor`, slim); Tester's client built its voxel model (`built voxel model for ...slim/noor.png#slim`) and drew it with extruded hair and sleeves.
- Downloaded-skin path: `/skinlayerstest` registers a synthetic 64x64 skin as a DynamicTexture, builds wide and slim: 3613 and 3473 quads, both equal to a direct SkinVoxels count, built in 3.7 and 6.4 ms, slim has fewer quads than wide.
- Other players: Tester saw Tester2 with the voxel layer; the server then sent `body.cheer` and `body.dance` to Tester2 (`/animplay`): the log shows both clips played and a screenshot of the cheer pose shows the sleeve voxels following the raised arms.
- Frame cost (software rasteriser llvmpipe, 4 CPUs, third person, Tester alone, ms per frame): vanilla overlay 41, voxel layer 44, +10 extra voxel models 49, +50 extra 62. About 0.4 to 0.6 ms per additional voxel player here, which is CPU-side vertex building plus software raster; a real GPU only pays the vertex building. A typical skin is 3.5k to 3.6k quads. Players beyond 20 blocks keep the flat overlay, so the cost is bounded by the players nearby.
Still not tested: a real GPU, a real Mojang-downloaded skin (offline server), first-person hand sleeves (unchanged, vanilla).
