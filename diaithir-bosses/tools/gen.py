"""Writes everything data-driven for diaithir-bosses: boss profiles (own bosses plus the 19 mythic-court kits), the elite table,
textures (recoloured from vanilla entity sprites by luminance ramps, nothing copied), item icons, lang, arena structures
(template pools, sets, NBT templates), age_gates.json, names_map.json, library and codex files.
Run: python3 -I tools/gen.py <dir holding the extracted vanilla 'assets/minecraft/textures' tree> <creature csv: bosses.csv>
"""
import csv, gzip, json, math, os, random, struct, sys, unicodedata
import numpy as np
from PIL import Image, ImageDraw

VAN = os.path.join(sys.argv[1], "assets/minecraft/textures")
BOSSCSV = sys.argv[2]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
NS = "diaithirbosses"
AS = os.path.join(RES, "assets", NS)
DA = os.path.join(RES, "data", NS)
ROM = ["", "I", "II", "III", "IV", "V", "VI", "VII"]
DATA_VERSION = 4790


def W(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)


def ascii_id(s):
    return unicodedata.normalize("NFD", s).encode("ascii", "ignore").decode().lower()


HP = {1: 220, 2: 300, 3: 400, 4: 520, 5: 650, 6: 800, 7: 950}
DMG = {1: 5, 2: 7, 3: 9, 4: 12, 5: 15, 6: 19, 7: 24}
ARM = {1: 4, 2: 6, 3: 8, 4: 10, 5: 12, 6: 14, 7: 16}

# kind -> (vanilla texture, w, h, fire immune, can fly/swim note)
KINDS = {
    "zombie": ("zombie/zombie.png", 0.6, 1.95),
    "illager": ("illager/vindicator.png", 0.6, 1.95),
    "golem": ("iron_golem/iron_golem.png", 1.4, 2.7),
    "spider": ("spider/spider.png", 1.4, 0.9),
    "ravager": ("illager/ravager.png", 1.95, 2.2),
    "blaze": ("blaze/blaze.png", 0.6, 1.8),
    "silverfish": ("silverfish/silverfish.png", 0.4, 0.3),
    "phantom": ("phantom/phantom.png", 0.9, 0.5),
    "guardian": ("guardian/guardian.png", 0.85, 0.85),
    "hoglin": ("hoglin/hoglin.png", 1.3965, 1.4),
}

import importlib.util
_sp = importlib.util.spec_from_file_location("archaic", "/mnt/project-files/diaithiris-archaic/tools/archaic.py")
ARCH = importlib.util.module_from_spec(_sp); _sp.loader.exec_module(ARCH)
_here = "/mnt/project-files/diaithiris-archaic/"
ARCH.MEMO.update(json.load(open(_here + "archaic_map.json")))
ARCH.OVERRIDES.update(json.load(open(_here + "tools/overrides.json")))
# modern Goidelic source words -> Diaithiris, by the tool (never by hand)
SRC = {"tribal_chief": "ceannfine", "stone_colossus": "famhairchlach", "bronze_warking": "rìghchogaidh",
       "iron_walker": "coiseachiarainn", "steel_marshal": "marascal", "ceramic_warlord": "tighearnachogaidh",
       "rot_swarm_lord": "tighearnaschaoth",
       "champion": "curaidh", "hulking": "mòr", "swift": "luath", "thorned": "dealg", "venomous": "nimh", "frostbitten": "reothadh",
       "vampiric": "fuil", "ablaze": "teine", "warbanded": "comhlan", "volatile": "spreadhadh", "shielded": "sgiath", "keen": "garg",
       "token": "comhartha", "altar": "altoir", "boss": "ceannard"}
D = {k: ARCH.text(v).capitalize() for k, v in SRC.items()}

# ---------------------------------------------------------------- Diaithiris names (built from the archaic lexicon, checked
# against it: no word equals a modern or Old Gaelic/Irish/Manx form)
CHAMPION = D["champion"]
AFFIX_NAMES = {k: D[v] for k, v in {"hulking": "hulking", "swift": "swift", "thorned": "thorned", "venomous": "venomous", "frostbitten": "frostbitten", "vampiric": "vampiric", "ablaze": "ablaze", "warbanded": "warbanded", "volatile": "volatile", "shielded": "shielded", "keen": "keen"}.items()}

# ---------------------------------------------------------------- attack builders
def A(type_, **kw):
    kw["type"] = type_
    return kw


def slam(age, r=5, m=1.0, tele=30, cd=90):
    return A("slam", telegraph=tele, cooldown=cd, radius=r, damage=round(DMG[age] * m, 1), knockback=1.2)


def charge(age, m=1.0, tele=24, cd=110, active=14):
    return A("charge", telegraph=tele, cooldown=cd, active=active, speed=1.15, damage=round(DMG[age] * m, 1), radius=2.4)


def leap(age, r=5, m=1.1, tele=28, cd=120):
    return A("leap", telegraph=tele, cooldown=cd, radius=r, damage=round(DMG[age] * m, 1), power=1.1, knockback=1.0)


def bolt(age, n=1, m=0.8, tele=26, cd=100, fire=False, effect=None, rng=30):
    d = A("bolt", telegraph=tele, cooldown=cd, count=n, damage=round(DMG[age] * m, 1), range=rng, fire=fire)
    if effect:
        d.update(effect=effect, amp=0, ticks=100)
    return d


def summon(age, minion, n=3, cap=8, tele=40, cd=220, r=6):
    return A("summon", telegraph=tele, cooldown=cd, count=n, minion=minion, cap=cap, radius=r)


def lightning(age, n=3, m=0.9, tele=36, cd=140):
    return A("lightning", telegraph=tele, cooldown=cd, count=n, damage=round(DMG[age] * m, 1), radius=3.0)


def cloud(age, effect="minecraft:poison", r=5, tele=24, cd=150, ticks=100, amp=1):
    return A("cloud", telegraph=tele, cooldown=cd, radius=r, duration=200, effect=effect, amp=amp, ticks=ticks)


def wave(age, r=10, m=0.8, tele=30, cd=130):
    return A("wave", telegraph=tele, cooldown=cd, radius=r, damage=round(DMG[age] * m, 1), knockback=1.6)


def heal(age, frac=0.04, tele=50, cd=400):
    return A("heal", telegraph=tele, cooldown=cd, amount=frac)


def roar(age, r=14, effect="minecraft:slowness", ticks=100, amp=1, tele=30, cd=180):
    return A("roar", telegraph=tele, cooldown=cd, radius=r, effect=effect, ticks=ticks, amp=amp, damage=round(DMG[age] * 0.4, 1))


