"""Minimal NBT reader/writer (gzip) used to drop hanging entities from structure templates, whose saved positions do not survive the 26.1 data fixers."""
import gzip, struct, sys, os, glob

class T:  # tagged value
    def __init__(self, t, v, et=None): self.t, self.v, self.et = t, v, et

def rd_payload(b, o, t):
    if t == 1: return T(1, b[o]), o + 1
    if t == 2: return T(2, b[o:o+2]), o + 2
    if t == 3: return T(3, b[o:o+4]), o + 4
    if t == 4: return T(4, b[o:o+8]), o + 8
    if t == 5: return T(5, b[o:o+4]), o + 4
    if t == 6: return T(6, b[o:o+8]), o + 8
    if t == 7:
        n = struct.unpack(">i", b[o:o+4])[0]; return T(7, b[o+4:o+4+n]), o + 4 + n
    if t == 8:
        n = struct.unpack(">H", b[o:o+2])[0]; return T(8, b[o+2:o+2+n]), o + 2 + n
    if t == 9:
        et = b[o]; n = struct.unpack(">i", b[o+1:o+5])[0]; o += 5; out = []
        for _ in range(n):
            v, o = rd_payload(b, o, et); out.append(v)
        return T(9, out, et), o
    if t == 10:
        d = {}
        while b[o] != 0:
            tt = b[o]; n = struct.unpack(">H", b[o+1:o+3])[0]; name = b[o+3:o+3+n]; o += 3 + n
            v, o = rd_payload(b, o, tt); d[name] = v
        return T(10, d), o + 1
    if t == 11:
        n = struct.unpack(">i", b[o:o+4])[0]; return T(11, b[o+4:o+4+4*n]), o + 4 + 4 * n
    if t == 12:
        n = struct.unpack(">i", b[o:o+4])[0]; return T(12, b[o+4:o+4+8*n]), o + 4 + 8 * n
    raise ValueError(t)

def wr_payload(t, x):
    if t in (1,): return bytes([x.v])
    if t in (2, 3, 4, 5, 6): return x.v
    if t == 7: return struct.pack(">i", len(x.v)) + x.v
    if t == 8: return struct.pack(">H", len(x.v)) + x.v
    if t == 9: return bytes([x.et]) + struct.pack(">i", len(x.v)) + b"".join(wr_payload(x.et, e) for e in x.v)
    if t == 10:
        out = b""
        for k, v in x.v.items(): out += bytes([v.t]) + struct.pack(">H", len(k)) + k + wr_payload(v.t, v)
        return out + b"\0"
    if t == 11: return struct.pack(">i", len(x.v) // 4) + x.v
    if t == 12: return struct.pack(">i", len(x.v) // 8) + x.v

def load(path):
    b = gzip.decompress(open(path, "rb").read())
    n = struct.unpack(">H", b[1:3])[0]
    root, _ = rd_payload(b, 3 + n, 10)
    return b[:3 + n], root

def save(path, head, root):
    open(path, "wb").write(gzip.compress(head + wr_payload(10, root)))

HANG = (b"minecraft:item_frame", b"minecraft:glow_item_frame", b"minecraft:painting", b"minecraft:leash_knot")
def strip(path):
    head, root = load(path)
    ents = root.v.get(b"entities")
    if not ents: return 0
    keep, drop = [], 0
    for e in ents.v:
        nbt = e.v.get(b"nbt")
        eid = nbt.v.get(b"id").v if nbt and b"id" in nbt.v else b""
        if eid in HANG: drop += 1
        else: keep.append(e)
    if drop:
        ents.v = keep; save(path, head, root)
    return drop

if __name__ == "__main__":
    total = 0
    for f in glob.glob(sys.argv[1] + "/**/*.nbt", recursive=True): total += strip(f)
    print("dropped hanging entities:", total)
