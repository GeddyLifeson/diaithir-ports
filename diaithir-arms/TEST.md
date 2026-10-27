# Test results

Build: `./gradlew build` with JDK 25, NeoForge 26.1.2.116, jar `diaithir-arms-neoforge-26.1.2-1.0.0.jar`.

Dedicated server boot (gradle runServer, rcon on a flat-free default world):
- Boots with no ERROR lines from the mod. Recipes parse; the only WARN lines are "empty ingredients" for recipes that need `c:ingots/bronze` and `c:ingots/steel`, which are empty tags on a bare server (the pack's Mekanism and Tinkers fill them).
- `/armsselftest`: ok=75 bad=0 (armour has equippable plus attribute modifiers; sword, axe and spear have weapon component; shields have `blocks_attacks` plus equippable; crossbows have `charged_projectiles`; every stack has durability).
- `summon item` for all 81 ids (75 gear, 6 parts): 81 summoned.
- `item replace` of all 81 ids into an armor stand's head, chest, legs, feet, main hand and off hand: 81 replaced (stands in for `/give`, no player was online). Readback of the stand's equipment shows the right ids.
- Static check of 81 item definitions: every referenced model, parent model, texture and equipment layer file exists.

Not tested: a real client (rendering of icons, worn armour layers, bow and crossbow pull animations, the flat shield model in the off hand), combat feel of the spear and shield numbers, real crafting with bronze and steel ingots present.
