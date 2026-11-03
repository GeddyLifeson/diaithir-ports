#!/usr/bin/env python3
"""Planet lairs: one jigsaw structure (a ring of stone with a boss altar) that generates in every planet dimension.
The altar is left without a boss; AltarBlockEntity picks one of the planet's bosses from the dimension and the altar position.
Usage: gen_planet_lairs.py <planets.tsv> <resources dir>"""
import gzip, json, os, struct, sys

planets_tsv, out = sys.argv[1:3]
D = f"{out}/data/diaithirbosses"


# ---- minimal NBT (compound / list / int / string / byte / short only, what a structure template needs)
def s(v): b = v.encode(); return struct.pack(">H", len(b)) + b
def tag(t, name, payload): return bytes([t]) + s(name) + payload
def i(v): return struct.pack(">i", v)
def lst(t, items): return bytes([t]) + i(len(items)) + b"".join(items)
def comp(**kw): return b"".join(kw_items(kw)) + b"\0"
def kw_items(kw): return [k for k in kw.values()]


def compound(entries):  # list of (type, name, payload)
    return b"".join(tag(t, n, p) for t, n, p in entries) + b"\0"


def read_dataversion(path):
    raw = gzip.open(path).read()
    k = raw.find(b"DataVersion")
    return struct.unpack(">i", raw[k + 11:k + 15])[0]


R = 7  # ring radius; the structure is (2R+1)^2 wide
palette, blocks = [], []
def pid(name, props=None):
    key = (name, tuple(sorted((props or {}).items())))
    for n, p in enumerate(palette):
        if p == key: return n
    palette.append(key); return len(palette) - 1


floor = pid("minecraft:stone_bricks"); mossy = pid("minecraft:mossy_stone_bricks"); crack = pid("minecraft:cracked_stone_bricks")
pillar = pid("minecraft:chiseled_stone_bricks"); lantern = pid("minecraft:soul_lantern", {"hanging": "false"}); altar = pid("diaithirbosses:boss_altar")
for x in range(2 * R + 1):
    for z in range(2 * R + 1):
        dx, dz = x - R, z - R
        d2 = dx * dx + dz * dz
        if d2 > R * R + 2: continue
        sel = floor if (x * 7 + z * 3) % 5 else (mossy if (x + z) % 2 else crack)
        blocks.append(((x, 0, z), sel, None))
for dx, dz in ((-5, -5), (5, -5), (-5, 5), (5, 5)):
    for h in (1, 2, 3):
        blocks.append(((R + dx, h, R + dz), pillar, None))
    blocks.append(((R + dx, 4, R + dz), lantern, None))
blocks.append(((R, 1, R), altar, ("boss", "")))

dv = read_dataversion(sys.argv[3]) if len(sys.argv) > 3 else 4800
pal_items = []
for name, props in palette:
    e = [(8, "Name", s(name))]
    if props: e.append((10, "Properties", compound([(8, k, s(v)) for k, v in props])))
    pal_items.append(compound(e))
blk_items = []
for pos, st, be in blocks:
    e = [(9, "pos", lst(3, [i(c) for c in pos])), (3, "state", i(st))]
    if be: e.append((10, "nbt", compound([(8, "id", s("diaithirbosses:boss_altar")), (8, "boss", s(be[1])), (4, "defeated_at", struct.pack(">q", -1)), (3, "defeats", i(0))])))
    blk_items.append(compound(e))
root = compound([(3, "DataVersion", i(dv)), (9, "size", lst(3, [i(2 * R + 1), i(5), i(2 * R + 1)])), (9, "palette", lst(10, pal_items)),
                 (9, "blocks", lst(10, blk_items)), (9, "entities", lst(10, []))])
os.makedirs(f"{D}/structure", exist_ok=True)
with gzip.GzipFile(f"{D}/structure/planet_lair.nbt", "wb", mtime=0) as f:
    f.write(b"\x0a" + s("") + root)

ids = [ln.split("\t")[0] for ln in open(planets_tsv, encoding="utf8").read().splitlines()[1:] if ln.split("\t")[0] != "diaithir"]
w = lambda p, o: (os.makedirs(os.path.dirname(f"{D}/{p}"), exist_ok=True), json.dump(o, open(f"{D}/{p}", "w"), indent=1))
w("tags/worldgen/biome/planet_lair_biomes.json", {"values": [{"id": f"#diaithir_planets:{p}", "required": False} for p in ids]})
w("worldgen/structure/planet_lair.json", {"type": "minecraft:jigsaw", "biomes": "#diaithirbosses:planet_lair_biomes", "step": "surface_structures",
   "spawn_overrides": {}, "terrain_adaptation": "beard_thin", "start_pool": "diaithirbosses:planet_lair/start", "size": 1,
   "start_height": {"absolute": 0}, "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 80, "use_expansion_hack": False})
w("worldgen/structure_set/planet_lair.json", {"structures": [{"structure": "diaithirbosses:planet_lair", "weight": 1}],
   "placement": {"type": "minecraft:random_spread", "spacing": 18, "separation": 7, "salt": 90311}})
w("worldgen/template_pool/planet_lair/start.json", {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {
   "element_type": "minecraft:single_pool_element", "location": "diaithirbosses:planet_lair", "processors": "minecraft:empty", "projection": "rigid"}}]})
print("lair for", len(ids), "planets, DataVersion", dv)
