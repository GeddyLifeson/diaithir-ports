# Diaithir Connected Textures

Clean-room connected textures (stands in for Fusion, all rights reserved). MIT. No code taken. The blocks load on both sides; the connecting is client only and **off by default**.

- Block state model type `diaithirctm:connected`: a blockstate entry `{"type": "diaithirctm:connected", "model": "ns:block/name"}` whose model uses a 4 by 4 sheet of 64 px tiles as its texture. Tile number = mask (bit 1 up, 2 right, 4 down, 8 left, set where the same block touches), row by row. The chunk renderer bakes block models before mods may swap them, so this must be a model type, not a post-bake swap.
- Client config `diaithirctm-client.toml`: `enabled = false` shows tile 0 (plain, framed) on every face; `true` picks tiles from the neighbours. Reload chunks (F3+A) after changing it.
- Ships Connected Glass and Connected Plating to try it. Item icons use the plain tile.
- Checked in the cloud client lane (real client): with the flag on, a 3x3 plating wall and a glass row show one frame around the outside only.
