#!/usr/bin/env python3
"""Writes every original texture, model, blockstate, item definition, loot table, recipe, tag, lang entry, age gate and library entry
for diaithir-forgeworks. Run from the module root: python3 tools/gen_assets.py. All art is drawn here from flat colours."""
import json, os, random
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
LIB = os.path.join(HERE, "..", "..", "library-sync", "diaithirforgeworks.json")
NS = "diaithirforgeworks"
KINDS = {  # id: display, front kind
    "crusher": "Brakkulon", "alloy_furnace": "Meldrathex", "sawmill": "Skerrivane", "crucible": "Ulmarkesh"}
AUGMENTS = {"speed_augment": "Veloqith", "efficiency_augment": "Thrimvael", "chance_augment": "Ostravine"}
# tier: (plate, light, dark, accent) real materials: iron plate with copper, bronze plate with iron, graphite steel with gold
TIERS = {4: ((126, 130, 136), (160, 164, 170), (72, 75, 80), (184, 115, 51)),
         5: ((168, 122, 58), (206, 158, 84), (104, 72, 32), (150, 154, 160)),
         6: ((66, 63, 72), (102, 98, 110), (34, 32, 39), (228, 188, 58))}
BLACK, RIVET = (18, 19, 22), (190, 192, 196)
FIRE, FIRE2 = (240, 110, 30), (255, 200, 70)


def A(c): return tuple(c) + (255,)
def clamp(v): return max(0, min(255, v))


def plate(age, seed):
    base, light, dark, acc = TIERS[age]
    img = Image.new("RGBA", (16, 16), A(base)); r = random.Random(seed * 31 + age)
    for x in range(16):
        for y in range(16):
            if r.random() < 0.2:
                d = r.choice((-9, 7)); img.putpixel((x, y), A([clamp(c + d) for c in base]))
    for i in range(16):
        for p in ((i, 0), (i, 15), (0, i), (15, i)): img.putpixel(p, A(dark))
        if 1 <= i <= 14:
            img.putpixel((i, 1), A(light)); img.putpixel((1, i), A(light))
    for p in ((2, 2), (13, 2), (2, 13), (13, 13)): img.putpixel(p, A(RIVET))
    return img


def side(age):
    img = plate(age, 1); acc = TIERS[age][3]; dark = TIERS[age][2]
    for x in range(2, 14):
        img.putpixel((x, 7), A(dark)); img.putpixel((x, 8), A(acc)); img.putpixel((x, 9), A(acc)); img.putpixel((x, 10), A(dark))
    return img


def top(age):
    img = plate(age, 2); dark = TIERS[age][2]; acc = TIERS[age][3]
    for y in range(4, 12, 2):
        for x in range(4, 12): img.putpixel((x, y), A(BLACK))
    for x in (4, 11):
        for y in range(4, 11): img.putpixel((x, y), A(dark))
    img.putpixel((13, 4), A(acc)); img.putpixel((13, 5), A(acc))
    return img


def bottom(age):
    img = plate(age, 3); dark = TIERS[age][2]
    for i in range(3, 13):
        img.putpixel((i, 3), A(dark)); img.putpixel((i, 12), A(dark)); img.putpixel((3, i), A(dark)); img.putpixel((12, i), A(dark))
    return img


def window(img, age):
    dark = TIERS[age][2]
    for x in range(3, 13):
        for y in range(3, 12): img.putpixel((x, y), A(BLACK))
    for x in range(2, 14):
        img.putpixel((x, 2), A(dark)); img.putpixel((x, 12), A(dark))
    for y in range(2, 13):
        img.putpixel((2, y), A(dark)); img.putpixel((13, y), A(dark))


