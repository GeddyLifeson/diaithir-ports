# diaithir-automation-crafting

Source tree for the consolidated automation and crafting module (FACTORIO_TECH.md section 1). First tenant: **Researcher**, the Foundry research bench.

## Researcher port
- Upstream: Tr100000/Researcher, tag `0.3.4+26.1.2` (Fabric), MIT. Licence text in `LICENSE-Researcher`.
- Support code from Tr100000/TrUtils (`26.1.x`, MIT), only the pieces Researcher uses, under `io.github.tr100000.trutils`. Licence text in `LICENSE-TrUtils`.
- Package `io.github.tr100000.researcher`, mod id and data namespace `researcher` are kept for credit; ids never change.
- Changes from upstream: Fabric API replaced by NeoForge events, payload registrar, `RegisterGuiLayersEvent`, `OnDatapackSyncEvent` recipe sync; fzzy_config replaced by `ModConfigSpec` (server options are COMMON so they are readable during datapack load); classtweaker replaced by `accesstransformer.cfg` and `interfaces.json`; JEI/REI/RRV compat, codec2schema and datagen API dropped; GUI art redrawn.
- Foundry/Age tie-in data lives under `data/researcher/research/` (see below).
