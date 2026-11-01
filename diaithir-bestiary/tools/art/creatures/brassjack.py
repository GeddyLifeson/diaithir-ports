"""Brassjack Walker: a stubby steam-powered brass walker with a boiler body, glass dome, smokestack and twin piston arms."""
import mats
import motifs as M
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "brassjack"


def make():
    brass = mats.brass((190, 146, 62), panel=(5, 5))
    brass_d = mats.brass((150, 108, 44), panel=(4, 4))
    iron = mats.plate((70, 74, 82), panel=(4, 4), rust=0.1, scratch=5)
    copper = mats.plate((170, 96, 58), panel=(5, 5), rust=0.0, scratch=6, lo=0.34, hi=1.5)
    chrome = mats.chrome()
    glass = mats.glass(ramp_of(["#3a2a10", "#7a5a20", "#d8a040", "#ffd878", "#fff6d0"]))
    GA = M.GLOW_AMBER
    BR = brass.rp

    def boiler(f):
        # riveted bands and a pressure gauge on the front
        for y in (6, f.h - 8):
            f.paint(f.rect(0, y, f.w, y + 2), copper.rp, 0.5, 1.0)
            for x in range(3, f.w - 2, 6):
                M.rivet(f, x, y + 1, copper.rp, r=0.9)
        if f.name == "north":
            cx, cy = f.w / 2, f.h / 2
            f.paint(f.ellipse(cx, cy, 6.5, 6.5), copper.rp, 0.5, 1.0)
            f.paint(f.ellipse(cx, cy, 5.0, 5.0), ramp_of(["#2a2a30", "#e8e0d0"]), 0.85, -0.4)
            f.paint(f.line([(cx, cy), (cx + 3, cy - 3)], 1.0, ss=1), iron.rp, 0.1, 0.0)
            f.paint(f.rect(cx - 4, cy + 2, cx - 1, cy + 3), M.GLOW_RED, 0.7, 0, emit=True)

    m = Model(NAME)
    body = m.bone("body", [0, 20, 0])
    body.cube([-8, 20, -8], [16, 15, 16], brass, deco={"sides": boiler})
    body.cube([-6.6, 35, -6.6], [13.2, 2.4, 13.2], brass_d)
    body.cube([-9, 24, -9], [18, 2, 18], iron)
    body.cube([-7, 21, 8], [14, 11, 4], iron)                                   # coal hopper
    body.cube([-5, 22, 12], [10, 8, 1.2], copper, deco={"north": lambda f: M.vent(f, 3, 3, f.w - 3, f.h - 3, copper.rp, iron.rp, slats=4, glow=GA)})
    st = m.bone("stack", [-4, 36, 3], "body")
    st.cube([-5.5, 36, 1.5], [3.4, 10, 3.4], chrome)
    st.cube([-6.2, 45.4, 0.8], [4.8, 1.8, 4.8], iron)
    st.cube([-5.2, 47.2, 1.8], [2.8, 0.8, 2.8], mats.glow(GA, core=False))
    st2 = m.bone("stack2", [4, 36, 3], "body")
    st2.cube([2.1, 36, 1.5], [3.4, 7, 3.4], chrome)
    st2.cube([1.4, 42.4, 0.8], [4.8, 1.6, 4.8], iron)
    dome = m.bone("dome", [0, 35, -2], "body")
    dome.cube([-5.5, 35.6, -6], [11, 4.4, 9], glass)
    dome.cube([-6, 35, -6.6], [12, 1, 10.2], iron)
    dome.cube([-3.5, 40, -4.5], [7, 1.6, 6], glass)
    dome.cube([-0.5, 35.8, -6.2], [1.0, 4.4, 0.5], iron)
    # eyes: two round lamps on the front
    for sx in (-1, 1):
        body.cube([sx * 4 - 1.8, 27, -8.8], [3.6, 3.6, 1.2], mats.glow(GA))
    # arms with piston cannons
    for s, sg in (("l", 1), ("r", -1)):
        a = m.bone("gun_" + s, [sg * 9, 30, 0], "body")
        a.cube([sg * 9 - 1.5 + (1.5 if sg > 0 else 0), 26, -3], [3, 8, 6], iron)
        a.cube([sg * 12.4 - 2, 26.6, -4], [4, 5.6, 8], brass_d)
        a.cube([sg * 12.4 - 1.2, 27.6, -14], [2.4, 2.4, 10.4], chrome)
        a.cube([sg * 12.4 - 1.8, 27.0, -16.2], [3.6, 3.6, 2.4], iron)
        a.cube([sg * 12.4 - 0.7, 28.2, -16.6], [1.4, 1.4, 0.6], mats.glow(GA, core=False))
    # legs
    for s, sg in (("l", 1), ("r", -1)):
        th = m.bone("leg_" + s, [sg * 4.5, 20, 0])
        th.cube([sg * 4.5 - 2.5, 9, -2.5], [5, 11.5, 5], iron)
        th.cube([sg * 4.5 - 1.0 + sg * 2.2, 10, -0.8], [1.6, 9, 1.6], chrome)
        ft = m.bone("foot_" + s, [sg * 4.5, 9, 0], "leg_" + s)
        ft.cube([sg * 4.5 - 4, 0, -6], [8, 3, 12], brass_d)
        ft.cube([sg * 4.5 - 3, 3, -4.5], [6, 4, 8], iron)
        ft.cube([sg * 4.5 - 4.4, 0.4, -6.8], [8.8, 2, 1.6], copper)
    anims = {}
    idle = Anim(2.0)
    idle.bob("body", 0.3, 1).sway("stack", 0, 2, 1, 0.2).sway("dome", 1, 2, 1)
    idle.sway("stack", 2, 1.5, 2, 0.4)
    anims["idle"] = idle
    walk = Anim(0.9)
    walk.bob("body", 0.9, 2, 0.25).sway("body", 2, 3).sway("leg_l", 0, 26).sway("leg_r", 0, -26)
    walk.sway("foot_l", 0, -12, 1, 0.3).sway("foot_r", 0, 12, 1, 0.3).sway("gun_l", 0, 6, 1, 0.5).sway("gun_r", 0, -6, 1, 0.5)
    walk.sway("stack", 2, 5, 2, 0.1).sway("stack2", 2, -5, 2, 0.1)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("gun_l", 0, [(0, 0), (0.08, 10), (0.16, -6), (0.5, 0)]).keys("gun_r", 0, [(0, 0), (0.2, 0), (0.28, 10), (0.36, -6), (0.5, 0)])
    atk.keys("body", 0, [(0, 0), (0.1, 4), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
