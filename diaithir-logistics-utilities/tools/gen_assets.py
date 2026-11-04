#!/usr/bin/env python3
"""Writes 64 px textures, models, blockstates, item definitions, lang, recipes, age gate and library entries for diaithir-logistics-utilities.
All art is drawn here from scratch (industrial factory look: dark worn steel, rivets, a coloured status band per pipe kind). No Not Logistics Pipes or Factorio art ships."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirlogistics"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
for d in ("textures/block", "models/block", "blockstates", "items", "lang"):
    os.makedirs(os.path.join(A, d), exist_ok=True)
os.makedirs(os.path.join(D, "recipe"), exist_ok=True)
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)

STEEL = [(58, 60, 64), (70, 73, 78), (84, 88, 94), (47, 49, 53)]
EDGE = (34, 35, 38)
RIVET = (122, 126, 132)
COL = {"pipe": (200, 200, 70), "sink_pipe": (80, 200, 110), "extractor_pipe": (230, 120, 30), "speed_pipe": (80, 150, 230)}
S = 64


def pipe_tex(name, color):
    rnd = random.Random(name)
    im = Image.new("RGBA", (S, S))
    px = im.load()
    for y in range(S):
        for x in range(S):
            base = STEEL[(x // 4 + y // 4) % 2 + (2 if rnd.random() < 0.18 else 0)]
            px[x, y] = base + (255,)
    for i in range(S):
        for j in (0, 1, S - 2, S - 1):
            px[i, j] = EDGE + (255,)
            px[j, i] = EDGE + (255,)
    for y in range(26, 38):
        for x in range(2, S - 2):
            px[x, y] = color + (255,) if 28 <= y < 36 else EDGE + (255,)
    for cx in range(8, S, 16):
        for cy in (10, 54):
            for dx in range(3):
                for dy in range(3):
                    px[cx + dx, cy + dy] = RIVET + (255,)
    im.save(os.path.join(A, "textures/block", name + ".png"))


def wj(path, obj):
    with open(os.path.join(A, path), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


FACE = lambda: {f: {"texture": "#all"} for f in ("north", "south", "east", "west", "up", "down")}
wj("models/block/pipe_core.json", {"textures": {"particle": "#all"}, "elements": [{"from": [5, 5, 5], "to": [11, 11, 11], "faces": FACE()}]})
wj("models/block/pipe_arm.json", {"textures": {"particle": "#all"}, "elements": [{"from": [5, 5, 0], "to": [11, 11, 5], "faces": FACE()}]})
wj("models/block/pipe_inventory.json", {"parent": "minecraft:block/block", "textures": {"particle": "#all"}, "elements": [
    {"from": [5, 5, 5], "to": [11, 11, 11], "faces": FACE()},
    {"from": [5, 5, 0], "to": [11, 11, 5], "faces": FACE()},
    {"from": [5, 5, 11], "to": [11, 11, 16], "faces": FACE()}]})
ROT = {"north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
for k, c in COL.items():
    pipe_tex(k, c)
    for part in ("core", "arm", "inventory"):
        wj(f"models/block/{k}_{part}.json", {"parent": f"{NS}:block/pipe_{part}", "textures": {"all": f"{NS}:block/{k}"}})
    multipart = [{"apply": {"model": f"{NS}:block/{k}_core"}}]
    for d, r in ROT.items():
        multipart.append({"when": {d: "true"}, "apply": dict({"model": f"{NS}:block/{k}_arm"}, **r)})
    wj(f"blockstates/{k}.json", {"multipart": multipart})
    wj(f"items/{k}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}_inventory"}})

NAMES = {
    "pipe": ("Vethrin", "Routed pipe. Joins every pipe next to it into one network. Items inside travel one block per second along the shortest route to the best sink. Age V."),
    "sink_pipe": ("Dolvareth", "Sink pipe. Delivers items into the inventories touching it. Use an item on it to list that item, a stick to raise its priority (sneak to lower), sneak with an empty hand to take everything. Higher priority is served first, then the nearer pipe. Age V."),
    "extractor_pipe": ("Quelmurak", "Extractor pipe. Every second pulls items from the inventories touching it and sends them to a sink pipe that wants them and has room. Age V."),
    "speed_pipe": ("Ashkirel", "Speed pipe. Items pass through four times faster than through a plain pipe. Age V."),
}
wj("lang/en_us.json", {f"block.{NS}.{k}": v[0] for k, v in NAMES.items()})
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")
with open(os.path.join(D, "age_gate.json"), "w") as f:
    json.dump({"tier1": {"age": 5, "items": [f"{NS}:{k}" for k in NAMES]}}, f, indent=2)
    f.write("\n")


def recipe(name, obj, count=1):
    obj["result"] = {"id": f"{NS}:{name}", "count": count}
    obj["category"] = "misc"
    with open(os.path.join(D, "recipe", name + ".json"), "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


recipe("pipe", {"type": "minecraft:crafting_shaped", "pattern": ["III", "GGG", "III"], "key": {"I": "minecraft:iron_ingot", "G": "minecraft:glass"}}, 8)
for name, extra in (("sink_pipe", "minecraft:hopper"), ("extractor_pipe", "minecraft:dropper"), ("speed_pipe", "minecraft:redstone")):
    recipe(name, {"type": "minecraft:crafting_shapeless", "ingredients": [f"{NS}:pipe", extra]})
print("ok")
