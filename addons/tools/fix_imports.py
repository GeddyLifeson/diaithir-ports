#!/usr/bin/env python3
"""Rewrite imports of old Create/Flywheel/Catnip/Ponder classes to where the Diaithir Create port keeps them.
Usage: fix_imports.py ADDON_SRC_DIR"""
import re, sys, pathlib, collections
CREATE = pathlib.Path('/home/user/work-1a/src/main/java')
idx = collections.defaultdict(list)
for p in CREATE.rglob('*.java'):
    rel = p.relative_to(CREATE).with_suffix('')
    fq = '.'.join(rel.parts)
    idx[rel.parts[-1]].append(fq)
OLD = ('com.zurrtum.create.', 'dev.engine_room.', 'net.createmod.', 'com.simibubi.create.')
IMP = re.compile(r'^import (static )?((?:com\.zurrtum\.create|dev\.engine_room|net\.createmod|com\.simibubi\.create)[\w.]*?)\.([A-Z]\w*)((?:\.[\w*]+)*);', re.M)
missing = collections.Counter()
def fix(s):
    def rep(m):
        st, pkg, cls, rest = m.groups()
        st = st or ''
        full = f'{pkg}.{cls}'
        # exists as is?
        if (CREATE / (full.replace('.', '/') + '.java')).exists():
            return m.group(0)
        cands = idx.get(cls, [])
        if not cands:
            missing[full] += 1
            return m.group(0)
        if len(cands) > 1:
            # prefer longest common package prefix
            def score(c):
                a, b = c.split('.'), full.split('.')
                n = 0
                for x, y in zip(a, b):
                    if x != y: break
                    n += 1
                return n
            cands = sorted(cands, key=score, reverse=True)
        return f'import {st}{cands[0]}{rest};'
    return IMP.sub(rep, s)
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_text(); o = s
    s = fix(s)
    if s != o: p.write_text(s)
for k, v in missing.most_common(): print(v, k)
