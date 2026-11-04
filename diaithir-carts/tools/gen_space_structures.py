#!/usr/bin/env python3
"""Index structure NBTs ported into data/diaithirmega/structure/<source>/ and write data/diaithirmega/space_structures/<source>.json.
Usage: gen_space_structures.py SOURCE_NBT_DIR SOURCE_NAME OUT_RESOURCES_DIR [SOURCE_NS]
Copies *.nbt (MIT upstream, see LICENSES.md) and records size, block count and palette so non-vanilla ids can be remapped.
Pure stdlib NBT reader."""
import gzip, json, os, shutil, struct, sys
from collections import Counter

def read(f):
    def u(fmt):
        n = struct.calcsize(fmt); return struct.unpack(fmt, f.read(n))[0]
    def payload(t):
        if t == 1: return u('>b')
        if t == 2: return u('>h')
        if t == 3: return u('>i')
        if t == 4: return u('>q')
        if t == 5: return u('>f')
        if t == 6: return u('>d')
        if t == 7: n = u('>i'); f.read(n); return None
        if t == 8: n = u('>H'); return f.read(n).decode('utf8', 'replace')
        if t == 9:
            it = u('>b'); n = u('>i'); return [payload(it) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                tt = u('>b')
                if tt == 0: return d
                n = u('>H'); k = f.read(n).decode(); d[k] = payload(tt)
        if t == 11: n = u('>i'); f.read(4 * n); return None
        if t == 12: n = u('>i'); f.read(8 * n); return None
        raise ValueError(t)
    t = u('>b'); n = u('>H'); f.read(n)
    return payload(t)

def main():
    src, name, out = sys.argv[1:4]
    ns = sys.argv[4] if len(sys.argv) > 4 else name
    dst = os.path.join(out, 'data/diaithirmega/structure', name)
    rows = []
    for root, _, files in sorted(os.walk(src)):
        for fn in sorted(files):
            if not fn.endswith('.nbt'): continue
            p = os.path.join(root, fn)
            rel = os.path.relpath(p, src).replace(os.sep, '/')
            with gzip.open(p) as g: t = read(g)
            pal = t.get('palette') or (t.get('palettes') or [[]])[0]
            ids = [e['Name'] for e in pal]
            cnt = Counter(ids[b['state']] for b in t.get('blocks', []))
            rows.append({'file': rel, 'size': t.get('size'), 'blocks': sum(cnt.values()),
                         'foreign_blocks': {k: v for k, v in sorted(cnt.items()) if not k.startswith('minecraft:')}})
            os.makedirs(os.path.dirname(os.path.join(dst, rel)), exist_ok=True)
            shutil.copy(p, os.path.join(dst, rel))
    od = os.path.join(out, 'data/diaithirmega/space_structures'); os.makedirs(od, exist_ok=True)
    with open(os.path.join(od, name + '.json'), 'w') as f:
        json.dump({'source': ns, 'license': 'MIT', 'structures': rows}, f, indent=1); f.write('\n')
    print(len(rows), 'structures')
main()
