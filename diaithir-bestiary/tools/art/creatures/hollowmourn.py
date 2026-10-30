"""Hollowmourn: a tall hooded mourner in a torn black shroud, a pale weeping mask, impossibly long arms and a dim lantern."""
import math

import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "hollowmourn"


def make():
    shroud = mats.shroud((38, 36, 48), tone=0.5, rag=7)
    shroud_d = mats.shroud((26, 24, 34), tone=0.5, rag=9)
    cowl = mats.shroud((32, 30, 42), tone=0.5, hem=False)
    pale = mats.pale((214, 208, 198), veins=False)
    bone = mats.bone((202, 194, 176))
    GL = ramp_of(["#1a3028", "#3a7a62", "#9ad8bc", "#e0fff0"])
    dark = ramp((14, 12, 18))

    def mask_front(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            ex = w / 2 + sx * w * 0.24
            f.paint(f.ellipse(ex, h * 0.40, 3.6, 4.6), dark, 0.08, -1.2)
            f.paint(f.rect(ex - 0.5, h * 0.40 - 0.5, ex + 0.6, h * 0.40 + 0.6), GL, 0.7, -0.5, emit=True)
            for k in range(3):                                           # weeping streaks
                f.paint(f.rect(ex - 0.5 + (k - 1) * 0.0, h * 0.40 + 4 + k * 2, ex + 0.6, h * 0.40 + 10 + k * 4), dark, 0.18, -0.4)
        f.paint(f.rect(w / 2 - 0.6, h * 0.55, w / 2 + 0.6, h * 0.66), pale.rp, 0.4, -0.5)
        M.stitches(f, ((w * 0.30, h * 0.80), (w * 0.70, h * 0.80)), dark, step=2.4, size=2.0, cross=False)
        f.paint(f.rect(w * 0.30, h * 0.80 - 0.5, w * 0.70, h * 0.80 + 0.6), dark, 0.1, -0.5)

    m = Model(NAME)
    hip = m.bone("hip", [0, 18, 0])
    hip.cube([-3, 16, -2.2], [6, 4, 4.4], shroud_d)
    # bent legs
    for s, sg in (("l", 1), ("r", -1)):
        th = m.bone("thigh_" + s, [sg * 1.8, 17, 0], "hip")
        th.cube([sg * 1.8 - 1.1, 8, -1.1], [2.2, 9.5, 2.2], shroud_d)
        sh = m.bone("shin_" + s, [sg * 1.8, 8, 0], "thigh_" + s)
        sh.cube([sg * 1.8 - 1.0, 0.5, -1.0], [2.0, 8.0, 2.0], shroud_d)
        ft = m.bone("foot_" + s, [sg * 1.8, 1, 0], "shin_" + s)
        ft.cube([sg * 1.8 - 1.4, 0, -3.2], [2.8, 1.4, 4.6], shroud_d)
    # hunched torso, long shroud
    tor = m.bone("torso", [0, 20, 0], "hip", rot=[14, 0, 0])
    tor.cube([-4.2, 20, -2.6], [8.4, 11, 5.2], shroud)
    tor.cube([-3.4, 30.4, -2.2], [6.8, 3.0, 4.4], shroud_d)
    sk = m.bone("skirt", [0, 20, 0], "hip")
    sk.cube([-5.0, 3.0, -3.6], [10.0, 14.0, 7.2], shroud)
    sk.cube([-5.6, 1.0, -4.0], [11.2, 5.0, 8.0], shroud_d, hide=("up",))
    # long thin arms with three long fingers
    for s, sg in (("l", 1), ("r", -1)):
        ua = m.bone("uarm_" + s, [sg * 4.8, 31.5, 0], "torso", rot=[0, 0, sg * -4])
        ua.cube([sg * 4.8 - 1.1, 22.0, -1.1], [2.2, 10.0, 2.2], shroud)
        fa = m.bone("farm_" + s, [sg * 4.9, 22.0, 0], "uarm_" + s)
        fa.cube([sg * 4.9 - 0.9, 11.5, -0.9], [1.8, 11.0, 1.8], pale)
        hd = m.bone("hand_" + s, [sg * 4.9, 11.6, 0], "farm_" + s)
        hd.cube([sg * 4.9 - 1.3, 8.8, -0.9], [2.6, 3.2, 1.8], pale)
        for k, dx in enumerate((-0.9, 0.0, 0.9)):
            hd.cube([sg * 4.9 + dx - 0.3, 3.6 + k * 0.6, -0.5], [0.6, 5.4 - k * 0.6, 0.9], bone)
    # cowl and mask
    head = m.bone("head", [0, 31, -1.0], "torso")
    head.cube([-3.2, 31.4, -3.8], [6.4, 7.6, 1.0], pale, deco={"north": mask_front}, hide=("down",))
    head.cube([-4.0, 38.4, -4.2], [8.0, 2.6, 8.4], cowl)
    head.cube([-4.4, 31.0, 0.6], [8.8, 9.0, 4.0], cowl, hide=("down",))
    head.cube([-4.4, 31.0, -3.8], [1.4, 8.8, 4.4], cowl, hide=("down",))
    head.cube([3.0, 31.0, -3.8], [1.4, 8.8, 4.4], cowl, hide=("down",))
    head.cube([-3.6, 40.6, -3.4], [7.2, 1.2, 7.4], cowl)
    # lantern hanging from the left hand, with a dim pale flame
    ln = m.bone("lantern", [4.9, 9.0, -1.5], "hand_l")
    ln.cube([4.4, 2.8, -2.4], [1.0, 6.0, 0.8], shroud_d)
    ln.cube([3.2, -2.6, -3.4], [3.4, 5.2, 3.4], mats.glow(GL, core=True))
    ln.cube([2.8, -3.2, -3.8], [4.2, 0.8, 4.2], shroud_d)
    ln.cube([2.8, 2.6, -3.8], [4.2, 0.8, 4.2], shroud_d)
    anims = {}
    idle = Anim(6.0)
    idle.sway("head", 2, 9, 1).sway("head", 1, 12, 1, 0.5).bob("hip", 0.2, 2)
    idle.sway("uarm_l", 0, 3, 1, 0.2).sway("uarm_r", 0, -3, 1, 0.6).sway("farm_l", 0, 3, 2).sway("farm_r", 0, 3, 2, 0.5)
    idle.sway("lantern", 0, 5, 2, 0.1).sway("skirt", 0, 1.5, 1)
    anims["idle"] = idle
    walk = Anim(1.1)
    walk.bob("hip", 0.5, 2, 0.25).sway("thigh_l", 0, 26).sway("thigh_r", 0, -26)
    walk.keys("shin_l", 0, [(0, 0), (0.3, 8), (0.55, 40), (0.85, 8), (1.1, 0)])
    walk.keys("shin_r", 0, [(0, 40), (0.3, 8), (0.55, 0), (0.85, 8), (1.1, 40)])
    walk.sway("uarm_l", 0, -10).sway("uarm_r", 0, 10).sway("head", 2, 7, 2, 0.2).sway("torso", 2, 3, 1).sway("skirt", 0, 8, 2, 0.1)
    walk.sway("lantern", 0, 10, 2, 0.4)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    for s in ("l", "r"):
        atk.keys("uarm_" + s, 0, [(0, 0), (0.15, -105), (0.28, -85), (0.5, 0)])
        atk.keys("farm_" + s, 0, [(0, 0), (0.15, -10), (0.28, 12), (0.5, 0)])
    atk.keys("head", 0, [(0, 0), (0.15, -18), (0.5, 0)])
    atk.keys("torso", 0, [(0, 0), (0.15, 12), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
