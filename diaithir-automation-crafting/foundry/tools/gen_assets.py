#!/usr/bin/env python3
"""Writes the 64 px Industrial Factory art, models, blockstates, item definitions, lang, recipes, age gate, machine stats
and the Age research datapack for the Foundry parts of diaithir-automation-crafting. All art is drawn here from scratch
(worn dark steel, rivets, hazard bands, amber lamps); no Factorio art is used or traced.

Usage: gen_assets.py [path/to/graph.json]   (default /mnt/project-files/plans/progression/graph.json)"""
import json, os, random, sys
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
FOUNDRY = os.path.normpath(os.path.join(HERE, ".."))
ROOT = os.path.join(FOUNDRY, "src", "main", "resources")
NS = "diaithircrafting"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
DP = os.path.join(FOUNDRY, "datapack", "foundry_ages")
GRAPH = sys.argv[1] if len(sys.argv) > 1 else "/mnt/project-files/plans/progression/graph.json"
for d in ("textures/block", "textures/item", "models/block", "models/item", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)
os.makedirs(os.path.join(DP, "data", "foundry", "research"), exist_ok=True)

S = 64
STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE, RIVET, YEL, BLK, AMBER, DARK = (30, 31, 34), (130, 134, 140), (240, 190, 30), (28, 28, 28), (255, 170, 40), (14, 17, 21)
PACK_COL = [(196, 160, 110), (170, 120, 70), (70, 150, 200), (80, 190, 110), (170, 90, 200), (220, 80, 70), (240, 235, 200)]
rnd = random.Random(73)


def plate():
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            base = STEEL[rnd.choice((0, 0, 1, 1, 3))]
            n = rnd.randint(-4, 4)
            px[x, y] = tuple(max(0, min(255, c + n)) for c in base) + (255,)
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, S - 1, S - 1], outline=EDGE + (255,), width=2)
    d.rectangle([2, 2, S - 3, S - 3], outline=STEEL[2] + (255,), width=1)
    for cx, cy in ((6, 6), (57, 6), (6, 57), (57, 57)):
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=RIVET + (255,), outline=EDGE + (255,))
    return im


def lamp(d, cx, cy, col):
    d.ellipse([cx - 4, cy - 4, cx + 4, cy + 4], fill=EDGE + (255,))
    d.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=col + (255,))
    d.ellipse([cx - 1, cy - 2, cx + 1, cy], fill=(255, 235, 170, 255))


