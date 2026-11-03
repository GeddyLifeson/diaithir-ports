# Keybind audit
- `extract_keymappings.py JAR_DIR OUT.csv`: static scan of every jar (and jar-in-jar) for `new KeyMapping(...)`.
- `check_keybinds.py CSV PACK_ROOT`: conflict = same key+modifier with overlapping contexts (GUI vs in-game do not overlap). Applies `PACK_ROOT/options.txt` defaults. `allowlist.txt` lists accepted pairs. Exit 1 on conflict or duplicate mapping name.
- `assign_keybinds.py CSV OUT`: regenerates `options.txt` key lines (first by priority keeps its key, the rest get free ALT/CTRL/SHIFT+key).
- `run_check.sh JAR_DIR PACK_ROOT`: all of the above for the pack build. Run it after any mod change.
- `dump_live_keys.py WORKDIR OUT JAR...`: boots a real client headless, saves every registered mapping (ground truth). `live_defaults.txt` is that dump of the pack's client set (without client-suite and entity-animation, which crash boot on this branch; their keys come from the static scan).
- `migrate_options.py PLAYER_OPTIONS PACK_OPTIONS live_defaults.txt`: for existing installs, applies the pack's remaps only to keys the player left at the mod default.
Static limits: static, so mappings whose name or key is computed at runtime (about 100 of 273) are not seen. Our own jars should declare constant defaults and a KeyConflictContext so this check can see them.
