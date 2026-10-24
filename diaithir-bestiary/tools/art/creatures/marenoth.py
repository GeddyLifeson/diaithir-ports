"""Marenoth, Avatar of the Deep Tide: a scaled sea god with a fin crest, coral crown, shell pauldrons, kelp cloak and a bronze trident."""
import math

import mats
import motifs as M
import parts
from humanoid import build, god_face, god_anims
from model import Anim
from pix import ramp, ramp_of

NAME = "marenoth"


def make():
    scale = mats.scales((56, 138, 160), size=5.0, lo=0.34, hi=1.5)
    scale_d = mats.scales((34, 92, 126), size=5.0, lo=0.34, hi=1.5)
    scale_b = mats.scales((46, 112, 138), size=5.0, lo=0.34, hi=1.5)
    bronze = mats.metal_hammered(M.BRONZE)
    kelp = mats.foliage((40, 112, 84), size=7, lo=0.34, hi=1.5)
    kelp_d = mats.foliage((28, 84, 70), size=7, lo=0.34, hi=1.5)
    finm = mats.fin((70, 190, 190))
    finm_d = mats.fin((40, 130, 160))
    pearl = mats.stone((226, 214, 206), crack=0.0, cell=30, lo=0.5, hi=1.1, tone=0.7)
    coral = mats.stone((228, 108, 92), crack=0.0, cell=9, lo=0.4, hi=1.4, tone=0.6)
    BR = M.BRONZE
    CY = M.GLOW_CYAN

    def head_front(f):
        god_face(f, scale.rp, CY, ramp((16, 50, 70)), mouth_rp=ramp((14, 40, 60)), eye_w=5, eye_h=3, eye_y=0.38, spacing=0.28)
        for sgn in (-1, 1):                     # cheek gills
            for k in range(3):
                x = f.w / 2 + sgn * (f.w * 0.40) - 1
                f.paint(f.rect(x - 0.5, f.h * 0.52 + k * 3, x + 1.5, f.h * 0.52 + k * 3 + 1), ramp((10, 40, 60)), 0.2, -0.6)

    def chest_deco(f):
        if f.name == "north":
            f.paint(f.rect(0, 0, f.w, 3), BR, 0.55, 0.8)
            # wave knotwork collar
            M.twist(f, 3, 11, BR, period=14)
            M.triskele(f, f.w / 2, 26, 10, wd=1.6, emit=CY)
            M.stud(f, 6, 6, 2.2, M.ENAMEL_BLUE)
            M.stud(f, f.w - 6, 6, 2.2, M.ENAMEL_BLUE)

    def belt(f):
        M.ring_chain(f, 2, f.h - 2, BR)

    o = dict(height=62, chest_w=16.0, chest_d=8.4, arm_w=4.8, leg_w=5.0,
             pelvis=scale_d, waist=scale_b, chest=scale, chest_deco={"all": chest_deco},
             skin=scale, head_deco={"north": head_front},
             uarm=scale, farm=scale_b, thigh=scale_d, shin=scale_d, boot=finm_d, hand=scale)
    m, p = build(NAME, o)
    sh = p["sh"]
    hip = m.by_name["hip"]
    hip.cube([-7.0, 30.2, -4.2], [14.0, 3.2, 8.4], bronze, deco={"sides": belt})
    # tide-cloth kilt of kelp
    for nm, piv, zo, zs in (("kilt_f", [0, 29, -4.4], -5.2, 1.6), ("kilt_b", [0, 29, 4.4], 3.6, 1.6)):
        b = m.bone(nm, piv, "hip")
        b.cube([-6.6, 14.5, zo], [13.2, 15.0, zs], kelp)
        b.cube([-6.0, 9.0, zo - (0.3 if nm == "kilt_f" else -0.3)], [12.0, 6.0, zs + 0.3], kelp_d)
    for nm, sx in (("kilt_l", 1), ("kilt_r", -1)):
        b = m.bone(nm, [sx * 6.0, 29, 0], "hip")
        b.cube([5.0 if sx > 0 else -6.6, 14.5, -3.8], [1.6, 15.0, 7.6], kelp_d)
    # shell pauldrons with nautilus spirals
    pl = m.pair("shell", [sh, 48, 0], "torso")
    pl.cube([sh - 4.2, 47.6, -4.6], [8.4, 3.4, 9.2], pearl, deco={"up": lambda f: M.spiral(f, f.w / 2, f.h / 2, 13, 2.4, 1.8, BR), "sides": lambda f: M.band(f, f.h - 4, f.h, BR)})
    pl.cube([sh - 3.2, 50.8, -3.4], [6.4, 2.2, 6.8], pearl)
    pl.cube([sh - 2.0, 53.0, -2.2], [4.0, 1.6, 4.4], pearl)
    # fin crest, ear fins, kelp hair
    cr = m.bone("crest", [0, 59, 1.0], "head")
    for k, (zz, hh) in enumerate(((-3.0, 4.5), (-1.0, 7.5), (1.0, 8.5), (3.0, 6.5), (4.6, 4.0))):
        cr.cube([-0.6, 60.0, zz - 0.9], [1.2, hh, 1.8], finm, deco={"all": lambda f: None})
    ef = m.pair("earfin", [4.0, 55.5, 0], "head")
    ef.cube([4.0, 54.0, -2.4], [5.6, 6.4, 0.9], finm, rot=[0, -20, 12], pivot=[4.0, 57.0, 0])
    ef.cube([4.2, 51.4, -1.6], [4.0, 3.6, 0.8], finm_d, rot=[0, -20, 24], pivot=[4.0, 54.0, 0])
    kh = m.bone("hair", [0, 58, 3.6], "head")
    kh.cube([-4.5, 59.2, -4.5], [9.0, 2.0, 9.0], kelp)
    kh.cube([-4.6, 54.5, -4.2], [1.0, 5.0, 8.4], kelp)
    kh.cube([3.6, 54.5, -4.2], [1.0, 5.0, 8.4], kelp)
    kh.cube([-4.4, 58.0, -4.6], [8.8, 1.6, 1.2], kelp)
    kh.cube([-5.0, 34.0, 3.4], [10.0, 26.0, 1.8], kelp_d)
    k2 = m.bone("hair_low", [0, 36, 4.2], "hair")
    k2.cube([-4.6, 16.0, 3.4], [9.2, 20.0, 1.8], kelp, hide=("up",))
    # coral crown with pearls
    cc = m.bone("crown", [0, 60, 0], "head")
    cc.cube([-4.6, 59.4, -4.6], [9.2, 1.8, 9.2], bronze)
    for k, (x, hh, rz) in enumerate(((-3.2, 5.0, 12), (-1.0, 7.0, 4), (1.2, 6.0, -4), (3.2, 4.6, -12), (0, 3.0, 0))):
        cc.cube([x - 0.7, 61.0, -3.4 + (k % 2) * 6.0 - 0.7], [1.4, hh, 1.4], coral, rot=[0, 0, rz], pivot=[x, 61, -3.4])
    cc.cube([-0.9, 59.8, -5.0], [1.8, 1.8, 1.0], mats.glow(CY))
    # gills on the neck and webbed flippers at the ankles
    for s, x0 in (("l", 1.0), ("r", -8.0)):
        sg = 1 if s == "l" else -1
        pass
    for s in ("l", "r"):
        sg = 1 if s == "l" else -1
        fb = m.by_name["shin_" + s]
        fb.cube([sg * 4.0 - 0.4, 8.0, -0.4 + 2.2], [0.8, 6.0, 3.0], finm, rot=[0, 0, sg * 10], pivot=[sg * 4.0, 10, 3.0])
    # trident
    tr = m.bone("trident", [-sh - 0.6, 24, 0], "hand_r")
    tr.cube([-sh - 1.6, 4, -0.9], [1.8, 50, 1.8], mats.bark((92, 70, 50)), hide=("down",))
    tr.cube([-sh - 2.4, 52, -1.3], [3.4, 3.0, 2.6], bronze)
    tr.cube([-sh - 1.3, 55, -0.6], [1.2, 12, 1.2], bronze)
    tr.cube([-sh - 6.2, 55, -0.6], [1.2, 9, 1.2], bronze)
    tr.cube([-sh + 3.0, 55, -0.6], [1.2, 9, 1.2], bronze)
    tr.cube([-sh - 6.2, 54, -0.7], [10.4, 1.4, 1.4], bronze)
    tr.cube([-sh - 1.3, 67, -0.6], [1.2, 2.0, 1.2], mats.glow(CY))
    tr.cube([-sh - 6.2, 64, -0.6], [1.2, 2.0, 1.2], mats.glow(CY))
    tr.cube([-sh + 3.0, 64, -0.6], [1.2, 2.0, 1.2], mats.glow(CY))
    # kelp cloak
    cl = m.bone("cloak", [0, 49, 4.8], "torso")
    cl.cube([-8.6, 26, 4.5], [17.2, 23, 1.4], kelp)
    cl.cube([-7.4, 10, 4.5], [14.8, 16.4, 1.4], kelp_d, hide=("up",))

    def ex_idle(a):
        a.sway("crest", 0, 3, 1, 0.1).sway("hair", 0, 2, 1, 0.3).sway("hair_low", 0, 4, 1, 0.6).sway("cloak", 0, 3, 1, 0.2)
        a.sway("earfin_l", 2, 6, 2, 0).sway("earfin_r", 2, -6, 2, 0)
        a.sway("kilt_f", 0, 2, 1).sway("kilt_b", 0, -2, 1)
    def ex_walk(a):
        a.sway("kilt_f", 0, -14).sway("kilt_b", 0, 14).sway("cloak", 0, 10, 2, 0.2).sway("hair_low", 0, 8, 2, 0.1)
        a.sway("earfin_l", 2, 10, 2).sway("earfin_r", 2, -10, 2)
    def ex_atk(a):
        L = a.length
        a.keys("trident", 0, [(0, 0), (L * .3, 24), (L * .55, -60), (L, 0)])
        a.keys("cloak", 0, [(0, 0), (L * .3, 16), (L * .6, -12), (L, 0)])
    anims = god_anims(o, extra_idle=ex_idle, extra_walk=ex_walk, extra_attack=ex_atk, stride=23)
    return m, anims
