# Diaithir Connected Textures

Clean-room connected textures (stands in for Fusion, all rights reserved). MIT. No code taken. The blocks load on both sides; the connecting is client only and **off by default**.

- Client config `diaithirctm-client.toml`: `enabled = false`. Turn it on to check.
- Ships two blocks to check with: Connected Glass and Connected Plating (both in the tag `diaithirctm:connect`). Each block's model uses a single plain tile; its sheet `<path>_sheet.png` is a 4 by 4 grid of 64 px tiles, tile number = mask (bit 1 up, 2 right, 4 down, 8 left, set where the same block touches), row by row. With the flag off you see the plain tile; with it on, edges frame only where nothing touches.
- To connect another block, put its id in the tag and add `textures/block/<path>_sheet.png` plus a `minecraft:single` entry in `atlases/blocks.json`. Blocks without a sheet are left alone.
- A face picks its tile from its neighbours. Face orientation for up, down and side faces is written from geometry and unverified: check the first sheet in a client and flip `Mask.right` if sides mirror.
- Compiles on JDK 25. Not run: model baking needs a client.
