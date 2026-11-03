"""Minimal RCON client: python3 -I tools/rcon.py PORT PASSWORD 'cmd' ['cmd' ...]"""
import socket, struct, sys

def pkt(s, i, t, body):
    d = struct.pack("<ii", i, t) + body.encode() + b"\0\0"
    s.sendall(struct.pack("<i", len(d)) + d)

def rd(s):
    n = struct.unpack("<i", s.recv(4))[0]
    d = b""
    while len(d) < n:
        d += s.recv(n - len(d))
    return struct.unpack("<ii", d[:8]), d[8:-2].decode(errors="replace")

s = socket.create_connection(("127.0.0.1", int(sys.argv[1])))
pkt(s, 1, 3, sys.argv[2]); rd(s)
for c in sys.argv[3:]:
    pkt(s, 2, 2, c)
    print(c, "->", rd(s)[1])
