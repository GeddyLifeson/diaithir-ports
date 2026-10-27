# Diaithir Chunkloaders

Clean-room chunk loading for NeoForge 26.1.2, written from public descriptions of the idea only. No code, textures or names from any other mod. Code is MIT; the textures are drawn by `tools/gen_assets.py` from flat colours.

- Three Loader blocks (Velhorn, Marduvain, Ixthuran; ids `chunk_loader_1` to `_3`) keep 1x1, 3x3 and 5x5 chunks loaded while they hold fuel. They burn any furnace fuel, fed by hand (sneak to feed a whole stack); use one empty-handed to read radius, time left and owner. Tickets go through NeoForge's ticket controller, so they survive restarts and are validated against the loader registry on load.
- Ownership: the placer owns a loader. The owner, scoreboard teammates and operators may fuel or break it; others can only read it.
- Rhovel Cartseal (optional tier, `carts.enabled`): use on a minecart to keep the chunks around it loaded while it rides, following it; 30 minutes per seal.
- TPS safety, all in `serverconfig/diaithirchunkloaders-server.toml`: `maxLoadersPerPlayer` (4), `maxCartsPerPlayer` (2), per-tier radius and fuel cost, `entityTicking` (false keeps chunks loaded without ticking them), fuel cap in hours.
- Commands: `/diaithirchunkloaders list` (yours and your team's), `list all` (operators), `selftest` (owners): places a loader of each tier far from players, checks its chunks are force-loaded while it burns and released on burnout and on break, then the same for a cart's moving chunk square.
- Data: `data/diaithirchunkloaders/age_gate.json` (tier 1 Age III, tier 2 Age V, tier 3 Age VII; the Cartseal sits with tier 2), `library-sync/diaithirchunkloaders.json`.

Not done: power (FE) as a fuel; a container GUI (status is an action-bar line and item tooltips).

Licence: MIT.
