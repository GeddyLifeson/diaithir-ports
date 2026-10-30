"""Tiny RCON client: python3 -I tools/rcon.py <port> <command> [<command> ...]  (password x)."""
import socket, struct, sys

def pkt(i, t, body):
    b = body.encode()
    return struct.pack("<iii", 10 + len(b), i, t) + b + b"\0\0"

def rd(s):
    n = struct.unpack("<i", s.recv(4))[0]
    d = b""
    while len(d) < n: d += s.recv(n - len(d))
    return struct.unpack("<ii", d[:8]), d[8:-2].decode(errors="replace")

def run(port, cmds, pw="x"):
    s = socket.create_connection(("127.0.0.1", port), timeout=30)
    s.sendall(pkt(1, 3, pw)); rd(s)
    out = []
    for c in cmds:
        s.sendall(pkt(2, 2, c)); out.append(rd(s)[1])
    s.close()
    return out

if __name__ == "__main__":
    for c, o in zip(sys.argv[2:], run(int(sys.argv[1]), sys.argv[2:])): print(c, "->", o)
