"""Aurelith, Avatar of the Dawn: a tall sun god in cream linen and hammered gold, haloed, with a sun spear."""
import math

import mats
import motifs as M
from humanoid import build, god_face, god_anims
from model import Anim
from pix import ramp, ramp_of

NAME = "aurelith"


def make():
    skin = mats.skin((214, 166, 120))
    linen = mats.cloth((232, 224, 204), fold=3.0, amp=0.12)
    linen_d = mats.cloth((206, 196, 172), fold=3.0, amp=0.12)
    gold = mats.metal_hammered(M.GOLD)
    hair = mats.hair((222, 168, 60))
    leather = mats.cloth((120, 78, 44), fold=4, weave=False, amp=0.08)
    G = M.GOLD
    SK = skin.rp

    def head_front(f):
        god_face(f, SK, M.GLOW_AMBER, ramp((120, 70, 40)), mouth_rp=ramp((150, 70, 60)), eye_w=5, eye_h=3)

    def hem(f):
        M.twist(f, 2, f.h - 2, G, period=f.h * 1.6)

    def skirt_front(f):
        if f.name == "north":
            M.triskele(f, f.w / 2, f.h / 2 - 3, 9, wd=2.0, rp=G)
            M.ogham(f, f.w / 2, 2, f.h - 3, M.GLOW_AMBER, "lr c l rc", horizontal=False)

    def torso_front(f):
        if f.name == "north":
            f.paint(f.rect(0, 0, f.w, 3), G, 0.55, 0.8)
            M.twist(f, 0, 6, G)
            f.paint(f.ellipse(f.w / 2, 24, 11, 11), G, 0.5, 0.9)
            f.paint(f.ellipse(f.w / 2, 24, 8.6, 8.6), M.GLOW_AMBER, 0.3, -0.8, emit=True)
            M.triskele(f, f.w / 2, 24, 7, wd=1.6, rp=G, emit=None, hb=1.2)
        else:
            f.paint(f.rect(0, 0, f.w, 3), G, 0.55, 0.8)

    def band_deco(f):
        M.twist(f, 1, f.h - 1, G)

    o = dict(
        height=61, chest_w=15.5, chest_d=8.2, arm_w=4.8,
        pelvis=linen_d, waist=linen,
        chest=linen, chest_deco={"all": torso_front},
        skin=skin, head_deco={"north": head_front},
        uarm=skin, farm=skin, thigh=linen_d, shin=leather, boot=leather,
        uarm_deco={}, farm_deco={},
    )
    m, p = build(NAME, o)
    # gold boots trim
    m.by_name["foot_l"].cube([2.0, 3.6, -5.6], [5.3, 1.0, 8.0], gold, hide=("down",))
    m.by_name["foot_r"].cube([-7.3, 3.6, -5.6], [5.3, 1.0, 8.0], gold, hide=("down",))

    # belt with a sun buckle
    m.by_name["hip"].cube([-6.7, 30.2, -4.1], [13.4, 2.6, 8.2], gold, deco={"north": lambda f: (f.paint(f.rect(f.w / 2 - 5, 1, f.w / 2 + 5, f.h - 1), M.GLOW_AMBER, 0.4, -0.6, emit=True))})

    # linen robe skirt: four panels on the hip
    for nm, piv, (xo, zo, xs, zs) in (("skirt_f", [0, 29, -4.2], (-6.8, -5.0, 13.6, 1.8)), ("skirt_b", [0, 29, 4.2], (-6.8, 3.2, 13.6, 1.8))):
        b = m.bone(nm, piv, "hip")
        b.cube([xo, 17, zo], [xs, 12.8, zs], linen, deco={"north": skirt_front} if nm == "skirt_f" else {})
        b.cube([xo - 1.0, 5.0, zo - (0.7 if nm == "skirt_f" else -0.0)], [xs + 2.0, 12.2, zs + 0.7], linen, deco={})
        b.cube([xo - 1.2, 4.2, zo - (0.9 if nm == "skirt_f" else -0.0)], [xs + 2.4, 4.0, zs + 0.9], gold, deco={"sides": hem})
    for nm, sx in (("skirt_l", 1), ("skirt_r", -1)):
        b = m.bone(nm, [sx * 6.0, 29, 0], "hip")
        xo = 5.0 if sx > 0 else -6.8
        b.cube([xo, 17, -3.4], [1.8, 12.8, 6.8], linen)
        b.cube([xo - (1.0 if sx > 0 else 0.0), 5.0, -4.4], [2.8, 12.2, 8.8], linen)
        b.cube([xo - (1.2 if sx > 0 else 0.0), 4.2, -4.7], [3.0, 4.0, 9.4], gold, deco={"sides": hem})

    # golden torc, shoulder discs, armlets, bracers
    t = m.by_name["neck"]
    t.cube([-3.6, 49.4, -3.6], [7.2, 1.8, 7.2], gold)
    t.cube([2.2, 48.2, -4.6], [2.2, 2.2, 2.2], gold, rot=[0, 0, 0])
    t.cube([-4.4, 48.2, -4.6], [2.2, 2.2, 2.2], gold)
    pl = m.pair("pauldron", [p["sh"], 48, 0], "torso")
    pl.cube([p["sh"] - 3.6, 47.6, -3.8], [7.2, 2.4, 7.6], gold, deco={"up": lambda f: M.spiral(f, f.w / 2, f.h / 2, 11, 2, 1.6, G, hb=1.0)})
    ua2 = m.pair("armlet", [p["sh"], 42, 0], ("uarm_l", "uarm_r"))
    ua2.cube([p["sh"] - 2.9, 40.0, -2.9], [5.8, 2.6, 5.8], gold, deco={"sides": lambda f: M.twist(f, 1, f.h - 1, G, period=9)})
    br = m.pair("bracer", [p["sh"] + 0.4, 36.5, 0], ("farm_l", "farm_r"))
    br.cube([p["sh"] + 0.4 - 2.5, 27.0, -2.5], [5.0, 7.0, 5.0], gold, deco={"sides": lambda f: (M.ogham(f, f.w / 2, 2, f.h - 2, M.GLOW_AMBER, "lrcdl", horizontal=True, stem=True))})

    # hair: cap, fringe, long braid down the back
    h = m.bone("hair", [0, 58, 3.4], "head")
    h.cube([-4.5, 59.2, -4.5], [9.0, 2.2, 9.0], hair)
    h.cube([-4.5, 51.6, 3.4], [9.0, 8.4, 1.8], hair)
    h.cube([-4.6, 55.0, -4.2], [1.0, 5.2, 8.4], hair)
    h.cube([3.6, 55.0, -4.2], [1.0, 5.2, 8.4], hair)
    h.cube([-4.5, 58.0, -4.6], [9.0, 1.6, 1.2], hair)
    br_ = m.bone("braid", [0, 52, 4.6], "head")
    br_.cube([-2.4, 38.0, 3.6], [4.8, 14.4, 2.6], hair)
    br_.cube([-1.8, 31.0, 3.8], [3.6, 7.2, 2.2], hair)
    br_.cube([-2.2, 29.2, 3.6], [4.4, 2.2, 2.6], gold)

    # halo: a ring of gold behind the head with sun rays
    halo = m.bone("halo", [0, 60.0, 6.2], "torso")
    R = 10.5
    for k in range(8):
        a = math.radians(45 * k)
        cx, cy = R * math.cos(a), 60.0 + R * math.sin(a)
        ln = 2 * R * math.sin(math.radians(22.5)) + 1.4
        halo.cube([cx - ln / 2, cy - 1.2, 5.4], [ln, 2.4, 1.8], gold, rot=[0, 0, -(45 * k + 90)], pivot=[cx, cy, 6.2],
                  deco={"north": lambda f: f.paint(f.rect(2, f.h / 2 - 1, f.w - 2, f.h / 2 + 1), M.GLOW_AMBER, 0.5, -0.3, emit=True)})
    for k in range(8):
        a = math.radians(45 * k + 22.5)
        cx, cy = (R + 3.6) * math.cos(a), 60.0 + (R + 3.6) * math.sin(a)
        halo.cube([cx - 0.9, cy - 3.0, 5.6], [1.8, 6.0, 1.4], gold, rot=[0, 0, -(45 * k + 22.5 - 90)], pivot=[cx, cy, 6.3])
    # sun spear in the right hand
    sp = m.bone("spear", [-p["sh"] - 0.6, 24, 0], "hand_r")
    sp.cube([-p["sh"] - 1.6, 2, -0.9], [1.8, 60, 1.8], mats.bark((120, 88, 52)), hide=("down",))
    sp.cube([-p["sh"] - 2.4, 22.5, -1.5], [3.4, 1.4, 3.0], gold)
    sp.cube([-p["sh"] - 2.0, 62, -0.5], [2.6, 5.0, 1.0], gold)
    sp.cube([-p["sh"] - 1.4, 66, -0.5], [1.4, 3.0, 1.0], gold)
    sp.cube([-p["sh"] - 3.6, 59.5, -0.6], [5.6, 2.2, 1.2], gold)
    sp.cube([-p["sh"] - 2.6, 56.0, -0.9], [3.6, 3.4, 1.8], mats.glow(M.GLOW_AMBER), deco={})

    def ex_idle(a):
        a.rot("halo", 2, lambda t: 360 * t / 4.0 / 2)
        a.sway("braid", 0, 3, 1, 0.3).sway("hair", 0, 1.5, 1, 0.1)
        a.sway("skirt_f", 0, 2, 1, 0.2).sway("skirt_b", 0, -2, 1, 0.2)
    def ex_walk(a):
        a.rot("halo", 2, lambda t: 360 * t / 1.3 / 3)
        a.sway("skirt_f", 0, -22).sway("skirt_b", 0, 22).sway("braid", 0, 8, 2, 0.3)
        a.sway("skirt_l", 2, 2).sway("skirt_r", 2, -2)
    def ex_atk(a):
        L = a.length
        a.keys("braid", 0, [(0, 0), (L * .3, 30), (L * .6, -10), (L, 0)])
    anims = god_anims(o, extra_idle=ex_idle, extra_walk=ex_walk, extra_attack=ex_atk)
    return m, anims
