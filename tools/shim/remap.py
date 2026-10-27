#!/usr/bin/env python3
"""Constant-pool remapper: rewrites class names (+ descriptors/signatures/ldc strings with dotted names) in every .class of a jar.
No ASM needed: class names live only in Utf8 entries, so renaming = rewriting Utf8 text. Member renames: --members members.json
({"owner#name#desc": "newname"}) applied to NameAndType via owner-aware pass (see apply_members).
Usage: remap.py in.jar out.jar map.json [--toml]"""
import sys, json, re, struct, zipfile, io, os

def remap_class(b, rx, sub, members=None):
    p = 10; n = struct.unpack('>H', b[8:10])[0]; out = [b[:10]]; i = 1
    while i < n:
        t = b[p]
        if t == 1:
            l = struct.unpack('>H', b[p+1:p+3])[0]; s = b[p+3:p+3+l]
            try:
                txt = s.decode('utf8'); new = rx.sub(sub, txt)
                if new != txt: s = new.encode('utf8')
            except UnicodeDecodeError: pass
            out.append(b'\x01' + struct.pack('>H', len(s)) + s); p += 3 + l
        elif t in (3, 4): out.append(b[p:p+5]); p += 5
        elif t in (5, 6): out.append(b[p:p+9]); p += 9; i += 1
        elif t in (7, 8, 16, 19, 20): out.append(b[p:p+3]); p += 3
        elif t in (9, 10, 11, 12, 17, 18): out.append(b[p:p+5]); p += 5
        elif t == 15: out.append(b[p:p+4]); p += 4
        else: raise ValueError(t)
        i += 1
    out.append(b[p:]); return b''.join(out)


REDIRECTS = {}
try: REDIRECTS = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'redirects.json')))
except OSError: pass

def load_table(port_jar):
    """owner -> fields/methods table from the port jar, for descriptor relaxation."""
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import cf
    T = {}
    for _, c in cf.jar_classes(port_jar):
        if c['name'].startswith('com/zurrtum/'):
            f = {n: d for n, d, a in c['fields']}
            m = {}
            for n, d, a in c['methods']:
                m.setdefault((n, d[:d.index(')') + 1]), set()).add(d)
            T[c['name']] = (f, m, [x for x in [c['super']] + c['ifs'] if x], bool(c['acc'] & 0x200))
    return T

def _lookup(T, owner, kind, key, seen=None):
    e = T.get(owner)
    if not e: return None
    if kind == 'F':
        if key in e[0]: return e[0][key]
    elif key in e[1]: return e[1][key]
    for sname in e[2]:
        r = _lookup(T, sname, kind, key)
        if r is not None: return r
    return None