def phases(*ps):
    out = []
    for i, p in enumerate(ps):
        d = {"at": p[0], "name": ROM[i + 1], "speed": p[1], "damage": p[2], "cooldown": p[3], "attacks": p[4]}
        if len(p) > 5:
            d.update(p[5])
        out.append(d)
    return out


# loot helpers. item, min, max, min_age, chance, per_age (extra max per Age past min_age)
def L(item, lo, hi, min_age=1, chance=1.0, per_age=0):
    return {"item": item, "min": lo, "max": hi, "min_age": min_age, "chance": chance, "per_age": per_age}


def arms_set(mat, age, chance=0.35):
    return [L(f"diaithirarms:{mat}_{s}", 1, 1, age, chance) for s in ("helmet", "chestplate", "leggings", "boots")]


MATERIAL = {1: "minecraft:bone", 2: "minecraft:copper_ingot", 3: "minecraft:iron_nugget", 4: "minecraft:iron_ingot",
            5: "minecraft:gold_ingot", 6: "minecraft:diamond", 7: "minecraft:netherite_scrap"}


def tiered_loot(age, scale=1):
    out = []
    for a in range(1, 8):
        out.append(L(MATERIAL[a], 2 * scale, 5 * scale, a, 1.0 if a <= age else 0.6, 1))
    return out


BOSSES = []   # filled below
OWN_NAMES = {k: D[k] for k in ("tribal_chief","stone_colossus","bronze_warking","iron_walker","steel_marshal","ceramic_warlord","rot_swarm_lord")}


def add(id_, name, name_en, kind, scale, age, **kw):
    p = {"id": id_, "name": name, "name_en": name_en, "age": age,
         "own": {"kind": kind, "scale": scale, "hp": HP[age], "damage": DMG[age], "armor": ARM[age],
                 "speed": 0.28, "knockback_resistance": 1.0, "follow_range": 48},
         "bar": {"color": kw.pop("bar", "red"), "overlay": kw.pop("overlay", "notched_10")},
         "scaling": {"hp_per_age": 0.22, "damage_per_age": 0.1, "hp_per_extra_player": 0.45, "max_age_steps": 4},
         "arena": {"radius": kw.pop("radius", 22), "lock": True, "leash": True, "burn": round(DMG[age] * 0.5, 1)},
         "max_hit_fraction": 0.1,
         "respawn": kw.pop("respawn", {"mode": "cooldown", "ticks": 48000}),
         "hostile": True}
    for k in ("hp", "damage", "speed", "armor"):
        if k in kw:
            p["own"][k] = kw.pop(k)
    p["xp"] = kw.pop("xp", 120 * age + 200)
    p.update(kw)
    BOSSES.append(p)
    return p


# ---- own bosses (non-mythic)
add("tribal_chief", "Kiendfedna", "Tribal Chief", "zombie", 1.45, 1, bar="green", radius=18, hp=260,
    tint=("zombie", (0.12, 0.08, 0.05), (0.52, 0.38, 0.24), (0.93, 0.84, 0.66)),
    equipment={"mainhand": "minecraft:bone", "head": "minecraft:leather_helmet"},
    attacks={"stomp": slam(1, 4, 1.0), "rush": charge(1, 1.1), "rally": summon(1, "minecraft:zombie", 3, 6, 40, 240)},
    phases=phases((1.0, 1.0, 1.0, 1.0, ["stomp", "rush"]),
                  (0.55, 1.25, 1.2, 0.8, ["stomp", "rush", "rally"], {"invuln": 40, "say": "rally"})),
    loot=[L("minecraft:bone", 6, 12, 1), L("minecraft:leather", 4, 8, 1), L("minecraft:flint", 3, 6, 1, 0.8)]
         + tiered_loot(1) + arms_set("bone", 1, 0.4) + [L("minecraft:copper_ingot", 3, 6, 2, 0.9, 1)],
    structure={"style": 1, "biomes": "#minecraft:is_overworld", "spacing": 40, "separation": 16})
add("stone_colossus", "Famuirklach", "Stone Colossus", "golem", 2.3, 2, bar="white", radius=24,
    tint=("golem", (0.10, 0.10, 0.11), (0.45, 0.45, 0.46), (0.82, 0.83, 0.80)),
    attacks={"quake": slam(2, 7, 1.0, 40, 100), "boulder": bolt(2, 3, 0.9, 34, 110, rng=32), "shatter": wave(2, 12, 0.9, 36, 150)},
    phases=phases((1.0, 0.8, 1.0, 1.0, ["quake", "boulder"]), (0.6, 0.9, 1.15, 0.85, ["quake", "boulder", "shatter"], {"invuln": 30}),
                  (0.25, 1.1, 1.3, 0.7, ["quake", "boulder", "shatter"], {"invuln": 30})),
    loot=[L("minecraft:cobblestone", 24, 48, 1), L("minecraft:raw_copper", 6, 12, 2, 1.0, 2), L("minecraft:iron_ingot", 3, 6, 4, 0.8, 2),
          L("minecraft:diamond", 1, 2, 6, 0.5, 1)] + tiered_loot(2) + arms_set("copper", 2, 0.35),
    structure={"style": 2, "biomes": "#minecraft:is_hill", "spacing": 44, "separation": 18})
add("bronze_warking", "Rìgkogad", "Bronze War-King", "illager", 1.5, 3, bar="yellow", radius=22,
    tint=("illager", (0.12, 0.07, 0.03), (0.60, 0.40, 0.14), (0.98, 0.82, 0.42)),
    equipment={"mainhand": "minecraft:golden_axe"},
    attacks={"cleave": slam(3, 4, 1.0, 26, 80), "charge": charge(3, 1.2, 22, 100), "guard": summon(3, "minecraft:vindicator", 2, 4, 44, 260),
             "warcry": roar(3, 12, "minecraft:weakness", 120, 0)},
    phases=phases((1.0, 1.0, 1.0, 1.0, ["cleave", "charge"]), (0.6, 1.15, 1.15, 0.9, ["cleave", "charge", "guard"], {"invuln": 40}),
                  (0.3, 1.3, 1.3, 0.75, ["cleave", "charge", "guard", "warcry"], {"invuln": 40})),
    loot=tiered_loot(3) + arms_set("bronze", 3, 0.4) + [L("minecraft:emerald", 2, 5, 3, 0.7, 1)],
    structure={"style": 3, "biomes": "minecraft:plains,minecraft:sunflower_plains,minecraft:meadow", "spacing": 44, "separation": 18})
