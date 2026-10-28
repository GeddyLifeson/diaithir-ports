# Default keybinds
`options.txt` (pack root, packwiz `preserve`) gives new installs a conflict-free key map; `keybinds.txt` is the same key lines for the PC updater to merge into an existing options.txt.
Regenerate after adding or removing mods:
1. Boot the client once with a fresh game folder, copy its options.txt (it lists every registered key).
2. `python3 tools/keybinds/build_keybinds.py that_options.txt keybinds.txt`, then rebuild options.txt as `version:4790` plus the lines of keybinds.txt, and refresh the hash in index.toml and pack.toml.
A file with key lines only and no `version:` line is ignored by the game, so keep that first line.
Frequent actions live in KEEP in the script (M Atlas, B backpack, G curios, K skills, J timeline, Z/X/V/N origin skills, C zoom, R reload); every other mod action is unbound.
