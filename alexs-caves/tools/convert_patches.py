#!/usr/bin/env python3
"""One-off: 26.1 removed minecraft:random_patch. Turn each such configured feature into its inner feature and move
tries / spread / inner placement into the placed features that reference it. Idempotent (skips already converted)."""
import json, glob, os
R = 'src/main/resources/data/alexscaves/worldgen/'
conv = {}
for f in glob.glob(R + 'configured_feature/*.json'):
    d = json.load(open(f))
    if d['type'] != 'minecraft:random_patch': continue
    c = d['config']; name = 'alexscaves:' + os.path.basename(f)[:-5]
    inner = c['feature']
    uni = lambda v: {"type": "minecraft:uniform", "min_inclusive": -v, "max_inclusive": v}
    conv[name] = [{"type": "minecraft:count", "count": c.get('tries', 128)},
                  {"type": "minecraft:random_offset", "xz_spread": uni(c.get('xz_spread', 7)), "y_spread": uni(c.get('y_spread', 3))}] + inner.get('placement', [])
    json.dump(inner['feature'], open(f, 'w'), indent=2)
for f in glob.glob(R + 'placed_feature/*.json'):
    d = json.load(open(f))
    if d['feature'] in conv:
        d['placement'] = d['placement'] + conv[d['feature']]
        json.dump(d, open(f, 'w'), indent=2); print('patched', f)
