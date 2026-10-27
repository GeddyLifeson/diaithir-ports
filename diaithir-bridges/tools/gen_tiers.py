"""Writes the tier assets for diaithir-bridges from Spec.java's table: textures (tier trim and screen colour), blockstates, models, items,
loot tables, tags and en_us names. Factorio-style panels: the stripe above the hazard band is the tier metal (copper, brass, gold).
Usage: python3 gen_tiers.py   (run from diaithir-bridges; reads the tier-1 art already in textures/block)"""
import json, re, os, colorsys
from PIL import Image

RES = "src/main/resources"
A = f"{RES}/assets/diaithirbridges"
D = f"{RES}/data/diaithirbridges"
SPEC = re.compile(r'new Spec\(Kind\.(\w+), (\d), "(\w+)", "(\w+)", ([\d_]+), ([\d_]+), ([\d_]+), ([\d_]+), ([\d_]+), ([\d_]+), ([\d_]+)\)')
rows = [m.groups() for m in SPEC.finditer(open("src/main/java/net/diaithir/bridges/Spec.java").read())]
specs = [r[:4] for r in rows]
NAMES = {"tobardealain": "Toburdeluin", "lochrandraoidheachd": "Lòchrundraeidechd", "aibhnedealain": "Aibnedeluin"}
SUFFIX = {"umha": "uima", "oir": "òiri"}
TRIM = {1: None, 2: (196, 158, 58), 3: (236, 200, 70)}          # stripe metal: copper (kept), brass, gold
SCREEN = {"SOURCE": None, "CHARGER": None, "RELAY": (70, 170, 200)}  # relay screen is cyan; the other two keep their art


def base_of(sid):
    return "tobardealain" if sid.startswith("tobardealain") or sid.startswith("aibhnedealain") else "lochrandraoidheachd"


def tint(px, rgb):
    lum = (px[0] * .3 + px[1] * .59 + px[2] * .11) / 150
    return tuple(min(255, int(c * lum)) for c in rgb)


def make(sid, tier, kind):
    src = base_of(sid)
    for part in ("top", "bottom", "side"):
        im = Image.open(f"{A}/textures/block/{src}_{part}.png").convert("RGB")
        px = im.load()
        for y in range(64):
            for x in range(64):
                r, g, b = px[x, y]
                if part == "side" and TRIM[tier] and r > 140 and 70 < g < 130 and b < 90 and y < 48:
                    px[x, y] = tuple(int(c * (0.8 + 0.2 * g / 100)) for c in TRIM[tier])
                if part == "side" and SCREEN[kind] and 20 <= x <= 44 and 17 <= y <= 36 and g > 100 and g > r + 25:
                    px[x, y] = tint((r, g, b), SCREEN[kind])
                if SCREEN[kind] and part == "top" and kind == "RELAY" and g > r + 25:
                    px[x, y] = tint((r, g, b), SCREEN[kind])
        if tier == 3 and part == "side":  # gold machines get a corner rivet in the metal
            for cx, cy in ((9, 7), (53, 7)):
                for dx in (0, 1):
                    for dy in (0, 1):
                        px[cx + dx, cy + dy] = TRIM[3]
        im.save(f"{A}/textures/block/{sid}_{part}.png")


lang = {}
for kind, tier, sid, age in specs:
    tier = int(tier)
    stem = next(s for s in NAMES if sid.startswith(s))
    suf = sid[len(stem):]
    lang[f"block.diaithirbridges.{sid}"] = NAMES[stem] + (SUFFIX[suf] if suf else "")
    if tier > 1 or stem == "aibhnedealain":
        make(sid, tier, kind)
    os.makedirs(f"{A}/blockstates", exist_ok=True)
    for sub, doc in (
        (f"blockstates/{sid}.json", {"variants": {"": {"model": f"diaithirbridges:block/{sid}"}}}),
        (f"items/{sid}.json", {"model": {"type": "minecraft:model", "model": f"diaithirbridges:block/{sid}"}}),
        (f"models/block/{sid}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {p: f"diaithirbridges:block/{sid}_{p}" for p in ("top", "bottom", "side")}}),
    ):
        json.dump(doc, open(f"{A}/{sub}", "w"), indent=2)
    os.makedirs(f"{D}/loot_table/blocks", exist_ok=True)
    json.dump({"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"diaithirbridges:{sid}"}],
                                                       "conditions": [{"condition": "minecraft:survives_explosion"}]}],
               "random_sequence": f"diaithirbridges:blocks/{sid}"}, open(f"{D}/loot_table/blocks/{sid}.json", "w"), indent=2)
ids = [f"diaithirbridges:{s[2]}" for s in specs]
for tag in ("mineable/pickaxe", "needs_stone_tool"):
    json.dump({"values": ids}, open(f"{RES}/data/minecraft/tags/block/{tag}.json", "w"), indent=2)
json.dump(lang, open(f"{A}/lang/en_us.json", "w"), indent=2, ensure_ascii=False)
print(len(specs), "tiers written")

FIELDS = ("rate", "effPct", "outPerTick", "capacity", "inPerTick", "cost", "lossPct")
json.dump([{"kind": r[0], "tier": int(r[1]), "id": r[2], "age": r[3], "name": lang[f"block.diaithirbridges.{r[2]}"],
            **{f: int(v.replace("_", "")) for f, v in zip(FIELDS, r[4:])}} for r in rows], open("tiers.json", "w"), indent=1, ensure_ascii=False)
