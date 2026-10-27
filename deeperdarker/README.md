# Deeper Darker (Diaithir port)

Port of **Deeper and Darker** by Kyanite Mods to Minecraft 26.1.2 / NeoForge 26.1.2.116, for the Diaithir pack.

- Upstream: https://github.com/KyaniteMods/DeeperAndDarker (branch `neoforge-26.1`, commit 78ed826, 2026-06-23). No 26.1 release exists on Modrinth (latest published build is 1.4.1 for 1.21.1), so this is built from the upstream 26.1 branch and finished here.
- Licence: **GPL-3.0** (upstream `LICENSE`, kept unchanged in this directory; the Modrinth page lists AGPL-3.0, the repository file and `mod_license` say GPL-3.0). All code and assets stay under the upstream licence. This module is kept separate from the MIT modules in this repository for that reason.
- Jar: `deeperdarker-neoforge-26.1.2-1.4.1-diaithir.1.jar`

## What it is in the pack

The Otherside is the **Otherworld**: the night realm under the hills (the Sidhe-like otherworld of Celtic tale). You reach it through a *Bealach*, a frame of reinforced deepslate lit with the Warden's heart. Its four lands are Tìrdumuind (open caves), Koildemaktuldach (echo forest), Uomunblàtach (blooming caverns) and Kolbundeilach (gloom columns). The creatures there (sculk centipede, leech, snapper, shattered, stalker, shriek worm, sludge) are the *sìthfhàs*, the sidhe-growth, and answer to sound.

## Diaithir changes

- **Age VI.** Everything in the `deeperdarker` namespace is Age VI (`age_gates.json`). The portal also checks the entity tag `diaithir_age_vi` on players when the server config `othersideRequiredTag` is set (empty by default, so the mod works outside the pack).
- **Display names in Diaithiris** (`src/generated/resources/assets/deeperdarker/lang/en_us.json`). Ids are unchanged. The upstream English names are kept in `lang_source/en_us.upstream.json`; `tools/diaithiris_lang.py` rebuilds the lang file from them with the archaic-layer tools. Descriptions are English, framed as the Otherworld.
- **Operator command** `/otherside light <pos>` lights a portal frame without a Warden heart.
- **26.1 port work:** registries get explicit block ids, item definitions replace the 1.21 model overrides (`tools/gen_assets_26_1.py`), the datapack uses the 26.1 formats (dimension type attributes, biome attributes, flat carvers, recipe ingredients as plain ids, one file per loot modifier), and the datagen and JEI classes were dropped (the generated data is checked in under `src/generated`).
- Sculk transmitter dyeing is now ordinary `crafting_transmute` recipes (the upstream custom recipe class was missing).
- The gloomslate pot item shows the vanilla decorated-pot item model (no custom special renderer yet).
- Library entries for the in-game Library: `library/deeperdarker.json`.
