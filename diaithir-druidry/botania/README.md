# Botania port (inside diaithir-druidry)

Port of Botania (VazkiiMods/Botania, branch `1.21.1-porting`, Xplat + NeoForge sources) to NeoForge 26.1.2.

Licence: Botania License (`LICENSE.txt` here, plus `ALTERNATE_LICENSES.txt` for the Mekanism-derived MIT parts). Copyleft clause: this
module stays open source with the licence text beside it; no charging for access (non-monetary clause); credit Vazkii; changes are
marked (every ported file is an alteration of the original). Botania API code (`vazkii/botania/api`) is kept as obtained apart from
the mechanical 26.1 renames needed to compile. The rest of diaithir-druidry stays MIT; Botania code lives only under this folder.

Art: Botania's textures carry the same Botania License (no separate art licence), so they stay credited to the Botania artists
(`assets/botania/totextureartists.txt`). Per the pack texture rule they are redrawn in the Industrial Factory style over time; `ART.md`
tracks which are redrawn.

Layout: `src/main/java` is the active (ported, compiling) set; `pending/java` holds files not yet ported; `pending-META-INF` and
`*.mixins.json`, `botania.mods.toml.pending` are wired in when the mixin and registry batches land.

Batches: 1 API + helpers; 2 mana pool / spreader / flower core; then the rest.
