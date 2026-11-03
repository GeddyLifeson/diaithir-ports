# Test results

Build: JDK 25, NeoForge 26.1.2.116, PAL 1.2.8+mc.26.1 from Modrinth. `./gradlew compileJava` ok.

Dedicated server (gradle runServer + rcon), mod alone with PAL: boots with no error from the mod. `/animselftest` ok=455 bad=0:
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