add("iron_walker", "Koisichiiarund", "Iron Walker", "golem", 2.0, 4, bar="white", radius=26,
    tint=("golem", (0.10, 0.06, 0.04), (0.50, 0.32, 0.22), (0.88, 0.62, 0.40)),
    attacks={"stride": slam(4, 6, 1.0, 34, 90), "chain": bolt(4, 4, 0.8, 30, 100, rng=34), "steam": cloud(4, "minecraft:slowness", 6, 26, 160, 80, 2),
             "overrun": charge(4, 1.25, 30, 130, 18)},
    phases=phases((1.0, 0.9, 1.0, 1.0, ["stride", "chain"]), (0.65, 1.0, 1.15, 0.85, ["stride", "chain", "steam"], {"invuln": 30}),
                  (0.3, 1.15, 1.3, 0.7, ["stride", "chain", "steam", "overrun"], {"invuln": 30})),
    loot=tiered_loot(4, 2) + arms_set("mail", 4, 0.4) + [L("minecraft:iron_block", 1, 2, 4, 0.5, 1)],
    structure={"style": 4, "biomes": "#minecraft:is_badlands", "spacing": 46, "separation": 20})
add("steel_marshal", "Maraskul", "Steel Marshal", "illager", 1.55, 5, bar="blue", radius=24,
    tint=("illager", (0.07, 0.08, 0.11), (0.45, 0.50, 0.58), (0.86, 0.90, 0.95)),
    equipment={"mainhand": "minecraft:iron_sword", "head": "minecraft:iron_helmet"},
    attacks={"strike": slam(5, 4.5, 1.0, 24, 70), "volley": bolt(5, 5, 0.7, 30, 110, rng=36), "advance": charge(5, 1.2, 20, 100),
             "levy": summon(5, "minecraft:vindicator", 3, 6, 40, 240), "fortify": roar(5, 14, "minecraft:mining_fatigue", 120, 1)},
    phases=phases((1.0, 1.0, 1.0, 1.0, ["strike", "volley", "advance"]), (0.6, 1.15, 1.15, 0.85, ["strike", "volley", "advance", "levy"], {"invuln": 40}),
                  (0.3, 1.3, 1.3, 0.7, ["strike", "volley", "advance", "levy", "fortify"], {"invuln": 40})),
    loot=tiered_loot(5, 2) + arms_set("steel", 5, 0.4) + [L("diaithirarms:steel_sword", 1, 1, 5, 0.3), L("minecraft:diamond", 1, 3, 5, 0.6, 1)],
    structure={"style": 5, "biomes": "#minecraft:is_taiga", "spacing": 48, "separation": 20})
add("ceramic_warlord", "Tigernakogad", "Ceramic Warlord", "zombie", 1.6, 7, bar="white", radius=26,
    tint=("zombie", (0.55, 0.57, 0.62), (0.88, 0.88, 0.90), (1.0, 1.0, 1.0)),
    equipment={"mainhand": "minecraft:netherite_sword", "head": "minecraft:netherite_helmet", "chest": "minecraft:netherite_chestplate"},
    attacks={"cut": slam(7, 4.5, 1.0, 22, 60), "volley": bolt(7, 6, 0.65, 26, 90, rng=40), "dash": charge(7, 1.2, 18, 90, 16),
             "legion": summon(7, "minecraft:vindicator", 4, 8, 36, 220), "fury": roar(7, 16, "minecraft:weakness", 120, 1), "bombard": lightning(7, 4, 0.8, 34, 140)},
    phases=phases((1.0, 1.1, 1.0, 1.0, ["cut", "volley", "dash"]), (0.65, 1.25, 1.15, 0.85, ["cut", "volley", "dash", "legion"], {"invuln": 40}),
                  (0.3, 1.4, 1.3, 0.7, ["cut", "volley", "dash", "legion", "fury", "bombard"], {"invuln": 40})),
    loot=tiered_loot(7, 3) + arms_set("composite", 7, 0.45) + [L("diaithirarms:ceramic_plate", 2, 5, 7, 1.0, 1), L("diaithirarms:composite_sword", 1, 1, 7, 0.3)],
    structure={"style": 7, "biomes": "#minecraft:is_overworld", "spacing": 52, "separation": 22})
add("rot_swarm_lord", "Tigernaskaed", "Plague Swarm Lord", "spider", 2.8, 3, bar="green", radius=22,
    tint=("spider", (0.05, 0.07, 0.02), (0.30, 0.42, 0.12), (0.72, 0.80, 0.30)),
    attacks={"bite": leap(3, 5, 1.0, 26, 100), "spores": cloud(3, "minecraft:poison", 6, 24, 120, 120, 1), "brood": summon(3, "minecraft:silverfish", 5, 12, 36, 180, 7),
             "blight": cloud(3, "minecraft:hunger", 7, 28, 160, 160, 2), "fester": heal(3, 0.03, 50, 360)},
    phases=phases((1.0, 1.0, 1.0, 1.0, ["bite", "spores"]), (0.6, 1.1, 1.15, 0.85, ["bite", "spores", "brood", "blight"], {"invuln": 40}),
                  (0.3, 1.25, 1.3, 0.7, ["bite", "spores", "brood", "blight", "fester"], {"invuln": 40})),
    loot=tiered_loot(3) + [L("minecraft:spider_eye", 6, 12, 3, 1.0, 2), L("minecraft:fermented_spider_eye", 2, 4, 3, 0.6, 1), L("minecraft:rotten_flesh", 8, 16, 3)],
    structure={"style": 3, "biomes": "minecraft:swamp,minecraft:mangrove_swamp", "spacing": 42, "separation": 18})

