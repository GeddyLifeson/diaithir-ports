"""Summons every item of the haulage pack on a running server (rcon) and reports ids that resolve to Air (unknown item).
Usage: python3 -I tools/ivboot_summon.py PORT PASSWORD <module dir>"""
import json, socket, struct, sys
port, pw, M = int(sys.argv[1]), sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else '.'
man = json.load(open(f'{M}/truck_manifest.json'))
ids = list(json.load(open(f'{M}/names_map_trucks.json')))
for v in man['tractors'] + man['trailers']: ids += ['mts:diaithirtrucks.' + v['id'] + s for s in ('_white', '_blue')]
for v in man['construction']: ids += ['mts:diaithirtrucks.' + v['id'] + s for s in ('_orange', '_white')]
s = socket.create_connection(('127.0.0.1', port))
def pkt(i, t, b):
    d = struct.pack('<ii', i, t) + b.encode() + b'\0\0'; s.sendall(struct.pack('<i', len(d)) + d)
def rd():
    n = struct.unpack('<i', s.recv(4))[0]; d = b''
    while len(d) < n: d += s.recv(n - len(d))
    return d[8:-2].decode(errors='replace')
pkt(1, 3, pw); rd()
bad = []
for i in ids:
    pkt(2, 2, 'summon item ~ ~ ~ {Item:{id:"%s",count:1}}' % i)
    if 'Air' in rd(): bad.append(i)
print('items', len(ids), 'resolved', len(ids) - len(bad), 'unknown', len(bad)); print(bad[:10])
