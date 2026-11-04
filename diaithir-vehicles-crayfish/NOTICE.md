# Notice

This module is licensed GPL-3.0-only (see `LICENSE-GPL-3.0.txt`). It folds behaviour and logic from
**MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port)** by bay4lly, https://github.com/Bay4lly/MrCrayfish-Vehicle-Unofficial, itself the work of
MrCrayfish with contributions by Phylogeny.

Licence check (2026-11-03): Modrinth lists GPL-3.0-only and `META-INF/mods.toml` says "GNU General Public License 3.0", but the `LICENSE` file in that repository is the text of the GNU GPL **version 2** (June 1991). Either way the code is copyleft and this module keeps the same terms for what it takes: GPL-3.0-only, source in this repository. If the repository owner's intent is GPL-2.0-only, this module must not be combined into a GPL-3 jar. It ships as its own source tree for that reason. The author's art and sounds are all-rights-reserved style assets and are **not** used.

What was taken, and from which upstream classes:
- `Hitch`, `TankTrailer` (pulling and detach logic): `entity/TrailerEntity` (tow bar rotation, hitch length against height difference, yaw to the bar, pitch clamp of 20 degrees at 0.25 smoothing, detach beyond a threshold, only a player link is saved).
- `GasPumpBlock`, `GasPumpEntity`: `block/GasPumpBlock`, `tileentity/GasPumpTileEntity` (pump stands on its tank, one player holds the hose, the hose lets go beyond a maximum distance with a break sound).
- `VehicleHealth`, `Hammer`: `entity/VehicleEntity` (health 100, damage on hit, fall damage at 4 blocks and over 1 block per tick falling, half the distance, hammer heals 5, destroyed stage `10 - max(1, ceil(10 * health / max))`).

Changes: Forge 1.20.1 to NeoForge 26.1.2 APIs; NeoForge transfer fluid tanks replaced by the shared `FuelStore` capability from `diaithir-vehicles`; the upstream vehicle framework, workstation, entity jack, models, textures and sounds are not included.
