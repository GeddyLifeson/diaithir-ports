#!/usr/bin/env python3
"""Writes the benches' blocks, recipes, lang and the tintable part layers for every blueprint (reads the generated blueprint json)."""
import json, os
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources")
A = os.path.join(ROOT, "assets/diaithirgear"); D = os.path.join(ROOT, "data/diaithirgear"); W = os.path.join(D, "workshop")
def put(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(obj, open(path, "w"), indent=1)

bps = json.load(open(W + "/blueprints_arms.json")) + json.load(open(W + "/blueprints_guns.json"))
mats = json.load(open(W + "/materials_base.json")); imps = json.load(open(W + "/improvements_base.json"))

# ---- bench blocks ----------------------------------------------------------------------------------------------------------
BENCH = {"smithy": ("Smithy", (70, 70, 78), (120, 120, 130)), "gunsmith_bench": ("Gunsmith Bench", (50, 62, 80), (110, 130, 160)), "workshop_bench": ("Workshop Bench", (110, 78, 44), (170, 130, 80))}
for bid, (name, dark, light) in BENCH.items():
    for part in ("top", "side", "bottom"):
        im = Image.new("RGB", (16, 16), dark if part != "top" else light); d = ImageDraw.Draw(im)
        for i in range(0, 16, 4): d.line([(0, i), (15, i)], fill=tuple(c + 14 for c in dark if True) if part == "side" else dark)
        d.rectangle([0, 0, 15, 15], outline=tuple(max(0, c - 30) for c in dark))
        if part == "top": d.rectangle([3, 3, 12, 12], outline=dark); d.line([(5, 8), (10, 8)], fill=dark)
        os.makedirs(A + "/textures/block", exist_ok=True); im.save(A + f"/textures/block/{bid}_{part}.png")
    put(A + f"/models/block/{bid}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {"top": f"diaithirgear:block/{bid}_top", "side": f"diaithirgear:block/{bid}_side", "bottom": f"diaithirgear:block/{bid}_bottom", "particle": f"diaithirgear:block/{bid}_side"}})
    put(A + f"/blockstates/{bid}.json", {"variants": {"": {"model": f"diaithirgear:block/{bid}"}}})
    put(A + f"/items/{bid}.json", {"model": {"type": "minecraft:model", "model": f"diaithirgear:block/{bid}"}})
    put(D + f"/loot_table/blocks/{bid}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"diaithirgear:{bid}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
put(ROOT + "/data/c/tags/item/ingots/gunmetal.json", {"values": []})  # keeps the gunsmith recipe valid without firearms
put(ROOT + "/data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"diaithirgear:{b}" for b in BENCH]})
def shaped(bid, pat, key): put(D + f"/recipe/{bid}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pat, "key": key, "result": {"id": f"diaithirgear:{bid}", "count": 1}})
shaped("smithy", ["CCC", "CFC", "CCC"], {"C": "minecraft:cobblestone", "F": "minecraft:furnace"})
shaped("workshop_bench", ["PSP", "PCP", "PPP"], {"P": "#minecraft:planks", "S": "minecraft:string", "C": "minecraft:crafting_table"})
shaped("gunsmith_bench", ["GGG", "SAS", "GGG"], {"G": "#c:ingots/gunmetal", "S": "minecraft:smithing_table", "A": "minecraft:anvil"})
put(A + "/items/schematic.json", {"model": {"type": "minecraft:model", "model": "minecraft:item/paper"}})

# ---- tintable part layers --------------------------------------------------------------------------------------------------
def line(d, a, b, w=1, c=235):
    d.line([a, b], fill=(c, c, c, 255), width=w)
def layers(bid):
    """name -> list of draw calls on a 16x16 RGBA canvas, keyed by slot name"""
    L = {}
    def sword(blade_end, handle_start, guard):
        L["head"] = lambda d: (line(d, (7, 8), blade_end, 2), d.point(blade_end, fill=(255, 255, 255, 255)))
        L["guard"] = lambda d: line(d, guard[0], guard[1], 1, 215)
        L["handle"] = lambda d: line(d, (5, 10), handle_start, 1, 200)
        L["binding"] = lambda d: d.point([(4, 11), (3, 12)], fill=(190, 190, 190, 255))
    if bid == "sword": sword((14, 1), (2, 13), ((4, 8), (8, 12)))
    elif bid == "dagger": sword((11, 4), (3, 12), ((5, 9), (8, 12)))
    elif bid in ("axe", "pickaxe", "shovel", "spear"):
        L["handle"] = lambda d: line(d, (2, 14), (11, 5), 1, 200)
        L["binding"] = lambda d: d.point([(9, 7), (10, 6), (8, 8)], fill=(190, 190, 190, 255))
        if bid == "axe": L["head"] = lambda d: d.polygon([(9, 2), (14, 5), (14, 9), (11, 8), (9, 6)], fill=(235, 235, 235, 255))
        elif bid == "pickaxe": L["head"] = lambda d: (line(d, (5, 3), (13, 11), 2), line(d, (4, 4), (4, 6)), line(d, (13, 11), (14, 13)))
        elif bid == "shovel": L["head"] = lambda d: d.ellipse([10, 1, 14, 6], fill=(235, 235, 235, 255))
        else: L["head"] = lambda d: d.polygon([(11, 6), (14, 1), (15, 2), (12, 7)], fill=(240, 240, 240, 255))
    return L
def gun_layers():
    return {"barrel": lambda d: d.rectangle([7, 5, 15, 7], fill=(235, 235, 235, 255)),
            "lock": lambda d: d.rectangle([4, 5, 8, 9], fill=(210, 210, 210, 255)),
            "stock": lambda d: d.polygon([(0, 8), (4, 6), (5, 10), (2, 12)], fill=(200, 200, 200, 255))}
for b in bps:
    if b["kind"] == "ranged": continue
    fn = gun_layers() if b["kind"] == "gun" else layers(b["id"])
    tex = {}; tints = []
    for i, s in enumerate(b["slots"]):
        im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); fn[s["name"]](ImageDraw.Draw(im))
        os.makedirs(A + "/textures/item/workshop", exist_ok=True); im.save(A + f"/textures/item/workshop/{b['id']}_{s['name']}.png")
        tex[f"layer{i}"] = f"diaithirgear:item/workshop/{b['id']}_{s['name']}"
        tints.append({"type": "minecraft:custom_model_data", "index": i, "default": 16777215})
    put(A + f"/models/item/workshop/{b['id']}.json", {"parent": "minecraft:item/handheld" if b["kind"] != "gun" else "minecraft:item/generated", "textures": tex})
    put(A + f"/items/workshop/{b['id']}.json", {"model": {"type": "minecraft:model", "model": f"diaithirgear:item/workshop/{b['id']}", "tints": tints}})

# ---- lang (merged into the existing file) ----------------------------------------------------------------------------------
p = A + "/lang/en_us.json"; lang = json.load(open(p))
lang.update({"block.diaithirgear.smithy": "Smithy", "block.diaithirgear.gunsmith_bench": "Gunsmith Bench", "block.diaithirgear.workshop_bench": "Workshop Bench", "item.diaithirgear.schematic": "Schematic",
    "workshop.diaithirgear.name": "%1$s %2$s", "workshop.diaithirgear.honed": "%1$s is honed to level %2$s", "workshop.diaithirgear.part": "%1$s: %2$s", "workshop.diaithirgear.mass": "Mass %1$s kg",
    "workshop.diaithirgear.hone": "Honing %1$s of %2$s (%3$s uses)", "workshop.diaithirgear.pick": "No blueprint. Sneak with an empty hand to pick one.",
    "workshop.diaithirgear.none": "No blueprints for this bench yet.", "workshop.diaithirgear.cleared": "Parts handed back.", "workshop.diaithirgear.unknown_schematic": "That item is not installed.",
    "workshop.diaithirgear.no_schematic": "You have not learned this schematic.", "workshop.diaithirgear.learned": "Learned %1$s", "workshop.diaithirgear.too_early": "Too early: needs Age %1$s.",
    "workshop.diaithirgear.need_more": "Needs %1$s of that item.", "workshop.diaithirgear.done": "Made %1$s", "workshop.diaithirgear.wrong_bench": "Wrong bench for this item.",
    "workshop.diaithirgear.has_improvement": "Already has that improvement.", "workshop.diaithirgear.full": "No improvement slots left.",
    "workshop.diaithirgear.serviced": "%1$s: honing %2$s, damage %3$s, durability %4$s, %5$s kg", "workshop.diaithirgear.slot_need": "%1$s x %2$s",
    "workshop.diaithirgear.complete": "%1$s is ready. Take it with an empty hand.", "workshop.diaithirgear.missing": "%1$s needs %2$s"})
for b in bps:
    lang[f"blueprint.diaithirgear.{b['id']}"] = b["id"].replace("_", " ").title()
    for s in b["slots"]: lang[f"slot.diaithirgear.{s['name']}"] = s["name"].replace("_", " ").title()
for m in mats: lang[f"material.diaithirgear.{m['id']}"] = m["id"].replace("_", " ").title()
json.dump(lang, open(p, "w"), indent=1, ensure_ascii=False)

# ---- Age gates -------------------------------------------------------------------------------------------------------------
p = os.path.join(ROOT, "..", "..", "..", "age_gates.json"); g = json.load(open(p))
g["items"].update({"diaithirgear:smithy": "I", "diaithirgear:workshop_bench": "I", "diaithirgear:gunsmith_bench": "VI", "diaithirgear:schematic": "IV"})
json.dump(g, open(p, "w"), indent=1)
print(len(bps), "blueprints rendered")
