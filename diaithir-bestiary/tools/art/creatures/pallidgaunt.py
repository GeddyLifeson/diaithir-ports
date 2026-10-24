"""Pallidgaunt: a three-metre emaciated white figure with a blank face, a ribcage you can count, arms that reach the floor and a mouth full of teeth."""
import math

import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "pallidgaunt"


def make():
    pale = mats.pale((218, 214, 206), veins=False, tone=0.62)
    pale_d = mats.pale((196, 190, 184), veins=False, tone=0.55)
    bonem = mats.bone((214, 206, 188))
    rag = mats.shroud((56, 54, 60), tone=0.5, rag=6)
    dark = ramp((18, 14, 18))
    TEETH = ramp((236, 230, 214))

    def head_front(f):
        w, h = f.w, f.h
        for sx in (-1, 1):                                           # faint pits where eyes would be
            f.paint(f.ellipse(w / 2 + sx * w * 0.24, h * 0.30, 1.8, 1.2), pale.rp, 0.38, -0.5)
        f.paint(f.rect(w / 2 - 0.5, h * 0.40, w / 2 + 0.5, h * 0.52), pale.rp, 0.4, -0.3)
        M.teeth(f, 1.5, h * 0.62, w - 1.5, h * 0.62 + 7, TEETH, count=8, h=3, dark=dark)

    def ribs(f):
        if f.name == "north":
            f.paint(f.rect(f.w / 2 - 0.5, 0, f.w / 2 + 0.5, f.h * 0.8), pale.rp, 0.3, -0.5)      # sternum shadow
            for k in range(6):
                y = 3 + k * 3.4
                for sx in (-1, 1):
                    pts = [(f.w / 2 + sx * 1.2, y), (f.w / 2 + sx * (f.w * 0.28), y + 1.4), (f.w / 2 + sx * (f.w * 0.46), y + 0.2)]
                    f.paint(f.line(pts, 1.6, ss=1), bonem.rp, 0.74, 0.8)
                    f.paint(f.line([(x, yy + 1.4) for x, yy in pts], 1.0, ss=1), pale.rp, 0.28, -0.6)
        elif f.name == "south":
            for k in range(9):
                f.paint(f.rect(f.w / 2 - 0.5, 1 + k * 2, f.w / 2 + 0.5, 2 + k * 2), bonem.rp, 0.78, 0.8)

    m = Model(NAME)
    hip = m.bone("hip", [0, 26, 0])
    hip.cube([-3.0, 24, -2.0], [6, 4.5, 4], pale_d)
    hip.cube([-3.6, 22, -2.4], [7.2, 3, 4.8], rag)
    for s, sg in (("l", 1), ("r", -1)):
        th = m.bone("thigh_" + s, [sg * 1.8, 25, 0], "hip")
        th.cube([sg * 1.8 - 1.0, 12, -1.0], [2.0, 13.5, 2.0], pale_d)
        sh = m.bone("shin_" + s, [sg * 1.8, 12, 0], "thigh_" + s)
        sh.cube([sg * 1.8 - 0.9, 1.5, -0.9], [1.8, 11, 1.8], pale_d)
        ft = m.bone("foot_" + s, [sg * 1.8, 2, 0], "shin_" + s)
        ft.cube([sg * 1.8 - 1.2, 0, -3.6], [2.4, 1.6, 5.6], pale)
        for dx in (-0.7, 0.7):
            ft.cube([sg * 1.8 + dx - 0.3, 0, -6.2], [0.6, 1.0, 2.8], bonem)
    tor = m.bone("torso", [0, 27, 0], "hip", rot=[10, 0, 0])
    tor.cube([-2.6, 27, -1.8], [5.2, 4, 3.6], pale_d)                  # belly
    tor.cube([-3.6, 31, -2.4], [7.2, 10, 4.8], pale, deco={"all": ribs})
    tor.cube([-4.4, 39.4, -2.4], [8.8, 2.4, 4.8], pale_d)               # shoulders
    nk = m.bone("neck", [0, 41.5, 0], "torso", rot=[-6, 0, 0])
    nk.cube([-1.3, 41.5, -1.3], [2.6, 6.5, 2.6], pale_d)
    head = m.bone("head", [0, 47, 0], "neck", rot=[0, 0, 6])
    head.cube([-2.8, 46.8, -2.8], [5.6, 11, 5.6], pale, deco={"north": head_front})
    head.cube([-2.6, 56.6, -2.6], [5.2, 1.6, 5.2], pale_d)
    lank = mats.hair((26, 24, 30), tone=0.5, strand=1.6, contrast=0.25)
    for i, (x, ln) in enumerate(((-2.2, 16), (-0.9, 22), (0.5, 19), (1.9, 14))):       # lank black strands trailing from the skull
        head.cube([x - 0.45, 56.6 - ln, 2.8 + 0.3 * (i % 2)], [0.9, ln, 0.9], lank)
        head.cube([x - 0.45, 56.6, 2.8 + 0.3 * (i % 2)], [0.9, 1.0, 0.9], lank)
    for s, sg in (("l", 1), ("r", -1)):
        ua = m.bone("uarm_" + s, [sg * 5.2, 40, 0], "torso", rot=[0, 0, sg * -3])
        ua.cube([sg * 5.2 - 1.0, 25.5, -1.0], [2.0, 15, 2.0], pale_d)
        el = m.bone("farm_" + s, [sg * 5.2, 25.5, 0], "uarm_" + s)
        el.cube([sg * 5.2 - 0.9, 8.5, -0.9], [1.8, 17.5, 1.8], pale)
        el.cube([sg * 5.2 - 1.4, 24.6, -1.4], [2.8, 1.8, 2.8], bonem)
        hd = m.bone("hand_" + s, [sg * 5.2, 8.6, 0], "farm_" + s)
        hd.cube([sg * 5.2 - 1.2, 5.2, -0.9], [2.4, 3.6, 1.8], pale)
        for k, dx in enumerate((-0.9, -0.3, 0.3, 0.9)):
            hd.cube([sg * 5.2 + dx * 1.0 - 0.25, -1.4 + abs(k - 1.5) * 0.4, -0.4], [0.5, 6.8 - abs(k - 1.5) * 0.4, 0.8], bonem)
    anims = {}
    idle = Anim(6.0)
    idle.bob("hip", 0.15, 2).sway("head", 2, 10, 1, 0.1).sway("head", 1, 16, 1, 0.6).sway("neck", 0, 4, 2, 0.3)
    idle.sway("uarm_l", 0, 2, 1, 0.3).sway("uarm_r", 0, -2, 1, 0.8).sway("farm_l", 0, 3, 2, 0.1).sway("farm_r", 0, 3, 2, 0.6)
    idle.sway("torso", 2, 2, 1)
    anims["idle"] = idle
    walk = Anim(1.0)
    walk.bob("hip", 0.6, 2, 0.25).sway("thigh_l", 0, 30).sway("thigh_r", 0, -30)
    walk.keys("shin_l", 0, [(0, 0), (0.25, 8), (0.5, 46), (0.75, 8), (1.0, 0)])
    walk.keys("shin_r", 0, [(0, 46), (0.25, 8), (0.5, 0), (0.75, 8), (1.0, 46)])
    walk.sway("uarm_l", 0, -14).sway("uarm_r", 0, 14).sway("head", 2, 12, 1, 0.5).sway("torso", 1, 6).sway("farm_l", 0, 6, 2).sway("farm_r", 0, 6, 2)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    for s in ("l", "r"):
        atk.keys("uarm_" + s, 0, [(0, 0), (0.14, -95), (0.26, -70), (0.5, 0)])
    atk.keys("head", 0, [(0, 0), (0.14, 20), (0.5, 0)])
    atk.keys("torso", 0, [(0, 0), (0.14, 16), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
