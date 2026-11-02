# diaithir-automation-crafting

Source tree for the consolidated automation and crafting module (FACTORIO_TECH.md section 1). First tenant: **Researcher**, the Foundry research bench.

## Researcher port
- Upstream: Tr100000/Researcher, tag `0.3.4+26.1.2` (Fabric), MIT. Licence text in `LICENSE-Researcher`.
- Support code from Tr100000/TrUtils (`26.1.x`, MIT), only the pieces Researcher uses, under `io.github.tr100000.trutils`. Licence text in `LICENSE-TrUtils`.
- Package `io.github.tr100000.researcher`, mod id and data namespace `researcher` are kept for credit; ids never change.
- Changes from upstream: Fabric API replaced by NeoForge events, payload registrar, `RegisterGuiLayersEvent`, `OnDatapackSyncEvent` recipe sync; fzzy_config replaced by `ModConfigSpec` (server options are COMMON so they are readable during datapack load); classtweaker replaced by `accesstransformer.cfg` and `interfaces.json`; JEI/REI/RRV compat, codec2schema and datagen API dropped; GUI art redrawn.
- Foundry/Age tie-in data lives under `data/researcher/research/` (see below).

## Art and audio
All GUI art (`textures/gui/*`, `textures/research/default.png`, `icon.png`) and the toast chime are our own, drawn by `tools/gen_researcher_art.py` and `tools/gen_research_chime.sh` in the Industrial Factory look (steel plates, rust orange, hazard yellow). No upstream or Factorio art ships. Research item icons are vanilla or pack item textures resolved at runtime.

## Testing
`testpack/` is a smoke-test datapack (one research, vanilla recipe unlock). It is NOT shipped in the jar. Run: copy it to `run/world/datapacks/`, start `./gradlew runServer`, and for a client `./gradlew runClient -PjoinServer=localhost:25598`. Press R in game to open the research screen.
Researches that lock vanilla recipes must never ship in this jar: the real tree (Foundry and Age nodes) is authored as data in the Foundry thread.

## Known follow-ups
- Foundry/Age tie-in data (research nodes, science-pack items) is separate content, not part of the port.
- Large research payloads: upstream used a "large" payload; NeoForge's play packet cap applies (8 MB), plenty for trees of a few thousand nodes.
- Datapack sync requests all crafting recipes so unlock tooltips can resolve them (same as upstream's synchronised serializers).
- Config: client options in `researcher-client.toml`, server options in `researcher-common.toml`.
