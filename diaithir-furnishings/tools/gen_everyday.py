"""Everyday objects: writes items, models, 16x16 textures, lang, tags, stonecutting recipes, Age gates and
everyday.json from roster/roster.csv. Run from the module: python3 -I tools/gen_everyday.py (needs Pillow). Deterministic."""
import csv, json, hashlib, re
from pathlib import Path
from PIL import Image, ImageDraw

M = Path(__file__).resolve().parents[1]
RES = M / "src/main/resources"
A = RES / "assets/diaithirfurnishings"
D = RES / "data/diaithirfurnishings"
NS = "diaithirfurnishings"
EXISTING = {"rope", "sack"}  # already registered as blocks

TOOLS = {"stone_tool", "woodwork", "metalwork", "farm", "mining", "masonry", "general", "fishing", "leather",
         "sharpening", "hunting", "measuring_tool"}
SOURCE_MATERIAL = {  # category -> ingredient per Age band, 'M' = by-Age metal
    "stone_tool": "minecraft:flint", "fire": "minecraft:flint", "woodwork": "M", "metalwork": "M", "farm": "M",
    "mining": "M", "masonry": "M", "general": "M", "sharpening": "M", "utensil": "M", "cookware": "C",
    "measuring": "M", "tableware": "minecraft:clay_ball", "pottery": "minecraft:clay_ball",
    "lighting": "minecraft:clay_ball", "storage": "#minecraft:planks", "furniture": "#minecraft:planks",
    "household": "#minecraft:planks", "dairy": "#minecraft:planks", "food_prep": "#minecraft:planks",
    "apiary": "minecraft:honeycomb", "writing": "minecraft:paper", "textile": "minecraft:string",
    "fishing": "minecraft:string", "hunting": "minecraft:string", "leather": "minecraft:leather",
    "glass": "minecraft:glass",
}
METAL = {"I": "minecraft:flint", "II": "#c:ingots/copper", "III": "#diaithirfurnishings:metal/bronze"}
PAL = {  # category -> (main, accent)
    "stone_tool": ((140, 138, 130), (92, 90, 86)), "woodwork": ((150, 150, 158), (130, 92, 52)),
    "metalwork": ((120, 124, 134), (70, 60, 56)), "farm": ((160, 160, 166), (140, 100, 56)),
    "mining": ((130, 134, 142), (110, 78, 44)), "masonry": ((176, 170, 160), (120, 100, 70)),
    "general": ((146, 150, 160), (120, 86, 50)), "sharpening": ((168, 160, 140), (110, 104, 96)),
    "fishing": ((90, 130, 170), (200, 190, 150)), "hunting": ((120, 100, 70), (80, 70, 60)),
    "leather": ((160, 110, 64), (210, 190, 150)), "textile": ((204, 190, 220), (150, 110, 150)),
    "measuring": ((196, 170, 90), (60, 60, 70)), "dairy": ((236, 230, 210), (150, 110, 70)),
    "apiary": ((226, 180, 60), (120, 80, 30)), "food_prep": ((214, 190, 150), (130, 96, 60)),
    "cookware": ((90, 92, 100), (60, 44, 36)), "utensil": ((190, 194, 204), (120, 88, 52)),
    "tableware": ((226, 220, 206), (80, 110, 160)), "storage": ((170, 124, 70), (110, 76, 40)),
    "lighting": ((240, 200, 90), (130, 110, 90)), "household": ((190, 160, 120), (120, 100, 80)),
    "furniture": ((160, 116, 66), (110, 78, 44)), "fire": ((210, 120, 50), (90, 84, 80)),
    "writing": ((236, 226, 196), (40, 40, 60)), "pottery": ((190, 110, 76), (130, 70, 50)),
    "glass": ((170, 210, 220), (110, 150, 170)),
}
GLYPH = {  # category -> glyph kinds the id hash chooses among
    "tool": ["axe", "blade", "hammer", "pick", "fork"],
    "vessel": ["pot", "bowl", "jug", "pan"],
    "box": ["box", "jug", "bowl"],
    "stick": ["spoon", "blade", "fork", "hammer"],
    "fabric": ["bundle", "ring", "blade"],
}
KIND = {"stone_tool": "tool", "woodwork": "tool", "metalwork": "tool", "farm": "tool", "mining": "tool",
        "masonry": "tool", "general": "tool", "sharpening": "tool", "fishing": "stick", "hunting": "stick",
        "leather": "stick", "textile": "fabric", "measuring": "box", "dairy": "vessel", "apiary": "vessel",
        "food_prep": "vessel", "cookware": "vessel", "utensil": "stick", "tableware": "vessel", "storage": "box",
        "lighting": "vessel", "household": "box", "furniture": "box", "fire": "stick", "writing": "stick",
        "pottery": "vessel", "glass": "vessel"}


def age(era):
    e = era.lower()
    if "mya" in e or "paleolithic" in e or re.search(r"c\. (\d{5,}) bc", e): return "I"
    if "neolithic" in e or re.search(r"c\. (\d{4}) bc", e) and int(re.search(r"(\d{4}) bc", e).group(1)) >= 3500: return "II"
    if "bronze" in e or re.search(r"c\. \d{3,4} bc", e) or "ancient" in e or "pre-columbian" in e: return "III"
    if "iron" in e or "roman" in e or "tang" in e or "han" in e or "heian" in e or "kofun" in e or "ming" in e or "japan" == e: return "IV"
    m = re.search(r"(\d{3,4})", e)
    if m:
        y = int(m.group(1))
        return "IV" if y < 1500 else "V" if y < 1850 else "VI"
    return "IV"  # Medieval and the rest


def col(c, k): return tuple(max(0, min(255, int(v * k))) for v in c)


