#!/usr/bin/env python3
"""One-time keybind migration for existing installs. Usage: migrate_options.py PLAYER_OPTIONS PACK_OPTIONS LIVE_DEFAULTS_TXT (tools/keybinds/live_defaults.txt)
For every key_<name> line in PACK_OPTIONS: if the player's options.txt has the line at the mod's own default
(or has no line), set it to the pack value; if the player changed it themselves, leave it alone.
Idempotent, writes a .bak once."""
import csv, sys, shutil, os
player, pack, live = sys.argv[1:4]
defaults = {l.split(':', 1)[0][4:]: l.strip() for l in open(live) if l.startswith('key_')}
want = {l.split(':', 1)[0]: l.strip() for l in open(pack) if l.startswith('key_')}
cur = open(player).read().splitlines() if os.path.exists(player) else []
if cur and not os.path.exists(player + '.bak'): shutil.copy(player, player + '.bak')
have = {l.split(':', 1)[0]: i for i, l in enumerate(cur) if l.startswith('key_')}
n = 0
for k, new in want.items():
    name = k[4:]
    if k in have:
        old = cur[have[k]].strip()
        if old == new or old != defaults.get(name): continue
        cur[have[k]] = new; n += 1
    else:
        cur.append(new); n += 1
open(player, 'w').write('\n'.join(cur) + '\n')
print(n, 'keybinds migrated')
