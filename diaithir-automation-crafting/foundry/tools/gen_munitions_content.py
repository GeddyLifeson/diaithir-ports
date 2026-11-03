"""Munitions Ages I to III for the Foundry stations (plans/MUNITIONS.md): part items, round items, 64 px icons,
lang and station recipes, all generated from diaithir-forgeworks/munitions/catalog.json. Called by gen_assets.py.
Game content only; ingredients are everyday items, no process detail."""
import json, os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
FOUNDRY = os.path.normpath(os.path.join(HERE, ".."))
CATALOG = os.path.normpath(os.path.join(FOUNDRY, "..", "..", "diaithir-forgeworks", "munitions", "catalog.json"))
NS = "diaithircrafting"
AGES = ("ancient", "ailean", "holy")
STATION = {"ancient": "station.hearth_bench", "ailean": "station.hearth_bench", "holy": "station.wright_bench"}
KIND_COL = {"casing": (120, 120, 126), "charge": (60, 58, 56), "filler": (214, 120, 40), "fuze": (200, 190, 150),
            "motor": (150, 110, 70), "guidance": (70, 150, 200), "round": (170, 60, 50)}
# (item, count) per part kind and Age index; shot casings are stone in Age I
ING = {
    "casing": {"ancient": ("minecraft:clay_ball", 2), "ailean": ("minecraft:iron_ingot", 1), "holy": ("minecraft:iron_ingot", 2)},
    "charge": {"ancient": ("minecraft:charcoal", 1), "ailean": ("minecraft:gunpowder", 1), "holy": ("minecraft:gunpowder", 2)},
    "filler": {"ancient": ("minecraft:coal", 1), "ailean": ("minecraft:blaze_powder", 1), "holy": ("minecraft:blaze_powder", 2)},
    "fuze": {"ancient": ("minecraft:string", 1), "ailean": ("minecraft:string", 1), "holy": ("minecraft:string", 2)},
    "motor": {"ancient": ("minecraft:bamboo", 2), "ailean": ("minecraft:paper", 2), "holy": ("minecraft:iron_nugget", 4)},
}
OVERRIDE = {"stone_shot_casing": ("minecraft:cobblestone", 2), "pitch_arrow_casing": ("minecraft:flint", 1),
            "fire_arrow_rocket_casing": ("minecraft:flint", 1)}


def title(s):
    return s.replace("_", " ").title()


def icon(path, col, big):
    im = Image.new("RGBA", (64, 64))
    d = ImageDraw.Draw(im)
    r = 22 if big else 14
    d.ellipse([32 - r, 32 - r, 32 + r, 32 + r], fill=col + (255,), outline=(28, 28, 28, 255), width=3)
    d.ellipse([32 - r // 2, 32 - r + 4, 32 - 2, 32 - 4], fill=tuple(min(255, c + 60) for c in col) + (255,))
    im.save(path)


def build(root, assets, wr, lang):
    """root = data/<ns>, assets = assets/<ns>; wr(path, obj) writes json; lang dict is extended."""
    cat = json.load(open(CATALOG))
    parts = [p for p in cat["parts"] if p["age"] in AGES]
    rounds = [r for r in cat["rounds"] if r["age"] in AGES]
    sr = os.path.join(root, "diaithir_stations", "station_recipe")
    for d in (sr, os.path.join(assets, "textures", "item"), os.path.join(assets, "models", "item"), os.path.join(assets, "items")):
        os.makedirs(d, exist_ok=True)
    for f in os.listdir(sr):
        if f.startswith("munition_"):
            os.remove(os.path.join(sr, f))
    ids = []

    def item(iid, name, col, big):
        n = "munition_" + iid
        ids.append(n)
        icon(os.path.join(assets, "textures", "item", n + ".png"), col, big)
        wr(os.path.join(assets, "models", "item", n + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{n}"}})
        wr(os.path.join(assets, "items", n + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{n}"}})
        lang[f"item.{NS}.{n}"] = name

    for p in parts:
        item(p["id"], title(p["id"]), KIND_COL[p["kind"]], False)
        ing, n = OVERRIDE.get(p["id"]) or ING[p["kind"]][p["age"]]
        wr(os.path.join(sr, f"munition_{p['id']}.json"), {"station": STATION[p["age"]], "stage": "part", "time_s": 4,
            "ingredients": [{"item": ing, "count": n}], "result": {"id": f"{NS}:munition_{p['id']}", "count": 1}})
    for r in rounds:
        item(r["id"], title(r["id"]), KIND_COL["round"], True)
        wr(os.path.join(sr, f"munition_{r['id']}.json"), {"station": STATION[r["age"]], "stage": "assembly", "time_s": 6,
            "ingredients": [{"item": f"{NS}:munition_{x}", "count": 1} for x in r["parts"]],
            "result": {"id": f"{NS}:munition_{r['id']}", "count": 1}})
    with open(os.path.join(FOUNDRY, "src", "main", "resources", "munition_items.txt"), "w") as f:
        f.write("\n".join(ids) + "\n")
    return len(parts), len(rounds)