def hazard(im, y0, y1):
    d = ImageDraw.Draw(im)
    for y in range(y0, y1):
        for x in range(4, S - 4):
            d.point((x, y), fill=(YEL if ((x + y) // 4) % 2 == 0 else BLK) + (255,))


def block(name, im):
    im.save(os.path.join(A, "textures", "block", name + ".png"))


# Operator Seat: a bolted steel chair; the side lamp is amber when staffed, dark when not
top = plate()
d = ImageDraw.Draw(top)
d.rectangle([14, 14, 49, 49], fill=STEEL[3] + (255,), outline=EDGE + (255,), width=2)
d.rectangle([18, 18, 45, 33], fill=STEEL[1] + (255,), outline=EDGE + (255,))
d.rectangle([18, 36, 45, 45], fill=(120, 70, 40, 255), outline=EDGE + (255,))
block("foundry_operator_seat_top", top)
block("foundry_operator_seat_bottom", plate())
for tag, col in (("off", (60, 50, 40)), ("on", AMBER)):
    side = plate()
    d = ImageDraw.Draw(side)
    d.rectangle([12, 14, 51, 40], fill=DARK + (255,), outline=EDGE + (255,), width=2)
    d.rectangle([24, 20, 39, 34], fill=(90, 94, 100, 255), outline=EDGE + (255,))
    lamp(d, 32, 48, col)
    hazard(side, 54, 59)
    block("foundry_operator_seat_side_" + tag, side)

# Research packs: a steel card with a coloured band and one rivet-pip per number
for i, col in enumerate(PACK_COL, 1):
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([10, 6, 53, 57], fill=STEEL[3] + (255,), outline=EDGE + (255,), width=2)
    d.rectangle([13, 9, 50, 54], outline=STEEL[2] + (255,))
    d.rectangle([14, 14, 49, 30], fill=col + (255,), outline=EDGE + (255,))
    for k in range(i):
        cx = 32 - (i - 1) * 4 + k * 8
        d.ellipse([cx - 2, 40 - 2, cx + 2, 40 + 2], fill=RIVET + (255,), outline=EDGE + (255,))
    hazard(im, 47, 52)
    im.save(os.path.join(A, "textures", "item", f"foundry_research_pack_{i}.png"))


def wr(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


wr(os.path.join(A, "blockstates", "foundry_operator_seat.json"), {"variants": {
    "staffed=false": {"model": NS + ":block/foundry_operator_seat_off"}, "staffed=true": {"model": NS + ":block/foundry_operator_seat_on"}}})
for tag in ("off", "on"):
    wr(os.path.join(A, "models", "block", f"foundry_operator_seat_{tag}.json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": NS + ":block/foundry_operator_seat_top", "bottom": NS + ":block/foundry_operator_seat_bottom", "side": NS + f":block/foundry_operator_seat_side_{tag}"}})
wr(os.path.join(A, "items", "foundry_operator_seat.json"), {"model": {"type": "minecraft:model", "model": NS + ":block/foundry_operator_seat_off"}})
lang = {"block.%s.foundry_operator_seat" % NS: "Operator Seat"}
for i in range(1, 8):
    n = f"foundry_research_pack_{i}"
    wr(os.path.join(A, "items", n + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
    wr(os.path.join(A, "models", "item", n + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
    lang[f"item.{NS}.{n}"] = f"Research Pack {i}"
wr(os.path.join(A, "lang", "en_us.json"), lang)

# Recipes: stand-in ingredients per the progression graph needs; boss drops replace the echo shard, nether star and dragon breath later
PACK_RECIPES = [
    ["minecraft:clay_ball", "minecraft:paper"],
    ["#c:gears/iron", "minecraft:paper", "minecraft:ink_sac"],
    ["#c:ingots/steel", "minecraft:lapis_lazuli", "minecraft:bread"],
    ["#c:plates/copper", "minecraft:redstone", "minecraft:gold_nugget"],
    ["#c:ingots/gold", "minecraft:redstone", "minecraft:echo_shard"],
    ["#c:raw_materials/core_fragment", "minecraft:paper", "minecraft:nether_star"],
    ["#c:ores/shard_metal", "minecraft:paper", "minecraft:dragon_breath"],
]
for i, ing in enumerate(PACK_RECIPES, 1):
    wr(os.path.join(D, "recipe", f"foundry_research_pack_{i}.json"), {"type": "minecraft:crafting_shapeless", "ingredients": ing,
        "result": {"id": f"{NS}:foundry_research_pack_{i}", "count": 2 if i < 3 else 1}, "category": "misc"})
wr(os.path.join(D, "recipe", "foundry_operator_seat.json"), {"type": "minecraft:crafting_shaped", "key": {
    "I": "minecraft:iron_ingot", "L": "minecraft:leather", "P": "minecraft:oak_planks", "R": "minecraft:redstone_lamp"},
    "pattern": ["IRI", "LLL", "PIP"], "result": {"id": NS + ":foundry_operator_seat", "count": 1}, "category": "misc"})

# Shared convention tags the pack recipes use. Empty here, other modules add their items; recipes load either way.
for t in ("gears/iron", "ingots/steel", "plates/copper", "raw_materials/core_fragment", "ores/shard_metal"):
    wr(os.path.join(ROOT, "data", "c", "tags", "item", t + ".json"), {"values": []})

# Age gate, Ages from the progression graph
g = json.load(open(GRAPH))
nodes = {n["id"]: n for n in g["nodes"]}
AGE_NUM = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "VIII": 8, "RODOS": 9}
items_age = {NS + ":foundry_operator_seat": nodes["tech.operator"]["age"]}
for i in range(1, 8):
    items_age[f"{NS}:foundry_research_pack_{i}"] = nodes[f"research.pack_{i}"]["age"]
by_age = {}
for it, a in items_age.items():
    by_age.setdefault(a, []).append(it)
wr(os.path.join(D, "age_gate.json"), {f"tier{AGE_NUM[a]}": {"age": AGE_NUM[a], "items": sorted(v)} for a, v in sorted(by_age.items(), key=lambda kv: AGE_NUM[kv[0]])})
wr(os.path.join(FOUNDRY, "diaithir-data", "age_gates.json"), {"namespaces": {}, "items": items_age,
    "_note": "Same Ages as data/diaithircrafting/age_gate.json, taken from plans/progression/graph.json."})

# W (watts) and s (seconds per job) per machine. Graph values where the graph has them; the rest are set here in SI per UNITS.md.
EXTRA = {"tech.belts": (0.0, None), "tech.circuits": (30000.0, 1.5), "tech.circuits_adv": (60000.0, 3.0), "tech.operator": (100.0, None),
         "tech.reclaimer": (100000.0, 3.0), "tech.heat_grid": (0.0, None), "tech.inserter": (None, 0.8), "tech.miner": (None, 4.0),
         "tech.core_drill": (None, 20.0), "tech.assembler": (None, None)}
stats = {}
for nid, n in nodes.items():
    if n["system"] != "tech" or nid not in EXTRA and "W" not in n and "s" not in n:
        continue
    W = n.get("W", EXTRA.get(nid, (None, None))[0])
    s = n.get("s", EXTRA.get(nid, (None, None))[1])
    row = {k: v for k, v in (("W", W), ("s", s)) if v is not None}
    if row:
        row["age"] = n["age"]
        stats[nid] = row
wr(os.path.join(D, "machine_stats.json"), {"_doc": "W = watts drawn while working, s = seconds one job takes. SI per plans/UNITS.md (1 FE = 1 J, 1 FE/t = 20 W). Keys are progression graph node ids.", "machines": stats})

# Age research nodes: one per gate, each unlocked by crafting packs of the Age, chained
pk = lambda n: f"{NS}:foundry_research_pack_{n}"
prev = None
for nid, n in nodes.items():
    if not nid.startswith("research.gate_"):
        continue
    age = nid.split("_", 1)[1]
    pack = int([x for x in n["needs"] if x.startswith("research.pack_")][0].split("_")[1])
    node = {"title": f"Age gate {age}", "description": f"Craft research packs to open Age {age}: {n['name']}.",
            "to_unlock": {"criterion": {"trigger": "researcher:item_crafted", "conditions": {"item": {"items": pk(pack)}}}, "count": 4 + 2 * pack},
            "display": {"type": "item", "item": {"id": pk(pack)}}}
    if prev:
        node["prerequisites"] = [f"foundry:gate_{prev.lower()}"]
    wr(os.path.join(DP, "data", "foundry", "research", f"gate_{age.lower()}.json"), node)
    prev = age
wr(os.path.join(DP, "pack.mcmeta"), {"pack": {"min_format": 101, "max_format": 101, "description": "Foundry Age research gates (progression graph)"}})
print("ok", len(stats), "machines", len(nodes), "nodes")
