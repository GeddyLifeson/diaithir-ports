#!/usr/bin/env python3
"""Generates the player animation clips (PAL JSON) and item tags for diaithir-animation.

Usage: gen_clips.py [coverage.csv]   (default /mnt/project-files/plans/ANIMATIONS_COVERAGE.csv)
Writes assets/diaithiranimation/player_animations/{melee,tools,guns,casting}.json, data/diaithiranimation/tags/item/anim/*.json
and clips.json (class -> clip names, read by the mod and the selftest).
Angles are degrees, vanilla ModelPart convention: arm xRot -90 = straight forward, yRot toward the body centre is negative for the right arm.
Bones: right_arm left_arm torso head (PAL bone names).
"""
import csv, json, os, re, sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main")
ASSET = os.path.join(ROOT, "resources", "assets", "diaithiranimation", "player_animations")
TAGS = os.path.join(ROOT, "resources", "data", "diaithiranimation", "tags", "item", "anim")
CSV = sys.argv[1] if len(sys.argv) > 1 else "/mnt/project-files/plans/ANIMATIONS_COVERAGE.csv"

def clip(length, bones, loop=False):
    """bones: {bone: [(t, [x,y,z]), ...]} -> one PAL/Bedrock-style animation."""
    out = {"animation_length": length, "bones": {}}
    if loop:
        out["loop"] = True
    for b, frames in bones.items():
        out["bones"][b] = {"rotation": {f"{t:.2f}": {"vector": v, "easing": "easeInOutSine"} for t, v in frames}}
    return out

def A(x=0, y=0, z=0):
    return [x, y, z]

REST = A()
# ---------------------------------------------------------------- melee (right hand weapon)
def swing(path, windup, hit, follow, length):
    """path: start->end arm pose for the stroke; torso twists with it."""
    (s, e) = path
    return clip(length, {
        "right_arm": [(0, REST), (windup, s), (hit, e), (follow, e), (length, REST)],
        "left_arm": [(0, REST), (windup, A(-20, 20, 0)), (hit, A(-10, 10, 0)), (length, REST)],
        "torso": [(0, REST), (windup, A(0, 18, 0)), (hit, A(0, -22, 0)), (length, REST)],
        "head": [(0, REST), (hit, A(0, 10, 0)), (length, REST)],
    })

def thrust(length=0.4):
    return clip(length, {
        "right_arm": [(0, REST), (0.12, A(-70, -10, 0)), (0.2, A(-95, 0, 0)), (0.3, A(-95, 0, 0)), (length, REST)],
        "left_arm": [(0, REST), (0.12, A(-60, 30, 0)), (length, REST)],
        "torso": [(0, REST), (0.12, A(0, 12, 0)), (0.2, A(0, -8, 0)), (length, REST)],
    })

def raise_loop(arm_pose, off_pose=None):
    b = {"right_arm": [(0, arm_pose), (1.0, arm_pose)]}
    if off_pose:
        b["left_arm"] = [(0, off_pose), (1.0, off_pose)]
    return clip(1.0, b, loop=True)

def draw_clip(pose, length=0.4):
    return clip(length, {"right_arm": [(0, A(-10, 0, 60)), (0.2, A(-110, 0, 20)), (length, pose)], "head": [(0, REST), (0.2, A(5, 0, 0)), (length, REST)]})

DOWN = A(30, 0, 10)
OVER = A(-150, -10, 5)       # overhead
BACK = A(-60, 40, 30)        # wound back to the right
SIDE_HI = A(-120, 30, 20)
FWD = A(-80, -20, 0)

