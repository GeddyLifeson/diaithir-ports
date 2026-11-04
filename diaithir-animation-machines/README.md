# diaithir-animation

Source package (no jar) for the machine, block and vehicle part of the animation module `diaithir-entity-animation`. Minecraft-free core: a `Controller` per instance (IDLE / WORKING / DONE with wind-up and wind-down), ten motion `Kind`s, and `Rules` that map any block, block entity or vehicle id to a `Spec` by first-match glob. `rules.tsv` is the single source of truth: the renderer glue and `tools/gen_coverage.py` both read it, so ANIMATIONS_COVERAGE.csv always matches what the game does.

Covers Foundry machines and operator seat, upgrade beacon, Keelwright helm/thrusters/sails, carts and rail, Immersive Vehicles/Aircraft (wheels, propellers, hatches, seats), doors, chests, levers, tanks, turrets, belts.

Test: `tools/run_tests.sh` (JDK 17+, no network). Next PR: NeoForge client glue (block entity renderer reading `Controller.pose()`, state from the block entity's working flag), then per-model part names.

## Block renderer (client)
`AnimatedBlockRenderer` draws a block entity with its own model moved by the pose from its rule. `BlockEntityDispatcherMixin` installs it after renderers are built, only for types with no renderer of their own whose blocks all use the normal model and all match an animatable rule. `SectionCompilerMixin` hides the static chunk copy of those blocks. State comes from a boolean block property (working, active, running, lit, powered, open, on, enabled, triggered, crafting, burning) or from `DiaithirAnimation.setOverride`. SCROLL (belts) and AIM (turrets) are not drawn here; entities (vehicles) use `DiaithirAnimation.pose`.
Client check: `./gradlew runServer` then `./gradlew runClient` under Xvfb (quickPlay joins 127.0.0.1:25565).
