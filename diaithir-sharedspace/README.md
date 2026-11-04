# Diaithir Shared Space

Small blocks share one block space on NeoForge 26.1.2. Right-click a small block with another small block and both fit, each in its own quarter of the space. Break them one at a time. Sneak to place beside the block as usual.

- Which blocks count as small is decided by measuring their shape: no wider than half a block either way (chains, lanterns, end rods, lightning rods, candles, and small blocks from any mod). Blocks with a block entity, fluid or a place in `diaithirsharedspace:denied` never share. `diaithirsharedspace:allowed` forces a block to count as small.
- A chain or rod placed from a side face stands upright so it fits a quarter.
- Up to four blocks per space. The brightest member lights the space.
- Not handled: slabs and wide blocks (hanging signs, fences) do not share; bare half-slab sharing is vanilla's own double slab.

Clean-room, MIT. Inspired by Olaf's Relatively Placed Blocks by LukeolafP (CC0-1.0 on its jar, Apache-2.0 on its Modrinth page). No code or art taken from it; behaviour is from its description and the tag and language names its jar ships.

Tests: `CellsTest` (cell maths). In-game: lantern plus chain share one space, three and four members, breaking removes one at a time (mini rig, screenshots in the PR).
