"""Writes the 0.3.0 content: creature JSON, loot tables and lang entries, and builds the art for all 19 creatures.

Run from diaithir-bestiary/: python3 tools/gen_content.py     (needs numpy and Pillow)

ART: models, animations, 64x textures and emissive glow masks come from the procedural toolkit in tools/art
(one module per creature in tools/art/creatures/). The cube specs below (C) are the legacy first-pass geometry and are
no longer written; the J, L and LOOT tables are still the source for the creature JSON, loot and lang.
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gen_examples as g  # noqa: E402

bone, pair, loop, swing, bob, attack = g.bone, g.pair, g.loop, g.swing, g.bob, g.attack
ROOT = g.ROOT
DATA = ROOT.parent.parent / "data/diaithirbestiary"

C = {}   # name -> geometry spec
J = {}   # name -> creature json
L = {}   # name -> display name
LOOT = {}  # name -> list of (item, min, max)


def lerp(a, b, t):
    return tuple(int(x + (y - x) * t) for x, y in zip(a, b))


def dark(c, f=0.7):
    return tuple(int(x * f) for x in c)


def spin(bone_name, length, axis=1):
    deg = [0, 0, 0]
    end = [0, 0, 0]
    end[axis] = 360
    return {bone_name: {"rotation": {"0.0": deg, str(length): end}}}


def waver(name, length, angle=4, axis=2):
    return swing([name], angle, length, axis=axis)


def atk_arms(arms, length=0.7, angle=-120):
    return {"loop": False, "animation_length": length,
            "bones": {a: {"rotation": {"0.0": [0, 0, 0], str(length * 0.4): [angle, 0, 0], str(length): [0, 0, 0]}} for a in arms}}


# ------------------------------------------------------------------ avatars (gods)

def avatar(name, title, skin, cloth, trim, extras, idle_extra, stats, boss_colour, spawn, loot, width=1.4, height=4.0, arm_w=4):
    bones = [
        *pair("leg", [2, 28, 0], [0.5, 0, -2], [3, 28, 4], cloth),
        bone("torso", [0, 28, 0], [([-7, 28, -3], [14, 20, 6])], cloth),
        bone("sash", [0, 28, 0], [([-7.5, 27, -3.5], [15, 3, 7])], trim, "torso"),
        *pair("arm", [9, 46, 0], [7, 26, -2], [arm_w, 22, arm_w], skin, "torso"),
        bone("head", [0, 48, 0], [([-4, 48, -4], [8, 9, 8])], skin, "torso", eyes=True),
        *extras,
    ]
    anims = {
        "idle": loop(4.0, {**g.swing(["arm_l", "arm_r"], 4, 4.0, axis=2), **idle_extra}),
        "walk": loop(1.4, {**swing(["leg_l", "leg_r"], 25, 1.4), **swing(["arm_r", "arm_l"], 20, 1.4)}),
        "attack": atk_arms(["arm_l", "arm_r"], 0.8, -150),
    }
    C[name] = {"bones": bones, "anims": anims}
    J[name] = {"preset": "avatar", "width": width, "height": height, "stats": stats,
               "boss": {"phase_two_at": 0.5, "bar_color": boss_colour}, "spawn": spawn, "loot_table": f"diaithirbestiary:entities/{name}"}
    L[name] = title
    LOOT[name] = loot


GREEN, BARK, LEAF = (84, 132, 70), (92, 70, 50), (60, 116, 52)
avatar("verdanai", "Verdanai, Avatar of the Wild Green", GREEN, BARK, (150, 190, 90),
       [bone("antler_l", [3, 57, 0], [([3, 57, -1], [1, 8, 1]), ([3, 64, -1], [5, 1, 1]), ([6, 64, -1], [1, 4, 1])], BARK, "head"),
        bone("antler_r", [-3, 57, 0], [([-4, 57, -1], [1, 8, 1]), ([-8, 64, -1], [5, 1, 1]), ([-7, 64, -1], [1, 4, 1])], BARK, "head"),
        bone("cloak", [0, 47, 3], [([-8, 24, 3], [16, 23, 1])], LEAF, "torso"),
        bone("leaves", [0, 40, 0], [([-14, 38, -1], [3, 1, 3]), ([12, 44, 2], [3, 1, 3]), ([-3, 50, 12], [3, 1, 3]), ([2, 34, -13], [3, 1, 3])], (130, 190, 80))],
       spin("leaves", 6.0),
       {"health": 380, "armor": 8, "damage": 13, "speed": 0.3, "follow_range": 48, "knockback_resistance": 1.0}, "green",
       {"biomes": "minecraft:is_forest", "weight": 2, "min_age": "III", "rare": True},
       [("minecraft:oak_log", 16, 32), ("minecraft:golden_apple", 1, 2), ("minecraft:emerald", 4, 9)])

SILVER, ROBE, GLOW = (214, 220, 232), (58, 66, 104), (236, 240, 255)
avatar("orsolune", "Orsolune, Avatar of the Hushed Moon", SILVER, ROBE, (150, 160, 210),
       [bone("skirt", [0, 28, 0], [([-8, 12, -4], [16, 16, 8])], ROBE, "torso"),
        bone("crescent", [0, 57, 0], [([-5, 57, -1], [10, 1, 2]), ([-5, 58, -1], [2, 3, 2]), ([3, 58, -1], [2, 3, 2])], GLOW, "head"),
        bone("orb", [0, 44, 12], [([-3, 41, 10], [6, 6, 6])], GLOW, "torso")],
       {**g.swing(["orb"], 12, 4.0, axis=2), **bob("orb", 2.0, 4.0)},
       {"health": 320, "armor": 6, "damage": 12, "speed": 0.34, "follow_range": 56, "knockback_resistance": 1.0}, "white",
       {"biomes": "minecraft:is_taiga", "weight": 2, "max_light": 7, "min_age": "IV", "rare": True},
       [("minecraft:amethyst_shard", 12, 24), ("minecraft:ender_pearl", 2, 4), ("minecraft:diamond", 1, 3)], width=1.2)

ASH, CHAR, EMBER = (150, 60, 44), (44, 36, 36), (255, 150, 50)
avatar("ashenvar", "Ashenvar, Avatar of the Ember Hour", ASH, CHAR, (200, 90, 40),
       [bone("horn_l", [3, 56, 0], [([3, 56, -1], [2, 4, 2]), ([4, 59, -1], [2, 5, 2])], (30, 24, 24), "head"),
        bone("horn_r", [-3, 56, 0], [([-5, 56, -1], [2, 4, 2]), ([-6, 59, -1], [2, 5, 2])], (30, 24, 24), "head"),
        bone("crest", [0, 57, 0], [([-1, 57, -3], [2, 6, 6])], EMBER, "head"),
        bone("pauldron_l", [9, 47, 0], [([6, 46, -3], [7, 3, 6])], CHAR, "torso"),
        bone("pauldron_r", [-9, 47, 0], [([-13, 46, -3], [7, 3, 6])], CHAR, "torso")],
       waver("crest", 2.0, 6, axis=2),
       {"health": 440, "armor": 12, "damage": 16, "speed": 0.3, "follow_range": 48, "knockback_resistance": 1.0}, "red",
       {"biomes": "minecraft:is_badlands", "weight": 2, "min_age": "IV", "rare": True},
       [("minecraft:blaze_rod", 8, 16), ("minecraft:gold_ingot", 8, 16), ("minecraft:netherite_scrap", 1, 2)], arm_w=6)

TIDE, SCALE, FIN = (70, 140, 170), (40, 82, 116), (110, 200, 210)
avatar("marenoth", "Marenoth, Avatar of the Deep Tide", TIDE, SCALE, (120, 210, 200),
       [bone("fin_l", [4, 52, 0], [([4, 51, -1], [5, 5, 1])], FIN, "head"),
        bone("fin_r", [-4, 52, 0], [([-9, 51, -1], [5, 5, 1])], FIN, "head"),
        bone("tailfin", [0, 28, 3], [([-1, 12, 3], [2, 18, 5])], FIN, "torso"),
        bone("spear", [-9, 30, -2], [([-9.5, 28, -22], [1, 1, 30]), ([-10.5, 27, -26], [3, 3, 4])], (190, 200, 205), "arm_r")],
       waver("tailfin", 3.0, 8, axis=1),
       {"health": 360, "armor": 8, "damage": 14, "speed": 0.32, "follow_range": 48, "knockback_resistance": 1.0}, "blue",
       {"biomes": "minecraft:is_beach", "weight": 2, "min_age": "III", "rare": True},
       [("minecraft:prismarine_shard", 12, 24), ("minecraft:heart_of_the_sea", 1, 1), ("minecraft:nautilus_shell", 1, 2)])

# ------------------------------------------------------------------ mechs

def mech(name, title, bones, anims, width, height, stats, fuel, spawn, loot):
    C[name] = {"bones": bones, "anims": anims}
    J[name] = {"preset": "mech", "width": width, "height": height, "stats": stats, "fuel": fuel, "spawn": spawn,
               "loot_table": f"diaithirbestiary:entities/{name}"}
    L[name] = title
    LOOT[name] = loot


BRASS, GUNMETAL, GLASS = (176, 138, 66), (84, 90, 100), (96, 170, 200)
mech("brassjack", "Brassjack Walker",
     [bone("cabin", [0, 22, 0], [([-7, 22, -6], [14, 14, 12]), ([-1.5, 36, 4], [3, 6, 3])], BRASS),
      bone("dome", [0, 36, 0], [([-5, 36, -4], [10, 5, 8])], GLASS, "cabin", eyes=True),
      *pair("leg", [3, 22, 0], [2, 3, -2], [4, 19, 4], GUNMETAL),
      *pair("foot", [3, 3, 0], [1.5, 0, -5], [6, 3, 9], dark(GUNMETAL)),
      *pair("gun", [8, 30, 0], [7, 28, -10], [3, 3, 10], GUNMETAL, "cabin")],
     {"idle": loop(2.0, bob("cabin", 0.3, 2.0)),
      "walk": loop(0.9, {**swing(["leg_l", "leg_r"], 24, 0.9), **swing(["foot_l", "foot_r"], 24, 0.9), **bob("cabin", 0.8, 0.45)}),
      "attack": attack("gun_l", -18)},
     1.4, 2.7, {"health": 70, "armor": 6, "speed": 0.3, "knockback_resistance": 0.4}, {"item": "minecraft:coal", "ticks": 800},
     {"biomes": "c:is_desert", "weight": 2, "min_age": "V", "rare": True},
     [("minecraft:copper_ingot", 4, 9), ("minecraft:iron_nugget", 6, 14)])

WARD, WARDDK = (96, 110, 92), (60, 72, 60)
mech("siegewarden", "Siegewarden Walker",
     [bone("hull", [0, 24, 0], [([-20, 24, -16], [40, 14, 32])], WARD, eyes=True),
      bone("turret", [0, 38, 0], [([-8, 38, -8], [16, 8, 16])], WARDDK, "hull"),
      bone("barrel", [0, 42, -8], [([-2, 41, -26], [4, 4, 18])], GUNMETAL, "turret"),
      *pair("leg_f", [16, 24, -10], [13, 0, -13], [6, 24, 6], GUNMETAL),
      *pair("leg_b", [16, 24, 10], [13, 0, 7], [6, 24, 6], GUNMETAL)],
     {"idle": loop(3.0, bob("hull", 0.3, 3.0)),
      "walk": loop(1.6, {**swing(["leg_f_l", "leg_f_r"], 18, 1.6), **swing(["leg_b_r", "leg_b_l"], 18, 1.6), **bob("hull", 0.6, 0.8)}),
      "attack": attack("barrel", -10)},
     3.0, 3.0, {"health": 260, "armor": 18, "speed": 0.16, "knockback_resistance": 1.0}, {"item": "minecraft:coal", "ticks": 900},
     {"biomes": "minecraft:is_badlands", "weight": 1, "min_age": "VI", "rare": True},
     [("minecraft:iron_ingot", 8, 18), ("minecraft:gunpowder", 4, 10)])

COPPERBODY = (190, 108, 62)
mech("tinkerspider", "Tinkerspider Crawler",
     [bone("body", [0, 8, 0], [([-6, 8, -6], [12, 6, 12])], COPPERBODY),
      bone("eye", [0, 11, -6], [([-3, 10, -9], [6, 4, 3])], GLASS, "body", eyes=True),
      *pair("leg_a", [6, 9, -4], [6, 0, -5], [2, 9, 2], GUNMETAL),
      *pair("leg_b", [6, 9, 0], [6, 0, -1], [2, 9, 2], GUNMETAL),
      *pair("leg_c", [6, 9, 4], [6, 0, 3], [2, 9, 2], GUNMETAL)],
     {"idle": loop(2.0, bob("body", 0.2, 2.0)),
      "walk": loop(0.5, {**swing(["leg_a_l", "leg_b_r", "leg_c_l"], 30, 0.5), **swing(["leg_a_r", "leg_b_l", "leg_c_r"], -30, 0.5)}),
      "attack": attack("eye", -25)},
     1.3, 1.1, {"health": 30, "armor": 2, "speed": 0.36, "knockback_resistance": 0.1}, {"item": "minecraft:redstone", "ticks": 500},
     {"biomes": "c:is_desert", "weight": 2, "min_age": "V", "rare": True},
     [("minecraft:redstone", 2, 6), ("minecraft:copper_ingot", 1, 3)])

# ------------------------------------------------------------------ stalkers (horror)

def stalker(name, title, bones, anims, width, height, stats, spawn, loot):
    C[name] = {"bones": bones, "anims": anims}
    J[name] = {"preset": "stalker", "width": width, "height": height, "stats": stats, "spawn": spawn,
               "loot_table": f"diaithirbestiary:entities/{name}"}
    L[name] = title
    LOOT[name] = loot


PALE, SHADE = (222, 218, 210), (30, 28, 34)
stalker("pallidgaunt", "Pallidgaunt",
        [*pair("leg", [1.5, 30, 0], [0, 0, -1], [2, 30, 2], SHADE),
         bone("torso", [0, 30, 0], [([-3, 30, -1.5], [6, 16, 3])], SHADE),
         *pair("arm", [4, 44, 0], [3, 16, -1], [2, 28, 2], PALE, "torso"),
         bone("head", [0, 46, 0], [([-2.5, 46, -2.5], [5, 8, 5])], PALE, "torso", eyes=True)],
        {"idle": loop(6.0, waver("head", 6.0, 8, axis=2)), "walk": loop(1.0, swing(["leg_l", "leg_r"], 28, 1.0)),
         "attack": atk_arms(["arm_l", "arm_r"], 0.5, -100)},
        0.6, 3.1, {"health": 44, "armor": 2, "damage": 8, "speed": 0.42, "follow_range": 48},
        {"biomes": "minecraft:is_forest", "weight": 1, "max_light": 4, "min_age": "IV"},
        [("minecraft:bone", 2, 5), ("minecraft:spider_eye", 1, 2)])

BONE, MARROW = (200, 190, 168), (92, 40, 40)
stalker("marrowcrawl", "Marrowcrawl",
        [bone("body", [0, 8, 0], [([-5, 6, -10], [10, 6, 20]), ([-1, 12, -6], [2, 3, 12])], MARROW),
         bone("head", [0, 9, -10], [([-3, 7, -15], [6, 6, 6])], BONE, "body", eyes=True),
         *pair("arm_f", [5, 9, -7], [5, 0, -8], [2, 10, 2], BONE),
         *pair("arm_b", [5, 9, 7], [5, 0, 6], [2, 10, 2], BONE)],
        {"idle": loop(4.0, bob("body", 0.3, 4.0)), "walk": loop(0.7, {**swing(["arm_f_l", "arm_b_r"], 35, 0.7), **swing(["arm_f_r", "arm_b_l"], -35, 0.7)}),
         "attack": attack("head", -35)},
        1.4, 0.9, {"health": 36, "armor": 0, "damage": 7, "speed": 0.5, "follow_range": 40},
        {"biomes": "c:is_swamp", "weight": 2, "max_light": 4, "min_age": "IV"},
        [("minecraft:bone", 3, 7), ("minecraft:rotten_flesh", 1, 4)])

BURLAP, STRAW, HAT = (150, 124, 84), (212, 184, 96), (52, 40, 34)
stalker("veilstitch", "Veilstitch",
        [*pair("leg", [2, 16, 0], [0.5, 0, -1.5], [3, 16, 3], BURLAP),
         bone("torso", [0, 16, 0], [([-4, 16, -2], [8, 16, 4])], BURLAP),
         *pair("arm", [4, 30, 0], [4, 28, -1], [14, 2, 2], STRAW, "torso"),
         bone("head", [0, 32, 0], [([-4, 32, -4], [8, 8, 8])], BURLAP, "torso", eyes=True),
         bone("hat", [0, 40, 0], [([-6, 40, -6], [12, 1, 12]), ([-3, 41, -3], [6, 4, 6])], HAT, "head")],
        {"idle": loop(5.0, waver("head", 5.0, 10, axis=2)), "walk": loop(0.9, swing(["leg_l", "leg_r"], 26, 0.9)),
         "attack": atk_arms(["arm_l", "arm_r"], 0.5, -60)},
        0.8, 2.6, {"health": 40, "armor": 1, "damage": 7, "speed": 0.4, "follow_range": 48},
        {"biomes": "c:is_plains", "weight": 1, "max_light": 4, "min_age": "III"},
        [("minecraft:wheat", 2, 6), ("minecraft:string", 2, 5), ("minecraft:pumpkin_seeds", 1, 4)])

# ------------------------------------------------------------------ toons (friendly, rideable, tamable)

def toon(name, title, preset, bones, anims, width, height, stats, tame, spawn, loot):
    C[name] = {"bones": bones, "anims": anims}
    J[name] = {"preset": preset, "width": width, "height": height, "stats": stats, "hostile": False,
               "tame": tame, "spawn": spawn, "loot_table": f"diaithirbestiary:entities/{name}"}
    if preset == "beast":
        J[name]["rideable"] = True
    L[name] = title
    LOOT[name] = loot


SKY, ORANGE = (110, 176, 232), (244, 170, 60)
toon("puddlepip", "Puddlepip", "beast",
     [bone("body", [0, 4, 0], [([-6, 4, -6], [12, 10, 12])], SKY),
      bone("head", [0, 14, -4], [([-4, 13, -8], [8, 7, 8]), ([-0.5, 20, -5], [1, 3, 1])], lerp(SKY, (255, 255, 255), 0.3), "body", eyes=True),
      bone("bill", [0, 15, -8], [([-3, 14, -12], [6, 2, 4])], ORANGE, "head"),
      *pair("wing", [6, 10, 0], [6, 5, -3], [1, 6, 6], dark(SKY, 0.85), "body"),
      *pair("foot", [2, 4, 0], [1, 0, -3], [3, 4, 5], ORANGE)],
     {"idle": loop(2.0, {**bob("body", 0.4, 2.0), **swing(["wing_l", "wing_r"], 8, 2.0, axis=2)}),
      "walk": loop(0.5, {**swing(["foot_l", "foot_r"], 40, 0.5), **bob("body", 0.8, 0.25)}),
      "attack": attack("bill", -30)},
     0.9, 1.1, {"health": 14, "damage": 1, "speed": 0.26}, {"item": "minecraft:wheat_seeds", "chance": 0.5},
     {"biomes": "minecraft:is_river", "weight": 8, "min_group": 2, "max_group": 4, "min_age": "IV"},
     [("minecraft:feather", 1, 3), ("minecraft:egg", 0, 1)])

LILAC = (176, 160, 204)
toon("grumblebun", "Grumblebun", "beast",
     [bone("body", [0, 5, 0], [([-5, 5, -6], [10, 9, 12])], LILAC),
      bone("head", [0, 12, -6], [([-4, 12, -11], [8, 7, 7])], lerp(LILAC, (255, 255, 255), 0.25), "body", eyes=True),
      bone("brow", [0, 17, -11], [([-4, 17, -11.5], [8, 1, 1])], (50, 36, 60), "head"),
      *pair("ear", [2, 19, -8], [1, 19, -8], [2, 9, 1], dark(LILAC, 0.85), "head"),
      *pair("leg_f", [3, 5, -4], [2, 0, -5], [3, 5, 3], dark(LILAC, 0.8)),
      *pair("leg_b", [4, 6, 4], [3, 0, 2], [3, 6, 5], dark(LILAC, 0.8)),
      bone("tail", [0, 8, 6], [([-1.5, 7, 6], [3, 3, 3])], (250, 246, 250), "body")],
     {"idle": loop(2.5, swing(["ear_l", "ear_r"], 6, 2.5, axis=2, phase=True)),
      "walk": loop(0.45, {**swing(["leg_f_l", "leg_f_r"], 40, 0.45), **swing(["leg_b_r", "leg_b_l"], 40, 0.45), **bob("body", 1.0, 0.22)}),
      "attack": attack("head", -25)},
     0.8, 1.0, {"health": 12, "damage": 1.5, "speed": 0.3}, {"item": "minecraft:carrot", "chance": 0.5},
     {"biomes": "minecraft:is_forest", "weight": 8, "min_group": 1, "max_group": 3, "min_age": "V"},
     [("minecraft:rabbit_hide", 1, 2), ("minecraft:carrot", 0, 2)])

TEAL, CREAM = (86, 190, 180), (250, 236, 200)
toon("twirlfin", "Twirlfin", "flyer",
     [bone("body", [0, 10, 0], [([-5, 8, -8], [10, 8, 16])], TEAL),
      bone("head", [0, 12, -8], [([-4, 9, -14], [8, 7, 7])], lerp(TEAL, CREAM, 0.3), "body", eyes=True),
      bone("fin", [0, 16, 0], [([-0.5, 16, -2], [1, 4, 8])], dark(TEAL, 0.8), "body"),
      *pair("wing", [5, 15, 0], [5, 15, -3], [10, 1, 6], CREAM, "body"),
      bone("prop", [0, 12, 8], [([-6, 11.5, 8], [12, 1, 1]), ([-0.5, 6, 8], [1, 12, 1])], (230, 80, 70), "body")],
     {"idle": loop(2.0, {**bob("body", 0.4, 2.0), **spin("prop", 1.0, axis=2)}),
      "walk": loop(0.8, {**bob("body", 0.6, 0.4), **spin("prop", 0.8, axis=2)}),
      "fly": loop(0.7, {**swing(["wing_l", "wing_r"], 30, 0.7, axis=2), **spin("prop", 0.3, axis=2)}),
      "attack": attack("head", -30)},
     1.2, 1.1, {"health": 20, "damage": 2, "speed": 0.24, "flying_speed": 0.55}, {"item": "minecraft:cod", "chance": 0.4},
     {"biomes": "c:is_plains", "weight": 5, "min_age": "V"},
     [("minecraft:feather", 1, 3), ("minecraft:copper_ingot", 0, 1)])

MOSS, MOSSDK = (110, 150, 70), (76, 108, 52)
toon("mossmop", "Mossmop", "beast",
     [bone("body", [0, 6, 0], [([-8, 6, -8], [16, 14, 16])], MOSS),
      bone("skirt", [0, 6, 0], [([-9, 2, -9], [18, 5, 18])], MOSSDK, "body"),
      bone("face", [0, 14, -8], [([-4, 12, -11], [8, 6, 3])], (234, 214, 170), "body", eyes=True),
      bone("tuft", [0, 20, 0], [([-2, 20, -2], [4, 4, 4])], MOSSDK, "body"),
      *pair("foot", [3, 2, 0], [1, 0, -4], [4, 2, 4], (60, 46, 36))],
     {"idle": loop(3.0, {**bob("body", 0.5, 3.0), **waver("tuft", 3.0, 8, axis=2)}),
      "walk": loop(0.8, {**swing(["foot_l", "foot_r"], 30, 0.8), **bob("body", 1.0, 0.4)}),
      "attack": attack("body", -12)},
     1.6, 1.5, {"health": 40, "damage": 3, "speed": 0.2, "knockback_resistance": 0.3}, {"item": "minecraft:wheat", "chance": 0.4},
     {"biomes": "c:is_swamp", "weight": 6, "min_group": 1, "max_group": 2, "min_age": "IV"},
     [("minecraft:moss_block", 2, 5), ("minecraft:string", 1, 3)])

# Pleistocene megafauna for the Earth timeline (Age I): realistic art, no cartoon face. Names are Diaithiris compounds.
toon("dobhranfamhair", "Dobhranfamhair", "beast", [], {}, 1.3, 1.0,
     {"health": 34, "damage": 5, "speed": 0.22, "knockback_resistance": 0.4}, {"item": "minecraft:stripped_oak_log", "chance": 0.25},
     {"biomes": "c:is_swamp", "weight": 5, "min_group": 1, "max_group": 2, "min_age": "I"},
     [("minecraft:leather", 1, 3), ("minecraft:stick", 2, 6), ("minecraft:bone", 1, 2)])
J["dobhranfamhair"]["rideable"] = False

toon("eachreidhlein", "Eachrèidhlein", "beast", [], {}, 1.0, 1.45,
     {"health": 22, "damage": 3, "speed": 0.3}, {"item": "minecraft:wheat", "chance": 0.35},
     {"biomes": "c:is_plains", "weight": 8, "min_group": 2, "max_group": 5, "min_age": "I"},
     [("minecraft:leather", 1, 2), ("minecraft:bone", 0, 2)])


def loot_json(entries):
    pools = []
    for item, lo, hi in entries:
        fn = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi} if hi > lo else lo}]
        pools.append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": fn}]})
    return {"type": "minecraft:entity", "pools": pools}


ART = ["aurelith", "ironstrider", "hollowmourn", "bumblesnout", "emberwyrm", "verdanai", "orsolune", "ashenvar", "marenoth",
       "brassjack", "siegewarden", "tinkerspider", "pallidgaunt", "marrowcrawl", "veilstitch", "puddlepip", "grumblebun",
       "twirlfin", "mossmop", "dobhranfamhair", "eachreidhlein"]


def main():
    sys.path.insert(0, str(Path(__file__).resolve().parent / "art"))
    import build as art_build  # noqa: E402
    for n in ART:
        art_build.build(n)
    for name, data in J.items():
        p = DATA / "diaithirbestiary/creature" / (name + ".json")
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps(data, indent=2) + "\n")
        lp = DATA / "loot_table/entities" / (name + ".json")
        lp.parent.mkdir(parents=True, exist_ok=True)
        lp.write_text(json.dumps(loot_json(LOOT[name]), indent=2) + "\n")
    lang_path = ROOT / "lang/en_us.json"
    lang = json.loads(lang_path.read_text())
    for name, title in L.items():
        lang[f"entity.diaithirbestiary.{name}"] = title
    lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n")


if __name__ == "__main__":
    main()