def front(kind, age, lit):
    img = plate(age, 4); acc = TIERS[age][3]; window(img, age)
    steel, steel_l = (112, 118, 126), (160, 166, 174)
    if kind == "crusher":
        for cx in (5, 10):  # two toothed rollers
            for y in range(3, 12):
                img.putpixel((cx, y), A(steel)); img.putpixel((cx + 1, y), A(steel_l))
                if y % 2 == 0: img.putpixel((cx - 1, y), A(steel)); img.putpixel((cx + 2, y), A(steel))
        if lit:
            for y in range(3, 12): img.putpixel((8, y), A(FIRE)); img.putpixel((7, y), A((120, 50, 16)))
    elif kind == "alloy_furnace":
        for x in range(4, 12):  # brick arch mouth
            for y in range(6, 12): img.putpixel((x, y), A((130, 56, 44) if (x + y) % 3 else (92, 40, 32)))
        for x in range(5, 11):
            for y in range(7, 12): img.putpixel((x, y), A(BLACK))
        if lit:
            for x in range(5, 11):
                for y in range(8, 12): img.putpixel((x, y), A(FIRE if (x + y) % 2 else FIRE2))
            img.putpixel((7, 7), A(FIRE2)); img.putpixel((8, 7), A(FIRE))
    elif kind == "sawmill":
        cx, cy = 7.5, 7.5
        for x in range(3, 13):
            for y in range(3, 12):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if d <= 17: img.putpixel((x, y), A(steel_l if d > 6 else steel))
                if d <= 2: img.putpixel((x, y), A(BLACK))
        for x, y in ((3, 7), (12, 8), (7, 3), (8, 11), (4, 4), (11, 4), (4, 10), (11, 10)): img.putpixel((x, y), A(steel_l))
        if lit:
            for x, y in ((11, 5), (12, 6), (3, 9), (4, 10)): img.putpixel((x, y), A(FIRE2))
    elif kind == "crucible":
        for x in range(4, 12):  # bowl
            for y in range(5, 11):
                if (x - 7.5) ** 2 / 16 + (y - 5) ** 2 / 28 <= 1: img.putpixel((x, y), A(steel))
        for x in range(4, 12): img.putpixel((x, 5), A(steel_l))
        for x in range(5, 11):
            for y in (6, 7): img.putpixel((x, y), A(FIRE if lit else (70, 40, 30)))
        if lit:
            for x in (6, 8, 9): img.putpixel((x, 6), A(FIRE2))
    for i, x in enumerate(range(3, 13, 3)): img.putpixel((x, 13), A(acc))  # status lamps under the window
    return img


def item_img(): return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def dust(col):
    img = item_img(); r = random.Random(sum(col))
    for y in range(6, 14):
        w = (y - 5) * 1.0 + 1
        for x in range(16):
            if abs(x - 7.5) <= w and abs(x - 7.5) <= 6.5 and r.random() < 0.97:
                d = r.choice((-24, -10, 0, 0, 12)); img.putpixel((x, y), A([clamp(c + d) for c in col]))
    for x, y in ((7, 5), (8, 5), (6, 6)): img.putpixel((x, y), A(col))
    return img


def ingot():
    img = item_img(); base, light, dark = (180, 126, 62), (222, 168, 96), (112, 72, 34)
    for y in range(5, 12):
        for x in range(2 + (11 - y) // 2, 13 + (11 - y) // 2 - 0):
            if 1 <= x <= 14: img.putpixel((x, y), A(base))
    for x in range(4, 15):
        img.putpixel((min(14, x), 5), A(light))
    for y in range(5, 12):
        xs = [x for x in range(16) if img.getpixel((x, y))[3]]
        if xs: img.putpixel((xs[0], y), A(light)); img.putpixel((xs[-1], y), A(dark))
    for x in range(16):
        if img.getpixel((x, 11))[3]: img.putpixel((x, 11), A(dark))
    return img


def augment(kind):
    img = item_img(); card, edge, pin = (46, 50, 56), (88, 94, 102), (214, 176, 70)
    for x in range(3, 13):
        for y in range(2, 14): img.putpixel((x, y), A(card))
    for x in range(3, 13):
        img.putpixel((x, 2), A(edge)); img.putpixel((x, 13), A(edge))
    for y in range(2, 14):
        img.putpixel((3, y), A(edge)); img.putpixel((12, y), A(edge))
    for x in (4, 6, 8, 10): img.putpixel((x, 14), A(pin)); img.putpixel((x, 15), A(pin))
    if kind == "speed_augment":
        c = (232, 96, 48)
        for i in range(4):
            for d in range(3): img.putpixel((4 + i + d, 4 + i), A(c)); img.putpixel((4 + i + d, 11 - i), A(c))
    elif kind == "efficiency_augment":
        c = (80, 200, 110)
        for y in range(4, 12): img.putpixel((7, y), A(c)); img.putpixel((8, y), A(c))
        for i in range(3): img.putpixel((5 + i, 8 - i), A(c)); img.putpixel((10 - i, 8 - i), A(c))
        for x in range(5, 11): img.putpixel((x, 11), A(c))
    else:
        c = (110, 150, 240)
        for x in range(5, 11):
            for y in range(5, 11): img.putpixel((x, y), A(c))
        for p in ((6, 6), (9, 6), (7, 8), (6, 9), (9, 9)): img.putpixel(p, A((20, 30, 60)))
    return img


def write(path, data):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(data, f, indent=2); f.write("\n")


def png(path, img):
    path = os.path.join(ROOT, path); os.makedirs(os.path.dirname(path), exist_ok=True); img.save(path)


def item_def(name, model): write(f"assets/{NS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:{model}"}})


