# Join Diaithir

Diaithir is a free Minecraft modpack for NeoForge 26.1.2. You need Minecraft Java Edition and a PC with 16 GB of RAM. The game gets 12 GB by default (the pack needs about 5 GB at the default view distance 8 once you are in a world). With 16 GB of RAM lower the maximum to 8192 MB in the instance settings.

## Install

1. Install [Prism Launcher](https://prismlauncher.org/download/) and sign in with your Microsoft account.
2. In Prism, click **Add Instance**, then **Import**.
3. Paste this link and click **OK**:
   `https://raw.githubusercontent.com/GeddyLifeson/diaithir-ports/refs/heads/claude/project-thread-ysjkxr/Diaithir.zip`
4. Select the new **Diaithir** instance and click **Launch**.

The first launch downloads about 800 MB of mods, so it takes a few minutes. A small window shows the progress. Every launch after that checks for updates and fetches only what changed.

## Play

The server is already in your multiplayer list as **Diaithir**. If it is missing, add it with the address `ssh-elem.tun.ply.gg`.

## If something goes wrong

- **The game says a mod is missing or a version is wrong:** close the game and launch it again. The update runs before every start.
- **Out of memory:** in Prism, right-click the instance, choose **Edit**, then **Settings** and **Java**, and keep the maximum at 12288 MB (32 GB PC) and keep the render distance at 24 or lower. Close browsers and other big programs while you play.
- **The download stops:** launch again. Files that finished are kept.

## What gets installed

Mods download straight from their authors' Modrinth and CurseForge pages, and each file is checked against a sha256 hash before it is used. Our own builds of open-source mods are hosted here; see [LICENSES.md](LICENSES.md).

## Memory and view distance
The world is 4064 blocks tall (254 sections per chunk), so one chunk costs far more heap than in vanilla. Measured in a 4 CPU / 14 GB container: singleplayer at render distance 8, simulation 8 held 4.85 GB live with about 800 chunks loaded, on top of a 2.6 GB empty-world footprint. Heap grows with the square of the render distance (roughly 2.7 MB per loaded chunk), so render distance 24 needs about 10 GB and 32 about 15 GB; render distance 56 cannot fit in 32 GB. These figures for 24 and 32 are extrapolated, not measured. The pack therefore ships `config/c2me.toml` with the NBT cache cut from 8192/32678 to 512/1024 chunks, `lowMemoryMode` on, 6 worker threads and a client render distance cap of 24, and keeps the G1 flags. Defaults in options.txt stay at render distance 8, simulation 6.
Threads: `diaithir-perf` 0.3.1 stops Distant Horizons from creating its per-dimension thread pools for dimensions in `ignoredDimensionCsv` (207 ids, mostly the Backrooms levels). Singleplayer thread count went from 1109 to 472 in the same world. It also stops Lightman's Currency from blocking the server thread on chunk loads when a far teleport drops thousands of chunks.