def paint(iid, cat):
    h = int(hashlib.sha256(iid.encode()).hexdigest(), 16)
    main, acc = PAL.get(cat, ((150, 150, 150), (100, 80, 60)))
    k = 0.85 + (h % 31) / 100
    main = col(main, k); acc = col(acc, 0.85 + ((h >> 8) % 31) / 100)
    kinds = GLYPH[KIND.get(cat, "tool")]; g = kinds[(h >> 16) % len(kinds)]
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(im); o = col(main, 0.4) + (255,)
    s = (h >> 24) % 3  # size/pose nudge
    if g in ("axe", "hammer", "pick", "blade", "fork", "spoon"):
        d.line([(3 + s, 13), (11, 5 + s % 2)], fill=acc + (255,), width=2)  # handle
        if g == "axe": d.polygon([(9, 2), (14, 4), (13, 9), (9, 7)], fill=main + (255,), outline=o)
        if g == "hammer": d.rectangle([8, 2, 14, 6], fill=main + (255,), outline=o)
        if g == "pick": d.arc([6, 1, 15, 10], 200, 340, fill=main + (255,), width=2)
        if g == "blade": d.polygon([(8, 9), (14, 2), (15, 4), (10, 11)], fill=main + (255,), outline=o)
        if g == "fork":
            for x in (9, 11, 13): d.line([(x, 1), (x, 6)], fill=main + (255,))
        if g == "spoon": d.ellipse([9, 1, 14, 6], fill=main + (255,), outline=o)
    elif g in ("pot", "bowl", "pan", "jug"):
        if g == "pot": d.rounded_rectangle([3, 5, 12, 13], 2, fill=main + (255,), outline=o); d.line([(2, 4), (13, 4)], fill=acc + (255,))
        if g == "bowl": d.pieslice([2, 3, 13, 14], 0, 180, fill=main + (255,), outline=o)
        if g == "pan": d.ellipse([2, 6, 11, 12], fill=main + (255,), outline=o); d.line([(11, 9), (15, 6)], fill=acc + (255,), width=2)
        if g == "jug": d.rounded_rectangle([4, 4, 11, 14], 3, fill=main + (255,), outline=o); d.rectangle([6, 1, 9, 4], fill=acc + (255,))
    elif g == "box":
        d.rectangle([2, 5, 13, 13], fill=main + (255,), outline=o); d.line([(2, 8), (13, 8)], fill=acc + (255,))
        if s: d.rectangle([6, 3, 9, 5], fill=acc + (255,))
    elif g == "bundle":
        d.ellipse([3, 4, 12, 13], fill=main + (255,), outline=o); d.line([(5, 6), (10, 11)], fill=acc + (255,))
    elif g == "ring":
        d.ellipse([2, 2, 13, 13], outline=main + (255,), width=3); d.line([(8, 8), (14, 14)], fill=acc + (255,), width=2)
    return im


def main():
    rows = [r for r in csv.DictReader(open(M / "roster/roster.csv")) if r["id"] not in EXISTING]
    lang_p = A / "lang/en_us.json"; lang = json.load(open(lang_p))
    gates = {}; entries = []; tags = {}
    for p in ("items", "models/item", "textures/item"): (A / p).mkdir(parents=True, exist_ok=True)
    (D / "recipe/everyday").mkdir(parents=True, exist_ok=True)
    for r in rows:
        iid, cat = r["id"], r["category"]; ag = age(r["earliest"])
        tool = cat in TOOLS and cat != "measuring"
        entries.append({"id": iid, "category": cat, "age": ag, "durability": 200 if tool else 0, "stack": 1 if tool else 16})
        lang[f"item.{NS}.{iid}"] = r["name"]
        json.dump({"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}}, open(A / f"items/{iid}.json", "w"), indent=1)
        json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}}, open(A / f"models/item/{iid}.json", "w"), indent=1)
        paint(iid, cat).save(A / f"textures/item/{iid}.png")
        base = SOURCE_MATERIAL.get(cat, "#minecraft:planks")
        if base == "M": base = METAL.get(ag, "#c:ingots/iron")
        elif base == "C": base = "minecraft:clay_ball" if ag in ("I", "II") else METAL.get(ag, "#c:ingots/iron")
        json.dump({"type": "minecraft:stonecutting", "ingredient": base, "result": {"id": f"{NS}:{iid}", "count": 1}},
                  open(D / f"recipe/everyday/{iid}.json", "w"), indent=1)
        gates[f"{NS}:{iid}"] = "RODOS" if ag == "RODOS" else ag
        tags.setdefault(cat, []).append(f"{NS}:{iid}")
    json.dump(lang, open(lang_p, "w"), indent=1, ensure_ascii=False)
    json.dump({"items": entries}, open(D / "everyday.json", "w"), indent=1)
    (D / "tags/item/everyday").mkdir(parents=True, exist_ok=True)
    for cat, ids in tags.items():
        json.dump({"replace": False, "values": ids}, open(D / f"tags/item/everyday/{cat}.json", "w"), indent=1)
    (D / "tags/item/metal").mkdir(parents=True, exist_ok=True)  # bronze comes from other mods; copper keeps this module loadable alone
    json.dump({"replace": False, "values": [{"id": "#c:ingots/bronze", "required": False},
               {"id": "diaithirforgeworks:bronze_ingot", "required": False}, "minecraft:copper_ingot"]},
              open(D / "tags/item/metal/bronze.json", "w"), indent=1)
    json.dump({"_doc": "Everyday objects by earliest widespread use (roster/roster.csv). Same shape as DiaithirRPG content/tech_ages.json.",
               "namespaces": {NS: "I"}, "items": gates}, open(M / "age_gates.json", "w"), indent=1)
    print(len(entries), "items")


main()