def flat_item(name, img):
    png(f"assets/{NS}/textures/item/{name}.png", img)
    write(f"assets/{NS}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    item_def(name, f"item/{name}")


def loot(name):
    write(f"data/{NS}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{NS}:{name}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})


def shaped(name, pattern, key, result, count=1):
    write(f"data/{NS}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc", "key": key, "pattern": pattern,
                                            "result": {"id": result, "count": count}})


def mrec(kind, name, inputs, result=None, bonus=None, chance=0.0, fluid=None, energy=2400, time=200):
    d = {"type": f"{NS}:{kind}", "inputs": [{"ingredient": i, "count": c} for i, c in inputs], "energy": energy, "time": time}
    if result: d["result"] = {"id": result[0], "count": result[1]}
    if bonus: d["bonus"] = {"id": bonus, "count": 1}; d["bonus_chance"] = chance
    if fluid: d["fluid"] = {"id": fluid[0], "amount": fluid[1]}
    write(f"data/{NS}/recipe/{kind}/{name}.json", d)


def main():
    lang, ids, lib = {}, {}, []
    machine_names = []
    for age in (4, 5, 6):
        png(f"assets/{NS}/textures/block/age{age}_side.png", side(age))
        png(f"assets/{NS}/textures/block/age{age}_top.png", top(age))
        png(f"assets/{NS}/textures/block/age{age}_bottom.png", bottom(age))
        for kind, disp in KINDS.items():
            name = f"{kind}_age{age}"; machine_names.append(name)
            png(f"assets/{NS}/textures/block/{name}_front.png", front(kind, age, False))
            png(f"assets/{NS}/textures/block/{name}_front_lit.png", front(kind, age, True))
            for lit in (False, True):
                write(f"assets/{NS}/models/block/{name}{'_lit' if lit else ''}.json", {
                    "parent": "minecraft:block/orientable_with_bottom",
                    "textures": {"top": f"{NS}:block/age{age}_top", "bottom": f"{NS}:block/age{age}_bottom", "side": f"{NS}:block/age{age}_side",
                                 "front": f"{NS}:block/{name}_front{'_lit' if lit else ''}"}})
            variants = {}
            for fc, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                for act in ("false", "true"):
                    v = {"model": f"{NS}:block/{name}{'_lit' if act == 'true' else ''}"}
                    if y: v["y"] = y
                    variants[f"active={act},facing={fc}"] = v
            write(f"assets/{NS}/blockstates/{name}.json", {"variants": variants})
            item_def(name, f"block/{name}")
            loot(name)
            lang[f"block.{NS}.{name}"] = f"{disp} (Age {['', '', '', '', 'IV', 'V', 'VI'][age]})"
    for name, disp in AUGMENTS.items():
        flat_item(name, augment(name)); lang[f"item.{NS}.{name}"] = disp + " Augment"
    mats = {"iron_dust": ((190, 194, 200), "Iron Dust"), "copper_dust": ((196, 112, 70), "Copper Dust"), "gold_dust": ((240, 200, 60), "Gold Dust"),
            "sawdust": ((206, 170, 112), "Sawdust")}
    for n, (c, d) in mats.items():
        flat_item(n, dust(c)); lang[f"item.{NS}.{n}"] = d
    flat_item("bronze_ingot", ingot()); lang[f"item.{NS}.bronze_ingot"] = "Bronze Ingot"
    lang[f"itemGroup.{NS}"] = "Diaithir Forgeworks"
    write(f"assets/{NS}/lang/en_us.json", dict(sorted(lang.items())))

    write("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:{n}" for n in machine_names]})
    write("data/minecraft/tags/block/needs_stone_tool.json", {"values": [f"{NS}:{n}" for n in machine_names]})
    for d, items in (("dusts/iron", ["iron_dust"]), ("dusts/copper", ["copper_dust"]), ("dusts/gold", ["gold_dust"]), ("ingots/bronze", ["bronze_ingot"]),
                     ("dusts/wood", ["sawdust"]), ("dusts", ["iron_dust", "copper_dust", "gold_dust", "sawdust"]), ("ingots", ["bronze_ingot"])):
        write(f"data/c/tags/item/{d}.json", {"values": [f"{NS}:{i}" for i in items]})

    # age gate: Age IV, V, VI casings, augments with Age V, materials with Age IV
    gate = {"age4": {"age": 4, "items": [f"{NS}:{n}" for n in machine_names if n.endswith("age4")] + [f"{NS}:{n}" for n in list(mats) + ["bronze_ingot"]]},
            "age5": {"age": 5, "items": [f"{NS}:{n}" for n in machine_names if n.endswith("age5")] + [f"{NS}:{n}" for n in AUGMENTS]},
            "age6": {"age": 6, "items": [f"{NS}:{n}" for n in machine_names if n.endswith("age6")]}}
    write(f"data/{NS}/age_gate.json", gate)

    # crafting
    I, C, R, B, D = "minecraft:iron_ingot", "minecraft:copper_ingot", "minecraft:redstone", f"{NS}:bronze_ingot", "minecraft:diamond"
    base = {"crusher": (["IFI", "RPR", "ICI"], {"I": I, "F": "minecraft:flint", "R": R, "P": "minecraft:piston", "C": C}),
            "alloy_furnace": (["ICI", "RFR", "ICI"], {"I": I, "C": C, "R": R, "F": "minecraft:furnace"}),
            "sawmill": (["III", "RSR", "ICI"], {"I": I, "R": R, "S": "minecraft:stonecutter", "C": C}),
            "crucible": (["ICI", "RUR", "IFI"], {"I": I, "C": C, "R": R, "U": "minecraft:cauldron", "F": "minecraft:furnace"})}
    for k, (pat, key) in base.items():
        shaped(f"craft/{k}_age4", pat, key, f"{NS}:{k}_age4")
        shaped(f"craft/{k}_age5", [" B ", "BMB", " B "], {"B": B, "M": f"{NS}:{k}_age4"}, f"{NS}:{k}_age5")
        shaped(f"craft/{k}_age6", ["BDB", "DMD", "BDB"], {"B": B, "D": D, "M": f"{NS}:{k}_age5"}, f"{NS}:{k}_age6")
    shaped("craft/speed_augment", ["RGR", "GCG", "RGR"], {"R": R, "G": "minecraft:gold_ingot", "C": C}, f"{NS}:speed_augment")
    shaped("craft/efficiency_augment", ["RCR", "CLC", "RCR"], {"R": R, "C": C, "L": "minecraft:lapis_lazuli"}, f"{NS}:efficiency_augment")
    shaped("craft/chance_augment", ["RQR", "QAQ", "RQR"], {"R": R, "Q": "minecraft:quartz", "A": "minecraft:amethyst_shard"}, f"{NS}:chance_augment")
    for dname, ing in (("iron", "minecraft:iron_ingot"), ("copper", "minecraft:copper_ingot"), ("gold", "minecraft:gold_ingot")):
        for typ, cook in (("smelting", 200), ("blasting", 100)):
            write(f"data/{NS}/recipe/{typ}/{dname}_dust.json", {"type": f"minecraft:{typ}", "category": "misc", "ingredient": f"{NS}:{dname}_dust",
                                                               "result": {"id": ing}, "experience": 0.7, "cookingtime": cook})

    # machine recipes (data driven; datapacks and KubeJS add more of the same shape)
    d = lambda n: f"{NS}:{n}_dust"
    mrec("crusher", "iron_ore", [("#c:ores/iron", 1)], (d("iron"), 2), d("copper"), 0.1)
    mrec("crusher", "copper_ore", [("#c:ores/copper", 1)], (d("copper"), 2), d("iron"), 0.1)
    mrec("crusher", "gold_ore", [("#c:ores/gold", 1)], (d("gold"), 2), "minecraft:redstone", 0.15)
    mrec("crusher", "raw_iron", [("#c:raw_materials/iron", 1)], (d("iron"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "raw_copper", [("#c:raw_materials/copper", 1)], (d("copper"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "raw_gold", [("#c:raw_materials/gold", 1)], (d("gold"), 1), None, 0, energy=1200, time=100)
    mrec("crusher", "cobblestone", [("minecraft:cobblestone", 1)], ("minecraft:gravel", 1), None, 0, energy=800, time=80)
    mrec("crusher", "gravel", [("minecraft:gravel", 1)], ("minecraft:sand", 1), "minecraft:flint", 0.1, energy=800, time=80)
    mrec("crusher", "blaze_rod", [("minecraft:blaze_rod", 1)], ("minecraft:blaze_powder", 4), None, 0, energy=1200, time=100)
    mrec("crusher", "bone", [("minecraft:bone", 1)], ("minecraft:bone_meal", 6), None, 0, energy=800, time=80)
    mrec("alloy_furnace", "bronze", [(C, 3), (I, 1)], (f"{NS}:bronze_ingot", 4), energy=3200, time=200)
    mrec("alloy_furnace", "bronze_from_dust", [(d("copper"), 3), (d("iron"), 1)], (f"{NS}:bronze_ingot", 4), energy=2400, time=160)
    woods = [("oak", "logs"), ("spruce", "logs"), ("birch", "logs"), ("jungle", "logs"), ("acacia", "logs"), ("dark_oak", "logs"), ("cherry", "logs"),
             ("mangrove", "logs"), ("crimson", "stems"), ("warped", "stems")]
    for w, suffix in woods:
        tag = f"#minecraft:{w}_{suffix}"
        mrec("sawmill", w, [(tag, 1)], (f"minecraft:{w}_planks", 6), f"{NS}:sawdust", 0.25, energy=1600, time=100)
    mrec("sawmill", "bamboo_block", [("minecraft:bamboo_block", 1)], ("minecraft:bamboo_planks", 6), f"{NS}:sawdust", 0.25, energy=1600, time=100)
    mrec("crucible", "cobblestone", [("minecraft:cobblestone", 1)], fluid=("minecraft:lava", 250), energy=6000, time=400)
    mrec("crucible", "netherrack", [("minecraft:netherrack", 1)], fluid=("minecraft:lava", 100), energy=3000, time=200)
    mrec("crucible", "obsidian", [("minecraft:obsidian", 1)], fluid=("minecraft:lava", 1000), energy=20000, time=800)

    # library entries
    tiername = {4: "Age IV", 5: "Age V", 6: "Age VI"}
    desc = {"crusher": "Crusher. Feed it ore and FE and it grinds it to dust, with a chance of a bonus item.",
            "alloy_furnace": "Alloy furnace. Takes two inputs and fuses them to an alloy, for example 3 copper and 1 iron to 4 bronze.",
            "sawmill": "Sawmill. Turns a log into 6 planks, with a chance of sawdust.",
            "crucible": "Crucible. Melts items into fluid, for example cobblestone into 250 mB of lava. Pipes or a bucket take it out."}
    speed = {4: "1x", 5: "2x", 6: "4x"}
    for age in (4, 5, 6):
        for kind, disp in KINDS.items():
            lib.append({"id": f"{NS}:{kind}_age{age}", "name": f"{disp} ({tiername[age]})",
                        "text": f"{desc[kind]} {tiername[age]} casing, {speed[age]} speed. Has GUI, FE bar, redstone control and per-face side config."})
    lib += [{"id": f"{NS}:speed_augment", "name": "Veloqith Augment", "text": "Augment. Put up to 4 in the augment slot: each adds 50% speed for a little extra FE per job."},
            {"id": f"{NS}:efficiency_augment", "name": "Thrimvael Augment", "text": "Augment. Put up to 4 in the augment slot: each cuts the FE cost of a job by 15%."},
            {"id": f"{NS}:chance_augment", "name": "Ostravine Augment", "text": "Augment. Put up to 4 in the augment slot: each raises the chance of bonus outputs by half again."},
            {"id": f"{NS}:iron_dust", "name": "Iron Dust", "text": "Ground iron ore. Smelt it back to an ingot."},
            {"id": f"{NS}:copper_dust", "name": "Copper Dust", "text": "Ground copper ore. Smelt it back to an ingot."},
            {"id": f"{NS}:gold_dust", "name": "Gold Dust", "text": "Ground gold ore. Smelt it back to an ingot."},
            {"id": f"{NS}:sawdust", "name": "Sawdust", "text": "Bonus output of the sawmill."},
            {"id": f"{NS}:bronze_ingot", "name": "Bronze Ingot", "text": "Alloy of copper and iron from the alloy furnace. Used in Age V casings."}]
    os.makedirs(os.path.dirname(LIB), exist_ok=True)
    with open(LIB, "w") as f:
        f.write("[\n" + ",\n".join("  " + json.dumps(e, ensure_ascii=False) for e in lib) + "\n]\n")


main()
