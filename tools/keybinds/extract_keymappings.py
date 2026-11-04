#!/usr/bin/env python3
"""Static KeyMapping extractor. Usage: extract_keymappings.py JAR_DIR OUT.csv
Scans every class that references net/minecraft/client/KeyMapping (jars and jar-in-jar),
disassembles with javap and records each `new KeyMapping(...)`: name, default key,
conflict context, modifier, category. Dynamic args (iload) are marked '?'.
"""
import sys, os, re, io, zipfile, subprocess, tempfile, csv, concurrent.futures as cf

KEY = {}
def glfw_names():
    # GLFW keycodes: letters, digits, F-keys, common
    d = {}
    for i in range(26): d[65+i] = chr(65+i)
    for i in range(10): d[48+i] = str(i)
    for i in range(25): d[290+i] = f"F{i+1}"
    d.update({32:'SPACE',39:'APOSTROPHE',44:'COMMA',45:'MINUS',46:'PERIOD',47:'SLASH',59:'SEMICOLON',61:'EQUAL',
      91:'LBRACKET',92:'BACKSLASH',93:'RBRACKET',96:'GRAVE',256:'ESCAPE',257:'ENTER',258:'TAB',259:'BACKSPACE',
      260:'INSERT',261:'DELETE',262:'RIGHT',263:'LEFT',264:'DOWN',265:'UP',266:'PAGE_UP',267:'PAGE_DOWN',268:'HOME',269:'END',
      280:'CAPS_LOCK',281:'SCROLL_LOCK',282:'NUM_LOCK',283:'PRINT_SCREEN',284:'PAUSE',
      320:'KP_0',321:'KP_1',322:'KP_2',323:'KP_3',324:'KP_4',325:'KP_5',326:'KP_6',327:'KP_7',328:'KP_8',329:'KP_9',
      330:'KP_DECIMAL',331:'KP_DIVIDE',332:'KP_MULTIPLY',333:'KP_SUBTRACT',334:'KP_ADD',335:'KP_ENTER',
      340:'LSHIFT',341:'LCTRL',342:'LALT',343:'LSUPER',344:'RSHIFT',345:'RCTRL',346:'RALT',347:'RSUPER',348:'MENU',-1:'UNKNOWN'})
    return d
KEYS = glfw_names()

def classes(zf, label, depth=0):
    for n in zf.namelist():
        if n.endswith('.class') and not n.startswith('META-INF/versions'):
            yield label, n, zf.read(n)
        elif n.endswith('.jar') and depth < 3:
            try: yield from classes(zipfile.ZipFile(io.BytesIO(zf.read(n))), label + '!' + os.path.basename(n), depth+1)
            except zipfile.BadZipFile: pass

NEW = re.compile(r'new\s+#\d+\s+// class net/minecraft/client/KeyMapping\b')
INIT = re.compile(r'invokespecial.*net/minecraft/client/KeyMapping\."<init>"')
OP = re.compile(r'^\s*\d+:\s+(\S+)\s*(.*)$')

def parse(text, jar, cls):
    out = []
    method = ''
    stack = None
    for line in text.splitlines():
        if line.startswith('  ') and not line.startswith('   ') and '(' in line and line.rstrip().endswith(';'):
            method = line.strip()
        m = OP.match(line)
        if not m: continue
        op, rest = m.groups()
        if NEW.search(line):
            stack = []
            continue
        if stack is None: continue
        if INIT.search(line):
            out.append(dict(jar=jar, cls=cls, method=method, args=stack, init=rest))
            stack = None; continue
        c = rest.split('//', 1)[1].strip() if '//' in rest else ''
        if op.startswith('iconst_'): stack.append(('int', int(op[7:].replace('m1', '-1'))))
        elif op in ('bipush', 'sipush'): stack.append(('int', int(rest.split()[0])))
        elif op in ('ldc', 'ldc_w'): stack.append(('str' if c.startswith('String') else 'int' if c.startswith('int') else 'other', c.split(' ', 1)[1] if ' ' in c else c))
        elif op == 'getstatic': stack.append(('field', c.replace('Field ', '')))
        elif op in ('iload', 'iload_0', 'iload_1', 'iload_2', 'iload_3', 'aload', 'aload_0', 'aload_1', 'aload_2', 'aload_3'): stack.append(('dyn', op))
        elif op == 'invokedynamic': stack.append(('dyn', 'concat'))
        elif op.startswith('invoke'): stack.append(('call', c))
        elif op == 'getfield': stack.append(('field', c))
    return out

def summarize(rec):
    name = key = ctx = mod = cat = ''
    keyval = None
    for k, v in rec['args']:
        if k == 'str' and not name: name = v.strip('"')
        elif k == 'int' and keyval is None: keyval = int(v)
        elif k == 'field':
            if 'KeyConflictContext' in v: ctx = v.split('.')[-1].split(':')[0]
            elif 'KeyModifier' in v: mod = v.split('.')[-1].split(':')[0]
            elif 'KeyMapping$Category' in v or 'Category' in v: cat = v.split(':')[0]
            elif 'InputConstants$Type' in v and keyval is None: pass
        elif k == 'call' and 'KeyConflictContext' in v: ctx = 'dyn'
    key = KEYS.get(keyval, str(keyval)) if keyval is not None else '?'
    return [rec['jar'], rec['cls'], name or '?', key, ctx or 'UNIVERSAL(default)', mod or 'NONE', cat or '?', rec['method']]

def scan_jar(path):
    rows = []
    try: zf = zipfile.ZipFile(path)
    except Exception: return rows
    for label, n, data in classes(zf, os.path.basename(path)):
        if b'net/minecraft/client/KeyMapping' not in data: continue
        with tempfile.TemporaryDirectory() as td:
            p = os.path.join(td, 'C.class'); open(p, 'wb').write(data)
            r = subprocess.run(['javap', '-c', '-p', p], capture_output=True, text=True)
        for rec in parse(r.stdout, label, n):
            rows.append(summarize(rec))
    return rows

if __name__ == '__main__':
    d, outp = sys.argv[1], sys.argv[2]
    jars = sorted(os.path.join(d, f) for f in os.listdir(d) if f.endswith('.jar'))
    with cf.ThreadPoolExecutor(4) as ex: res = list(ex.map(scan_jar, jars))
    with open(outp, 'w', newline='') as f:
        w = csv.writer(f); w.writerow(['jar', 'class', 'name', 'default_key', 'context', 'modifier', 'category', 'method'])
        for r in res: w.writerows(r)
    print(sum(len(r) for r in res), 'keymappings')
