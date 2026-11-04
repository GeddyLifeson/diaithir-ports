"""Munitions for the Foundry stations (plans/MUNITIONS.md, MUNITIONS_CONTAINERS.md): part, round, container and link items,
64 px icons, lang, station recipes, c: tags, codex and quest, all generated from diaithir-forgeworks/munitions/catalog.json.
Called by gen_assets.py. Game content only; ingredients are everyday items, no process detail."""
import json, os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
FOUNDRY = os.path.normpath(os.path.join(HERE, ".."))
CATALOG = os.path.normpath(os.path.join(FOUNDRY, "..", "..", "diaithir-forgeworks", "munitions", "catalog.json"))
NS = "diaithircrafting"
AGES = ["ancient", "ailean", "holy", "sundering", "strangers", "kingdom", "dubhan", "future"]
ROMAN = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII"]
STATION = ["station.hearth_bench", "station.hearth_bench", "station.wright_bench", "station.smeltery",
           "station.steam_forge", "machine.assembler", "machine.assembler", "station.fabricator"]
STEAM_W = 20000.0  # watts drawn for steam-forge jobs (Age V)
KIND_COL = {"casing": (120, 120, 126), "charge": (60, 58, 56), "filler": (214, 120, 40), "fuze": (200, 190, 150),
            "motor": (150, 110, 70), "guidance": (70, 150, 200), "round": (170, 60, 50), "container": (90, 130, 90), "link": (180, 180, 190)}
# (item or #tag, count) per part kind, by Age index
ING = {
    "casing": [("minecraft:clay_ball", 2), ("minecraft:iron_ingot", 1), ("minecraft:iron_ingot", 2), ("minecraft:iron_ingot", 3),
               ("#c:ingots/steel", 2), ("#c:plates/steel", 2), ("#c:plates/steel", 3), ("#c:ores/shard_metal", 2)],
    "charge": [("minecraft:charcoal", 1), ("minecraft:gunpowder", 1), ("minecraft:gunpowder", 2), ("minecraft:gunpowder", 3),
               ("minecraft:blaze_powder", 2), ("minecraft:blaze_powder", 3), ("minecraft:glowstone_dust", 3), ("minecraft:dragon_breath", 1)],
    "filler": [("minecraft:coal", 1), ("minecraft:blaze_powder", 1), ("minecraft:blaze_powder", 2), ("minecraft:gunpowder", 3),
               ("minecraft:tnt", 1), ("minecraft:tnt", 2), ("minecraft:tnt", 3), ("#c:ores/shard_metal", 1)],
    "fuze": [("minecraft:string", 1), ("minecraft:string", 1), ("minecraft:string", 2), ("minecraft:string", 2),
             ("minecraft:redstone", 1), ("minecraft:redstone", 2), ("minecraft:redstone", 3), ("minecraft:echo_shard", 1)],
    "motor": [("minecraft:bamboo", 2), ("minecraft:paper", 2), ("minecraft:iron_nugget", 4), ("minecraft:iron_nugget", 6),
              ("minecraft:copper_ingot", 2), ("minecraft:copper_ingot", 3), ("minecraft:iron_ingot", 4), ("minecraft:breeze_rod", 2)],
    "guidance": [("minecraft:compass", 1)] * 4 + [("minecraft:compass", 1), ("minecraft:comparator", 1), ("minecraft:observer", 1), ("minecraft:recovery_compass", 1)],
}
OVERRIDE = {"stone_shot_casing": ("minecraft:cobblestone", 2), "pitch_arrow_casing": ("minecraft:flint", 1),
            "fire_arrow_rocket_casing": ("minecraft:flint", 1)}
SPRING = ("minecraft:iron_nugget", 3)


def title(s):
    return s.replace("_", " ").title()


def ing(spec):
    item, n = spec
    return {"tag": item[1:], "count": n} if item.startswith("#") else {"item": item, "count": n}