# ---- mythic kits: the 19 avatar creatures of the dead courts
MYTH_MOD = {"toimsechunleomund", "natuirkriegach", "natuirmoir", "tarbiumachuoirtach"}  # entities diaithir_myth already spawns
MYTH_KIT = {
    "ridiriknok": dict(kind="zombie", scale=1.55, bar="purple", tint=("zombie", (0.04, 0.06, 0.08), (0.22, 0.34, 0.30), (0.62, 0.86, 0.70)),
        atk=lambda a: {"slam": slam(a, 5, 1.0), "lunge": charge(a, 1.2), "hollow": summon(a, "minecraft:skeleton", 3, 6), "dirge": roar(a, 14, "minecraft:weakness", 100, 0)},
        ph=[["slam", "lunge"], ["slam", "lunge", "hollow"], ["slam", "lunge", "hollow", "dirge"]]),
    "toimsechunleomund": dict(kind="ravager", scale=1.5, bar="yellow", tint=("ravager", (0.18, 0.12, 0.05), (0.72, 0.58, 0.28), (0.98, 0.90, 0.60)),
        atk=lambda a: {"pounce": leap(a, 5), "sand": cloud(a, "minecraft:blindness", 6, 24, 140, 80, 0), "riddle": bolt(a, 3, 0.9, 30, 100, effect="minecraft:slowness"), "gale": wave(a, 11)},
        ph=[["pounce", "riddle"], ["pounce", "riddle", "sand"], ["pounce", "riddle", "sand", "gale"]]),
    "tarbspeiri": dict(kind="ravager", scale=1.9, bar="blue", tint=("ravager", (0.08, 0.09, 0.14), (0.40, 0.46, 0.64), (0.90, 0.92, 1.0)),
        atk=lambda a: {"gore": charge(a, 1.3, 26, 100, 16), "hoofquake": wave(a, 11, 0.9), "heavenfire": lightning(a, 3, 1.0, 38, 140), "stampede": charge(a, 1.1, 18, 80, 12)},
        ph=[["gore", "hoofquake"], ["gore", "hoofquake", "stampede"], ["gore", "hoofquake", "stampede", "heavenfire"]]),
    "driegunsalund": dict(kind="phantom", scale=4.0, bar="white", tint=("phantom", (0.25, 0.28, 0.30), (0.78, 0.80, 0.78), (1.0, 1.0, 0.96)),
        atk=lambda a: {"brine": cloud(a, "minecraft:poison", 7, 26, 130, 120, 1), "tide": wave(a, 12), "spit": bolt(a, 3, 0.9, 28, 100, effect="minecraft:wither"), "spawn": summon(a, "minecraft:silverfish", 4, 10)},
        ph=[["brine", "spit"], ["brine", "spit", "tide"], ["brine", "spit", "tide", "spawn"]]),
    "natuirkriegach": dict(kind="silverfish", scale=5.5, bar="white", tint=("silverfish", (0.10, 0.10, 0.10), (0.44, 0.42, 0.40), (0.80, 0.78, 0.74)),
        atk=lambda a: {"coil": slam(a, 6, 1.0), "grit": cloud(a, "minecraft:slowness", 6, 24, 130, 80, 2), "shards": bolt(a, 4, 0.8, 28, 100), "brood": summon(a, "minecraft:silverfish", 4, 10)},
        ph=[["coil", "shards"], ["coil", "shards", "grit"], ["coil", "shards", "grit", "brood"]]),
    "natuirmoir": dict(kind="guardian", scale=3.4, bar="blue", tint=("guardian", (0.04, 0.10, 0.14), (0.16, 0.42, 0.52), (0.70, 0.95, 0.92)),
        atk=lambda a: {"heads": bolt(a, 7, 0.55, 34, 100, rng=34), "maw": slam(a, 5, 1.1), "surge": wave(a, 13, 0.9), "drowned": summon(a, "minecraft:drowned", 4, 8)},
        ph=[["heads", "maw"], ["heads", "maw", "surge"], ["heads", "maw", "surge", "drowned"]]),
    "tarbiumachuoirtach": dict(kind="ravager", scale=1.7, bar="red", tint=("ravager", (0.10, 0.05, 0.03), (0.46, 0.24, 0.14), (0.82, 0.60, 0.40)),
        atk=lambda a: {"gore": charge(a, 1.3, 24, 100, 16), "slam": slam(a, 5, 1.0), "bound": leap(a, 6, 1.1), "roar": roar(a, 14, "minecraft:slowness", 100, 1)},
        ph=[["gore", "slam"], ["gore", "slam", "bound"], ["gore", "slam", "bound", "roar"]]),
    "oilebeistbouglach": dict(kind="silverfish", scale=7.0, bar="green", tint=("silverfish", (0.04, 0.08, 0.04), (0.20, 0.42, 0.20), (0.62, 0.86, 0.40)),
        atk=lambda a: {"heads": bolt(a, 9, 0.5, 36, 100, rng=32), "venom": cloud(a, "minecraft:poison", 7, 26, 120, 140, 2), "lunge": leap(a, 6), "regrow": heal(a, 0.05, 56, 420)},
        ph=[["heads", "lunge"], ["heads", "lunge", "venom"], ["heads", "lunge", "venom", "regrow"]]),
    "maktirikriuch": dict(kind="hoglin", scale=2.4, bar="white", tint=("hoglin", (0.07, 0.08, 0.10), (0.34, 0.38, 0.46), (0.82, 0.88, 0.95)),
        atk=lambda a: {"rend": leap(a, 5, 1.2), "chase": charge(a, 1.25, 20, 90, 16), "howl": roar(a, 16, "minecraft:slowness", 100, 1), "pack": summon(a, "minecraft:vindicator", 2, 4)},
        ph=[["rend", "chase"], ["rend", "chase", "howl"], ["rend", "chase", "howl", "pack"]]),
    "natuirsaegul": dict(kind="guardian", scale=4.4, bar="blue", tint=("guardian", (0.03, 0.05, 0.12), (0.12, 0.26, 0.50), (0.60, 0.80, 0.98)),
        atk=lambda a: {"coil": slam(a, 6, 1.1), "tidal": wave(a, 14, 1.0), "ink": cloud(a, "minecraft:blindness", 7, 26, 140, 80, 0), "lance": bolt(a, 4, 0.9, 30, 100)},
        ph=[["coil", "lance"], ["coil", "lance", "tidal"], ["coil", "lance", "tidal", "ink"]]),
    "botunkas": dict(kind="spider", scale=3.2, bar="purple", tint=("spider", (0.10, 0.07, 0.04), (0.42, 0.30, 0.18), (0.80, 0.66, 0.40)),
        atk=lambda a: {"stomp": leap(a, 5, 1.1), "brew": cloud(a, "minecraft:poison", 6, 24, 130, 100, 1), "hexbolt": bolt(a, 3, 0.9, 28, 100, effect="minecraft:weakness"), "thralls": summon(a, "minecraft:cave_spider", 5, 10)},
        ph=[["stomp", "hexbolt"], ["stomp", "hexbolt", "brew"], ["stomp", "hexbolt", "brew", "thralls"]]),
    "natuirrig": dict(kind="silverfish", scale=6.0, bar="green", tint=("silverfish", (0.05, 0.07, 0.03), (0.26, 0.40, 0.18), (0.78, 0.84, 0.30)),
        atk=lambda a: {"strike": leap(a, 5, 1.1), "venom": cloud(a, "minecraft:poison", 7, 24, 110, 140, 2), "crown": summon(a, "minecraft:silverfish", 5, 12), "rush": charge(a, 1.2, 20, 90, 14)},
        ph=[["strike", "rush"], ["strike", "rush", "venom"], ["strike", "rush", "venom", "crown"]]),
    "driegunmorgil": dict(kind="phantom", scale=3.6, bar="red", tint=("phantom", (0.12, 0.05, 0.04), (0.55, 0.22, 0.16), (1.0, 0.70, 0.40)),
        atk=lambda a: {"breath": bolt(a, 4, 0.9, 30, 90, True, rng=36), "dive": leap(a, 6, 1.2), "wing": wave(a, 12), "gorge": cloud(a, "minecraft:wither", 7, 26, 140, 100, 0)},
        ph=[["breath", "dive"], ["breath", "dive", "wing"], ["breath", "dive", "wing", "gorge"]]),
    "toisechmarb": dict(kind="zombie", scale=1.6, bar="blue", tint=("zombie", (0.03, 0.08, 0.10), (0.16, 0.38, 0.40), (0.66, 0.88, 0.80)),
        atk=lambda a: {"hook": slam(a, 5, 1.0), "swell": wave(a, 12, 0.9), "crew": summon(a, "minecraft:drowned", 4, 8), "anchor": charge(a, 1.2, 22, 100)},
        ph=[["hook", "anchor"], ["hook", "anchor", "swell"], ["hook", "anchor", "swell", "crew"]]),
    "maerkistechuimni": dict(kind="golem", scale=2.2, bar="white", tint=("golem", (0.08, 0.10, 0.14), (0.40, 0.52, 0.62), (0.86, 0.96, 1.0)),
        atk=lambda a: {"index": bolt(a, 5, 0.8, 30, 90, rng=40), "seal": slam(a, 6, 1.0), "scribes": summon(a, "minecraft:vex", 3, 6), "recall": roar(a, 16, "minecraft:mining_fatigue", 120, 1)},
        ph=[["index", "seal"], ["index", "seal", "scribes"], ["index", "seal", "scribes", "recall"]]),
    "natuirtuar": dict(kind="phantom", scale=4.2, bar="white", tint=("phantom", (0.18, 0.22, 0.26), (0.62, 0.74, 0.82), (0.96, 1.0, 1.0)),
        atk=lambda a: {"glint": bolt(a, 4, 0.8, 26, 90, rng=42), "coil": leap(a, 6, 1.1), "shatter": wave(a, 13, 0.9), "arc": lightning(a, 4, 0.8, 36, 140)},
        ph=[["glint", "coil"], ["glint", "coil", "shatter"], ["glint", "coil", "shatter", "arc"]]),
    "stoirmiduislach": dict(kind="blaze", scale=3.0, bar="yellow", fire_immune=True, tint=("blaze", (0.20, 0.14, 0.06), (0.70, 0.55, 0.30), (1.0, 0.94, 0.70)),
        atk=lambda a: {"dust": cloud(a, "minecraft:blindness", 7, 24, 130, 100, 0), "gale": wave(a, 13, 0.9), "bolts": lightning(a, 4, 0.9, 36, 130), "scour": roar(a, 16, "minecraft:slowness", 120, 2)},
        ph=[["dust", "gale"], ["dust", "gale", "bolts"], ["dust", "gale", "bolts", "scour"]]),
    "famuirmeirgi": dict(kind="golem", scale=3.1, bar="red", fire_immune=True, tint=("golem", (0.12, 0.05, 0.02), (0.52, 0.28, 0.14), (0.84, 0.52, 0.28)),
        atk=lambda a: {"crush": slam(a, 7, 1.1, 38, 90), "flake": cloud(a, "minecraft:weakness", 7, 26, 130, 120, 1), "stride": charge(a, 1.2, 28, 110, 16), "scrap": summon(a, "minecraft:silverfish", 5, 10)},
        ph=[["crush", "stride"], ["crush", "stride", "flake"], ["crush", "stride", "flake", "scrap"]]),
    "niechgleididdouras": dict(kind="golem", scale=2.5, bar="purple", tint=("golem", (0.07, 0.06, 0.12), (0.34, 0.30, 0.52), (0.78, 0.72, 0.96)),
        atk=lambda a: {"bar": slam(a, 6, 1.1, 34, 90), "ward": wave(a, 12, 0.9), "toll": roar(a, 16, "minecraft:slowness", 100, 2), "sentinels": summon(a, "minecraft:vex", 3, 6)},
        ph=[["bar", "ward"], ["bar", "ward", "toll"], ["bar", "ward", "toll", "sentinels"]]),
}
MYTH_LOOT_ITEMS = {  # a themed extra per kit
    "ridiriknok": "minecraft:amethyst_shard", "toimsechunleomund": "minecraft:sand", "tarbspeiri": "minecraft:lapis_lazuli",
    "driegunsalund": "minecraft:prismarine_shard", "natuirkriegach": "minecraft:raw_iron", "natuirmoir": "minecraft:prismarine_crystals",
    "tarbiumachuoirtach": "minecraft:leather", "oilebeistbouglach": "minecraft:slime_ball", "maktirikriuch": "minecraft:white_wool",
    "natuirsaegul": "minecraft:nautilus_shell", "botunkas": "minecraft:redstone", "natuirrig": "minecraft:emerald",
    "driegunmorgil": "minecraft:blaze_powder", "toisechmarb": "minecraft:heart_of_the_sea", "maerkistechuimni": "minecraft:book",
    "natuirtuar": "minecraft:quartz", "stoirmiduislach": "minecraft:glowstone_dust", "famuirmeirgi": "minecraft:raw_copper",
    "niechgleididdouras": "minecraft:ender_pearl"}
