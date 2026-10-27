#!/usr/bin/env python3
"""Scan addon jars for com/simibubi/create refs; compare with the port jar. Output JSON per addon + global tables."""
import sys, json, glob, os, collections
sys.path.insert(0, os.path.dirname(__file__))
import cf
OLD, NEW = 'com/simibubi/create/', 'com/zurrtum/create/'

def port_index(jar):
    idx = {}
    for _, c in cf.jar_classes(jar):
        if c['name'].startswith(NEW):
            idx[c['name']] = c
    return idx

def main(port_jar, out, *addons):
    port = port_index(port_jar)
    # member lookup walking supers within port
    def has(owner, kind, name, desc, seen=None):
        c = port.get(owner)
        if not c: return None
        ms = c['methods'] if kind == 'M' else c['fields']
        if any(m[0] == name and m[1] == desc for m in ms): return True
        if any(m[0] == name for m in ms): return 'desc'
        for s in [c['super']] + c['ifs']:
            if s and s in port:
                r = has(s, kind, name, desc)
                if r: return r
        return False
    res = {}
    for a in addons:
        refs = collections.Counter(); classes = collections.Counter()
        n = 0
        for _, c in cf.jar_classes(a):
            n += 1
            for k in c['classes'] + [c['super'] or ''] + c['ifs']:
                if k.startswith(OLD): classes[k] += 1
            for kind, o, nm, d in c['refs']:
                if o.startswith(OLD): refs[(kind, o, nm, d)] += 1
        miss_c = sorted(k for k in classes if NEW + k[len(OLD):] not in port)
        miss_m = []
        for (kind, o, nm, d), cnt in refs.items():
            no = NEW + o[len(OLD):]
            if no not in port: continue
            r = has(no, kind, nm, d.replace(OLD, NEW))
            if r is not True and nm not in ('<init>',) or (nm == '<init>' and r is not True):
                miss_m.append([kind, o, nm, d, r, cnt])
        res[os.path.basename(a)] = dict(classes=n, simibubi_classes=len(classes), missing_classes=miss_c,
                                        refs=len(refs), missing_members=sorted(miss_m))
        print(os.path.basename(a), 'cls', n, 'create-classes', len(classes), 'missing classes', len(miss_c), 'missing members', len(miss_m))
    json.dump(res, open(out, 'w'), indent=1)
if __name__ == '__main__': main(sys.argv[1], sys.argv[2], *sys.argv[3:])
