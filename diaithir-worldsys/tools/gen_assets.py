"""Deterministic 64 px textures, models, blockstates, item definitions and lang for every item and block of diaithirworldsys.
Usage: python3 tools/gen_assets.py   (run from diaithir-worldsys/). Idempotent; reads the id lists in tools/catalog.json."""
import hashlib, json, os, random
from PIL import Image, ImageDraw
R = "src/main/resources/assets/diaithirworldsys/"
cat = json.load(open("tools/catalog.json"))  # {"items":[ids], "blocks":[ids]}

def col(name, l=0):
    h = hashlib.sha256(name.encode()).digest()
    r, g, b = 70 + h[0] % 150, 70 + h[1] % 150, 70 + h[2] % 150
    return tuple(max(0, min(255, c + l)) for c in (r, g, b))

def shape(name):
    for k in ("ring", "necklace"):
        if k in name: return k
    for k in ("magnet", "coil"):
        if k in name: return "magnet"
    if "egg" in name: return "egg"
    if "compass" in name: return "compass"
    if name.split("_")[-1] in ("ruby", "sapphire", "topaz", "citrine", "jade", "tanzanite") or "gem" in name: return "gem"
    return "tile"

def item(name):
    im = Image.new("RGBA", (64, 64), (0, 0, 0, 0)); d = ImageDraw.Draw(im); c = col(name); dk = col(name, -50); lt = col(name, 50)
    s = shape(name)
    if s == "ring":
        d.ellipse((14, 14, 50, 50), outline=dk, width=9); d.ellipse((16, 16, 48, 48), outline=c, width=5); d.polygon([(32, 4), (40, 12), (32, 20), (24, 12)], fill=lt, outline=dk)
    elif s == "necklace":
        d.arc((8, 4, 56, 52), 20, 160, fill=dk, width=5); d.polygon([(32, 40), (42, 50), (32, 62), (22, 50)], fill=lt, outline=dk)
    elif s == "gem":
        d.polygon([(32, 6), (56, 24), (32, 58), (8, 24)], fill=c, outline=dk); d.polygon([(32, 6), (44, 24), (32, 58), (20, 24)], fill=lt)
    elif s == "magnet":
        d.arc((10, 8, 54, 60), 180, 360, fill=c, width=12); d.rectangle((10, 30, 22, 52), fill=(200, 60, 60)); d.rectangle((42, 30, 54, 52), fill=(230, 230, 230))
    elif s == "egg":
        d.ellipse((14, 6, 50, 58), fill=c, outline=dk, width=3); d.ellipse((22, 24, 32, 34), fill=lt); d.ellipse((34, 38, 42, 46), fill=dk)
    elif s == "compass":
        d.ellipse((6, 6, 58, 58), fill=(90, 90, 100), outline=dk, width=4); d.polygon([(32, 12), (38, 34), (32, 30), (26, 34)], fill=(210, 50, 50)); d.polygon([(32, 52), (38, 34), (32, 38), (26, 34)], fill=lt)
    else:
        d.rounded_rectangle((8, 8, 56, 56), 8, fill=c, outline=dk, width=4); d.rectangle((18, 18, 46, 28), fill=lt)
    return im

def block(name):
    rnd = random.Random(name); c = col(name); im = Image.new("RGB", (64, 64), c); px = im.load()
    for x in range(64):
        for y in range(64):
            n = rnd.randint(-12, 12); px[x, y] = tuple(max(0, min(255, v + n)) for v in c)
    d = ImageDraw.Draw(im); d.rectangle((0, 0, 63, 63), outline=col(name, -60), width=3); d.rectangle((12, 12, 51, 51), outline=col(name, 40), width=2)
    return im

def w(p, o):
    os.makedirs(os.path.dirname(R + p), exist_ok=True)
    open(R + p, "w").write(json.dumps(o, indent=1) + "\n")

lang = {}
for n in cat["items"]:
    os.makedirs(R + "textures/item", exist_ok=True); item(n).save(R + f"textures/item/{n}.png")
    w(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"diaithirworldsys:item/{n}"}})
    w(f"models/item/{n}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"diaithirworldsys:item/{n}"}})
    lang[f"item.diaithirworldsys.{n}"] = n.replace("_", " ").title()
for n in cat["blocks"]:
    os.makedirs(R + "textures/block", exist_ok=True); block(n).save(R + f"textures/block/{n}.png")
    w(f"blockstates/{n}.json", {"variants": {"": {"model": f"diaithirworldsys:block/{n}"}}})
    w(f"models/block/{n}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"diaithirworldsys:block/{n}"}})
    w(f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"diaithirworldsys:block/{n}"}})
    lang[f"block.diaithirworldsys.{n}"] = n.replace("_", " ").title()
p = R + "lang/en_us.json"; cur = json.load(open(p)) if os.path.exists(p) else {}
for k, v in lang.items(): cur.setdefault(k, v)
os.makedirs(os.path.dirname(p), exist_ok=True); json.dump(cur, open(p, "w"), indent=1, ensure_ascii=False)
print(len(cat["items"]), "items", len(cat["blocks"]), "blocks")
