# Test results

Build: JDK 25, NeoForge 26.1.2.116, PAL 1.2.8+mc.26.1 from Modrinth. `./gradlew compileJava` ok.

Dedicated server (gradle runServer + rcon), mod alone with PAL: boots with no error from the mod. `/animselftest` ok=455 bad=0:
- all four clip files load with PAL's own loader (`UniversalAnimLoader`), every clip has length and bones;
- every class in `clips.json` has every listed clip; every gun class has hold, aim, fire, reload, dry, inspect, draw, sprint; all 17 own-gun reload clips exist;
- tag resolution: iron sword/axe/pickaxe/shovel/hoe, bow, crossbow, shield, shears resolve to the right class (vanilla items only; the firearms jar was not loaded).

Not tested: a real client (PAL layer registration, trigger and playback, pose look, first person, the H key, packets on a live connection), firearms ids in tags with the firearms jar loaded (hook compiles on #158), Komrag combo timing with real swings. Done in the coverage CSV means clip plus tag class exists and loads, not that the poses were seen in game.
