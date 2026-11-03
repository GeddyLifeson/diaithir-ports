# Diaithir Connected Textures

Clean-room connected textures (stands in for Fusion, all rights reserved). MIT. No code taken. Client only, **off by default**.

- Client config `diaithirctm-client.toml`: `enabled = false`. Turn it on to check.
- Add blocks to the block tag `diaithirctm:connect` (empty now). Each such block's texture must be a sheet of 4 by 4 tiles; tile number = mask (bit 1 up, 2 right, 4 down, 8 left, set where the same block touches), row by row.
- A face picks its tile from its neighbours. Face orientation for up, down and side faces is written from geometry and unverified: check the first sheet in a client and flip `Mask.right` if sides mirror.
- Compiles on JDK 25. Not run: model baking needs a client.