def icon(path, col, big):
    im = Image.new("RGBA", (64, 64))
    d = ImageDraw.Draw(im)
    r = 22 if big else 14
    d.ellipse([32 - r, 32 - r, 32 + r, 32 + r], fill=col + (255,), outline=(28, 28, 28, 255), width=3)
    d.ellipse([32 - r // 2, 32 - r + 4, 32 - 2, 32 - 4], fill=tuple(min(255, c + 60) for c in col) + (255,))
    im.save(path)


def build(root, assets, wr, lang):
    """root = data/<ns>, assets = assets/<ns>; wr(path, obj) writes json; lang dict is extended. Returns {item id: Age roman}."""
    cat = json.load(open(CATALOG))
    sr = os.path.join(root, "diaithir_stations", "station_recipe")
    tags = os.path.join(FOUNDRY, "src", "main", "resources", "data", "c", "tags", "item")
    ntags = os.path.join(root, "tags", "item")
    for d in (sr, os.path.join(assets, "textures", "item"), os.path.join(assets, "models", "item"), os.path.join(assets, "items")):
        os.makedirs(d, exist_ok=True)
    for f in os.listdir(sr):
        if f.startswith("munition_"):
            os.remove(os.path.join(sr, f))
    lines, ages_of = [], {}
    tag_items = {}

    def item(iid, name, col, big, age, extra=""):
        lines.append(iid + extra)
        ages_of[f"{NS}:{iid}"] = ROMAN[AGES.index(age)]
        icon(os.path.join(assets, "textures", "item", iid + ".png"), col, big)
        wr(os.path.join(assets, "models", "item", iid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
        wr(os.path.join(assets, "items", iid + ".json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}})
        lang[f"item.{NS}.{iid}"] = name

    def recipe(fname, age, stage, ingredients, rid, count=1, secs=None):
        a = AGES.index(age)
        r = {"station": STATION[a], "stage": stage, "time_s": secs or 4 + 2 * a, "ingredients": ingredients,
             "result": {"id": f"{NS}:{rid}", "count": count}}
        if a == 4:
            r.update(power="steam", w=STEAM_W)
        if a == 6:
            r["operator"] = True
        wr(os.path.join(sr, fname + ".json"), r)

    for p in cat["parts"]:
        item("munition_" + p["id"], title(p["id"]), KIND_COL[p["kind"]], False, p["age"])
        recipe("munition_" + p["id"], p["age"], "part", [ing(OVERRIDE.get(p["id"]) or ING[p["kind"]][AGES.index(p["age"])])], "munition_" + p["id"])
    for r in cat["rounds"]:
        item("munition_" + r["id"], title(r["id"]), KIND_COL["round"], True, r["age"])
        recipe("munition_" + r["id"], r["age"], "assembly", [{"item": f"{NS}:munition_{x}", "count": 1} for x in r["parts"]],
               "munition_" + r["id"], secs=6 + 2 * AGES.index(r["age"]))
        for t in r["tags"]:
            tag_items.setdefault(t, []).append(f"{NS}:munition_{r['id']}")
    for c in cat["containers"]:
        a = AGES.index(c["age"])
        item(c["id"], title(c["id"]), KIND_COL["container"], True, c["age"], f"|{c['class']}|{c['capacity']}")
        needs = [ing(ING["casing"][a]), ing(SPRING)]
        if c["link"]:
            needs = [{"item": f"{NS}:{c['link']}", "count": 8}, ing(SPRING)]
            item(c["link"], title(c["link"]), KIND_COL["link"], False, c["age"])
            recipe("munition_" + c["link"], c["age"], "part", [ing(ING["casing"][a])], c["link"], count=8)
        recipe("munition_container_" + c["id"], c["age"], "assembly", needs, c["id"])
        for t in c["tags"]:
            tag_items.setdefault(t, []).append(f"{NS}:{c['id']}")
    for t, vals in sorted(tag_items.items()):
        ns, path = t.split(":", 1)
        base = tags if ns == "c" else ntags
        wr(os.path.join(base, path + ".json"), {"values": sorted(vals)})
    # parents: c:ammo/<line> lists its per-Age subtags; empty hooks for guns (other threads add values)
    for line in {r["line"] for r in cat["rounds"]}:
        subs = sorted({t for r in cat["rounds"] if r["line"] == line for t in r["tags"] if t.count("/") == 2})
        wr(os.path.join(tags, "ammo", line + ".json"), {"values": ["#" + s for s in subs]})
    for k in sorted({c["kind"] for c in cat["containers"]}):
        wr(os.path.join(ntags, "uses_container", k + ".json"), {"values": []})
    with open(os.path.join(FOUNDRY, "src", "main", "resources", "munition_items.txt"), "w") as f:
        f.write("\n".join(lines) + "\n")
    wr(os.path.join(FOUNDRY, "diaithir-data", "codex", "munitions.json"), {
        "id": f"{NS}:codex/munitions", "name_en": "Munitions",
        "what": "Shot, shells, rockets, bombs, torpedoes and missiles for ships, aircraft, trains and tanks, plus the magazines, drums, belts and boxes that carry them. Each part is cast or made at a station, then a round is assembled in one step. Older stages stay craftable.",
        "connects": {"stations": "hearth bench to fabricator, one per Age", "tags": "c:ammo/<line>, c:ammo_container/<kind>",
                     "component": f"{NS}:ammo_container {{class, capacity, count, round}}", "plan": "plans/MUNITIONS.md",
                     "quest": f"{NS}:quest/first_munition"}})
    wr(os.path.join(FOUNDRY, "diaithir-data", "quests", "first_munition.json"), {
        "id": f"{NS}:quest/first_munition", "title": "Cast your first round",
        "steps": ["Cast a stone shot casing and a charge at the hearth bench", "Assemble them into a stone shot",
                  "Keep rounds in an armoured magazine: filled rounds can chain-detonate when their block breaks"],
        "next": f"{NS}:quest/first_operator"})
    return ages_of
