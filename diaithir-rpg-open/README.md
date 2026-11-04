# diaithir-rpg-open

MIT fold of Apotheosis 9.1.0 and its MIT dependencies into one jar, five mod ids: `apotheosis`, `placebo` 10.0.2, `apothic_attributes` 3.1.0, `apothic_enchanting` 2.1.0, `apothic_spawners` 2.0.1. Source: Shadows-of-Fire repos, branch `26.1`. Code is MIT (LICENSE_* files); Apotheosis assets are All Rights Reserved (LICENSE_ASSETS), so they stay unmodified.

Changes from upstream: removed JEI, Jade and Gateways to Eternity compat and the datagen providers that needed them (Codex replaces JEI; Patchouli stays a separate optional jar, CC-BY-NC-SA). Merged accesstransformer.cfg and a single neoforge.mods.toml with five `[[mods]]`.

Status: `compileJava` and `jar` pass on JDK 25. Not yet boot tested (server run was blocked in the cloud session). Needs id migration check: ids are unchanged from the standalone mods, so no migration is needed when the standalone jars are dropped.
