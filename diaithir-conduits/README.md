# Diaithir Conduits

A clean-room wireless transfer mod for NeoForge 26.1.2, written from public descriptions of the idea only (no code, textures or names of any other mod). MIT.

Conduit nodes (Vaurelith tier 1, Ordhevane tier 2) join a numbered channel. A channel is an owner plus a number, so your channel 1 and another player's channel 1 never meet. Every node on a channel can trade items, fluids and FE with every other node on it, in any dimension, as long as both chunks are loaded.

Each face of a node is off, send or receive. Sneak with an empty hand to step the face you click. A send face pulls from the block beside it (chest, tank, battery, machine) and also accepts input from pipes and cables. A receive face pushes what arrives into the block beside it. `/diaithirconduits kinds <items|fluids|energy> <true|false>` switches a kind off for the node you look at.

Set the channel with the tuner (Thorvaneth): use it on a node to join your channel 1 and again to step up, sneak to step down. Or look at a node and use `/diaithirconduits channel <n>`. To join someone else's channel: they run `/diaithirconduits trust <you>`, then you run `/diaithirconduits join <owner> <n>`. Only a node's owner (or an operator, if `opsManageAll`) can retune, reconfigure or break it. `/diaithirconduits untrust`, `list` and `info` do what they say.

Bandwidth is per run (every `intervalTicks`, default 8) and per node, and is shared by pulling and by pipes feeding it: tier 1 sends 16 items, 1000 mB and 4000 FE; tier 2 sends eight times that. Config caps: channels per player (8), nodes per channel (32), channel numbers (1 to 9999). The registry lives in world SavedData (`diaithirconduits:channels`). Nothing is sent to an unloaded chunk.

The fluid cell (Sulmerith) and energy cell (Kaldruvane) are plain buffers any face can fill and empty.

Age gate data is in `data/diaithirconduits/age_gate.json` (tier 1 Age VI, tier 2 Age VII); library text is in `library-sync/diaithirconduits.json`. `tools/gen_assets.py` writes the textures, models, lang, recipes, age gate and library entries.

`/diaithirconduits selftest` (owners): two nodes at two positions on one channel move items between chests, fluid between fluid cells and FE between energy cells; checks the tier bandwidth, face modes, pipe input, a third node in the Nether, that a stranger is refused until trusted, and the channel cap.