MELEE = {
    "sword": {
        "swing1": swing((SIDE_HI, A(-40, -50, 0)), 0.15, 0.3, 0.38, 0.5),
        "swing2": swing((A(-40, -50, 0), SIDE_HI), 0.15, 0.3, 0.38, 0.5),
        "swing3": swing((OVER, A(-30, -10, 0)), 0.2, 0.38, 0.45, 0.6),
        "thrust": thrust(),
        "block": clip(0.3, {"right_arm": [(0, REST), (0.15, A(-80, -40, 10)), (0.3, A(-80, -40, 10))], "left_arm": [(0, REST), (0.15, A(-30, 20, 0)), (0.3, A(-30, 20, 0))]}),
        "sheathe": clip(0.5, {"right_arm": [(0, A(-30, 0, 0)), (0.25, A(10, 20, 40)), (0.5, REST)], "left_arm": [(0, REST), (0.25, A(-20, -10, 0)), (0.5, REST)]}),
        "draw": clip(0.5, {"right_arm": [(0, A(10, 20, 40)), (0.25, A(-70, 10, 30)), (0.5, A(-20, 0, 0))], "torso": [(0, REST), (0.25, A(0, -8, 0)), (0.5, REST)]}),
    },
    "axe": {
        "swing1": swing((OVER, A(-10, -15, 0)), 0.2, 0.35, 0.45, 0.6),
        "swing2": swing((SIDE_HI, A(-30, -60, 0)), 0.2, 0.35, 0.45, 0.6),
        "swing3": swing((OVER, A(20, -10, 0)), 0.25, 0.42, 0.5, 0.7),
        "chop": swing((OVER, A(-20, 0, 0)), 0.2, 0.34, 0.42, 0.55),
        "draw": draw_clip(A(-20, 0, 0)),
    },
    "spear": {
        "thrust": thrust(0.45),
        "throw": clip(0.55, {"right_arm": [(0, REST), (0.2, A(-160, 0, 20)), (0.32, A(-70, 0, 0)), (0.55, REST)], "torso": [(0, REST), (0.2, A(0, 25, 0)), (0.32, A(0, -20, 0)), (0.55, REST)]}),
        "charge": clip(0.6, {"right_arm": [(0, REST), (0.25, A(-80, -15, 0)), (0.6, A(-85, -15, 0))], "left_arm": [(0, REST), (0.25, A(-50, 30, 0))], "torso": [(0, REST), (0.3, A(10, 0, 0))]}),
        "draw": draw_clip(A(-50, -10, 0)),
    },
    "dagger": {
        "stab": thrust(0.3),
        "slash": swing((A(-90, 30, 20), A(-60, -40, 0)), 0.1, 0.2, 0.26, 0.35),
        "throw": clip(0.4, {"right_arm": [(0, REST), (0.15, A(-150, 0, 20)), (0.25, A(-90, 0, 0)), (0.4, REST)]}),
        "draw": draw_clip(A(-40, -10, 0), 0.3),
    },
    "mace": {
        "overhead": swing((OVER, A(10, -5, 0)), 0.28, 0.46, 0.55, 0.75),
        "smash": swing((A(-170, 0, 0), A(30, 0, 0)), 0.35, 0.55, 0.7, 0.9),
        "draw": draw_clip(A(-20, 0, 0), 0.5),
    },
    "staff": {
        "cast_raise": clip(0.5, {"right_arm": [(0, REST), (0.5, A(-140, -10, 10))], "left_arm": [(0, REST), (0.5, A(-60, 20, 0))], "head": [(0, REST), (0.5, A(-10, 0, 0))]}),
        "channel": clip(1.0, {"right_arm": [(0, A(-140, -10, 10)), (0.5, A(-146, -6, 14)), (1.0, A(-140, -10, 10))], "left_arm": [(0, A(-60, 20, 0)), (0.5, A(-70, 24, 4)), (1.0, A(-60, 20, 0))]}, loop=True),
        "release": clip(0.4, {"right_arm": [(0, A(-140, -10, 10)), (0.15, A(-70, 0, 0)), (0.4, REST)], "left_arm": [(0, A(-60, 20, 0)), (0.4, REST)], "torso": [(0, REST), (0.15, A(8, 0, 0)), (0.4, REST)]}),
        "draw": draw_clip(A(-30, 0, 0), 0.4),
    },
    "shield": {
        "raise": clip(0.2, {"left_arm": [(0, REST), (0.2, A(-70, 30, -10))]}),
        "hold": clip(1.0, {"left_arm": [(0, A(-70, 30, -10)), (1.0, A(-70, 30, -10))]}, loop=True),
        "block_hit": clip(0.25, {"left_arm": [(0, A(-70, 30, -10)), (0.08, A(-60, 25, -10)), (0.25, A(-70, 30, -10))], "torso": [(0, REST), (0.08, A(-6, 0, 0)), (0.25, REST)]}),
        "lower": clip(0.2, {"left_arm": [(0, A(-70, 30, -10)), (0.2, REST)]}),
    },
    "bow": {
        "nock": clip(0.3, {"left_arm": [(0, REST), (0.3, A(-90, 10, 0))], "right_arm": [(0, REST), (0.3, A(-40, -30, 0))]}),
        "draw": clip(0.8, {"left_arm": [(0, A(-90, 10, 0)), (0.8, A(-92, 10, 0))], "right_arm": [(0, A(-40, -30, 0)), (0.8, A(-80, -60, 0))], "head": [(0, REST), (0.8, A(0, -10, 0))]}),
        "hold": clip(1.0, {"left_arm": [(0, A(-92, 10, 0)), (1.0, A(-92, 10, 0))], "right_arm": [(0, A(-80, -60, 0)), (1.0, A(-80, -60, 0))]}, loop=True),
        "release": clip(0.3, {"left_arm": [(0, A(-92, 10, 0)), (0.3, REST)], "right_arm": [(0, A(-80, -60, 0)), (0.08, A(-85, -20, 0)), (0.3, REST)]}),
    },
    "crossbow": {
        "load": clip(0.9, {"left_arm": [(0, REST), (0.3, A(-80, 20, 0)), (0.9, A(-80, 20, 0))], "right_arm": [(0, REST), (0.3, A(-50, -30, 0)), (0.6, A(-30, -20, 0)), (0.9, A(-60, -20, 0))]}),
        "aim": clip(1.0, {"right_arm": [(0, A(-85, -10, 0)), (1.0, A(-85, -10, 0))], "left_arm": [(0, A(-80, 20, 0)), (1.0, A(-80, 20, 0))]}, loop=True),
        "fire": clip(0.3, {"right_arm": [(0, A(-85, -10, 0)), (0.05, A(-95, -10, 0)), (0.3, REST)], "left_arm": [(0, A(-80, 20, 0)), (0.3, REST)], "torso": [(0, REST), (0.05, A(-4, 0, 0)), (0.3, REST)]}),
    },
}
TOOLS = {
    "pickaxe": {"mine": swing((OVER, A(-10, -10, 0)), 0.2, 0.34, 0.42, 0.55)},
    "shovel": {"dig": swing((A(-40, 0, 0), A(20, -10, 0)), 0.2, 0.36, 0.45, 0.6)},
    "hoe": {"till": swing((A(-110, -10, 5), A(10, -10, 0)), 0.2, 0.36, 0.45, 0.6)},
    "shears": {"snip": clip(0.4, {"right_arm": [(0, REST), (0.1, A(-60, -20, 0)), (0.2, A(-55, -20, 0)), (0.3, A(-60, -20, 0)), (0.4, REST)], "left_arm": [(0, REST), (0.1, A(-60, 20, 0)), (0.4, REST)]})},
}
# ---------------------------------------------------------------- guns (hold = lowered ready, aim = shouldered)
GUN = {  # class: (hold arm pose R, L, aim R, L, recoil deg, reload seconds, one-handed)
    "pistol":   (A(-50, -10, 0), A(-40, 20, 0), A(-88, -4, 0), A(-80, 25, 0), 8, 1.4, False),
    "smg":      (A(-45, -15, 0), A(-50, 30, 0), A(-86, -10, 0), A(-86, 40, 0), 4, 1.8, False),
    "rifle":    (A(-40, -20, 0), A(-55, 35, 0), A(-86, -18, 0), A(-90, 45, 0), 6, 2.2, False),
    "sniper":   (A(-35, -20, 0), A(-50, 40, 0), A(-88, -18, 0), A(-92, 48, 0), 10, 2.8, False),
    "shotgun":  (A(-40, -20, 0), A(-55, 35, 0), A(-86, -16, 0), A(-92, 45, 0), 12, 2.4, False),
    "lmg":      (A(-40, -25, 0), A(-60, 35, 0), A(-84, -20, 0), A(-88, 40, 0), 3, 3.4, False),
    "launcher": (A(-30, -25, 0), A(-60, 40, 0), A(-70, -35, 20), A(-80, 30, 0), 14, 3.0, False),
    "energy":   (A(-45, -15, 0), A(-50, 30, 0), A(-86, -12, 0), A(-88, 40, 0), 2, 2.0, False),
}
# reload ticks per own gun (diaithir-firearms guns.json "reload"); the clip is exactly as long as the server reload
RELOAD = {"sidearm_light": 40, "sidearm_heavy": 44, "magnum": 60, "smg_compact": 50, "carbine": 56, "assault_rifle": 60, "marksman_rifle": 64,
          "sniper_rifle": 70, "antimaterial_rifle": 90, "pump_shotgun": 70, "combat_shotgun": 80, "auto_shotgun": 90, "light_mg": 120, "minigun": 140,
          "grenade_launcher": 90, "rocket_launcher": 100, "energy_rifle": 60}
