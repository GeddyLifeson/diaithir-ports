#!/usr/bin/env python3
"""Minimal RCON client. usage: rcon.py PORT PASSWORD 'cmd' ['cmd' ...]"""
import socket, struct, sys, time
def run(port, pw, cmds, host='127.0.0.1'):
    s = socket.create_connection((host, port), timeout=60)
    def send(i, t, body):
        b = struct.pack('<ii', i, t) + body.encode() + b'\0\0'
        s.sendall(struct.pack('<i', len(b)) + b)
    def recv():
        n = struct.unpack('<i', s.recv(4))[0]
        d = b''
        while len(d) < n: d += s.recv(n - len(d))
        return struct.unpack('<ii', d[:8]) + (d[8:-2].decode(errors='replace'),)
    send(1, 3, pw); recv()
    out = []
    for c in cmds:
        send(2, 2, c); out.append(recv()[2])
    s.close(); return out
if __name__ == '__main__':
    for c, o in zip(sys.argv[3:], run(int(sys.argv[1]), sys.argv[2], sys.argv[3:])): print('>', c, '\n', o)
