# Port notes

Port of upstream 1.21.1 code (8.2.19) to NeoForge 26.1.2.116 / Minecraft 26.1.2. Follows the official Mekanism 26.1 branch for API changes.

## Stubbed, removed or stashed

Source kept out of the build lives in `port-stash/`.

- Third-party integrations (JEI, EMI, Patchouli, The One Probe, Jade/Waila, Curios, CC:Tweaked, Mekanism, Immersive Engineering, Create, AE2, Botania, CraftTweaker, FTB Teams, FTB Filter System, Game Stages, Cold Sweat, Tough As Nails, CoFH, Forestry, EnderIO, BuildCraft, Thaumcraft, Fusion): removed. None of those mods are API-ready on 26.1 in the same form.
- Client side (`client/`, all GUIs, renderers, models, HUD, key handling): stashed in `port-stash/` while the common side is ported; see the status list below for what is back.
- Datagen: stashed. The generated data under `src/generated/resources` is used as is (and converted to 26.1 formats where the format changed).