MYTH_BIOMES = {"diaithir": "#minecraft:is_forest", "nilotic": "#minecraft:is_badlands", "mesopotamian": "minecraft:plains,minecraft:sunflower_plains,minecraft:meadow",
               "anatolian": "#minecraft:is_mountain", "canaanite": "#minecraft:is_beach", "minoan": "#minecraft:is_hill",
               "hellenic": "minecraft:swamp,minecraft:mangrove_swamp", "germanic": "#minecraft:is_taiga", "slavic": "#minecraft:is_taiga",
               "baltic": "minecraft:swamp,minecraft:mangrove_swamp", "caucasus": "#minecraft:is_mountain"}

MYTH_ROWS = list(csv.DictReader(open(BOSSCSV, encoding="utf8")))
for r in MYTH_ROWS:
    cid = r["creature_id"]
    court = r["court"]
    cname = r["coined_name"]
    key = ascii_id(cid.split(".", 1)[1])
    age = int(r["age"])
    kit = MYTH_KIT[key]
    a = age
    atk = kit["atk"](a)
    hp = {2: 420, 3: 520, 4: 620, 5: 720, 6: 820, 7: 950}[a]
    p = add(key, cname, r["coined_name"], kit["kind"], kit["scale"], a, bar=kit["bar"], radius=26, hp=hp,
            fire_immune=kit.get("fire_immune", False), tint=kit["tint"], attacks=atk, xp=700 + 200 * a,
            phases=phases((1.0, 1.0, 1.0, 1.0, kit["ph"][0]),
                          (0.65, 1.12, 1.15, 0.85, kit["ph"][1], {"invuln": 40}),
                          (0.3, 1.25, 1.3, 0.7, kit["ph"][2], {"invuln": 40})),
            loot=tiered_loot(a, 2) + [L(MYTH_LOOT_ITEMS[key], 4, 10, a, 1.0, 2), L("minecraft:experience_bottle", 3, 8, a, 1.0, 2)],
            respawn={"mode": "once", "ticks": 0},
            structure=None)
    p["creature_id"] = cid
    p["court"] = court
    p["habitat"] = r["habitat"]
    p["existing_model"] = r["existing_model"]
    p["mythic"] = True
    if key in MYTH_MOD:
        p["entity"] = f"diaithir_myth:{key}"
        p["fallback_own"] = True   # own entity registered only when diaithir_myth is absent
    p["max_hit_fraction"] = 0.08
    p["arena"]["radius"] = 28

