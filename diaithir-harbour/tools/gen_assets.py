#!/usr/bin/env python3
"""Writes the textures (drawn from scratch), models, blockstates, items, lang, recipes, loot, tags, age gate and library entries for diaithir-harbour."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "diaithirharbour"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
rnd = random.Random(5)
os.makedirs(os.path.join(A, "textures/block"), exist_ok=True)
os.makedirs(os.path.join(A, "textures/item"), exist_ok=True)


def wj(base, path, obj):
    full = os.path.join(base, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")


def iron(w=16, h=16, base=84, tint=(0, 6, 14)):
    im = Image.new("RGBA", (w, h))
    px = im.load()
    for y in range(h):
        for x in range(w):
            b = base + rnd.randrange(-7, 8)
            edge = x in (0, w - 1) or y in (0, h - 1)
            c = (b - 30, b - 24, b - 14) if edge else (b + tint[0], b + tint[1], b + tint[2])
            px[x, y] = c + (255,)
    return im


def block_model(name, parent, tex):
    wj(A, f"models/block/{name}.json", {"parent": parent, "textures": tex})


def item_model(name, model):
    wj(A, f"items/{name}.json", {"model": {"type": "minecraft:model", "model": model}})


NAMES = {}
RECIPES = {}
GATE = {}


def register(name, native, text, age):
    NAMES[name] = (native, text)
    GATE.setdefault(age, []).append(f"{NS}:{name}")


# mooring bollard: a squat cast-iron post with a collar and a rope groove; "loose" shows the rope hanging free
for loose in (False, True):
    tex = iron(16, 16, 70)
    px = tex.load()
    for y in range(16):
        for x in range(16):
            if y in (2, 3) or (y in (6, 7)):
                px[x, y] = (150, 120, 70, 255) if not loose else (90, 80, 64, 255)
    tex.save(os.path.join(A, f"textures/block/mooring_bollard{'_loose' if loose else ''}.png"))
    top = iron(16, 16, 96)
    top.save(os.path.join(A, f"textures/block/mooring_bollard_top.png"))
    block_model(f"mooring_bollard{'_loose' if loose else ''}", "minecraft:block/block", {"side": f"{NS}:block/mooring_bollard{'_loose' if loose else ''}",
                "top": f"{NS}:block/mooring_bollard_top", "particle": f"{NS}:block/mooring_bollard_top"})
    full = f"{NS}:block/mooring_bollard{'_loose' if loose else ''}"
    elements = [{"from": [4, 0, 4], "to": [12, 12, 12], "faces": {"north": {"texture": "#side"}, "south": {"texture": "#side"}, "east": {"texture": "#side"}, "west": {"texture": "#side"},
                                                              "up": {"texture": "#top"}, "down": {"texture": "#top"}}},
                {"from": [3, 12, 3], "to": [13, 14, 13], "faces": {"north": {"texture": "#top"}, "south": {"texture": "#top"}, "east": {"texture": "#top"}, "west": {"texture": "#top"},
                                                               "up": {"texture": "#top"}, "down": {"texture": "#top"}}}]
    wj(A, f"models/block/mooring_bollard{'_loose' if loose else ''}.json", {"parent": "minecraft:block/block", "textures": {"side": f"{NS}:block/mooring_bollard{'_loose' if loose else ''}",
        "top": f"{NS}:block/mooring_bollard_top", "particle": f"{NS}:block/mooring_bollard_top"}, "elements": elements})
wj(A, "blockstates/mooring_bollard.json", {"variants": {f"loose={l},powered={p}": {"model": f"{NS}:block/mooring_bollard{'_loose' if l == 'true' else ''}"} for l in ("false", "true") for p in ("false", "true")}})
item_model("mooring_bollard", f"{NS}:block/mooring_bollard")
register("mooring_bollard", "Cleatvarn", "Mooring bollard. Make it fast and any boat within its reach is held at the quay: it cannot drift or be paddled away. Use it bare-handed to let go, or give it a redstone signal. Its reach grows with the harbour office. Age III.", 3)
RECIPES["mooring_bollard"] = {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"I": "minecraft:iron_ingot", "L": "minecraft:lead", "B": "minecraft:iron_block"},
                              "pattern": [" L ", "III", " B "], "result": {"id": f"{NS}:mooring_bollard", "count": 2}}

# ---- extension point: the other pieces append to NAMES/RECIPES/GATE above this line ----
lang = {f"block.{NS}.{k}": v[0] for k, v in NAMES.items() if not k.endswith("_crate")}
wj(A, "lang/en_us.json", lang)
for k, r in RECIPES.items():
    wj(D, f"recipe/{k}.json", r)
BLOCKS = [k for k in NAMES]
for k in BLOCKS:
    wj(D, f"loot_table/blocks/{k}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{k}"}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}], "random_sequence": f"{NS}:blocks/{k}"})
wj(ROOT, "data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{k}" for k in BLOCKS]})
wj(D, "age_gate.json", {f"tier{i + 1}": {"age": a, "items": items} for i, (a, items) in enumerate(sorted(GATE.items()))})
os.makedirs(os.path.join(HERE, "..", "..", "library-sync"), exist_ok=True)
with open(os.path.join(HERE, "..", "..", "library-sync", NS + ".json"), "w") as f:
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in NAMES.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")
print("ok")
