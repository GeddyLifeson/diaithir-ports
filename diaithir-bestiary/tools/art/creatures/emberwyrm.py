"""Emberwyrm: a crimson mountain wyrm with glowing ember veins in its wings, cream belly plates, a horned head and a flame-tipped tail."""
import math

import mats
import motifs as M
import parts
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "emberwyrm"
YS = 1.0   # sign of the y rotation in the renderer's convention


def make():
    red = mats.scales((176, 44, 36), size=5.0, tone=0.58, lo=0.34, hi=1.55)
    red_d = mats.scales((126, 30, 30), size=5.0, tone=0.54, lo=0.34, hi=1.5)
    belly = mats.scales((242, 188, 110), size=5.0, tone=0.62, lo=0.4, hi=1.3, outline=True)
    horn = mats.horn((226, 208, 170), ring=2.6)
    spike = mats.horn((70, 30, 28), ring=2.6, lo=0.3, hi=1.6)
    mem = mats.membrane2((200, 74, 38))
    mem_o = mats.membrane2((222, 100, 44))
    EM = M.GLOW_AMBER

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            ex = w / 2 + sx * w * 0.27
            f.paint(f.ellipse(ex, h * 0.40, 3.4, 3.0), ramp((40, 10, 10)), 0.15, -0.4, nolight=True)
            f.paint(f.ellipse(ex, h * 0.40, 2.6, 2.4), M.GLOW_AMBER, 0.7, 0, emit=True)
            f.paint(f.rect(ex - 0.5, h * 0.40 - 2, ex + 0.7, h * 0.40 + 2), ramp((20, 6, 6)), 0.1, 0, nolight=True)
            f.paint(f.line([(w / 2 + sx * w * 0.08, h * 0.22), (w / 2 + sx * w * 0.44, h * 0.30)], 1.6, ss=1), ramp((60, 14, 14)), 0.2, 0.3)

    def snout_face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            f.paint(f.ellipse(w / 2 + sx * w * 0.24, h * 0.34, 1.0, 1.2), ramp((30, 8, 8)), 0.1, -0.5)
        f.paint(f.rect(2, h * 0.72, w - 2, h * 0.72 + 1), EM, 0.5, -0.3, emit=True)

    m = Model(NAME)
    body = m.bone("body", [0, 14, 0])
    body.cube([-5, 9.5, -8], [10, 9, 17], red)
    body.cube([-4, 9.0, -7], [8, 1.2, 15], belly, hide=("up", "north", "south", "east", "west"))
    body.cube([-3.6, 8.9, -7.5], [7.2, 0.8, 15], belly)
    for k in range(5):                                           # dorsal spikes
        body.cube([-0.8, 18.4, -6.0 + k * 3.4], [1.6, 2.6 - abs(k - 2) * 0.3, 1.6], spike)
    # neck: three segments curving up, then the head
    n1 = m.bone("neck", [0, 17, -8], "body")
    n1.cube([-2.8, 15.4, -12.6], [5.6, 6, 5.4], red, rot=[-14, 0, 0], pivot=[0, 17, -8])
    n2 = m.bone("neck2", [0, 20.5, -11.5], "neck")
    n2.cube([-2.5, 19.0, -15.6], [5.0, 6, 5.0], red, rot=[-22, 0, 0], pivot=[0, 20.5, -11.5])
    head = m.bone("head", [0, 23, -14.5], "neck2")
    head.cube([-3.8, 21.0, -21.0], [7.6, 6.6, 7.0], red, deco={"north": face})
    sn = m.bone("snout", [0, 23, -21], "head")
    sn.cube([-2.5, 21.2, -25.6], [5.0, 3.0, 4.8], red, deco={"north": snout_face})
    jaw = m.bone("jaw", [0, 22.4, -21.0], "head")
    jaw.cube([-2.3, 19.8, -25.0], [4.6, 1.8, 4.6], red_d, hide=("up",))
    jaw.cube([-1.9, 21.5, -24.6], [3.8, 0.7, 4.0], mats.glow(EM, core=False), hide=("down", "up"))
    for sx in (-1, 1):                                           # horns sweeping back
        hn = m.bone("horn_" + ("l" if sx > 0 else "r"), [sx * 2.6, 27.0, -16.5], "head")
        pts = [(sx * 2.6, 26.8), (sx * 3.6, 29.2), (sx * 3.8, 31.2)]
        for i in range(len(pts) - 1):
            a, b = pts[i], pts[i + 1]
            mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
            L = math.hypot(b[0] - a[0], b[1] - a[1]) + 0.4
            t = 1.8 - 0.6 * i
            hn.cube([mx - t / 2, my - L / 2, -17.0 - t / 2 + 0.5 * i], [t, L, t], horn, rot=[-24, 0, math.degrees(math.atan2(b[0] - a[0], b[1] - a[1]))], pivot=[mx, my, -17.0])
        fr = m.bone("frill_" + ("l" if sx > 0 else "r"), [sx * 3.8, 24.6, -16], "head")
        fr.cube([sx * 3.8 + (0 if sx > 0 else -2.4), 23.0, -18.4], [2.4, 3.6, 3.0], spike, rot=[0, sx * -28, 0], pivot=[sx * 3.8, 24, -17])
    # tail: four tapering segments, flame at the end
    t1 = m.bone("tail", [0, 15, 9], "body")
    t1.cube([-3.0, 12.4, 8.5], [6.0, 5.4, 6.0], red)
    t2 = m.bone("tail2", [0, 15, 14.5], "tail")
    t2.cube([-2.4, 12.8, 14.0], [4.8, 4.4, 6.0], red_d)
    t3 = m.bone("tail3", [0, 15, 20], "tail2")
    t3.cube([-1.8, 13.0, 19.6], [3.6, 3.6, 6.0], red)
    t4 = m.bone("tail4", [0, 15, 25.5], "tail3")
    t4.cube([-1.2, 13.4, 25.2], [2.4, 2.8, 4.4], red_d)
    t4.cube([-1.4, 12.0, 28.8], [2.8, 5.0, 3.4], mats.flame(M.GLOW_AMBER), rot=[0, 0, 0])
    t4.cube([-0.9, 12.6, 31.2], [1.8, 3.4, 2.2], mats.flame(M.GLOW_RED))
    # wings: arm (shoulder, elbow, wrist), four curved finger spars fanning back from the wrist, and a membrane
    # stretched between them as stepped slices (narrow near the hub, wide at the rim) so it reads as a real bat wing
    WY = 18.8
    S_, E_, W_ = (4.5, -3.0), (11.0, -6.0), (17.5, -3.0)
    FING = [[(24.0, -7.5), (31.0, -9.0)], [(25.0, -1.0), (32.5, 3.0)], [(22.0, 5.0), (26.5, 12.5)], [(18.0, 8.5), (19.0, 16.5)]]
    B_ = (4.5, 12.0)
    ang = lambda p0, p1: math.degrees(math.atan2(p1[0] - p0[0], p1[1] - p0[1]))

    def spar(bn, p0, p1, t, mat, sg, y=WY - 0.4, th=None):
        dx, dz = (p1[0] - p0[0]) * sg, p1[1] - p0[1]
        L = math.hypot(dx, dz) + 0.4
        cx, cz = (p0[0] + p1[0]) / 2 * sg, (p0[1] + p1[1]) / 2
        bn.cube([cx - t / 2, y, cz - L / 2], [t, th or t, L], mat, rot=[0, YS * math.degrees(math.atan2(dx, dz)), 0], pivot=[cx, y, cz])

    def panel(bn, hub, ta, tb, mat, sg):
        mid = ((ta[0] + tb[0]) / 2, (ta[1] + tb[1]) / 2)
        dx, dz = (mid[0] - hub[0]) * sg, mid[1] - hub[1]
        L = math.hypot(dx, dz)
        chord = math.hypot(ta[0] - tb[0], ta[1] - tb[1])
        ry = YS * math.degrees(math.atan2(dx, dz))
        ux, uz = dx / L, dz / L
        for f0, f1, wf in ((0.0, 0.4, 0.34), (0.4, 0.72, 0.68), (0.72, 1.0, 0.98)):
            l = (f1 - f0) * L
            c = (f0 + f1) / 2 * L
            cx, cz = hub[0] * sg + ux * c, hub[1] + uz * c
            bn.cube([cx - chord * wf / 2, WY - 0.55, cz - l / 2], [chord * wf, 0.45, l], mat, rot=[0, ry, 0], pivot=[cx, WY - 0.55, cz])

    for s, sg in (("l", 1), ("r", -1)):
        w1 = m.bone("wing_" + s, [sg * 4.5, 18.5, -3.0], "body", rot=[0, 0, sg * -14])
        spar(w1, S_, E_, 2.6, red_d, sg, y=WY - 1.1, th=2.4)
        panel(w1, S_, E_, B_, mem, sg)
        panel(w1, E_, W_, B_, mem, sg) if False else None
        w2 = m.bone("wing2_" + s, [sg * 11.0, 18.8, -6.0], "wing_" + s)
        spar(w2, E_, W_, 2.2, red_d, sg, y=WY - 1.0, th=2.0)
        w2.cube([sg * W_[0] - 1.1, WY - 1.4, W_[1] - 1.1], [2.2, 2.6, 2.2], horn)           # wrist knuckle
        tips = [f[-1] for f in FING]
        for i in range(len(FING) - 1):
            panel(w2, W_, tips[i], tips[i + 1], mem_o, sg)
        panel(w2, W_, tips[-1], B_, mem, sg)
        for f in FING:
            spar(w2, W_, f[0], 1.5, horn, sg, y=WY - 0.7, th=1.1)
            spar(w2, f[0], f[1], 1.0, horn, sg, y=WY - 0.7, th=0.9)
            spar(w2, f[0], f[1], 0.0 + 0.7, mats.glow(EM, core=False), sg, y=WY - 0.2, th=0.35)
        w2.cube([sg * FING[0][1][0] - 0.8, WY - 1.0, FING[0][1][1] - 1.4], [1.6, 1.4, 1.8], spike)  # thumb claw at the leading tip
    # legs
    for nm, z in (("f", -5.0), ("b", 5.0)):
        for s, sg in (("l", 1), ("r", -1)):
            lg = m.bone(f"leg_{nm}_{s}", [sg * 4.4, 10.5, z], "body")
            lg.cube([sg * 4.4 - 1.8, 5.0, z - 1.8], [3.6, 6.2, 3.6], red_d)
            ft = m.bone(f"foot_{nm}_{s}", [sg * 4.4, 5.2, z], f"leg_{nm}_{s}")
            ft.cube([sg * 4.4 - 2.0, 0.0, z - 3.2], [4.0, 5.4, 5.4], red)
            for dx in (-1.1, 0, 1.1):
                ft.cube([sg * 4.4 + dx - 0.35, 0.0, z - 4.4], [0.7, 0.9, 1.4], horn)
    anims = {}
    idle = Anim(3.0)
    idle.bob("body", 0.3, 1).sway("neck", 0, 4, 1, 0.1).sway("neck2", 0, 5, 1, 0.3).sway("head", 0, 4, 1, 0.5).sway("jaw", 0, 5, 3)
    idle.sway("tail", 1, 10, 1).sway("tail2", 1, 12, 1, 0.1).sway("tail3", 1, 14, 1, 0.2).sway("tail4", 1, 16, 1, 0.3)
    idle.sway("wing_l", 2, 6, 1).sway("wing_r", 2, -6, 1).sway("wing2_l", 2, 8, 1, 0.1).sway("wing2_r", 2, -8, 1, 0.1)
    anims["idle"] = idle
    walk = Anim(0.9)
    walk.bob("body", 0.7, 2).sway("leg_f_l", 0, 28).sway("leg_f_r", 0, -28).sway("leg_b_l", 0, -28).sway("leg_b_r", 0, 28)
    walk.sway("tail", 1, 16, 1).sway("tail2", 1, 18, 1, 0.1).sway("tail3", 1, 20, 1, 0.2).sway("tail4", 1, 20, 1, 0.3)
    walk.sway("head", 1, 8, 1, 0.5).sway("neck", 1, 6, 1, 0.3).sway("wing_l", 2, 4, 2).sway("wing_r", 2, -4, 2).sway("body", 2, 3, 1)
    anims["walk"] = walk
    fly = Anim(0.8)
    fly.bob("body", 1.4, 1).sway("wing_l", 2, 34).sway("wing_r", 2, -34).sway("wing2_l", 2, 24, 1, 0.15).sway("wing2_r", 2, -24, 1, 0.15)
    for nm in ("leg_f_l", "leg_f_r", "leg_b_l", "leg_b_r"):
        fly.hold(nm, 0, 50)
    fly.sway("tail", 1, 8, 1).sway("tail2", 1, 10, 1, 0.1).sway("tail3", 1, 12, 1, 0.2).sway("tail4", 1, 14, 1, 0.3).sway("neck", 0, 5, 1, 0.5).sway("head", 0, 4, 1, 0.6)
    anims["fly"] = fly
    atk = Anim(0.6, loop=False)
    atk.keys("neck", 0, [(0, 0), (0.15, 18), (0.3, -22), (0.6, 0)])
    atk.keys("head", 0, [(0, 0), (0.15, 14), (0.3, -12), (0.6, 0)])
    atk.keys("jaw", 0, [(0, 0), (0.2, 12), (0.3, 36), (0.45, 0), (0.6, 0)])
    atk.keys("body", 0, [(0, 0), (0.15, -8), (0.3, 6), (0.6, 0)])
    anims["attack"] = atk
    return m, anims
