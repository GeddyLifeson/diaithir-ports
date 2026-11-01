"""Tinkerspider Crawler: a small copper clockwork spider with a cyan sensor eye, a ticking gear, eight jointed legs and a trailing cable."""
import math

import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "tinkerspider"


def make():
    copper = mats.plate((186, 104, 60), panel=(4, 4), rust=0.0, scratch=6, lo=0.34, hi=1.55, grime=0.6)
    copper_d = mats.plate((130, 72, 46), panel=(3, 3), rust=0.0, scratch=4, lo=0.34, hi=1.5)
    steel = mats.plate((92, 100, 110), panel=(3, 3), rust=0.05, scratch=4)
    chrome = mats.chrome()
    dark = mats.plate((44, 48, 54), panel=(3, 3), rust=0.0, scratch=3)
    gearm = mats.gear((196, 140, 64))
    CY = M.GLOW_CYAN

    m = Model(NAME)
    body = m.bone("body", [0, 9, 0])
    body.cube([-5, 8, -5], [10, 5.5, 10], copper, deco={"up": lambda f: None})
    body.cube([-4, 13.5, -3.6], [8, 2.6, 8.4], copper_d)
    body.cube([-4.2, 7, 3.5], [8.4, 7, 6.8], copper, deco={"north": lambda f: None})   # abdomen
    body.cube([-3.0, 8, 10.0], [6.0, 5, 1.4], dark, deco={"south": lambda f: M.vent(f, 3, 3, f.w - 3, f.h - 3, steel.rp, dark.rp, slats=3, glow=M.GLOW_AMBER)})
    body.cube([-1.0, 12.4, 9.0], [2.0, 2.0, 3.0], steel)
    eye = m.bone("eye", [0, 11, -5], "body")
    eye.cube([-3.4, 9.2, -8.6], [6.8, 4.2, 3.8], dark)
    eye.cube([-2.4, 9.8, -9.2], [4.8, 3.0, 1.0], mats.glow(CY))
    for sx in (-1, 1):
        eye.cube([sx * 4.2 - 1.0, 12.6, -7.0], [2.0, 2.0, 2.0], mats.glow(CY, core=False))
    gear = m.bone("gear", [0, 16, 0], "body")
    gear.cube([-4.2, 16.0, -4.2], [8.4, 0.9, 8.4], gearm, rot=[0, 0, 0])
    gear.cube([-1.2, 16.9, -1.2], [2.4, 1.4, 2.4], steel)
    key = m.bone("key", [0, 12, 8], "body")                  # wind-up key on the back
    key.cube([-0.6, 11, 11], [1.2, 2.4, 1.0], steel)
    key.cube([-3.0, 12.6, 11.4], [6.0, 1.6, 0.8], copper)
    ant = m.bone("antenna", [3, 14, -2], "body")
    ant.cube([2.4, 14, -2.6], [0.9, 6, 0.9], chrome)
    ant.cube([2.0, 19.8, -3.0], [1.7, 1.7, 1.7], mats.glow(M.GLOW_RED))
    cable = m.bone("tail", [0, 9, 10], "body")
    cable.cube([-0.7, 6, 10.5], [1.4, 1.4, 6], dark)
    cable.cube([-0.7, 4.4, 15.5], [1.4, 1.4, 5], dark)
    cable.cube([-1.1, 3.8, 20], [2.2, 2.2, 1.6], mats.glow(M.GLOW_AMBER, core=False))
    # eight legs: four pairs, each two segments
    zs = (-5.0, -1.7, 1.7, 5.0)
    for i, z in enumerate(zs):
        for s, sg in (("l", 1), ("r", -1)):
            fan = (i - 1.5) * 12
            top = m.bone(f"leg{i}_{s}", [sg * 5, 11, z], "body")
            top.cube([sg * 5 + (0 if sg > 0 else -7.0), 10.2, z - 0.9], [7.0, 1.8, 1.8], steel, rot=[0, 0, sg * -14], pivot=[sg * 5, 11, z])
            kn = m.bone(f"knee{i}_{s}", [sg * 11.5, 11.6, z], f"leg{i}_{s}")
            kn.cube([sg * 11.5 - 1.1, 9.8, z - 1.1], [2.2, 2.2, 2.2], copper_d)
            low = m.bone(f"low{i}_{s}", [sg * 11.5, 10.5, z], f"knee{i}_{s}")
            low.cube([sg * 11.5 - 0.8 + sg * 0.0 + (0 if sg > 0 else 0), 0.4, z - 0.8], [1.6, 10.6, 1.6], steel, rot=[0, 0, sg * 8], pivot=[sg * 11.5, 10.5, z])
            low.cube([sg * 12.8 - 1.2, 0, z - 1.2], [2.4, 1.0, 2.4], dark)
    anims = {}
    idle = Anim(1.6)
    idle.bob("body", 0.25, 2).rot("gear", 1, lambda t: 360 * t / 1.6).sway("eye", 1, 14, 1, 0.1).sway("antenna", 2, 5, 3).sway("key", 1, 20, 2)
    idle.sway("tail", 1, 6, 1)
    anims["idle"] = idle
    walk = Anim(0.5)
    walk.bob("body", 0.5, 2).rot("gear", 1, lambda t: 360 * t / 0.5).sway("key", 1, 40, 2).sway("antenna", 2, 8, 2).sway("tail", 1, 12, 1)
    for i in range(4):
        for s, sg in (("l", 1), ("r", -1)):
            ph = 0.0 if (i + (0 if s == "l" else 1)) % 2 == 0 else 0.5
            walk.sway(f"leg{i}_{s}", 1, 22, 1, ph)
            walk.rot(f"knee{i}_{s}", 2, lambda t, ph=ph, sg=sg: sg * 14 * max(0.0, math.sin(2 * math.pi * (t / 0.5 + ph + 0.25))))
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("eye", 0, [(0, 0), (0.12, -26), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.1, -10), (0.2, 6), (0.5, 0)])
    for s, sg in (("l", 1), ("r", -1)):
        atk.keys(f"leg0_{s}", 2, [(0, 0), (0.1, sg * -30), (0.25, 0), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