OWN = {"sidearm_light": "pistol", "sidearm_heavy": "pistol", "magnum": "pistol", "smg_compact": "smg", "carbine": "rifle", "assault_rifle": "rifle",
       "marksman_rifle": "rifle", "sniper_rifle": "sniper", "antimaterial_rifle": "sniper", "pump_shotgun": "shotgun", "combat_shotgun": "shotgun",
       "auto_shotgun": "shotgun", "light_mg": "lmg", "minigun": "lmg", "grenade_launcher": "launcher", "rocket_launcher": "launcher", "energy_rifle": "energy"}

def reload_clip(c, rl):
    hr, hl = GUN[c][0], GUN[c][1]
    return clip(rl, {
        "right_arm": [(0, hr), (rl * .2, A(-50, -30, 10)), (rl * .6, A(-55, -35, 10)), (rl * .85, A(-60, -20, 5)), (rl, hr)],
        "left_arm": [(0, hl), (rl * .2, A(-20, 10, 0)), (rl * .4, A(-30, -30, 20)), (rl * .6, A(-60, -20, 10)), (rl * .85, A(-70, 10, 0)), (rl, hl)],
        "torso": [(0, REST), (rl * .3, A(4, 8, 0)), (rl * .85, A(2, 4, 0)), (rl, REST)],
        "head": [(0, REST), (rl * .3, A(18, 10, 0)), (rl * .85, A(10, 5, 0)), (rl, REST)]})

