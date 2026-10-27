#!/usr/bin/env python3
"""Second table: non-Create references in addon jars that do not resolve on the 26.1.2 runtime (MC/NeoForge/libs).
Usage: mcdiff.py out.json addon.jar... -- runtime.jar..."""
import sys, json, os, collections, zipfile
sys.path.insert(0, os.path.dirname(__file__))
import cf
def main():
    out = sys.argv[1]; a = sys.argv.index('--')
    addons, rt = sys.argv[2:a], sys.argv[a+1:]
    idx = {}
    for j in rt:
        for _, c in cf.jar_classes(j): idx.setdefault(c['name'], c)
        # also non-class presence e.g. resources irrelevant
    bysimple = collections.defaultdict(list)
    for n in idx: bysimple[n.split('/')[-1]].append(n)
    def has(o, kind, nm, d, depth=0):
        c = idx.get(o)
        if c is None: return None
        ms = c['methods'] if kind == 'M' else c['fields']
        if any(m[0] == nm and m[1] == d for m in ms): return True
        for s in [c['super']] + c['ifs']:
            if s and not s.startswith(('java/','javax/')):
                r = has(s, kind, nm, d, depth+1)
                if r: return True
                if r is None and s not in idx: return 'unknown'   # super outside index: cannot judge
        return False
    res = {}
    for ad in addons:
        own = {c['name'] for _, c in cf.jar_classes(ad)}
        mc, mm = collections.Counter(), collections.Counter()
        for _, c in cf.jar_classes(ad):
            for k in c['classes']:
                k = k.lstrip('[')
                if k.startswith('[') : continue
                if k[:1]=='L' and k.endswith(';'): k = k[1:-1]
                if k in own or k.startswith(('java/','javax/','sun/','jdk/','com/simibubi/')): continue
                if k not in idx: mc[k] += 1
            for kind, o, nm, d in c['refs']:
                if o in own or o.startswith(('java/','javax/','[','com/simibubi/')) or o not in idx: continue
                if nm == '<init>' and kind == 'F': continue
                r = has(o, kind, nm, d)
                if r is False: mm[(kind, o, nm, d)] += 1
        res[os.path.basename(ad)] = dict(
            missing_classes={k: dict(n=n, candidates=bysimple.get(k.split('/')[-1], [])[:3]) for k, n in mc.items()},
            missing_members=[list(k)+[n] for k, n in sorted(mm.items())])
        print(os.path.basename(ad), 'missing classes', len(mc), 'missing members', len(mm))
    json.dump(res, open(out, 'w'), indent=1)
main()
