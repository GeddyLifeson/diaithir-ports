#!/usr/bin/env python3
"""Immersive Armors (GPL-3.0) fold, stage 1: set table, item icons, worn textures, models, recipes, repair tags, lang, age gates.
All art is recoloured from this module's own steel set, not taken from the upstream mod. Stats follow upstream Items.java."""
import json, os
from PIL import Image
HERE = os.path.dirname(__file__); R = os.path.join(HERE, "..", "src", "main", "resources"); NS = "diaithirarms"
# name, dark, light, [helmet, chest, legs, boots], durability, toughness, knockback, weight, water, enchant, repair item/tag, recipe ingredient, sound, effects, antiSkeleton, age
SETS = [
 ("bone", (150, 140, 120), (240, 235, 215), [1, 3, 2, 1], 8, 0, 0, -0.02, 0, 15, "minecraft:bone", "minecraft:bone", "skeleton", [], True, "I"),
 ("wither", (20, 20, 26), (95, 95, 110), [2, 4, 3, 2], 12, 0, 0, -0.01, 0, 0, "minecraft:bone", "minecraft:bone_block", "wither_skeleton", [["wither", 0.25, 20]], True, "V"),
 ("warrior", (70, 50, 40), (200, 190, 180), [2, 5, 6, 2], 15, 1.0, 0, 0, 0, 5, "minecraft:iron_ingot", "minecraft:iron_ingot", "iron", [["berserk", 0.2], ["axe_efficiency", 0.05]], False, "III"),
 ("heavy", (50, 55, 62), (170, 178, 190), [4, 6, 5, 3], 20, 4.0, 0.5, 0.05, 0, 6, "minecraft:iron_ingot", "minecraft:iron_ingot", "iron", [], False, "IV"),
 ("robe", (120, 20, 50), (230, 90, 120), [2, 3, 2, 1], 14, 0, 0, 0, 0, 50, "#minecraft:wool", "#minecraft:wool", "wool", [["fire_resistance", 0.25], ["fire_inflicting", 20], ["magic_protection", 0.2]], False, "II"),
 ("slime", (40, 120, 40), (150, 235, 120), [3, 5, 4, 2], 20, 0, 0.25, 0, 0, 10, "minecraft:slime_ball", "minecraft:slime_ball", "slime", [["bouncing", 0.25], ["explosion_protection", 0.2]], False, "II"),
 ("divine", (190, 150, 40), (255, 240, 170), [3, 7, 5, 3], 18, 0, 0, 0, 0, 30, "minecraft:gold_ingot", "minecraft:gold_ingot", "iron", [["divine", 1200]], False, "V"),
 ("prismarine", (30, 100, 100), (130, 230, 210), [3, 8, 6, 3], 18, 0, 0, 0.02, 0.25, 8, "minecraft:prismarine_crystals", "minecraft:prismarine_crystals", "iron", [["spikes", 1]], False, "III"),
 ("wooden", (80, 55, 30), (190, 150, 90), [1, 3, 2, 1], 8, 0, 0, 0, 0, 4, "#minecraft:logs", "#minecraft:logs", "leather", [["arrow_block", 0.15], ["explosion_protection", 0.1]], False, "I"),
 ("steampunk", (110, 70, 40), (215, 160, 90), [3, 6, 3, 2], 10, 0, 0, 0, 0, 4, "minecraft:copper_ingot", "minecraft:copper_ingot", "redstone", [["explosion_protection", 0.1], ["steam_tech"]], False, "IV"),
]
PIECES = ["helmet", "chestplate", "leggings", "boots"]
PATTERN = {"helmet": ["MMM", "M M"], "chestplate": ["M M", "MMM", "MMM"], "leggings": ["MMM", "M M", "M M"], "boots": ["M M", "M M"]}
def ramp(img, dark, light):
    im = img.convert("RGBA"); px = im.load()
    for x in range(im.width):
        for y in range(im.height):
            r, g, b, a = px[x, y]
            if a == 0: continue
            l = (0.3 * r + 0.59 * g + 0.11 * b) / 255.0
            px[x, y] = tuple(int(dark[i] + (light[i] - dark[i]) * l) for i in range(3)) + (a,)
    return im
