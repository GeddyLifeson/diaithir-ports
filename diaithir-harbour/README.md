# Diaithir Harbour

Clean-room harbour works for boats, NeoForge 26.1.2, MIT. Written from the behaviour of a quay only; no code, art or names from any other mod. Real time, no turns.

- **Mooring bollard.** Made fast, it holds every boat within its reach (3 blocks) in place: no drifting, no paddling away. Use it bare-handed to let go; a redstone signal also lets go. Checks every 4 ticks.
- **Harbour office.** An age-evolving station: timber, brick, iron and steel quay by calendar Age (0-2, 3-4, 5-6, 7+; reads `diaithir_ages` softly, steel if it is absent). Each stage adds 2 blocks of reach to every bollard within 16 blocks and one more cargo crate per boat. `/diaithirharbour age <n>` (owners) overrides the Age for tests, -1 clears it.

`tools/gen_assets.py` writes textures (drawn from scratch), models, blockstates, lang, recipes, loot, tags, age gate and `../library-sync/diaithirharbour.json`.
