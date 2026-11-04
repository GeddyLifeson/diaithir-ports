"""Ashenvar, Avatar of the Ember Hour: a broad smith-god of cracked basalt, bronze plate, horns and a flame crest, with a forge hammer."""
import math

import mats
import motifs as M
import parts
from humanoid import build, god_face, god_anims
from model import Anim
from pix import ramp, ramp_of

NAME = "ashenvar"


def make():
    basalt = mats.charred((50, 40, 40), cracks=0.55, cell=18)
    basalt_s = mats.charred((58, 46, 44), cracks=0.35, cell=22)
    bronze = mats.metal_hammered(M.BRONZE)
    plate = mats.plate((150, 92, 44), panel=(5, 5), rust=0.0, lo=0.34, hi=1.5, grime=0.9, scratch=8)
    leather = mats.leather((72, 52, 44))
    horn = mats.horn((54, 44, 42), ring=3.0, lo=0.3, hi=1.7)
    EM = M.GLOW_AMBER
    BR = M.BRONZE

    def head_front(f):
        god_face(f, basalt.rp, EM, ramp((20, 14, 14)), mouth_rp=EM, eye_w=6, eye_h=3, brow_thick=2, eye_y=0.40, spacing=0.25)
        f.paint(f.rect(f.w / 2 - 5, f.h * 0.78, f.w / 2 + 5, f.h * 0.78 + 2), EM, 0.6, -0.6, emit=True)

    def chest_deco(f):
        if f.name == "north":
            f.paint(f.ellipse(f.w / 2, f.h / 2, 13, 13), BR, 0.5, 1.2)
            f.paint(f.ellipse(f.w / 2, f.h / 2, 10.5, 10.5), BR, 0.18, -0.8)
            M.vent(f, f.w / 2 - 8, f.h / 2 - 8, f.w / 2 + 8, f.h / 2 + 8, BR, BR, slats=6, glow=EM)
            for k in range(8):
                a = k * math.pi / 4
                M.stud(f, f.w / 2 + 12.5 * math.cos(a), f.h / 2 + 12.5 * math.sin(a), 1.6, M.ENAMEL_RED)

    def apron(f):
        if f.name == "north":
            M.twist(f, 2, 14, BR, period=24)
            M.spiral(f, f.w / 2, f.h / 2 + 4, 12, 2.2, 1.8, BR)
            for x in (4, f.w - 5):
                M.rivet(f, x, f.h - 4, BR)

    def bracer(f):
        M.twist(f, 1, f.h - 1, BR, period=11, wd=4)

    o = dict(height=63, chest_w=20.0, chest_d=10.5, arm_w=6.4, leg_w=6.4, arm_out=-6,
             pelvis=leather, waist=basalt_s, chest=basalt, chest_deco={"all": chest_deco},
             skin=basalt, head=(9.0, 9.0, 9.0), head_deco={"north": head_front},
             uarm=basalt, farm=basalt_s, thigh=basalt_s, shin=basalt, boot=plate, hand=basalt)
    m, p = build(NAME, o)
    sh = p["sh"]
    hip = m.by_name["hip"]
    # belt, hanging forge apron, knotwork hem
    hip.cube([-7.4, 30.0, -4.6], [14.8, 3.0, 9.2], bronze, deco={"north": lambda f: M.stud(f, f.w / 2, f.h / 2, 2.2, M.ENAMEL_RED)})
    for nm, piv, zo, zs in (("apron_f", [0, 29, -4.8], -5.6, 1.4), ("apron_b", [0, 29, 4.8], 4.2, 1.4)):
        b = m.bone(nm, piv, "hip")
        b.cube([-5.4, 11, zo], [10.8, 18.6, zs], leather, deco={"north": apron} if nm == "apron_f" else {})
        b.cube([-5.6, 9.4, zo - 0.1], [11.2, 2.0, zs + 0.2], bronze)
    # shoulder plate: huge bronze pauldrons with spikes and studs
    pl = m.pair("pauldron", [sh, 48.5, 0], "torso")
    pl.cube([sh - 5.0, 48.0, -5.4], [10.0, 3.4, 10.8], bronze, deco={"up": lambda f: M.spiral(f, f.w / 2, f.h / 2, 14, 2.5, 1.8, BR)})
    pl.cube([sh - 4.4, 51.2, -4.2], [8.8, 2.4, 8.4], plate)
    for dx, dz in ((-2.4, -2.4), (2.4, -2.4), (-2.4, 2.4), (2.4, 2.4), (0, 0)):
        pl.cube([sh + dx - 0.9, 53.4, dz - 0.9], [1.8, 3.4, 1.8], horn)
    cap = m.pair("cap", [sh, 40, 0], ("uarm_l", "uarm_r"))
    cap.cube([sh - 3.9, 39.2, -3.9], [7.8, 3.0, 7.8], bronze, deco={"sides": bracer})
    br = m.pair("bracer", [sh + 0.4, 36.5, 0], ("farm_l", "farm_r"))
    br.cube([sh + 0.4 - 3.8, 27.4, -3.8], [7.6, 7.4, 7.6], bronze, deco={"sides": lambda f: (M.stud(f, 4, f.h / 2, 1.6, M.ENAMEL_RED), M.stud(f, f.w - 4, f.h / 2, 1.6, M.ENAMEL_RED))})
    # torc and gorget
    nk = m.by_name["neck"]
    nk.cube([-4.4, 48.8, -4.4], [8.8, 2.4, 8.8], bronze)
    # horns
    hn = m.pair("horn", [4.0, 58.0, 0], "head")
    pts = [(3.8, 57.0), (7.4, 58.6), (10.6, 62.0), (11.8, 66.4), (10.6, 70.0)]
    for i in range(len(pts) - 1):
        t = 3.4 - 2.0 * i / (len(pts) - 2)
        a, b = pts[i], pts[i + 1]
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        L = math.hypot(b[0] - a[0], b[1] - a[1]) + 0.6
        hn.cube([mx - t / 2, my - L / 2, -t / 2], [t, L, t], horn, rot=[0, 0, math.degrees(math.atan2(b[0] - a[0], b[1] - a[1]))], pivot=[mx, my, 0])
    # flame crest
    fc = m.bone("crest", [0, 60, 0], "head")
    flame = mats.flame(M.GLOW_AMBER)
    flame2 = mats.flame(M.GLOW_RED)
    for k, (zz, hh) in enumerate(((-3.2, 6), (-0.6, 9), (2.0, 7), (4.0, 5))):
        fc.cube([-0.9, 60.0, zz - 1.1], [1.8, hh, 2.2], flame if k % 2 == 0 else flame2, rot=[0, 0, (k - 1.5) * 4], pivot=[0, 60, zz])
    fc.cube([-3.2, 60.0, -0.9], [6.4, 4.5, 1.8], flame2)
    # mane of embers down the back
    mn = m.bone("mane", [0, 58, 4.8], "head")
    mn.cube([-4.2, 44, 4.4], [8.4, 14, 1.8], flame2)
    mn.cube([-3.0, 36, 4.4], [6.0, 8.4, 1.6], flame, hide=("up",))
    # forge hammer
    hm = m.bone("hammer", [-sh - 0.6, 24, 0], "hand_r")
    hm.cube([-sh - 1.6, 8, -1.2], [2.4, 40, 2.4], mats.bark((84, 60, 40)), hide=("down",))
    hm.cube([-sh - 6.4, 47, -4.4], [12.0, 9.0, 8.8], bronze, deco={"north": lambda f: M.spiral(f, f.w / 2, f.h / 2, 12, 2.2, 2.0, BR), "south": lambda f: M.spiral(f, f.w / 2, f.h / 2, 12, 2.2, 2.0, BR, flip=True)})
    hm.cube([-sh - 7.4, 47.6, -3.6], [1.2, 7.8, 7.2], mats.glow(EM))
    hm.cube([sh * 0 - sh + 5.0, 47.6, -3.6], [1.2, 7.8, 7.2], mats.glow(EM))
    hm.cube([-sh - 1.8, 56, -1.4], [2.8, 3.0, 2.8], bronze)
    hm.cube([-sh - 1.8, 5.5, -1.4], [2.8, 3.0, 2.8], bronze)

    def ex_idle(a):
        a.sway("crest", 2, 5, 3, 0.0).sway("crest", 0, 3, 2, 0.4).bob("crest", 0.3, 3)
        a.sway("mane", 0, 4, 2, 0.2).sway("apron_f", 0, 2, 1, 0.1).sway("apron_b", 0, -2, 1, 0.1)
        a.sway("hammer", 0, 2, 1, 0.5)
    def ex_walk(a):
        a.sway("apron_f", 0, -16).sway("apron_b", 0, 16).sway("mane", 0, 10, 2, 0.2).sway("crest", 0, 6, 2)
    def ex_atk(a):
        L = a.length
        a.keys("hammer", 0, [(0, 0), (L * .3, 20), (L * .5, -50), (L, 0)])
        a.keys("crest", 0, [(0, 0), (L * .4, -22), (L, 0)])
    anims = god_anims(o, idle_len=3.6, walk_len=1.5, extra_idle=ex_idle, extra_walk=ex_walk, extra_attack=ex_atk, stride=22, arm_swing=12, atk_len=1.0)
    return m, anims
