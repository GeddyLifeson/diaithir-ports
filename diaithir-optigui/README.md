# OptiGUI for NeoForge 26.1.2

A NeoForge port of [OptiGUI](https://github.com/opekope2/OptiGUI) by opekope2 (LGPL-3.0-or-later, see `COPYING` and `COPYING.LESSER`; upstream's `dev` branch is LGPL-3.0-or-later, the 26.1 branch this was ported from carries the MIT text, kept as `LICENSE-MIT-upstream-26.1`).
Resource packs retexture GUI screens (chests, furnaces, villagers, horses, books and more) by condition,
using the OptiFine custom GUI format (`assets/minecraft/optifine/gui/container/*.properties`) or OptiGUI's own JSON format.

Source: upstream branch `legacy-release/26.1` (commit `88a0c80`; the 26.x line is Fabric only, the
upstream NeoForge module on `dev` targets 1.21.1 and is unreleased). Only the loader glue differs from upstream:

| Fabric | NeoForge |
|---|---|
| `fabric.mod.json` entrypoints | `OptiGuiMod` (`@Mod`, client only, Kotlin for Forge) |
| `optigui.accesswidener` | `META-INF/accesstransformer.cfg` |
| Fabric `UseBlock/UseEntity/UseItemCallback` | `PlayerInteractEvent` (RightClickBlock, EntityInteract, RightClickItem) |
| `ScreenEvents.beforeExtract/afterExtract` | `ScreenEvent.Render.Pre/Post` |
| `ResourceManagerHelper` reload listener | `AddClientReloadListenersEvent` |
| `KeyMappingHelper` | `RegisterKeyMappingsEvent` |
| `custom` field `optigui:container_default_gui_textures` | `[modproperties.<modid>]` keys `optigui:container_default_gui_textures:<container id>` |
| `if.mods` load selector (Fabric version predicates) | NeoForge mod versions, operators `>= <= > < = ^ ~` |

Needs Kotlin for Forge 6.3.0 or newer. `testpack/` is a test resource pack: a chest named `Tuath` gets a green screen
(`make_texture.py` builds the texture from a Minecraft jar, the png itself is not committed).
