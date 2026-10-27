"""Minimal classfile parser: constant pool refs + declared members. No deps."""
import struct, zipfile

def parse(b):
    p = 10
    n = struct.unpack('>H', b[8:10])[0]
    cp = [None] * n
    i = 1
    while i < n:
        t = b[p]; p += 1
        if t == 1:
            l = struct.unpack('>H', b[p:p+2])[0]; cp[i] = ('u', b[p+2:p+2+l].decode('utf8', 'replace')); p += 2 + l
        elif t in (3, 4): cp[i] = ('n',); p += 4
        elif t in (5, 6): cp[i] = ('n',); p += 8; i += 1
        elif t in (7, 8, 16, 19, 20): cp[i] = (t, struct.unpack('>H', b[p:p+2])[0]); p += 2
        elif t in (9, 10, 11, 12, 17, 18): cp[i] = (t, *struct.unpack('>HH', b[p:p+4])); p += 4
        elif t == 15: cp[i] = (t, b[p], struct.unpack('>H', b[p+1:p+3])[0]); p += 3
        else: raise ValueError(t)
        i += 1
    u = lambda k: cp[k][1]
    acc, this, sup, ic = struct.unpack('>HHHH', b[p:p+8]); p += 8
    ifs = [u(cp[x][1]) for x in struct.unpack('>%dH' % ic, b[p:p+2*ic])]; p += 2 * ic
    def members():
        nonlocal p
        c = struct.unpack('>H', b[p:p+2])[0]; p += 2
        out = []
        for _ in range(c):
            a, ni, di, ac = struct.unpack('>HHHH', b[p:p+8]); p += 8
            out.append((u(ni), u(di), a))
            for _ in range(ac):
                _n, l = struct.unpack('>HI', b[p:p+6]); p += 6 + l
        return out
    fields = members(); methods = members()
    refs = []
    for e in cp:
        if e and e[0] in (9, 10, 11):
            owner = u(cp[e[1]][1]); nt = cp[e[2]]
            refs.append(({9: 'F', 10: 'M', 11: 'M'}[e[0]], owner, u(nt[1]), u(nt[2])))
    classes = [u(e[1]) for e in cp if e and e[0] == 7]
    utf = [e[1] for e in cp if e and e[0] == 'u']
    return dict(name=u(cp[this][1]), super=u(cp[sup][1]) if sup else None, ifs=ifs,
                fields=fields, methods=methods, refs=refs, classes=classes, utf=utf, acc=acc)

def jar_classes(path):
    with zipfile.ZipFile(path) as z:
        for n in z.namelist():
            if n.endswith('.class') and not n.startswith('META-INF/'):
                try: yield n, parse(z.read(n))
                except Exception as e: pass