for p in BOSSES:
    if p["id"] in OWN_NAMES:
        p["name"] = OWN_NAMES[p["id"]]
    p.setdefault("mythic", False)
    p.setdefault("entity", f"{NS}:{p['id']}")
    p.setdefault("creature_id", None)
    p["own"]["fire_immune"] = bool(p.pop("fire_immune", False))
    if p["mythic"]:
        p["own"]["hp"] = p["own"]["hp"]
    p.setdefault("equipment", {})

# ---------------------------------------------------------------- champion elites
ELITES = {
    "_doc": "Champion elites: a monster that rolls an elite spawn gets the champion prefix plus 1-3 affixes from its base entry; "
            "the roll chance and affix count rise with the world Age. Affix mechanics are in Elites.java by id; numbers are here.",
    "chance_by_age": [0.03, 0.04, 0.06, 0.08, 0.10, 0.12, 0.15, 0.18],
    "affixes_by_age": [1, 1, 1, 2, 2, 2, 3, 3],
    "extra_hp_per_age": 0.06,
    "xp_multiplier": 3.0,
    "token": {"item": "diaithirbosses:champion_token", "chance": 0.35, "min_age": 1},
    "bases": {
        "minecraft:zombie": {"weight": 10, "affixes": ["hulking", "swift", "thorned", "venomous", "vampiric", "warbanded", "shielded", "ablaze"]},
        "minecraft:husk": {"weight": 4, "affixes": ["hulking", "swift", "venomous", "vampiric", "warbanded", "shielded"]},
        "minecraft:drowned": {"weight": 4, "affixes": ["hulking", "swift", "frostbitten", "vampiric", "warbanded", "shielded"]},
        "minecraft:skeleton": {"weight": 10, "affixes": ["keen", "swift", "frostbitten", "venomous", "vampiric", "warbanded", "shielded", "ablaze"]},
        "minecraft:stray": {"weight": 4, "affixes": ["keen", "frostbitten", "venomous", "shielded", "warbanded"]},
        "minecraft:creeper": {"weight": 8, "affixes": ["volatile", "swift", "hulking", "shielded", "ablaze", "thorned"]},
        "minecraft:spider": {"weight": 6, "affixes": ["swift", "venomous", "hulking", "thorned", "vampiric", "warbanded"]},
        "minecraft:cave_spider": {"weight": 2, "affixes": ["swift", "venomous", "warbanded"]},
        "minecraft:pillager": {"weight": 3, "affixes": ["keen", "swift", "vampiric", "shielded", "warbanded"]},
        "minecraft:vindicator": {"weight": 3, "affixes": ["hulking", "swift", "vampiric", "shielded", "warbanded", "thorned"]},
        "minecraft:witch": {"weight": 2, "affixes": ["venomous", "frostbitten", "shielded", "vampiric"]},
        "minecraft:enderman": {"weight": 2, "affixes": ["swift", "thorned", "vampiric", "shielded"]},
    },
    "affixes": {
        "hulking": {"attributes": {"minecraft:max_health": [0.6, "add_multiplied_total"], "minecraft:attack_damage": [0.25, "add_multiplied_total"],
                                   "minecraft:scale": [0.2, "add_multiplied_total"]}},
        "swift": {"attributes": {"minecraft:movement_speed": [0.3, "add_multiplied_total"]}},
        "thorned": {"reflect": 0.3},
        "venomous": {"on_hit": {"effect": "minecraft:poison", "ticks": 100, "amp": 0}},
        "frostbitten": {"on_hit": {"effect": "minecraft:slowness", "ticks": 100, "amp": 1}},
        "vampiric": {"leech": 0.5},
        "ablaze": {"on_hit": {"fire": 5}, "fire_immune": True},
        "warbanded": {"summon_on_hurt": {"count": 2, "once": True}},
        "volatile": {"blast_on_death": {"power": 3.0, "fire": False}},
        "shielded": {"attributes": {"minecraft:max_absorption": [8.0, "add_value"]}, "absorption": 8.0},
        "keen": {"attributes": {"minecraft:follow_range": [16.0, "add_value"], "minecraft:attack_damage": [0.15, "add_multiplied_total"]}},
    },
}

# ---------------------------------------------------------------- textures
def lum(img):
    a = np.asarray(img.convert("RGBA")).astype(np.float32) / 255.0
    l = 0.299 * a[..., 0] + 0.587 * a[..., 1] + 0.114 * a[..., 2]
    return l, a[..., 3]


def ramp(l, lo, mid, hi):
    lo, mid, hi = (np.array(c, np.float32) for c in (lo, mid, hi))
    lo_n, hi_n = float(l.min()), float(l.max())
    t = (l - lo_n) / max(1e-4, hi_n - lo_n)
    t = t[..., None]
    c = np.where(t < 0.5, lo + (mid - lo) * (t * 2), mid + (hi - mid) * ((t - 0.5) * 2))
    return c


