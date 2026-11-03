#!/usr/bin/env python3
"""Keybind conflict check. Usage: check_keybinds.py KEYMAP_CSV [PACK_ROOT] [LIVE_OPTIONS_TXT]
KEYMAP_CSV comes from extract_keymappings.py. Pack defaults in PACK_ROOT/options.txt
(key_<mapping>:key.keyboard.x, optional modifier suffix) override jar defaults.
Conflict = same key + modifier with overlapping contexts. Pairs in allowlist.txt
(one 'name_a|name_b' per line, '#' comments) are accepted. Exit 1 on any conflict."""
import csv, sys, os, itertools, collections

VANILLA = {  # name: (key, modifier, contexts)
 'key.forward': 'W', 'key.left': 'A', 'key.back': 'S', 'key.right': 'D', 'key.jump': 'SPACE', 'key.sneak': 'LSHIFT',
 'key.sprint': 'LCTRL', 'key.inventory': 'E', 'key.swapOffhand': 'F', 'key.drop': 'Q', 'key.chat': 'T', 'key.playerlist': 'TAB',
 'key.command': 'SLASH', 'key.socialInteractions': 'P', 'key.advancements': 'L', 'key.saveToolbarActivator': 'C',
 'key.loadToolbarActivator': 'X', 'key.screenshot': 'F2', 'key.togglePerspective': 'F5', 'key.fullscreen': 'F11',
 'key.hotbar.1': '1', 'key.hotbar.2': '2', 'key.hotbar.3': '3', 'key.hotbar.4': '4', 'key.hotbar.5': '5',
 'key.hotbar.6': '6', 'key.hotbar.7': '7', 'key.hotbar.8': '8', 'key.hotbar.9': '9'}
GUI_ONLY_CTX = {'GUI'}
VNAMES = {'playerlist', 'loadToolbarActivator', 'chat', 'quickActions', 'advancements', 'inventory', 'saveToolbarActivator', 'smoothCamera', 'spectatorHotbar', 'swapOffhand', 'pickItem', 'sneak', 'right', 'drop', 'back', 'socialInteractions', 'forward', 'screenshot', 'left', 'sprint', 'togglePerspective', 'jump', 'command', 'use', 'attack', 'fullscreen', 'spectatorOutlines'}
LK = {'LEFT_SHIFT': 'LSHIFT', 'LEFT_CONTROL': 'LCTRL', 'LEFT_ALT': 'LALT', 'RIGHT_SHIFT': 'RSHIFT', 'RIGHT_CONTROL': 'RCTRL', 'RIGHT_ALT': 'RALT', 'LSHIFT': 'LSHIFT'}
def ctxset(c):
    c = c.upper()
    if c.startswith('GUI'): return {'gui'}
    if c.startswith('IN_GAME') or c.startswith('BLUEPRINT'): return {'game'}
    return {'game', 'gui'}   # UNIVERSAL, dynamic, unknown: assume both

def load_overrides(root):
    o = {}
    p = os.path.join(root or '', 'options.txt')
    if os.path.exists(p):
        for l in open(p):
            if l.startswith('key_'):
                n, v = l.strip()[4:].split(':', 1)
                v, _, mod = v.partition(':')
                k = v.replace('key.keyboard.', '').upper().replace('LEFT.', 'L').replace('RIGHT.', 'R').replace('.', '_')
                o[n] = (k, mod.upper() or 'NONE')
    return o

def main():
    csvp = sys.argv[1]; root = sys.argv[2] if len(sys.argv) > 2 else ''
    ov = load_overrides(root)
    allow = set()
    ap = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'allowlist.txt')
    if os.path.exists(ap):
        for l in open(ap):
            l = l.split('#')[0].strip()
            if '|' in l: allow.add(frozenset(l.split('|')))
    live = {}
    if len(sys.argv) > 3:
        for l in open(sys.argv[3]):
            n, _, v = l.strip()[4:].partition(':'); v, _, mod = v.partition(':')
            k = v.replace('key.keyboard.', '').replace('key.mouse.', 'MOUSE_').upper().replace('LEFT.', 'L').replace('RIGHT.', 'R').replace('.', '_')
            if k not in ('UNKNOWN', 'KEY_UNKNOWN', 'MOUSE_UNKNOWN'): live[n] = (k, mod.upper() or 'NONE')
    maps = []
    for n, k in VANILLA.items(): maps.append((n, k, 'NONE', {'game'}, 'vanilla'))
    seen = collections.defaultdict(set)
    for r in csv.DictReader(open(csvp)):
        n = r['name']
        if n in ('?', 'key') or r['default_key'] in ('UNKNOWN', '?'): continue
        k, m = ov.get(n, (r['default_key'], r['modifier']))
        maps.append((n, k, m, ctxset(r['context']), r['jar'])); seen[n].add(r['jar'])
    if live:
        ctx = {r['name']: ctxset(r['context']) for r in csv.DictReader(open(csvp))}
        import re
        vset = {m[0]: m for m in maps if m[4] == 'vanilla'}
        maps = []
        for n, (k, m) in live.items():
            k, m = ov.get(n, (k, m))
            c = {'game'} if (n[4:] in VNAMES or re.match(r'^key\.hotbar\.\d$', n)) and n != 'key.chat' else ctx.get(n, {'game', 'gui'})
            if n == 'key.chat': c = {'game'}
            maps.append((n, LK.get(k, k), m, c, 'live'))
    bad = []
    by = collections.defaultdict(list)
    for x in maps: by[(x[1], x[2])].append(x)
    for key, v in sorted(by.items()):
        for a, b in itertools.combinations(v, 2):
            if a[0] == b[0] or frozenset((a[0], b[0])) in allow: continue
            if a[3] & b[3]: bad.append((key, a, b))
    dup = {n: j for n, j in seen.items() if len(j) > 1}
    for key, a, b in bad: print(f'CONFLICT {key[1]}+{key[0]}: {a[0]} [{a[4][:30]}] vs {b[0]} [{b[4][:30]}]')
    for n, j in dup.items(): print(f'DUPLICATE-NAME {n}: {sorted(j)}')
    print(len(bad), 'conflicts,', len(dup), 'duplicate names')
    sys.exit(1 if bad or dup else 0)
main()
