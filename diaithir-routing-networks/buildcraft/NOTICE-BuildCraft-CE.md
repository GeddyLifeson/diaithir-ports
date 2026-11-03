# BuildCraft Community Edition (vendored, MPL-2.0)

Source: https://github.com/BCCE-team/BuildCraft (commit 5ad517c2f8010f22b36084444b030c2104df8e36, main), version 8.0.26, target 26.1.2-neoforge.
Licence: Mozilla Public License 2.0, full text in LICENSE.txt. Files in this directory tree are Covered Software under MPL-2.0 and stay MPL-2.0 (file-level copyleft). Modifications are made available in this repository.

Credit: BuildCraft Community Edition by CurativeTree, ShipovskijKorp, Memesis414 and Smail (BCCE-team), a port of BuildCraft 8.0.0 and 7.1.27. All original BuildCraft work belongs to the original BuildCraft team and contributors (https://github.com/BuildCraft/BuildCraft).

Only the layers needed for 26.1.2 NeoForge are vendored (source-shared, 1.21.X and 26.X families, neoforge platform, 26.1.2 version overlay, build-config, build-logic, builds/26.X, scripts). Forge, Fabric and older-generation layers are omitted.


Art: 966 textures are replaced by our own 64 px Industrial Factory redraws (same paths, status `own-64px` in TEXTURE_MANIFEST.csv; fluid `.mcmeta` files kept from upstream). Still upstream originals: 51 GUI sheets and guide pages, and the robot entity skins (placeholder redraws are not used). These must be replaced before this module ships. Mod id and block ids stay as upstream; new Foundry content gets the `foundry_` prefix.
