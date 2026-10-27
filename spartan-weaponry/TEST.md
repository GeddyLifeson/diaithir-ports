# Test results

Build: `./gradlew build` with JDK 25, NeoForge 26.1.2.116, jar `spartan-weaponry-neoforge-26.1.2-3.2.1-diaithir.1.jar`.

Dedicated server boot (gradle runServer, rcon):
- Boots with no ERROR lines; all recipes and tags parse (the first boot caught the missing `c:ingots/bronze` tag, fixed by shipping an empty tag).
- `/spartanselftest`: ok=114 bad=0 (every weapon builds a stack with attack damage attribute > 0, durability and the weapon component).
- `summon item` for all 118 ids (114 weapons, 4 handles and poles): 118 summoned.
- Static check of the 118 item definitions: every model, parent model and texture exists.

Not tested: a real client (rendering of the models), combat behaviour of the conditional habits in `TraitEvents`, sweep and reach feel in play.
