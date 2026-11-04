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

# harbour office: four stages of quay office (timber, brick, iron, steel), same box with a pitched sign board
STAGES = [("timber", (122, 92, 58), (96, 70, 42)), ("brick", (150, 78, 62), (112, 54, 44)), ("iron", (104, 112, 124), (70, 76, 86)), ("steel", (176, 188, 204), (120, 132, 150))]
for i, (nm, light, dark) in enumerate(STAGES):
    side = Image.new("RGBA", (16, 16))
    sp = side.load()
    for y in range(16):
        for x in range(16):
            c = light if (y // 4 + (x // 8 if nm == "brick" else 0)) % 2 == 0 else dark
            if nm in ("iron", "steel") and (x % 8 == 0 or y % 8 == 0):
                c = (c[0] - 30, c[1] - 30, c[2] - 30)
            sp[x, y] = tuple(max(0, min(255, v + rnd.randrange(-6, 7))) for v in c) + (255,)
    for y in range(5, 9):
        for x in range(4, 12):
            sp[x, y] = (30, 40, 56, 255)          # window
    for x in range(3, 13):
        sp[x, 11] = (240, 190, 30, 255)         # sign board stripe
    side.save(os.path.join(A, f"textures/block/harbour_office_{i}_side.png"))
    top = iron(16, 16, 80 + i * 10, (6, 4, 0))
    top.save(os.path.join(A, f"textures/block/harbour_office_{i}_top.png"))
    block_model(f"harbour_office_{i}", "minecraft:block/cube_column", {"end": f"{NS}:block/harbour_office_{i}_top", "side": f"{NS}:block/harbour_office_{i}_side"})
wj(A, "blockstates/harbour_office.json", {"variants": {f"stage={i}": {"model": f"{NS}:block/harbour_office_{i}"} for i in range(4)}})
item_model("harbour_office", f"{NS}:block/harbour_office_0")
register("harbour_office", "Quaymorrek", "Harbour office. A quay station that grows with the Age without any input: timber, then brick, iron and steel. Each stage widens the reach of every mooring bollard within 16 blocks (3 blocks, then 5, 7 and 9) and lets a boat there carry more cargo crates. Use it to read its stage. Age III.", 3)
RECIPES["harbour_office"] = {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"P": "minecraft:oak_planks", "I": "minecraft:iron_ingot", "C": "minecraft:clock", "B": "minecraft:bell"},
                            "pattern": ["PBP", "PCP", "IPI"], "result": {"id": f"{NS}:harbour_office", "count": 1}}

# cargo crate item: a slatted timber crate with iron corners
crate = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
cp = crate.load()
for y in range(2, 15):
    for x in range(1, 15):
        slat = (y - 2) % 4 == 3
        c = (86, 62, 36) if slat else (140, 104, 62)
        c = tuple(v + rnd.randrange(-6, 7) for v in c)
        if (x in (1, 14)) and (y in range(2, 6) or y in range(11, 15)):
            c = (92, 98, 108)
        cp[x, y] = c + (255,)
for i in range(2, 14):
    cp[3 + (i * 10 // 12), i + 1] = (100, 74, 44, 255)
crate.save(os.path.join(A, "textures/item/cargo_crate.png"))
wj(A, "items/cargo_crate.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/cargo_crate"}})
wj(A, "models/item/cargo_crate.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/cargo_crate"}})
ITEM_NAMES = {"cargo_crate": ("Holdvarn", "Cargo crate. Use it on a boat to fit a row of nine hold slots; sneak and use the boat with an empty hand to open the hold. A boat carries one crate, or more with a harbour office nearby (two, then one more for each stage). Crates and contents drop if the boat is destroyed. Age III.", 3)}
for k, v in ITEM_NAMES.items():
    GATE.setdefault(v[2], []).append(f"{NS}:{k}")
RECIPES["cargo_crate"] = {"type": "minecraft:crafting_shaped", "category": "misc", "key": {"P": "minecraft:oak_planks", "I": "minecraft:iron_ingot", "C": "minecraft:chest"},
                          "pattern": ["IPI", "PCP", "IPI"], "result": {"id": f"{NS}:cargo_crate", "count": 1}}

# ---- extension point: the other pieces append to NAMES/RECIPES/GATE above this line ----
lang = {f"block.{NS}.{k}": v[0] for k, v in NAMES.items()}
for k, v in ITEM_NAMES.items():
    lang[f"item.{NS}.{k}"] = v[0]
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
    json.dump([{"id": f"{NS}:{k}", "name": v[0], "text": v[1]} for k, v in {**NAMES, **ITEM_NAMES}.items()], f, indent=1, ensure_ascii=False)
    f.write("\n")
print("ok")