def w(path, obj):
    p = os.path.join(R, path); os.makedirs(os.path.dirname(p), exist_ok=True); open(p, "w").write(json.dumps(obj, indent=1))
T = f"{R}/assets/{NS}/textures"
lang = json.load(open(f"{R}/assets/{NS}/lang/en_us.json")); gates = json.load(open(f"{HERE}/../age_gates.json"))
data = []
for (name, dark, light, prot, dur, tough, kb, weight, water, ench, repair, ing, sound, effects, anti, age) in SETS:
    data.append(dict(name=name, protection=prot, durability=dur, toughness=tough, knockback=kb, weight=weight, water=water, enchant=ench, repair=f"{NS}:immersive/repair/{name}", sound=sound, effects=effects, antiSkeleton=anti))
    rt = {"values": [repair]}
    w(f"data/{NS}/tags/item/immersive/repair/{name}.json", {"replace": False, "values": [repair]})
    for d in ("humanoid", "humanoid_leggings", "humanoid_baby"):
        os.makedirs(f"{T}/entity/equipment/{d}", exist_ok=True)
        ramp(Image.open(f"{T}/entity/equipment/{d}/steel.png"), dark, light).save(f"{T}/entity/equipment/{d}/imm_{name}.png")
    w(f"assets/{NS}/equipment/imm_{name}.json", {"layers": {"humanoid": [{"texture": f"{NS}:imm_{name}"}], "humanoid_leggings": [{"texture": f"{NS}:imm_{name}"}], "humanoid_baby": [{"texture": f"{NS}:imm_{name}"}]}})
    for pc in PIECES:
        iid = f"{name}_{pc}_imm"
        ramp(Image.open(f"{T}/item/steel_{pc}.png"), dark, light).save(f"{T}/item/{iid}.png")
        w(f"assets/{NS}/models/item/{iid}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
        w(f"assets/{NS}/items/{iid}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}})
        key = {"M": ({"tag": ing[1:]} if False else ing)}
        w(f"data/{NS}/recipe/{iid}.json", {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": PATTERN[pc], "key": {"M": ing}, "result": {"id": f"{NS}:{iid}", "count": 1}})
        lang[f"item.{NS}.{iid}"] = f"{name.capitalize()} {pc.capitalize()}"
        gates["items"][f"{NS}:{iid}"] = age
w(f"data/{NS}/immersive_sets.json", {"sets": data})
for k, v in {"armorEffect.fireResistance": "Fire resistance %s%%", "armorEffect.magicResistance": "Magic resistance %s%%", "armorEffect.explosionResistance": "Explosion resistance %s%%",
             "armorEffect.berserk": "Berserk: up to +%s%% damage per piece as health falls", "armorEffect.wither": "Attackers wither for %s ticks per piece", "armorEffect.fireInflicting": "Sets attackers alight for %s ticks per piece",
             "armorEffect.bounceback": "Knocks attackers back (%s%%)", "armorEffect.spikes": "Spikes: %s damage per piece to attackers", "armorEffect.arrowBlock": "%s%% per piece to block a projectile",
             "armorEffect.weaponEfficiency": "+%s%% damage per piece with axes", "armorEffect.divine": "Full set: one hit nullified every minute", "armorEffect.steamTech": "Boots brake falls, helmet reveals monsters when sneaking", "armorEffect.antiSkeleton": "Skeletons lose interest in the wearer"}.items():
    lang[v and k] = v
open(f"{R}/assets/{NS}/lang/en_us.json", "w").write(json.dumps(lang, indent=1, ensure_ascii=False))
open(f"{HERE}/../age_gates.json", "w").write(json.dumps(gates, indent=1, ensure_ascii=False))
print(len(SETS) * 4, "items")
