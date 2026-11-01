"""Ironstrider: a riveted two-legged industrial walker with hydraulic legs, a glass cockpit and twin autocannons."""
import math

import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "ironstrider"


def make():
    steel = mats.plate((104, 112, 122), panel=(5, 4), rust=0.14, grime=0.8, scratch=7)
    steel_d = mats.plate((66, 72, 80), panel=(4, 4), rust=0.2, grime=0.9, scratch=5)
    paint = mats.plate((196, 112, 38), panel=(5, 5), rust=0.12, grime=0.9, scratch=8, rustc=(110, 60, 30))
    chrome = mats.chrome()
    tread = mats.tread()
    glass = mats.glass(ramp_of(["#1a3040", "#2a5870", "#4a90ac", "#9cd8e8", "#e8fbff"]))
    GA = M.GLOW_AMBER
    ST = steel.rp

    def hazard_front(f):
        if f.name in ("north", "south"):
            M.hazard(f, f.h - 6, f.h - 2, ramp((200, 160, 36)), ramp((30, 30, 34)), step=4)

    def cab_front(f):
        if f.name == "north":
            M.vent(f, 6, f.h * 0.55, 26, f.h * 0.55 + 14, ST, steel_d.rp, slats=5, glow=GA)
        if f.name in ("east", "west"):
            M.vent(f, f.w / 2 - 10, f.h * 0.3, f.w / 2 + 10, f.h * 0.3 + 16, ST, steel_d.rp, slats=5, glow=GA)

    m = Model(NAME)
    # ---- cabin
    cab = m.bone("cabin", [0, 40, 0])
    cab.cube([-11, 40, -10], [22, 12, 22], steel, deco={"all": cab_front})
    cab.cube([-12, 38, -11], [24, 2.4, 24], paint, deco={"sides": hazard_front})
    cab.cube([-9, 52, -8], [18, 2.6, 18], steel_d)
    cab.cube([-10, 41, 11.0], [20, 9, 3.4], paint)                      # rear engine block
    cab.cube([-8.4, 43, 14.4], [6.0, 5.0, 1.0], steel_d, deco={"north": lambda f: None})
    for sx in (-1, 1):
        cab.cube([sx * 4.5 - 1.6, 46, 12.0], [3.2, 12.0, 3.2], chrome)          # exhaust stacks
        cab.cube([sx * 4.5 - 2.2, 57.5, 11.4], [4.4, 1.2, 4.4], steel_d)
        cab.cube([sx * 4.5 - 1.5, 58.5, 11.7], [3.0, 0.8, 3.0], mats.glow(GA, core=False))
    dome = m.bone("dome", [0, 52, -3], "cabin")
    dome.cube([-6.5, 52.4, -9.5], [13, 6, 9], glass, deco={"all": lambda f: None})
    dome.cube([-7.0, 52, -10.0], [14, 1.2, 10], steel_d)
    dome.cube([-1.0, 52.6, -9.8], [2.0, 5.8, 0.6], steel)
    dome.cube([-6.8, 53.4, -9.8], [0.8, 4.4, 0.6], steel)
    dome.cube([6.0, 53.4, -9.8], [0.8, 4.4, 0.6], steel)
    # head lamps and radio mast
    for sx in (-1, 1):
        cab.cube([sx * 8.5 - 1.5, 44.5, -10.8], [3.0, 3.0, 1.2], mats.glow(M.GLOW_AMBER))
    mast = m.bone("mast", [-8, 54, 6], "cabin")
    mast.cube([-8.5, 54, 5.5], [1.0, 14, 1.0], chrome)
    mast.cube([-9.0, 67.6, 5.0], [2.0, 1.2, 2.0], mats.glow(M.GLOW_RED))
    # autocannons on side pylons
    for s, sg in (("l", 1), ("r", -1)):
        b = m.bone("gun_" + s, [sg * 14, 44, -2], "cabin")
        b.cube([sg * 14 - 3, 40, -6], [6, 9, 10], steel_d)
        b.cube([sg * 14 - 1.9, 42, -18], [3.8, 3.8, 14], chrome, deco={"all": lambda f: None})
        b.cube([sg * 14 - 2.6, 41.4, -20], [5.2, 5.2, 3.6], steel_d)
        b.cube([sg * 14 - 1.0, 43, -20.4], [2.0, 2.0, 0.8], mats.glow(M.GLOW_ORANGE if hasattr(M, "GLOW_ORANGE") else GA))
        b.cube([sg * 14 - 1.4, 49, -4], [2.8, 3.0, 6], paint)
    # ---- legs
    for s, sg in (("l", 1), ("r", -1)):
        hip = m.bone("thigh_" + s, [sg * 9, 38, 0])
        hip.cube([sg * 9 - 4.5, 31, -4.5], [9, 8, 9], steel_d)              # hip joint block
        hip.cube([sg * 9 - 3.0, 22, -3.0], [6, 10, 6], paint)
        hip.cube([sg * 9 - 1.3 + sg * 3.6, 24, -1.0], [2.6, 12, 2.0], chrome)         # outer piston
        knee = m.bone("shin_" + s, [sg * 9, 22, 0], "thigh_" + s)
        knee.cube([sg * 9 - 4.0, 18, -4.0], [8, 7, 8], steel_d, deco={"all": lambda f: None})
        knee.cube([sg * 9 - 2.8, 5, -2.8], [5.6, 14, 5.6], steel)
        knee.cube([sg * 9 - 1.1 - sg * 3.4, 7, -1.0], [2.2, 11, 2.0], chrome)
        foot = m.bone("foot_" + s, [sg * 9, 5, 0], "shin_" + s)
        foot.cube([sg * 9 - 5.5, 0, -8.0], [11, 4, 16], tread)
        foot.cube([sg * 9 - 4.0, 3.6, -6.0], [8, 2.6, 12], steel_d)
        foot.cube([sg * 9 - 3.0, 4.0, -9.0], [6, 1.8, 2], paint)
    anims = {}
    idle = Anim(2.4)
    idle.bob("cabin", 0.35, 1).sway("dome", 0, 1.0, 1, 0.3).sway("mast", 0, 3, 2).sway("mast", 2, 2, 1, 0.2)
    anims["idle"] = idle
    walk = Anim(1.2)
    walk.bob("cabin", 1.0, 2, 0.25).sway("cabin", 2, 2.2).sway("cabin", 0, 1.0, 2, 0.1)
    walk.sway("thigh_l", 0, 24).sway("thigh_r", 0, -24)
    walk.keys("shin_l", 0, [(0, 0), (0.3, -6), (0.6, -38), (0.9, -8), (1.2, 0)])
    walk.keys("shin_r", 0, [(0, -38), (0.3, -8), (0.6, 0), (0.9, -6), (1.2, -38)])
    walk.sway("foot_l", 0, -10, 1, 0.3).sway("foot_r", 0, 10, 1, 0.3)
    walk.sway("mast", 0, 6, 2, 0.1).sway("gun_l", 0, 2, 2).sway("gun_r", 0, -2, 2)
    anims["walk"] = walk
    atk = Anim(0.6, loop=False)
    atk.keys("gun_l", 0, [(0, 0), (0.1, 6), (0.18, -4), (0.6, 0)]).keys("gun_r", 0, [(0, 0), (0.3, 0), (0.4, 6), (0.48, -4), (0.6, 0)])
    atk.keys("gun_l", 2, [(0, 0), (0.1, 2), (0.6, 0)], kind="position") if False else None
    atk.keys("cabin", 0, [(0, 0), (0.1, 3), (0.25, -2), (0.6, 0)])
    anims["attack"] = atk
    return m, anims
