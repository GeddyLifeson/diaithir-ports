"""Rewrite old block and item ids in a world to the Diaithir replacements. Offline; stop the server first.
Usage: python3 -I tools/migrate_world.py WORLD_DIR MIGRATIONS... [--dry-run]
MIGRATIONS are any number of id_migrations*.json files or folders (searched recursively), so one pass covers every module.
Each file holds old->new id pairs under "map" or "items".
Needs: pip install nbtlib. NeoForge 26.1 has no registry-alias hook, so old ids would load as air; this renames them in region
chunks (block palettes, block entities, containers), entity chunks and player data. Unmapped ids are left alone."""
import io, json, struct, sys, zlib, gzip
from pathlib import Path
import nbtlib
from nbtlib import tag

from collections import Counter
SEEN = Counter()

def load_map(paths):
    files = []
    for p in map(Path, paths):
        files += sorted(p.rglob("id_migrations*.json")) if p.is_dir() else [p]
    merged = {}
    for f in files:
        d = json.load(open(f))
        pairs = d.get("map") or d.get("items") or {}
        merged.update({k: v for k, v in pairs.items() if v and k != v})
        print(f"loaded {f} ({len(pairs)} ids)")
    return merged

def walk(t, m, counter):
    if isinstance(t, tag.String):
        new = m.get(str(t))
        if new:
            counter[0] += 1
            SEEN[str(t).split(':')[0]] += 1
            return tag.String(new)
        return t
    if isinstance(t, tag.Compound):
        for k in list(t.keys()):
            t[k] = walk(t[k], m, counter)
        return t
    if isinstance(t, tag.List):
        for i in range(len(t)):
            t[i] = walk(t[i], m, counter)
        return t
    return t

def read_chunks(data):
    for i in range(1024):
        off = struct.unpack(">I", b"\0" + data[i * 4:i * 4 + 3])[0]
        cnt = data[i * 4 + 3]
        if not off or not cnt:
            continue
        pos = off * 4096
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        comp = data[pos + 4]
        raw = data[pos + 5:pos + 4 + length]
        if comp == 2:
            raw = zlib.decompress(raw)
        elif comp == 1:
            raw = gzip.decompress(raw)
        elif comp != 3:
            raise SystemExit(f"unsupported chunk compression {comp}")
        yield i, struct.unpack(">I", data[4096 + i * 4:4100 + i * 4])[0], raw

def write_region(chunks):
    header, stamps, body = bytearray(4096), bytearray(4096), bytearray()
    sector = 2
    for i, ts, raw in chunks:
        comp = zlib.compress(raw)
        rec = struct.pack(">IB", len(comp) + 1, 2) + comp
        pad = (-len(rec)) % 4096
        n = (len(rec) + pad) // 4096
        header[i * 4:i * 4 + 4] = struct.pack(">I", sector)[1:] + bytes([n])
        stamps[i * 4:i * 4 + 4] = struct.pack(">I", ts)
        body += rec + b"\0" * pad
        sector += n
    return bytes(header) + bytes(stamps) + bytes(body)

def parse_nbt(raw):
    f = nbtlib.File.parse(io.BytesIO(raw))
    return f

def dump_nbt(f):
    b = io.BytesIO()
    f.write(b)
    return b.getvalue()

def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    world, mp = Path(args[0]), load_map(args[1:])
    dry = "--dry-run" in sys.argv
    total = 0
    dirs = [p for d in ("region", "entities") for p in list(world.glob(f"dimensions/*/*/{d}")) + list(world.glob(f"DIM*/{d}")) + [world / d]]
    for sub in dirs:
        for mca in sorted(sub.glob("r.*.mca")) if sub.exists() else []:
            data = mca.read_bytes(); out = []; c = [0]
            if len(data) < 8192:
                continue
            for i, ts, raw in read_chunks(data):
                f = parse_nbt(raw)
                walk(f, mp, c)
                out.append((i, ts, dump_nbt(f)))
            if c[0] and not dry:
                mca.write_bytes(write_region(out))
            total += c[0]; print(mca, c[0])
    for dat in list((world / "playerdata").glob("*.dat")) + [world / "level.dat"]:
        if dat.exists():
            f = nbtlib.load(dat); c = [0]; walk(f, mp, c)
            if c[0] and not dry:
                f.save(dat)
            total += c[0]; print(dat, c[0])
    print("renamed", total, "ids" if not dry else "ids (dry run)")
    print("by old namespace:", dict(SEEN.most_common()))

if __name__ == "__main__":
    main()
