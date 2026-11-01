"""Verdanai, Avatar of the Wild Green: an antlered forest god, bark and moss, leaf cloak, gold torc, oak staff."""
import math

import numpy as np

import mats
import motifs as M
import parts
from humanoid import build, god_face, god_anims
from pix import ramp, ramp_of

NAME = "verdanai"


def make():
    skin = mats.skin((112, 140, 84))
    barkm = mats.bark((92, 66, 42))
    barkd = mats.bark((70, 50, 34))
    mossy = mats.stone((96, 112, 70), moss=0.7, crack=0.0)
    beardm = mats.hair((86, 124, 60), strand=2.6)
    fur = mats.fur((112, 82, 54), clump=2.5, contrast=0.16, steps=3)
    gold = mats.metal_hammered(M.GOLD)
    leaf = mats.foliage((70, 126, 56), size=9)
    leaf_d = mats.foliage((48, 96, 44), size=9)
    hoof = mats.stone((48, 40, 36), crack=0.0, cell=8)
    G = M.GOLD
    SK = skin.rp
    GG = M.GLOW_GREEN

    def head_front(f):
        god_face(f, SK, GG, ramp((46, 60, 36)), mouth_rp=ramp((40, 56, 30)), eye_w=5, eye_h=3, beard=ramp((86, 124, 60)))

    def waist_front(f):
        if f.name == "north":
            f.paint(f.rect(f.w / 2 - 4, f.h * 0.3, f.w / 2 + 4, f.h * 0.3 + 8), G, 0.55, 1.0)
            f.paint(f.rect(f.w / 2 - 2.5, f.h * 0.3 + 1.5, f.w / 2 + 2.5, f.h * 0.3 + 6.5), GG, 0.5, -0.5, emit=True)

    def chest_deco(f):
        if f.name in ("north",):
            M.ogham(f, f.w / 2, 4, f.h - 4, GG, "lrc ldr cl", horizontal=False, spacing=3.4)
            M.stud(f, 7, 7, 2.4, M.ENAMEL_BLUE)
            M.stud(f, f.w - 7, 7, 2.4, M.ENAMEL_BLUE)

    def vine_band(f):
        M.twist(f, 1, f.h - 1, ramp((70, 120, 50)), period=10, wd=3)

    o = dict(height=62, chest_w=17.0, chest_d=9.0, arm_w=5.0, leg_w=5.4,
             pelvis=fur, waist=barkd, waist_deco={"all": waist_front},
             chest=mossy, chest_deco={"all": chest_deco},
             skin=skin, head_deco={"north": head_front},
             uarm=skin, farm=barkm, thigh=fur, shin=fur, boot=hoof, hand=skin)
    m, p = build(NAME, o)
    sh = p["sh"]

    # kilt of ferns and leaves, front, back, sides
    for nm, piv, xo, zo, xs, zs in (("kilt_f", [0, 29, -4.4], -7.6, -5.4, 15.2, 2.0), ("kilt_b", [0, 29, 4.4], -7.6, 3.4, 15.2, 2.0)):
        b = m.bone(nm, piv, "hip")
        b.cube([xo, 14, zo], [xs, 15.5, zs], leaf)
        b.cube([xo + 1.0, 9, zo - (0.3 if nm == "kilt_f" else -0.3)], [xs - 2.0, 6, zs + 0.3], leaf_d)
    for nm, sx in (("kilt_l", 1), ("kilt_r", -1)):
        b = m.bone(nm, [sx * 6.4, 29, 0], "hip")
        b.cube([5.6 if sx > 0 else -7.6, 14, -3.6], [2.0, 15.5, 7.2], leaf_d)

    # belt of braided vine with a gold buckle
    m.by_name["hip"].cube([-7.0, 29.6, -4.2], [14.0, 2.6, 8.4], mats.cloth((60, 98, 44), fold=2, amp=0.1), deco={"sides": vine_band})

    # torc and necklace
    nk = m.by_name["neck"]
    nk.cube([-3.8, 49.2, -3.8], [7.6, 1.8, 7.6], gold)
    for sx in (1, -1):
        nk.cube([sx * 2.4 - 1.1, 48.0, -4.9], [2.2, 2.2, 2.2], gold)

    # mossy shoulder mantle with vines hanging, and leaf cloak on the back
    pl = m.pair("mantle", [sh, 48, 0], "torso")
    pl.cube([sh - 4.0, 47.4, -4.4], [8.0, 3.6, 8.8], mossy)
    pl.cube([sh - 3.0, 50.8, -3.0], [6.0, 1.2, 6.0], mossy)
    cl = m.bone("cloak", [0, 49, 4.6], "torso")
    cl.cube([-8.4, 30, 4.4], [16.8, 19, 1.4], leaf)
    cl.cube([-7.2, 18, 4.4], [14.4, 12.4, 1.4], leaf_d, hide=("up",))
    cl.cube([-5.6, 9, 4.4], [11.2, 9.4, 1.4], leaf, hide=("up",))

    # bracers of wood, vine wraps
    br = m.pair("wrap", [sh, 42, 0], ("uarm_l", "uarm_r"))
    br.cube([sh - 3.0, 40, -3.0], [6.0, 2.2, 6.0], mats.cloth((70, 118, 50), fold=2, amp=0.1), deco={"sides": vine_band})
    bc = m.pair("bracer", [sh + 0.4, 36.5, 0], ("farm_l", "farm_r"))
    bc.cube([sh + 0.4 - 2.8, 28.5, -2.8], [5.6, 5.0, 5.6], gold, deco={"sides": lambda f: M.ogham(f, f.w / 2, 1, f.h - 1, GG, "lrc", horizontal=True)})

    # hooves with a gold band
    for s, x0 in (("l", 1.4), ("r", -6.4)):
        pass

    # beard of moss over chin, side whiskers
    hd = m.bone("beard", [0, 52, -3.6], "head")
    hd.cube([-3.6, 45.4, -4.7], [7.2, 7.0, 1.4], beardm, hide=("up",))
    hd.cube([-2.6, 41.6, -4.5], [5.2, 4.2, 1.2], beardm, hide=("up",))
    hd.cube([-1.4, 38.6, -4.3], [2.8, 3.4, 1.0], beardm, hide=("up",))
    hd.cube([-4.5, 51.8, -3.0], [1.0, 5.2, 6.4], beardm)
    hd.cube([3.5, 51.8, -3.0], [1.0, 5.2, 6.4], beardm)
    # hair: wild moss-green mane
    hr = m.bone("hair", [0, 58, 3.4], "head")
    hr.cube([-4.5, 59.2, -4.5], [9.0, 2.4, 9.0], beardm)
    hr.cube([-4.5, 49.0, 3.4], [9.0, 11.2, 2.0], beardm)
    hr.cube([-4.6, 56.0, -4.2], [1.0, 4.4, 8.4], beardm)
    hr.cube([3.6, 56.0, -4.2], [1.0, 4.4, 8.4], beardm)

    # great antlers
    an = m.pair("antler", [3.0, 61, 0], "head")
    main = [(2.6, 60.6), (4.4, 66.0), (7.6, 70.6), (11.4, 73.4), (14.8, 74.4)]
    tines = [[(4.4, 66.0), (3.6, 71.4), (3.8, 75.2)], [(7.6, 70.6), (7.6, 76.2), (8.4, 79.6)], [(11.4, 73.4), (12.0, 78.4), (12.6, 81.2)],
             [(7.6, 70.6), (10.4, 67.4), (12.8, 66.4)]]
    parts.antler(an, barkm, main, tines, t0=2.4, t1=1.3, d=2.2, tt=1.4)
    # brow tines
    an.cube([2.6, 59.6, -4.6], [1.2, 1.2, 3.4], barkm)

    # oak staff in the left hand topped by a root cage around a green flame
    st = m.bone("staff", [sh + 0.6, 24, 0], "hand_l")
    st.cube([sh - 0.4, 2, -0.9], [1.8, 52, 1.8], barkm, hide=("down",))
    st.cube([sh - 1.4, 52, -1.9], [3.8, 4.0, 3.8], barkd)
    for dx, dz, rz in ((1.7, 0.0, 14), (-1.7, 0.0, -14), (0, 1.7, 0), (0, -1.7, 0)):
        st.cube([sh + 0.5 - 0.6 + dx, 55, dz - 0.6], [1.2, 7.0, 1.2], barkm, rot=[(-14 if dz else 0) * np.sign(dz) if dz else 0, 0, rz], pivot=[sh + 0.5 + dx, 55, dz])
    st.cube([sh - 0.6, 56.5, -1.5], [3.0, 4.0, 3.0], mats.glow(GG))

    def ex_idle(a):
        a.sway("antler_l", 2, 1.2, 1, 0.1).sway("antler_r", 2, -1.2, 1, 0.1)
        a.sway("cloak", 0, 2.5, 1, 0.2).sway("beard", 0, 3, 1, 0.5)
        a.sway("kilt_f", 0, 2, 1, 0.1).sway("kilt_b", 0, -2, 1, 0.1)
    def ex_walk(a):
        a.sway("kilt_f", 0, -14).sway("kilt_b", 0, 14).sway("cloak", 0, 8, 2, 0.25).sway("beard", 0, 6, 2, 0.1)
        a.sway("antler_l", 2, 1.5, 2).sway("antler_r", 2, -1.5, 2)
    def ex_atk(a):
        L = a.length
        a.keys("antler_l", 0, [(0, 0), (L * .3, -14), (L * .55, 12), (L, 0)]).keys("antler_r", 0, [(0, 0), (L * .3, -14), (L * .55, 12), (L, 0)])
        a.keys("cloak", 0, [(0, 0), (L * .3, 14), (L * .6, -10), (L, 0)])
    anims = god_anims(o, extra_idle=ex_idle, extra_walk=ex_walk, extra_attack=ex_atk, stride=22, arm_swing=12)
    return m, anims