def make_texture(boss, rnd):
    kind = boss["own"]["kind"]
    base = Image.open(os.path.join(VAN, "entity", KINDS[kind][0]))
    tint = boss["tint"]
    l, alpha = lum(base)
    # keep the eye/glow pixels (very bright) bright
    c = ramp(l, tint[1], tint[2], tint[3])
    c = ramp(l, tint[1], tint[2], tint[3])
    h, w = l.shape
    noise = (np.array([[rnd.random() for _ in range(w)] for _ in range(h)], np.float32) - 0.5) * 0.10
    c = np.clip(c + noise[..., None], 0, 1)
    # wear: a few darker scratches per boss
    for _ in range(max(6, w * h // 700)):
        x, y = rnd.randrange(w), rnd.randrange(h)
        for i in range(rnd.randrange(2, 6)):
            if x + i < w:
                c[y, x + i] = c[y, x + i] * 0.72
    out = np.dstack([c, alpha])
    img = Image.fromarray((out * 255).astype(np.uint8), "RGBA")
    return img


def icon(path, body, rim, kind="egg"):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if kind == "egg":
        d.ellipse((3, 1, 12, 14), fill=rim)
        d.ellipse((4, 2, 11, 13), fill=body)
        d.ellipse((6, 4, 9, 7), fill=tuple(min(255, int(v * 1.5)) for v in body))
    else:  # trophy: skull-ish medallion
        d.ellipse((2, 2, 13, 13), fill=rim)
        d.ellipse((3, 3, 12, 12), fill=body)
        d.rectangle((5, 6, 6, 7), fill=rim)
        d.rectangle((9, 6, 10, 7), fill=rim)
        d.rectangle((6, 10, 9, 11), fill=rim)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def hexc(c):
    return tuple(int(255 * v) for v in c)


# ---------------------------------------------------------------- NBT writer (structure templates)
def _s(v):
    b = v.encode("utf8")
    return struct.pack(">H", len(b)) + b


def nbt(tag, v):
    t = tag
    if t == "int":
        return struct.pack(">i", v)
    if t == "str":
        return _s(v)
    if t == "list":
        et, items = v
        out = bytes([ID[et]]) + struct.pack(">i", len(items))
        for it in items:
            out += nbt(et, it)
        return out
    if t == "compound":
        out = b""
        for k, (tt, vv) in v.items():
            out += bytes([ID[tt]]) + _s(k) + nbt(tt, vv)
        return out + b"\x00"
    raise ValueError(t)


ID = {"int": 3, "str": 8, "list": 9, "compound": 10}

STYLE = {1: ("minecraft:coarse_dirt", "minecraft:podzol", "minecraft:bone_block"),
         2: ("minecraft:stone", "minecraft:andesite", "minecraft:cobblestone_wall"),
         3: ("minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:stone_brick_wall"),
         4: ("minecraft:deepslate_bricks", "minecraft:cracked_deepslate_bricks", "minecraft:deepslate_brick_wall"),
         5: ("minecraft:polished_blackstone_bricks", "minecraft:blackstone", "minecraft:polished_blackstone_brick_wall"),
         6: ("minecraft:cut_copper", "minecraft:exposed_cut_copper", "minecraft:cut_copper"),
         7: ("minecraft:smooth_quartz", "minecraft:quartz_bricks", "minecraft:quartz_pillar")}


def arena_nbt(boss_id, style, radius, path):
    floor, accent, pillar = STYLE[style]
    pal = {}
    blocks = []

    def st(name, props=None):
        k = (name, tuple(sorted((props or {}).items())))
        if k not in pal:
            pal[k] = len(pal)
        return pal[k]

    size = radius * 2 + 1
    height = 8
    rnd = random.Random(boss_id)
    cx = cz = radius
    # floor foundation 3 deep below y=0: place at y=1..  we use template y=0..: floor at y=2, foundation y=0..1
    for x in range(size):
        for z in range(size):
            d = math.hypot(x - cx, z - cz)
            if d > radius + 0.3:
                continue
            for y in (0, 1):
                blocks.append(((x, y, z), st(accent if rnd.random() < 0.3 else floor), None))
            top = accent if (rnd.random() < 0.18 or abs(d - radius * 0.55) < 0.6) else floor
            blocks.append(((x, 2, z), st(top), None))
            for y in range(3, height):
                blocks.append(((x, y, z), st("minecraft:air"), None))
    # rim pillars
    for k in range(12):
        ang = k * math.tau / 12
        px, pz = int(round(cx + math.cos(ang) * (radius - 1))), int(round(cz + math.sin(ang) * (radius - 1)))
        for y in range(3, 7):
            blocks.append(((px, y, pz), st(pillar), None))
        blocks.append(((px, 7, pz), st("minecraft:lantern" if style < 7 else "minecraft:sea_lantern", {"hanging": "false"} if style < 7 else None), None))
    # altar
    blocks.append(((cx, 3, cz), st(f"{NS}:boss_altar"), {"id": ("str", f"{NS}:boss_altar"), "boss": ("str", boss_id)}))
    palette = [None] * len(pal)
    for (name, props), i in pal.items():
        e = {"Name": ("str", name)}
        if props:
            e["Properties"] = ("compound", {k: ("str", v) for k, v in props})
        palette[i] = e
    root = {"DataVersion": ("int", DATA_VERSION), "size": ("list", ("int", [size, height, size])),
            "palette": ("list", ("compound", palette)), "entities": ("list", ("compound", [])),
            "blocks": ("list", ("compound", [
                {"pos": ("list", ("int", list(p))), "state": ("int", s), **({"nbt": ("compound", n)} if n else {})}
                for p, s, n in blocks]))}
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with gzip.open(path, "wb") as f:
        f.write(bytes([10]) + _s("") + nbt("compound", root))


# ---------------------------------------------------------------- emit
def main():
    import shutil
    for d in (AS, DA):
        shutil.rmtree(d, ignore_errors=True)
    names = {}
    lang = {}
    index = []
    library = []
    age_items = {}
    age_entities = {}
    chapters = {}
    for p in BOSSES:
        rnd = random.Random(p["id"])
        tex = make_texture(p, rnd)
        os.makedirs(os.path.join(AS, "textures/entity"), exist_ok=True)
        tex.save(os.path.join(AS, "textures/entity", p["id"] + ".png"))
        body = hexc(p["tint"][2])
        rim = hexc(p["tint"][1])
        icon(os.path.join(AS, "textures/item", p["id"] + "_spawn_egg.png"), body, rim, "egg")
        icon(os.path.join(AS, "textures/item", p["id"] + "_trophy.png"), hexc(p["tint"][3]), rim, "trophy")
        for it in ("spawn_egg", "trophy"):
            W(os.path.join(AS, "items", f"{p['id']}_{it}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{p['id']}_{it}"}})
            W(os.path.join(AS, "models/item", f"{p['id']}_{it}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{p['id']}_{it}"}})
        nm = p["name"]
        names[nm] = p["name_en"]
        lang[f"entity.{NS}.{p['id']}"] = nm
        lang[f"boss.{NS}.{p['id']}"] = nm
        lang[f"item.{NS}.{p['id']}_spawn_egg"] = nm
        lang[f"item.{NS}.{p['id']}_trophy"] = "Kuochbuoda " + nm
        p.pop("tint")
        W(os.path.join(DA, "boss", p["id"] + ".json"), p)
        index.append(p["id"])
        age_entities[p["entity"]] = ROM[p["age"]]
        age_items[f"{NS}:{p['id']}_trophy"] = ROM[p["age"]]
        age_items[f"{NS}:{p['id']}_spawn_egg"] = ROM[p["age"]]
        if p.get("structure"):
            s = p["structure"]
            rad = 14 + 2 * (p["age"] // 3)
            arena_nbt(p["id"], s["style"], rad, os.path.join(DA, "structure", p["id"] + ".nbt"))
            W(os.path.join(DA, "worldgen/template_pool", p["id"], "start.json"),
              {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {"element_type": "minecraft:single_pool_element",
               "location": f"{NS}:{p['id']}", "processors": "minecraft:empty", "projection": "rigid"}}]})
            W(os.path.join(DA, "worldgen/structure", p["id"] + ".json"),
              {"type": "minecraft:jigsaw", "biomes": f"#{NS}:has_{p['id']}", "step": "surface_structures", "spawn_overrides": {},
               "terrain_adaptation": "beard_thin", "start_pool": f"{NS}:{p['id']}/start", "size": 1, "start_height": {"absolute": 0},
               "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 80, "use_expansion_hack": False})
            W(os.path.join(DA, "worldgen/structure_set", p["id"] + ".json"),
              {"structures": [{"structure": f"{NS}:{p['id']}", "weight": 1}],
               "placement": {"type": "minecraft:random_spread", "spacing": s["spacing"], "separation": s["separation"],
                             "salt": 1000 + sum(map(ord, p["id"]))}})
            W(os.path.join(DA, "tags/worldgen/biome", f"has_{p['id']}.json"), {"values": [{"id": b, "required": False} for b in s["biomes"].split(",")]})
        chapters.setdefault(p["age"], []).append(p)
        if p["mythic"]:
            txt = (f"An avatar of the {p['court']} court, kept as a boss of the {p['habitat']}. Three phases, "
                   f"telegraphed strikes, an arena that holds the fighters in, and drops that grow with the Age. Age {ROM[p['age']]}.")
        else:
            txt = (f"A boss from Age {ROM[p['age']]}: phases, telegraphed strikes, an arena that holds the fighters in, and drops that grow "
                   f"with the Age. Summon it at its altar; it does not despawn.")
        pass
        # eggs/trophy are always free
    # elite affix lang
    for k, v in AFFIX_NAMES.items():
        lang[f"elite.{NS}.affix.{k}"] = v
    lang[f"elite.{NS}.name"] = f"{CHAMPION} %s %s"
    lang[f"elite.{NS}.name1"] = f"{CHAMPION} %s"
    lang["item.diaithirbosses.champion_token"] = "Komurra " + CHAMPION.lower()
    lang["block.diaithirbosses.boss_altar"] = D["altar"] + " " + D["boss"].lower()
    lang["itemGroup.diaithirbosses"] = D["boss"]
    lang["boss.diaithirbosses.phase"] = "%s · %s"
    lang["boss.diaithirbosses.dormant"] = "The altar sleeps until Age %s."
    lang["boss.diaithirbosses.cooldown"] = "The altar is cold. It wakes again in %s minutes."
    lang["boss.diaithirbosses.spent"] = "The altar is spent."
    lang["boss.diaithirbosses.present"] = "The challenger is already here."
    lang["boss.diaithirbosses.peaceful"] = "Nothing answers in peaceful lands."
    lang["boss.diaithirbosses.woken"] = "The altar wakes."
    names[CHAMPION] = "Champion"
    names["Komurra " + CHAMPION.lower()] = "Champion token"
    names[D["altar"] + " " + D["boss"].lower()] = "Boss altar"
    for k, v in AFFIX_NAMES.items():
        names[v] = k
    W(os.path.join(AS, "lang/en_us.json"), lang)
    W(os.path.join(DA, "boss_index.json"), index)
    W(os.path.join(DA, "elites.json"), ELITES)
    W(os.path.join(ROOT, "names_map.json"), names)
    # icon for token, altar model/blockstate/item
    icon(os.path.join(AS, "textures/item/champion_token.png"), (200, 160, 60), (90, 60, 20), "trophy")
    W(os.path.join(AS, "items/champion_token.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/champion_token"}})
    W(os.path.join(AS, "models/item/champion_token.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/champion_token"}})
    W(os.path.join(AS, "blockstates/boss_altar.json"), {"variants": {"": {"model": f"{NS}:block/boss_altar"}}})
    W(os.path.join(AS, "models/block/boss_altar.json"), {"parent": "minecraft:block/cube_all", "textures": {"all": "minecraft:block/chiseled_stone_bricks"}})
    W(os.path.join(AS, "models/item/boss_altar.json"), {"parent": f"{NS}:block/boss_altar"})
    W(os.path.join(AS, "items/boss_altar.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/boss_altar"}})
    W(os.path.join(DA, "loot_table/blocks/boss_altar.json"), {"type": "minecraft:block", "pools": []})
    # age gates
    age_items[f"{NS}:champion_token"] = "I"
    age_items[f"{NS}:boss_altar"] = "I"
    W(os.path.join(ROOT, "age_gates.json"), {
        "_doc": "Same shape as DiaithirRPG content/tech_ages.json: namespaces, per-item entries (items beat namespaces) and 'entities' "
                "(the Age a boss wakes at; its altar refuses earlier, and its drops scale up after it). Boss min Age is also in data/diaithirbosses/boss/<id>.json.",
        "namespaces": {NS: "I"}, "items": age_items, "entities": age_entities})
    # Library prose is generated by the compute job /mnt/project-files/compute/jobs/diaithir-bosses_lore (see README there)
    # codex chapters
    ch = []
    for age in sorted(chapters):
        ch.append({"tier": f"age{age}", "name": D["boss"] + " " + ROM[age], "age": ROM[age],
                   "text": "Bosses that wake at Age " + ROM[age] + ".",
                   "items": [{"id": f"{NS}:{p['id']}_trophy", "name": p["name"], "age": ROM[p["age"]], "text": ""} for p in chapters[age]]})
    W(os.path.join(ROOT, "codex/bosses_handbook.json"), {"id": f"{NS}:handbook", "title": D["boss"], "chapters": ch})


main()
