#!/usr/bin/env python3
"""Writes the crafting recipes and upgrade targets for worldsys items. Ingredients are shared c: tags where one exists so any mod's copy works."""
import json, os
R = "src/main/resources/data/diaithirworldsys/recipe"
U = "src/main/resources/data/diaithirworldsys/upgrades/targets"
T = "src/main/resources/data/c/tags/item/gems"
N = "diaithirworldsys:"
os.makedirs(R, exist_ok=True)
n = 0


def w(name, j):
    global n
    p = os.path.join(R, name + ".json")
    if os.path.exists(p):
        return
    json.dump(j, open(p, "w"), indent=1)
    n += 1


def shaped(name, pattern, key, result, count=1):
    k = {a: ("#" + b[1:] if b.startswith("#") else b) for a, b in key.items()}
    w(name, {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": pattern, "key": k, "result": {"id": result, "count": count}})


def shapeless(name, ings, result):
    w(name, {"type": "minecraft:crafting_shapeless", "category": "misc", "ingredients": ings, "result": {"id": result}})


# pocket workstation sticks: a stick plus the real block
for s, block in [("cartography_table", "cartography_table"), ("grindstone", "grindstone"), ("loom", "loom"), ("smithing_table", "smithing_table"),
                 ("stonecutter", "stonecutter"), ("anvil", "anvil"), ("chipped_anvil", "chipped_anvil"), ("damaged_anvil", "damaged_anvil")]:
    shapeless("stick_" + s, ["minecraft:stick", "minecraft:" + block], N + "crafting_on_a_stick_" + s)

# jewelry
ING = {"iron": "#c:ingots/iron", "gold": "#c:ingots/gold", "copper": "#c:ingots/copper"}
for m in ("copper", "iron", "gold"):
    shaped("jewelry_%s_ring" % m, [" I ", "I I", " I "], {"I": ING[m]}, N + "jewelry_%s_ring" % m)
GEM = {"citrine": "#c:gems/citrine", "jade": "#c:gems/jade", "tanzanite": "#c:gems/tanzanite", "ruby": "#c:gems/ruby",
       "sapphire": "#c:gems/sapphire", "topaz": "#c:gems/topaz", "diamond": "#c:gems/diamond", "emerald": "#c:gems/emerald"}
for g in ("citrine", "jade", "tanzanite", "diamond"):
    shaped("jewelry_%s_ring" % g, [" I ", "IGI", " I "], {"I": ING["gold"], "G": GEM[g]}, N + "jewelry_%s_ring" % g)
for g in ("citrine", "jade", "tanzanite", "ruby", "sapphire", "topaz", "diamond", "emerald"):
    shaped("jewelry_%s_necklace" % g, ["SIS", "S S", " G "], {"S": "#c:strings", "I": ING["gold"], "G": GEM[g]}, N + "jewelry_%s_necklace" % g)
for g in ("citrine", "jade", "tanzanite", "ruby", "sapphire", "topaz"):
    for t in ("ring", "necklace"):
        base = N + ("jewelry_%s_%s" % (g, t) if g in ("citrine", "jade", "tanzanite") or t == "necklace" else "%s_ring" % g)
        shapeless("jewelry_netherite_%s_%s" % (g, t), [base, "#c:ingots/netherite"], N + "jewelry_netherite_%s_%s" % (g, t))

# villager care
IRON, EM = "#c:ingots/iron", "#c:gems/emerald"
shaped("villager_carrier", ["LSL", "SES", "LSL"], {"L": "minecraft:leather", "S": "#c:strings", "E": EM}, N + "easy_villagers_villager")
shaped("trader", ["III", "ELE", "III"], {"I": IRON, "E": EM, "L": "minecraft:lectern"}, N + "easy_villagers_trader")
shaped("breeder", ["III", "HBH", "III"], {"I": IRON, "H": "minecraft:hay_block", "B": "#minecraft:beds"}, N + "easy_villagers_breeder")
shaped("farmer", ["III", "HCH", "III"], {"I": IRON, "H": "#minecraft:hoes", "C": "minecraft:composter"}, N + "easy_villagers_farmer")
shapeless("auto_trader", [N + "easy_villagers_trader", "minecraft:hopper", "#c:dusts/redstone"], N + "easy_villagers_auto_trader")
shaped("converter", ["III", "GBG", "III"], {"I": IRON, "G": "minecraft:golden_apple", "B": "minecraft:brewing_stand"}, N + "easy_villagers_converter")
shapeless("incubator", [N + "easy_villagers_breeder", "minecraft:campfire", "#c:glass_blocks"], N + "easy_villagers_incubator")
shapeless("iron_farm", [N + "easy_villagers_farmer", "#c:storage_blocks/iron", "#c:storage_blocks/iron", "#c:storage_blocks/iron", "#c:storage_blocks/iron",
                        "minecraft:carved_pumpkin"], N + "easy_villagers_iron_farm")
shapeless("inventory_viewer", ["minecraft:spyglass", "#c:chests/wooden"], N + "easy_villagers_inventory_viewer")

# utility blocks
shapeless("angel_block", ["minecraft:feather"] * 4 + ["#c:stones"], N + "angel_block")
shapeless("sky_sand", ["#minecraft:sand", "minecraft:feather"], N + "sky_sand")
shaped("redstone_clock", ["RRR", "RCR", "RRR"], {"R": "#c:dusts/redstone", "C": "minecraft:clock"}, N + "redstone_clock")
shapeless("fluid_hopper", ["minecraft:hopper", "minecraft:bucket"], N + "fluid_hopper")
shapeless("utility_lamp", ["#c:glass_blocks", "minecraft:glowstone", "minecraft:glowstone"], N + "utility_lamp")

# gem tags for the three gems that only exist as items here
for g in ("citrine", "jade", "tanzanite"):
    p = os.path.join(T, g + ".json")
    if not os.path.exists(p):
        json.dump({"replace": False, "values": [N + "jewelry_" + g]}, open(p, "w"), indent=1)

# upgrade targets: working blocks only (data-only opt-in, see plans/UPGRADES.md)
tg = json.load(open(os.path.join(U, "worldsys.json")))
have = {t["id"] for t in tg}
rules = [("villager_breeder", ["easy_villagers_breeder"], 3, ["speed", "efficiency"]),
         ("villager_farmer", ["easy_villagers_farmer"], 3, ["speed", "efficiency", "productivity"]),
         ("villager_auto_trader", ["easy_villagers_auto_trader"], 2, ["speed", "efficiency"]),
         ("villager_converter", ["easy_villagers_converter"], 2, ["speed", "efficiency"]),
         ("villager_incubator", ["easy_villagers_incubator"], 2, ["speed", "efficiency"]),
         ("villager_iron_farm", ["easy_villagers_iron_farm"], 3, ["speed", "efficiency", "productivity"])]
for i, ids, slots, kinds in rules:
    if i not in have:
        tg.append({"id": i, "targets": [N + x for x in ids], "slots": slots, "kinds": kinds, "max_quality": 3})
json.dump(tg, open(os.path.join(U, "worldsys.json"), "w"), indent=1)
print(n, "recipes written;", len(tg), "upgrade targets")
