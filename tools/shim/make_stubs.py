#!/usr/bin/env python3
"""Write a jar of empty public classes diaithir/shim/stub/<orig> for every class in map.json "stubs".
Keeps addon lambdas/signatures that mention removed 1.21.1 classes (mostly datagen) linkable; calling them still fails loudly.
Usage: make_stubs.py map.json out.jar"""
import sys, json, struct, zipfile
def cls(name):
    def u(s): b = s.encode(); return b'\x01' + struct.pack('>H', len(b)) + b
    cp = [u(name), b'\x07\x00\x01', u('java/lang/Object'), b'\x07\x00\x03']
    return (b'\xca\xfe\xba\xbe' + struct.pack('>HHH', 0, 65, len(cp) + 1) + b''.join(cp) +
            struct.pack('>HHHHHHH', 0x0021, 2, 4, 0, 0, 0, 0))
m = json.load(open(sys.argv[1]))
with zipfile.ZipFile(sys.argv[2], 'w', zipfile.ZIP_DEFLATED) as z:
    for k in m.get('stubs', []):
        n = 'diaithir/shim/stub/' + k
        z.writestr(n + '.class', cls(n))
    z.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\nFMLModType: GAMELIBRARY\n')
print(len(m.get('stubs', [])), 'stubs')
