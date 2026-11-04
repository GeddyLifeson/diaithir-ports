"""Veilstitch: a patched burlap scarecrow on a splintered post, a stitched grin, mismatched button eyes, a torn veil under a crushed hat, straw spilling from its cuffs and a long needle."""
import math

import mats
import motifs as M
import parts
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "veilstitch"


def make():
    burlap = mats.burlap((156, 126, 86))
    burlap_d = mats.burlap((118, 92, 64))
    patch = mats.burlap((96, 112, 120))
    straw = mats.straw()
    wood = mats.wood((96, 70, 46))
    hat = mats.cloth((40, 32, 30), fold=2, weave=True, amp=0.1, lo=0.34, hi=1.6)
    veil = mats.gauze((200, 200, 206))
    steel = mats.plate((150, 156, 168), panel=(9, 9), rivets=False, rust=0.12, scratch=2, seam=0)
    dark = ramp((22, 16, 14))
    THREAD = ramp((28, 22, 20))
    RED = ramp_of(["#401010", "#a02820", "#ff5040", "#ffc0a0"])

    def face(f):
        w, h = f.w, f.h
        # two mismatched buttons
        f.paint(f.ellipse(w * 0.30, h * 0.36, 3.2, 3.2), ramp((60, 50, 40)), 0.5, 1.0)
        f.paint(f.ellipse(w * 0.30, h * 0.36, 2.0, 2.0), RED, 0.7, 0.6, emit=True)
        f.paint(f.rect(w * 0.30 - 0.5, h * 0.36 - 0.5, w * 0.30 + 0.6, h * 0.36 + 0.6), dark, 0.0, 0)
        f.paint(f.ellipse(w * 0.70, h * 0.42, 2.4, 2.4), ramp((220, 214, 200)), 0.7, 1.0)
        f.paint(f.ellipse(w * 0.70, h * 0.42, 0.9, 0.9), dark, 0.0, 0.0)
        for (a, b) in (((w * 0.30 - 2, h * 0.36 - 2), (w * 0.30 + 2, h * 0.36 + 2)), ((w * 0.30 - 2, h * 0.36 + 2), (w * 0.30 + 2, h * 0.36 - 2))):
            f.paint(f.line([a, b], 1.0, ss=1), THREAD, 0.1, 0)
        # stitched mouth
        pts = [(w * 0.18 + k * (w * 0.64) / 8, h * 0.74 + (2 if k % 2 else -2)) for k in range(9)]
        f.paint(f.line(pts, 1.3, ss=1), THREAD, 0.08, -0.4)
        for k in range(9):
            x, y = pts[k]
            f.paint(f.line([(x, h * 0.74 - 4), (x, h * 0.74 + 4)], 1.0, ss=1), THREAD, 0.1, -0.2)
        M.stitches(f, ((2, 3), (2, h - 3)), THREAD, step=3.0, size=1.6)

    def torso_deco(f):
        if f.name == "north":
            f.paint(f.rect(2, 6, 14, 16), patch.rp, 0.55, 0.5)
            M.stitches(f, ((2, 6), (14, 6)), THREAD, step=2.4, size=1.2, cross=False)
            M.stitches(f, ((2, 16), (14, 16)), THREAD, step=2.4, size=1.2, cross=False)
            M.stitches(f, ((2, 6), (2, 16)), THREAD, step=2.4, size=1.2, cross=False)
            M.stitches(f, ((14, 6), (14, 16)), THREAD, step=2.4, size=1.2, cross=False)
            f.paint(f.rect(f.w / 2 - 0.5, 0, f.w / 2 + 0.5, f.h), THREAD, 0.1, -0.4)
            for k in range(5):
                f.paint(f.rect(f.w / 2 - 3, 4 + k * 8, f.w / 2 + 3, 5 + k * 8), THREAD, 0.1, -0.3)

    m = Model(NAME)
    hip = m.bone("hip", [0, 16, 0])
    hip.cube([-4.0, 14, -2.4], [8, 4, 4.8], burlap_d)
    for s, sg in (("l", 1), ("r", -1)):
        th = m.bone("leg_" + s, [sg * 2.0, 15, 0], "hip")
        th.cube([sg * 2.0 - 1.6, 2, -1.6], [3.2, 13, 3.2], burlap_d)
        th.cube([sg * 2.0 - 1.9, 5.5, -1.9], [3.8, 1.2, 3.8], straw)
        th.cube([sg * 2.0 - 2.0, 0, -3.2], [4.0, 2.4, 5.6], wood)
    tor = m.bone("torso", [0, 17, 0], "hip")
    tor.cube([-4.4, 17, -2.4], [8.8, 15, 4.8], burlap, deco={"all": torso_deco})
    tor.cube([-4.6, 29.4, -2.6], [9.2, 2.8, 5.2], burlap_d)
    post = m.bone("post", [0, 17, 3.0], "torso")
    post.cube([-1.2, 6, 2.4], [2.4, 40, 2.4], wood, hide=("down",))
    post.cube([-3.4, 22, 2.2], [6.8, 1.6, 2.8], wood)
    for k in range(3):
        tor.cube([-1.8 + k * 1.6, 12.6, -2.8 - k * 0.0], [1.2, 5, 1.2], straw)                # straw out of the hem
    # arms spread on the crossbar, with straw cuffs and one needle hand
    for s, sg in (("l", 1), ("r", -1)):
        ua = m.bone("arm_" + s, [sg * 4.6, 29.5, 0], "torso", rot=[0, 0, sg * -14])
        ua.cube([sg * 4.6 + (0 if sg > 0 else -9.0), 28.2, -1.4], [9.0, 2.8, 2.8], burlap)
        ua.cube([sg * 13.0 + (0 if sg > 0 else -4.6), 27.8, -1.8], [4.6, 3.6, 3.6], straw)
        for k in range(4):
            ua.cube([sg * 16.8 + (0 if sg > 0 else -1.0) + k * sg * 0.9 * 0, 27.2 + k * 0.9 - 0.9, -1.0 + (k - 1.5) * 0.9], [1.0, 3.4, 0.5], straw, rot=[0, 0, sg * (-30 + k * 12)], pivot=[sg * 17, 29, 0])
    nd = m.bone("needle", [-16.5, 29, 0], "arm_r")
    nd.cube([-17.2, 8, -0.4], [0.8, 22, 0.8], steel, hide=("down",))
    nd.cube([-17.6, 28.8, -0.5], [1.6, 3.4, 1.0], steel)
    nd.cube([-17.0, 29.6, -0.2], [0.4, 0.8, 0.4], mats.flat((22, 16, 14)))
    nd.cube([-16.8, 3.6, -0.3], [0.5, 4.4, 0.5], mats.burlap((150, 40, 40)))
    # head: a stuffed sack with stitches, a torn veil and a crushed hat
    head = m.bone("head", [0, 32, 0], "torso")
    head.cube([-4.0, 32, -4.0], [8, 8, 8], burlap, deco={"north": face})
    head.cube([-3.2, 31.0, -3.2], [6.4, 1.4, 6.4], burlap_d)
    head.cube([-4.4, 31.6, -4.4], [8.8, 1.2, 8.8], straw)
    vl = m.bone("veil", [0, 41, -4.8], "head")
    vl.cube([-5.2, 28.0, -5.4], [10.4, 12.5, 0.6], veil)
    vl.cube([-4.2, 24.0, -5.6], [8.4, 4.5, 0.6], veil)
    ht = m.bone("hat", [0, 40, 0], "head")
    ht.cube([-7.4, 39.6, -7.0], [14.8, 1.2, 14.2], hat)
    ht.cube([-3.8, 40.8, -3.8], [7.6, 5.4, 7.6], hat)
    ht.cube([-3.0, 46.2, -3.0], [6.0, 1.6, 6.0], hat, rot=[0, 0, 8], pivot=[0, 46, 0])
    ht.cube([-4.0, 40.8, -4.0], [8.0, 1.2, 8.0], mats.cloth((120, 40, 40), fold=2, amp=0.1))
    anims = {}
    idle = Anim(5.0)
    idle.sway("head", 2, 9, 1).sway("head", 1, 8, 1, 0.5).sway("veil", 0, 5, 2, 0.2).sway("arm_l", 2, 3, 2).sway("arm_r", 2, -3, 2, 0.3)
    idle.sway("hat", 2, 2, 1, 0.4).bob("hip", 0.15, 2).sway("needle", 0, 4, 1)
    anims["idle"] = idle
    walk = Anim(0.9)
    walk.bob("hip", 0.5, 2, 0.25).sway("leg_l", 0, 22).sway("leg_r", 0, -22).sway("torso", 2, 4).sway("head", 2, 8, 1, 0.5)
    walk.sway("arm_l", 2, 7, 2).sway("arm_r", 2, -7, 2).sway("veil", 0, 12, 2, 0.1).sway("needle", 0, 10, 2)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("arm_r", 0, [(0, 0), (0.12, -85), (0.24, -50), (0.5, 0)])
    atk.keys("arm_l", 0, [(0, 0), (0.12, -40), (0.5, 0)])
    atk.keys("needle", 0, [(0, 0), (0.12, -30), (0.24, 25), (0.5, 0)])
    atk.keys("head", 0, [(0, 0), (0.12, 14), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
