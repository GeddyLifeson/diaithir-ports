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

def remap_jar(zi, zo, rx, sub):
    n = 0
    if True:
        for it in zi.infolist():
            d = zi.read(it.filename)
            if it.filename.endswith('.class') and not it.filename.startswith('META-INF/versions'):
                d2 = remap_class(d, rx, sub); n += d2 != d; d = d2
            elif it.filename.endswith(('.sf', '.rsa', '.dsa')) and it.filename.startswith('META-INF/'): continue
            elif it.filename.endswith(('.json')) and ('mixins' in it.filename or 'mixin' in it.filename):
                d = rx.sub(sub, d.decode('utf8', 'replace')).encode()
            elif it.filename.startswith(('META-INF/jarjar/', 'META-INF/jars/')) and it.filename.endswith('.jar'):
                bio = io.BytesIO()
                with zipfile.ZipFile(io.BytesIO(d)) as zin, zipfile.ZipFile(bio, 'w', zipfile.ZIP_DEFLATED) as zout: remap_jar(zin, zout, rx, sub)
                d = bio.getvalue()
            elif it.filename == 'META-INF/neoforge.mods.toml':
                d = fix_toml(d.decode()).encode()
            zo.writestr(it.filename, d)
    return n

def main(inp, outp, mp, *flags):
    m = json.load(open(mp))['classes']
    both = dict(m); both.update({k.replace('/', '.'): v.replace('/', '.') for k, v in m.items()})
    rx, sub = build_rx(both)
    with zipfile.ZipFile(inp) as zi, zipfile.ZipFile(outp, 'w', zipfile.ZIP_DEFLATED) as zo: n = remap_jar(zi, zo, rx, sub)
    print(os.path.basename(inp), 'classes rewritten', n)
if __name__ == '__main__': main(*sys.argv[1:])