def relax_members(b, T):
    """Where a Fieldref/Methodref names a port member that exists with the same name (and, for methods, same
    parameters) but a different type/return, point the ref at the port's real descriptor. Safe for narrower
    (covariant) types, which is how the port differs from upstream (e.g. AllCTTypes.RECTANGLE: CTType -> RectangleCTType)."""
    n = struct.unpack('>H', b[8:10])[0]
    ents = [None] * n; p = 10; i = 1
    while i < n:
        t = b[p]; st = p
        if t == 1:
            l = struct.unpack('>H', b[p+1:p+3])[0]; p += 3 + l; ents[i] = (1, b[st:p], b[st+3:p].decode('utf8', 'replace'))
        elif t in (3, 4): p += 5; ents[i] = (t, b[st:p])
        elif t in (5, 6): p += 9; ents[i] = (t, b[st:p]); i += 1
        elif t in (7, 8, 16, 19, 20): p += 3; ents[i] = (t, b[st:p], struct.unpack('>H', b[st+1:p])[0])
        elif t in (9, 10, 11, 12, 17, 18): p += 5; ents[i] = (t, b[st:p], *struct.unpack('>HH', b[st+1:p]))
        elif t == 15: p += 4; ents[i] = (t, b[st:p])
        i += 1
    tail = b[p:]
    new = []; nextidx = n; cache = {}
    def utf(txt):
        nonlocal nextidx
        if ('u', txt) not in cache:
            bb = txt.encode('utf8'); new.append(b'\x01' + struct.pack('>H', len(bb)) + bb); cache[('u', txt)] = nextidx; nextidx += 1
        return cache[('u', txt)]
    changed = 0
    for i, e in enumerate(ents):
        if not e or e[0] not in (9, 10, 11): continue
        owner = ents[ents[e[2]][2]][2]; nt = ents[e[3]]; name = ents[nt[2]][2]; desc = ents[nt[3]][2]
        red = REDIRECTS.get(owner + '#' + name)
        if red and e[0] in (10, 11):
            if ('c', red) not in cache:
                ui = utf(red); new.append(b'\x07' + struct.pack('>H', ui)); cache[('c', red)] = nextidx; nextidx += 1
            ents[i] = (10, b'\x0a' + struct.pack('>HH', cache[('c', red)], e[3]), cache[('c', red)], e[3]); changed += 1
            continue
        if owner not in T: continue
        if e[0] == 9:
            real = _lookup(T, owner, 'F', name)
            if real is None or real == desc: continue
        else:
            if name.startswith('<'): continue
            rets = _lookup(T, owner, 'M', (name, desc[:desc.index(')') + 1]))
            if not rets or desc in rets or len(rets) != 1: continue
            real = next(iter(rets))
        key = (name, real)
        if ('nt', key) not in cache:
            ni = utf(real); new.append(b'\x0c' + struct.pack('>HH', nt[2], ni)); cache[('nt', key)] = nextidx; nextidx += 1
        ents[i] = (e[0], e[0].to_bytes(1, 'big') + struct.pack('>HH', e[2], cache[('nt', key)]), e[2], cache[('nt', key)]); changed += 1
    if not changed: return b
    out = [b[:8], struct.pack('>H', nextidx)]
    for i, e in enumerate(ents):
        if e: out.append(e[1])
    out.extend(new); out.append(tail)
    return b''.join(out)


OPLEN = {}
for _op in range(256): OPLEN[_op] = 1
for _op in (0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xa9, 0xbc): OPLEN[_op] = 2
for _op in list(range(0x11, 0x12)) + [0x13, 0x14, 0x84, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7] + list(range(0x99, 0xa9)): OPLEN[_op] = 3
OPLEN[0xc5] = 4; OPLEN[0xb9] = 5; OPLEN[0xba] = 5; OPLEN[0xc8] = 5; OPLEN[0xc9] = 5

def iface_to_virtual(b, T):
    """invokeinterface on a method whose owner is a *class* in the port -> invokevirtual (+2 nops), Interface->Methodref."""
    n = struct.unpack('>H', b[8:10])[0]
    ents = [None] * n; p = 10; i = 1
    while i < n:
        t = b[p]; st = p
        if t == 1: l = struct.unpack('>H', b[p+1:p+3])[0]; p += 3 + l; ents[i] = (1, b[st+3:p].decode('utf8', 'replace'))
        elif t in (3, 4): p += 5
        elif t in (5, 6): p += 9; i += 1
        elif t in (7, 8, 16, 19, 20): p += 3; ents[i] = (t, struct.unpack('>H', b[st+1:p])[0])
        elif t in (9, 10, 11, 12, 17, 18): p += 5; ents[i] = (t, st, *struct.unpack('>HH', b[st+1:p]))
        elif t == 15: p += 4
        i += 1
    bad = {}
    for i, e in enumerate(ents):
        if e and e[0] == 11:
            owner = ents[ents[e[2]][1]][1]
            if owner in T and not T[owner][3]: bad[i] = e[1]
    if not bad: return b
    b = bytearray(b)
    for i, st in bad.items(): b[st] = 10
    # walk members to find Code attributes
    q = p + 6; ic = struct.unpack('>H', b[q:q+2])[0]; q += 2 + 2 * ic
    for _ in range(2):
        cnt = struct.unpack('>H', b[q:q+2])[0]; q += 2
        for _ in range(cnt):
            q += 6; ac = struct.unpack('>H', b[q:q+2])[0]; q += 2
            for _ in range(ac):
                ni, ln = struct.unpack('>HI', b[q:q+6]); q += 6
                if ents[ni] and ents[ni][1] == 'Code':
                    cl = struct.unpack('>I', b[q+4:q+8])[0]; c0 = q + 8; c1 = c0 + cl; k = c0
                    while k < c1:
                        op = b[k]
                        if op == 0xaa:
                            k2 = (k - c0 + 4) & ~3; k = c0 + k2; lo, hi = struct.unpack('>ii', b[k+4:k+12]); k += 12 + 4 * (hi - lo + 1); continue
                        if op == 0xab:
                            k2 = (k - c0 + 4) & ~3; k = c0 + k2; npairs = struct.unpack('>i', b[k+4:k+8])[0]; k += 8 + 8 * npairs; continue
                        if op == 0xc4:
                            k += 6 if b[k+1] == 0x84 else 4; continue
                        if op == 0xb9:
                            idx = struct.unpack('>H', b[k+1:k+3])[0]
                            if idx in bad: b[k] = 0xb6; b[k+3] = 0; b[k+4] = 0
                        k += OPLEN[op]
                q += ln
    return bytes(b)

