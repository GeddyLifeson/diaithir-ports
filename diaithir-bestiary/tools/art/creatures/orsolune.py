"""Orsolune, Avatar of the Hushed Moon: a slim silver goddess in a midnight robe, crescent crown, long white hair, moon orb."""
import math

import mats
import motifs as M
import parts
from humanoid import build, god_face, god_anims
from model import Anim
from pix import ramp, ramp_of

NAME = "orsolune"


def make():
    skin = mats.skin((230, 236, 250), lo=0.42, hi=1.1, tone=0.86)
    _r = mats.cloth((46, 54, 104), fold=3.2, amp=0.13, lo=0.34, hi=1.5)
    _rl = mats.cloth((70, 80, 138), fold=3.2, amp=0.13, lo=0.34, hi=1.5)

    def starry(base):
        def fn(f):
            base(f)
            M.stars(f)
        fn.rp = base.rp
        return fn
    robe, robe_l = starry(_r), starry(_rl)
    silver = mats.metal_hammered(M.SILVER)
    hair = mats.hair((244, 246, 255), strand=2.8, lo=0.4, hi=1.05, tone=0.82)
    sleeve = mats.cloth((56, 66, 122), fold=2.4, amp=0.15, lo=0.34, hi=1.5)
    GS = M.GLOW_SILVER
    SV = M.SILVER

    def head_front(f):
        god_face(f, skin.rp, GS, ramp((60, 70, 110)), mouth_rp=ramp((130, 130, 170)), eye_w=5, eye_h=2, spacing=0.25, eye_y=0.42)

    def hem(f):
        M.twist(f, 2, f.h - 2, SV, period=f.h * 1.6)

    def skirt_front(f):
        if f.name == "north":
            M.triskele(f, f.w / 2, f.h / 2 - 4, 10, wd=1.6, emit=GS)
            M.ogham(f, f.w / 2 - 14, 4, f.h - 6, GS, "lr cl rc", horizontal=False)
            M.ogham(f, f.w / 2 + 14, 4, f.h - 6, GS, "cr lc rl", horizontal=False)

    def chest_deco(f):
        if f.name == "north":
            f.paint(f.rect(0, 0, f.w, 3), SV, 0.55, 0.8)
            f.paint(f.ellipse(f.w / 2, 22, 9, 9), SV, 0.5, 0.9)
            f.paint(f.ellipse(f.w / 2, 22, 7, 7), GS, 0.35, -0.8, emit=True)
            f.paint(f.ellipse(f.w / 2 + 2, 22, 5.6, 6.4), robe.rp, 0.2, 0.5, cut=False)
            f.paint(f.ellipse(f.w / 2 + 2.4, 22, 5.2, 6.4) & ~f.ellipse(f.w / 2 - 0.6, 22, 5, 6), GS, 0.7, -0.5, emit=True)

    o = dict(height=62, chest_w=13.2, chest_d=7.0, arm_w=3.8, leg_w=4.4,
             pelvis=robe, waist=robe_l, chest=robe_l, chest_deco={"all": chest_deco},
             skin=skin, head_deco={"north": head_front},
             uarm=sleeve, farm=sleeve, thigh=robe, shin=robe, boot=silver, hand=skin)
    m, p = build(NAME, o)
    sh = p["sh"]
    # sash
    m.by_name["hip"].cube([-6.4, 30.4, -3.9], [12.8, 2.4, 7.8], silver, deco={"north": lambda f: f.paint(f.rect(f.w / 2 - 3, 1, f.w / 2 + 3, f.h - 1), GS, 0.45, -0.5, emit=True)})
    # long flowing robe: four panels that flare to the floor
    for nm, piv, zo, zs in (("skirt_f", [0, 29, -3.8], -4.4, 1.8), ("skirt_b", [0, 29, 3.8], 2.6, 1.8)):
        b = m.bone(nm, piv, "hip")
        b.cube([-6.2, 16, zo], [12.4, 13.4, zs], robe, deco={"north": skirt_front} if nm == "skirt_f" else {})
        b.cube([-7.2, 4.2, zo - (0.6 if nm == "skirt_f" else 0)], [14.4, 12.0, zs + 0.6], robe_l)
        b.cube([-7.6, 2.6, zo - (0.9 if nm == "skirt_f" else 0)], [15.2, 3.6, zs + 0.9], silver, deco={"sides": hem})
    for nm, sx in (("skirt_l", 1), ("skirt_r", -1)):
        b = m.bone(nm, [sx * 5.4, 29, 0], "hip")
        xo = 4.6 if sx > 0 else -6.4
        b.cube([xo, 16, -3.0], [1.8, 13.4, 6.0], robe)
        b.cube([xo + (0.4 if sx > 0 else -1.4), 4.2, -3.8], [2.0 + 1.0, 12.0, 7.6], robe_l)
        b.cube([xo + (0.4 if sx > 0 else -1.8), 2.6, -4.2], [3.4, 3.6, 8.4], silver, deco={"sides": hem})
    # wide draped sleeves with silver cuffs
    for s in ("l", "r"):
        sg = 1 if s == "l" else -1
        fb = m.by_name["farm_" + s]
        fb.cube([sg * (sh + 0.4) - 3.0, 22.8, -3.0], [6.0, 4.0, 6.0], robe_l)
        fb.cube([sg * (sh + 0.4) - 3.4, 21.8, -3.4], [6.8, 2.2, 6.8], silver, deco={"sides": hem})
    # silver pauldron crescents
    pl = m.pair("pauldron", [sh, 48, 0], "torso")
    pl.cube([sh - 3.4, 47.8, -3.6], [6.8, 2.0, 7.2], silver, deco={"up": lambda f: M.spiral(f, f.w / 2, f.h / 2, 9, 2, 1.4, SV)})
    # torc
    nk = m.by_name["neck"]
    nk.cube([-3.4, 49.3, -3.4], [6.8, 1.6, 6.8], silver)
    # crescent crown standing over the brow
    cr = m.bone("crown", [0, 59, 0], "head")
    cres = parts.arc(0, 70.0, 9.5, 195, 345, 10)
    for i in range(len(cres) - 1):
        t = 1.2 + 2.2 * (1 - abs(i - 5) / 5)
        parts.seg(cr, cres[i], cres[i + 1], t, silver, z=-1.0, d=2.0, over=0.6)
    cr.cube([-3.2, 59.2, -4.6], [6.4, 1.8, 1.4], silver)
    cr.cube([-4.6, 59.6, -4.3], [1.4, 1.2, 0.8], silver)
    cr.cube([3.2, 59.6, -4.3], [1.4, 1.2, 0.8], silver)
    cr.cube([-1.0, 59.2, -4.7], [2.0, 2.0, 1.0], mats.glow(GS))
    cr.cube([-1.3, 62.0, -2.0], [2.6, 2.6, 2.0], mats.moon(GS))
    # hair: a long pale fall down the back, with tied lock
    hr = m.bone("hair", [0, 58, 3.4], "head")
    hr.cube([-4.5, 59.2, -4.5], [9.0, 2.2, 9.0], hair)
    hr.cube([-4.6, 55.0, -4.2], [1.0, 5.6, 8.4], hair)
    hr.cube([3.6, 55.0, -4.2], [1.0, 5.6, 8.4], hair)
    hr.cube([-4.4, 58.2, -4.6], [8.8, 1.8, 1.2], hair)
    hr.cube([-5.0, 36.0, 3.4], [10.0, 24.0, 2.0], hair)
    hr2 = m.bone("hair_low", [0, 38, 4.4], "hair")
    hr2.cube([-4.6, 20.0, 3.4], [9.2, 16.2, 2.0], hair)
    hr2.cube([-3.0, 14.0, 3.4], [6.0, 6.4, 1.6], hair)
    # floating moon orb with an orbiting ring
    orb = m.bone("orb", [-sh - 3.5, 40, -9.0], "torso")
    c = [-sh - 3.5, 40, -9.0]
    orb.cube([c[0] - 3.0, c[1] - 3.0, c[2] - 3.0], [6.0, 6.0, 6.0], mats.moon(GS))
    orb.cube([c[0] - 3.0, c[1] - 3.0, c[2] - 3.0], [6.0, 6.0, 6.0], mats.moon(GS), rot=[0, 45, 0], pivot=c)
    orb.cube([c[0] - 2.4, c[1] - 4.0, c[2] - 2.4], [4.8, 8.0, 4.8], mats.moon(GS), rot=[0, 22, 0], pivot=c)
    ring = m.bone("orbit", c, "orb")
    for k in range(8):
        a = math.radians(45 * k)
        rr = 6.4
        x, z = c[0] + rr * math.cos(a), c[2] + rr * math.sin(a)
        ring.cube([x - 1.0, c[1] - 0.7, z - 1.0], [2.0, 1.4, 2.0], mats.glow(GS, core=False))
    # moonlit mantle of stars at the back
    cp = m.bone("cape", [0, 49, 4.2], "torso")
    cp.cube([-7.0, 28, 3.8], [14.0, 21.0, 1.2], robe, deco={"north": lambda f: None, "south": lambda f: (M.triskele(f, f.w / 2, 24, 12, wd=1.6, emit=GS), M.ogham(f, f.w / 2, 50, f.h - 4, GS, "lrcdl"))})
    cp.cube([-6.4, 12, 3.8], [12.8, 16.5, 1.2], robe_l, hide=("up",))

    def ex_idle(a):
        a.bob("orb", 1.4, 1).sway("orb", 1, 12, 1)
        a.rot("orbit", 1, lambda t: 360 * t / a.length)
        a.sway("hair", 0, 2, 1, 0.2).sway("hair_low", 0, 3, 1, 0.5).sway("cape", 0, 3, 1, 0.2)
        a.sway("skirt_f", 0, 1.5, 1, 0.2).sway("skirt_b", 0, -1.5, 1, 0.2)
        a.bob("crown", 0.25, 2, 0.1)
    def ex_walk(a):
        a.bob("orb", 0.8, 2).rot("orbit", 1, lambda t: 360 * t / a.length)
        a.sway("skirt_f", 0, -12).sway("skirt_b", 0, 12).sway("hair_low", 0, 7, 2, 0.3).sway("cape", 0, 8, 2, 0.2)
    def ex_atk(a):
        L = a.length
        a.keys("orb", 0, [(0, 0), (L * .3, -20), (L * .6, 12), (L, 0)])
        a.keys("orb", 2, [(0, 0), (L * .3, 0), (L, 0)])
        a.keys("hair_low", 0, [(0, 0), (L * .3, 20), (L * .6, -14), (L, 0)])
    anims = god_anims(o, idle_len=4.0, extra_idle=ex_idle, extra_walk=ex_walk, extra_attack=ex_atk, stride=18, arm_swing=10)
    return m, anims
