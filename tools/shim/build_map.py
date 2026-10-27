#!/usr/bin/env python3
"""Build the class mapping table (1.21.1 / NeoForge Create world -> 26.1.2 / diaithir port world).
Rules first (package roots), then unique simple-name match inside the same family. Output map.json:
 {"classes": {old: new}, "unmapped": [..]}  Usage: build_map.py map.json runtime.jar... -- addon.jar..."""
import sys, json, os, collections, re
SIGRX = re.compile(r'L((?:com|net|dev|org|plus|mezz|vectorwing)/[A-Za-z0-9_/$]+)[;<]')
sys.path.insert(0, os.path.dirname(__file__))
import cf
P = 'com/zurrtum/create/'
ROOTS = [  # (old root, [new roots to try in order])
    ('com/simibubi/create/', [P, P + 'client/']),
    ('net/createmod/catnip/', [P + 'catnip/', P + 'client/catnip/']),
    ('net/createmod/ponder/', [P + 'client/ponder/', P + 'ponder/']),
    ('dev/engine_room/flywheel/', [P + 'client/flywheel/', P + 'flywheel/']),
    ('dev/engine_room/vanillin/', [P + 'client/vanillin/', P + 'vanillin/']),
]
STUB = 'diaithir/shim/stub/'
STUB_PREFIXES = ('net/minecraft/', 'net/neoforged/', 'com/mojang/', 'com/tterrag/')
FAMILIES = ['net/minecraft/', 'net/neoforged/neoforge/', 'com/mojang/']
def main():
    out = sys.argv[1]; a = sys.argv.index('--')
    rt, addons = sys.argv[2:a], sys.argv[a+1:]
    idx = set(); bysimple = collections.defaultdict(list)
    for j in rt:
        for _, c in cf.jar_classes(j):
            if c['name'] not in idx: idx.add(c['name']); bysimple[c['name'].split('/')[-1]].append(c['name'])
    used = set()
    for ad in addons:
        for _, c in cf.jar_classes(ad):
            for k in c['classes'] + c['ifs'] + [c['super'] or '']:
                k = k.lstrip('[')
                if k[:1] == 'L' and k.endswith(';'): k = k[1:-1]
                used.add(k)
            for _, o, _, d in c['refs']: used.add(o.lstrip('['))
            for u in c['utf']:
                if '/' in u: used.update(SIGRX.findall(u))
    own = set()
    for ad in addons:
        for _, c in cf.jar_classes(ad): own.add(c['name'])
    m, un, stubs = {}, [], []
    ov = json.load(open(os.path.join(os.path.dirname(__file__), 'overrides.json')))
    ov = {k: v for k, v in ov.items() if v in idx and k != v}
    for k in sorted(used - own):
        if not k or k.startswith(('java/', 'javax/', 'sun/', 'jdk/')) or k in idx: continue
        if k in ov: m[k] = ov[k]; continue
        done = False
        for old, news in ROOTS:
            if k.startswith(old):
                rest = k[len(old):]
                for n in news:
                    if n + rest in idx: m[k] = n + rest; done = True; break
                if not done:
                    # relocated deeper: unique simple-name under new roots
                    cands = [c for c in bysimple.get(k.split('/')[-1], []) if c.startswith(P)]
                    if len(cands) > 1:   # best common suffix
                        def sc(c):
                            a_, b_ = k.split('/')[::-1], c.split('/')[::-1]; n = 0
                            for x, y in zip(a_, b_):
                                if x != y: break
                                n += 1
                            return n
                        cands.sort(key=sc, reverse=True)
                        if sc(cands[0]) > sc(cands[1]): cands = cands[:1]
                    if len(cands) == 1: m[k] = cands[0]; done = True
                break
        else:
            if k.startswith(tuple(FAMILIES)):
                cands = [c for c in bysimple.get(k.split('/')[-1], []) if c.startswith(tuple(FAMILIES))]
                if len(cands) == 1: m[k] = cands[0]; done = True
        if not done:
            if k.startswith(STUB_PREFIXES) and not k.startswith(('net/neoforged/fml/', 'net/neoforged/bus/')): m[k] = STUB + k; stubs.append(k)
            else: un.append(k)
    json.dump(dict(classes=m, unmapped=un, stubs=stubs), open(out, 'w'), indent=1)
    print('mapped', len(m), 'unmapped', len(un))
main()