def build_rx(classes):
    keys = sorted(classes, key=len, reverse=True)
    # match slash form and dotted form, only at class-name boundaries
    alt = '|'.join(re.escape(k) for k in keys)
    rx = re.compile(r'(?<![A-Za-z0-9_$/])(L?)(' + alt + r')(?![A-Za-z0-9_])')
    def sub(m): return m.group(1) + classes[m.group(2)]
    return rx, sub

def fix_toml(t):
    """Per [[dependencies.*]] block: create -> diaithir_create; relax minecraft/neoforge/create version ranges to '*'."""
    out, blk = [], []
    def flush():
        txt = ''.join(blk)
        m = re.search(r'^\s*modId\s*=\s*"([^"]+)"', txt, re.M)
        if m and m.group(1) in ('create', 'minecraft', 'neoforge'):
            txt = re.sub(r'^(\s*)modId\s*=\s*"create"', r'\1modId="diaithir_create"', txt, flags=re.M)
            txt = re.sub(r'^(\s*)versionRange\s*=\s*"[^"]*"', r'\1versionRange="*"', txt, flags=re.M)
        out.append(txt); blk.clear()
    for line in t.splitlines(True):
        if line.lstrip().startswith('['):
            flush()
        blk.append(line)
    flush()
    return ''.join(out)

def remap_jar(zi, zo, rx, sub, T=None):
    n = 0
    if True:
        for it in zi.infolist():
            d = zi.read(it.filename)
            if it.filename.endswith('.class') and not it.filename.startswith('META-INF/versions'):
                d2 = remap_class(d, rx, sub)
                if T: d2 = iface_to_virtual(relax_members(d2, T), T)
                n += d2 != d; d = d2
            elif it.filename.endswith(('.sf', '.rsa', '.dsa')) and it.filename.startswith('META-INF/'): continue
            elif it.filename.endswith(('.json')) and ('mixins' in it.filename or 'mixin' in it.filename):
                d = rx.sub(sub, d.decode('utf8', 'replace')).encode()
            elif it.filename.startswith(('META-INF/jarjar/', 'META-INF/jars/')) and it.filename.endswith('.jar'):
                bio = io.BytesIO()
                with zipfile.ZipFile(io.BytesIO(d)) as zin, zipfile.ZipFile(bio, 'w', zipfile.ZIP_DEFLATED) as zout: remap_jar(zin, zout, rx, sub, T)
                d = bio.getvalue()
            elif it.filename == 'META-INF/neoforge.mods.toml':
                d = fix_toml(d.decode()).encode()
            zo.writestr(it.filename, d)
    return n

def main(inp, outp, mp, *flags):
    m = json.load(open(mp))['classes']
    both = dict(m); both.update({k.replace('/', '.'): v.replace('/', '.') for k, v in m.items()})
    rx, sub = build_rx(both)
    pj = os.environ.get('SHIM_PORT_JAR')
    T = load_table(pj) if pj else None
    with zipfile.ZipFile(inp) as zi, zipfile.ZipFile(outp, 'w', zipfile.ZIP_DEFLATED) as zo: n = remap_jar(zi, zo, rx, sub, T)
    print(os.path.basename(inp), 'classes rewritten', n)
if __name__ == '__main__': main(*sys.argv[1:])