def guns():
    out = {}
    for gid, rt in RELOAD.items():
        out[f"gun.{gid}.reload"] = reload_clip(OWN[gid], rt / 20)
    for c, (hr, hl, ar, al, rec, rl, _) in GUN.items():
        out[f"gun.{c}.hold"] = clip(1.0, {"right_arm": [(0, hr), (1, hr)], "left_arm": [(0, hl), (1, hl)]}, loop=True)
        out[f"gun.{c}.aim"] = clip(1.0, {"right_arm": [(0, ar), (1, ar)], "left_arm": [(0, al), (1, al)], "head": [(0, A(0, -6, 0)), (1, A(0, -6, 0))]}, loop=True)
        out[f"gun.{c}.sprint"] = clip(1.0, {"right_arm": [(0, A(-20, -10, 0)), (1, A(-20, -10, 0))], "left_arm": [(0, A(-25, 30, 0)), (1, A(-25, 30, 0))], "torso": [(0, A(8, 0, 0)), (1, A(8, 0, 0))]}, loop=True)
        out[f"gun.{c}.draw"] = clip(0.45, {"right_arm": [(0, A(10, 0, 30)), (0.25, A(-70, -10, 10)), (0.45, hr)], "left_arm": [(0, REST), (0.3, A(-40, 20, 0)), (0.45, hl)]})
        out[f"gun.{c}.fire"] = clip(0.25, {"right_arm": [(0, ar), (0.04, A(ar[0] + rec, ar[1], ar[2])), (0.25, ar)], "left_arm": [(0, al), (0.04, A(al[0] + rec * 0.6, al[1], al[2])), (0.25, al)], "torso": [(0, REST), (0.04, A(-rec * 0.4, 0, 0)), (0.25, REST)], "head": [(0, A(0, -6, 0)), (0.04, A(-rec * 0.5, -6, 0)), (0.25, A(0, -6, 0))]})
        out[f"gun.{c}.dry"] = clip(0.2, {"right_arm": [(0, ar), (0.05, A(ar[0] + 2, ar[1], ar[2])), (0.2, ar)]})
        # reload: lower, bring weapon across, hand to magazine, seat, rack, back to ready
        out[f"gun.{c}.reload"] = reload_clip(c, rl)
        out[f"gun.{c}.inspect"] = clip(2.2, {
            "right_arm": [(0, hr), (0.4, A(-70, -40, 20)), (1.1, A(-60, -60, 40)), (1.7, A(-70, -30, 10)), (2.2, hr)],
            "left_arm": [(0, hl), (0.4, A(-60, 0, 0)), (1.1, A(-50, -10, 0)), (2.2, hl)],
            "head": [(0, REST), (0.5, A(15, 15, 0)), (1.2, A(15, -10, 0)), (2.2, REST)]})
    return out
