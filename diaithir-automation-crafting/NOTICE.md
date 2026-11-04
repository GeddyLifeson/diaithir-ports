# Notice and credits: OpenComputers port

This folder ports OpenComputers II: Reimagined (OC2R) to Minecraft 26.1.2 NeoForge for the Diaithir pack.

| Work | Author | Licence | Source |
|---|---|---|---|
| OpenComputers II (original) | Florian "Sangar" Nücke | MIT code, CC0 assets | https://github.com/fnuecke/oc2 |
| OpenComputers II: Reimagined, branch 1.21.1, commit a025557b1b2a1b77406151fdc2e394b17ee56a7a | North Western Development | MIT code, CC0 assets | https://github.com/North-Western-Development/OC2R |
| Sedna (RISC-V VM, Java) | Florian Nücke, North Western Development | MIT | https://github.com/North-Western-Development/sedna |
| Ceres (serialisation) | Florian Nücke | MIT | https://github.com/fnuecke/ceres |
| JCodec parts | JCodec authors | see `upstream/oc2r-1.21.1/LICENSE-JCODEC` | bundled upstream |

The licence texts are kept unchanged in `upstream/oc2r-1.21.1/LICENSE` and `LICENSE-JCODEC`. The MIT notice must stay in every distributed jar.

Art: upstream PNG, OGG and WAV files are not copied here. All textures are redrawn in the Diaithir 64 px Industrial Factory style; no upstream or Factorio art ships.
Minux guest images: Buildroot-built Linux (GPL-2.0-or-later with per-package licences, listed in the unpacked `licenses/` folder; sources at https://github.com/North-Western-Development/minux). Fetched at build time, not stored in git; the jar ships the images with those licence texts, which satisfies GPL notice, and the source offer is the link above.
