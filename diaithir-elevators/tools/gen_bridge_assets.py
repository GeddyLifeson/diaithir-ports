#!/usr/bin/env python3
"""Writes the light bridge textures (drawn here from scratch), models, blockstates, item, lang additions, recipe, loot, library entries and age gate for the bridge emitter."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirelevators"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
rnd = random.Random(11)
os.makedirs(os.path.join(A, "textures/block"), exist_ok=True)


def wj(base, path, obj):
    full = os.path.join(base, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def steel(w=16, h=16):
    im = Image.new("RGBA", (w, h))
    px = im.load()
    for y in range(h):
        for x in range(w):
            b = 92 + rnd.randrange(-6, 7)
            edge = x in (0, w - 1) or y in (0, h - 1)
            px[x, y] = ((52, 58, 66) if edge else (b, b + 8, b + 18)) + (255,)
    return im


# bridge cell: translucent cyan plate with brighter edges and a pulsing grid
bridge = Image.new("RGBA", (16, 16))
bp = bridge.load()
for y in range(16):
    for x in range(16):
        edge = x in (0, 15) or y in (0, 15)
        grid = x % 4 == 0 or y % 4 == 0
        bp[x, y] = (200, 250, 255, 235) if edge else ((120, 225, 255, 170) if grid else (60, 170, 235, 110))
bridge.save(os.path.join(A, "textures/block/light_bridge.png"))
for k in ("top", "side"):
    im = steel()
    px = im.load()
    if k == "top":
        for y in range(5, 11):
            for x in range(5, 11):
                px[x, y] = (30, 34, 40, 255)
    else:
        for y in (4, 11):
            for x in range(1, 15):
                px[x, y] = (66, 72, 82, 255)
    im.save(os.path.join(A, f"textures/block/bridge_emitter_{k}.png"))
for on in (False, True):
    im = steel()
    px = im.load()
    for y in range(5, 11):
        for x in range(4, 12):
            px[x, y] = (24, 28, 34, 255)
    lens = (140, 240, 255, 255) if on else (50, 90, 110, 255)
    for y in range(6, 10):
        for x in range(6, 10):
            px[x, y] = lens
    im.save(os.path.join(A, f"textures/block/bridge_emitter_front{'_on' if on else ''}.png"))

wj(A, "models/block/light_bridge_x.json", {"parent": "minecraft:block/block", "render_type": "minecraft:translucent", "textures": {"all": f"{NS}:block/light_bridge", "particle": f"{NS}:block/light_bridge"},
    "elements": [{"from": [0, 0, 0], "to": [16, 2, 16], "faces": {d: {"texture": "#all", "uv": [0, 0, 16, 16] if d in ("up", "down") else [0, 14, 16, 16]} for d in ("up", "down", "north", "south", "east", "west")}}]})
wj(A, "blockstates/light_bridge.json", {"variants": {"axis_x=true": {"model": f"{NS}:block/light_bridge_x", "y": 90}, "axis_x=false": {"model": f"{NS}:block/light_bridge_x"}}})
for on in (False, True):
    wj(A, f"models/block/bridge_emitter{'_on' if on else ''}.json", {"parent": "minecraft:block/orientable", "textures": {
        "top": f"{NS}:block/bridge_emitter_top", "side": f"{NS}:block/bridge_emitter_side", "front": f"{NS}:block/bridge_emitter_front{'_on' if on else ''}"}})
wj(A, "blockstates/bridge_emitter.json", {"variants": {f"facing={f},powered={p}": {"model": f"{NS}:block/bridge_emitter{'_on' if p == 'true' else ''}", **({"y": y} if y else {})}
                                                       for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)) for p in ("false", "true")}})
wj(A, "items/bridge_emitter.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/bridge_emitter"}})

lang_path = os.path.join(A, "lang/en_us.json")
lang = json.load(open(lang_path))
lang[f"block.{NS}.bridge_emitter"] = "Lumvarrek"
lang[f"block.{NS}.light_bridge"] = "Lumvarrek light"
wj(A, "lang/en_us.json", lang)
wj(D, "recipe/bridge_emitter.json", {"type": "minecraft:crafting_shaped", "category": "redstone", "key": {"I": "minecraft:iron_ingot", "G": "minecraft:glowstone", "R": "minecraft:redstone", "D": "minecraft:diamond"},
    "pattern": ["IDI", "IGI", "IRI"], "result": {"id": f"{NS}:bridge_emitter", "count": 1}})
wj(D, "loot_table/blocks/bridge_emitter.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:bridge_emitter"}],
    "conditions": [{"condition": "minecraft:survives_explosion"}]}], "random_sequence": f"{NS}:blocks/bridge_emitter"})
gate_path = os.path.join(D, "age_gate.json")
gate = json.load(open(gate_path))
if f"{NS}:bridge_emitter" not in gate["items"]:
    gate["items"].append(f"{NS}:bridge_emitter")
wj(D, "age_gate.json", gate)
lib_path = os.path.join(HERE, "..", "..", "library-sync", NS + ".json")
lib = json.load(open(lib_path))
if not any(e["id"] == f"{NS}:bridge_emitter" for e in lib):
    lib.append({"id": f"{NS}:bridge_emitter", "name": "Lumvarrek", "text": "Light bridge emitter. With a redstone signal it lays a walkable strip of light 24 blocks long in front of it, stopping at the first solid block; with no signal the strip fades. Use it for a drawbridge or a gap in a base. Age V."})
with open(lib_path, "w") as f:
    json.dump(lib, f, indent=1, ensure_ascii=False)
    f.write("\n")
print("ok")