CASTING = {"cast.windup": MELEE["staff"]["cast_raise"], "cast.channel": MELEE["staff"]["channel"], "cast.release": MELEE["staff"]["release"],
           "cast.skill": clip(0.7, {"right_arm": [(0, REST), (0.3, A(-130, 0, 20)), (0.45, A(-60, 0, 0)), (0.7, REST)], "torso": [(0, REST), (0.3, A(-6, 0, 0)), (0.45, A(8, 0, 0)), (0.7, REST)]})}

# ---------------------------------------------------------------- item classification from the coverage csv
KIND = {"sword": "sword", "spear": "spear", "axe": "axe", "pickaxe": "pickaxe", "staff/wand": "staff", "shovel": "shovel", "shield": "shield",
        "hoe": "hoe", "dagger/knife": "dagger", "mace/hammer": "mace", "bow": "bow", "crossbow": "crossbow", "shears": "shears"}
GUNKEY = [("pistol", "pistol"), ("revolver", "pistol"), ("magnum", "pistol"), ("sidearm", "pistol"), ("smg", "smg"), ("submachine", "smg"),
          ("sniper", "sniper"), ("antimaterial", "sniper"), ("shotgun", "shotgun"), ("lmg", "lmg"), ("light_mg", "lmg"), ("minigun", "lmg"), ("machine", "lmg"),
          ("launcher", "launcher"), ("rocket", "launcher"), ("grenade", "launcher"), ("energy", "energy"), ("laser", "energy"), ("plasma", "energy")]
KOMRAG = {"dagger": "dagger", "sword": "sword", "rapier": "dagger", "greatsword": "sword", "axe": "axe", "battleaxe": "axe", "mace": "mace",
          "warhammer": "mace", "spear": "spear", "polearm": "spear", "staff": "staff", "bow": "bow", "longbow": "bow"}

def gun_class(thing):
    t = thing.lower()
    for k, c in GUNKEY:
        if k in t:
            return c
    return "rifle"

def main():
    os.makedirs(ASSET, exist_ok=True); os.makedirs(TAGS, exist_ok=True)
    groups = {"melee": {}, "tools": {}, "guns": guns(), "casting": CASTING}
    for c, cl in MELEE.items():
        groups["melee"].update({f"{c}.{n}": v for n, v in cl.items()})
    for c, cl in TOOLS.items():
        groups["tools"].update({f"{c}.{n}": v for n, v in cl.items()})
    for g, anims in groups.items():
        json.dump({"format_version": "1.8.0", "animations": anims}, open(os.path.join(ASSET, g + ".json"), "w"), indent=1)
    classes = {c: sorted(n.split(".", 1)[1] for n in a if n.startswith(c + ".")) for c in list(MELEE) + list(TOOLS) for a in [{**groups["melee"], **groups["tools"]}]}
    classes.update({f"gun.{c}": sorted(n.split(".", 2)[2] for n in groups["guns"] if n.startswith(f"gun.{c}.")) for c in GUN})
    classes["own_gun_reloads"] = sorted(RELOAD)
    json.dump({"classes": classes, "komrag": KOMRAG}, open(os.path.join(ROOT, "resources", "clips.json"), "w"), indent=1)
    tags = {}
    for r in csv.DictReader(open(CSV)):
        if r["owner"] != "2-melee-guns" or r["category"] not in ("melee", "gun") or not r["kind"].startswith("item"):
            continue
        m = re.match(r"item \((.+)\)", r["kind"])
        cls = KIND.get(m.group(1)) if m and r["category"] == "melee" else None
        if r["category"] == "gun":
            cls = "gun/" + gun_class(r["thing"])
        if cls:
            tags.setdefault(cls, set()).add(r["thing"])
    for k in (list(MELEE) + list(TOOLS)):
        tags.setdefault(k, set())
    for c in GUN:
        tags.setdefault("gun/" + c, set())
    # own guns by id (firearms module), and Komrag classes through its own tags
    for gid, c in OWN.items():
        tags["gun/" + c].add("diaithirfirearms:" + gid)
    for k, c in KOMRAG.items():
        tags[c].add("#diaithircombat:weapons/" + k)
    for k, v in tags.items():
        p = os.path.join(TAGS, k + ".json"); os.makedirs(os.path.dirname(p), exist_ok=True)
        vals = [{"id": x, "required": False} for x in sorted(v)]
        json.dump({"replace": False, "values": vals}, open(p, "w"), indent=0)
    print({g: len(a) for g, a in groups.items()}, {k: len(v) for k, v in tags.items()})

main()
