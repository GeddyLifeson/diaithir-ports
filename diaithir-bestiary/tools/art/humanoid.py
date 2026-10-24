"""Shared tall humanoid skeleton for the gods and horrors: hip, torso, head, shoulders, two-segment arms and legs.

Units: 16 per block, feet at y = 0. Bones: hip > torso > (head, uarm_*), uarm > farm > hand, hip > thigh > shin > foot.
"""
import math

import numpy as np

from model import Model, Anim
from pix import ramp
import motifs as M


def god_face(f, skin_rp, eye_rp, brow_rp, mouth_rp=None, eye_glow=True, beard=None, nose=True, eye_y=0.40, spacing=0.26,
             eye_w=5, eye_h=2, brow_thick=1, pupil=None, blush=None, scar=None):
    """Front face of a head: brow, eyes (optionally emissive), nose shading and mouth."""
    w, h = f.w, f.h
    cx = w / 2
    ey = int(h * eye_y)
    for sgn in (-1, 1):
        ex = cx + sgn * w * spacing
        x0, x1 = int(round(ex - eye_w / 2)), int(round(ex + eye_w / 2))
        f.paint(f.rect(x0 - 1, ey - 2, x1 + 1, ey - 2 + brow_thick + 1), brow_rp, 0.18, 0.4)          # brow
        f.paint(f.rect(x0, ey - 1, x1, ey + eye_h), brow_rp, 0.2, -0.4)                                 # socket
        f.paint(f.rect(x0, ey, x1, ey + eye_h - 1 + (1 if eye_h > 2 else 0)), eye_rp, 0.62, -0.1, emit=eye_glow)
        if eye_h >= 3:
            f.paint(f.rect(x0 + 1, ey, x1 - 1, ey + 1), eye_rp, 0.95, 0.0, emit=eye_glow, nolight=True)
    if nose:
        ny0, ny1 = ey + eye_h, int(h * 0.64)
        f.paint(f.rect(int(cx) - 1, ny0, int(cx) + 1, ny1), skin_rp, 0.74, 1.2)
        f.paint(f.rect(int(cx) - 2, ny1 - 1, int(cx) + 2, ny1 + 1), skin_rp, 0.38, 0.2)
    my = int(h * 0.80)
    f.paint(f.rect(int(cx - w * 0.14), my, int(cx + w * 0.14), my + 1), (brow_rp if mouth_rp is None else mouth_rp), 0.25, -0.6)
    if beard is not None:
        f.paint(f.rect(0, int(h * 0.62), w, h) & (np.abs(f.gx + 0.5 - cx) < (w / 2 + 1 - (f.gy - h * 0.62) * 0.15)), beard, 0.55, 0.8)
        f.paint(f.rect(int(cx - w * 0.14), my, int(cx + w * 0.14), my + 1), brow_rp, 0.2, -0.6)
    if blush is not None:
        for sgn in (-1, 1):
            f.paint(f.ellipse(cx + sgn * w * 0.36, h * 0.6, 2.2, 1.4), blush, 0.7, 0.0)
    if scar is not None:
        scar(f)


def build(name, o):
    """o: dict with materials and numbers. Returns (model, parts dict)."""
    m = Model(name)
    cw = o.get("chest_w", 15.0)       # chest width
    cd = o.get("chest_d", 8.0)
    sh = o.get("shoulder", cw / 2 + 2.2)
    aw = o.get("arm_w", 4.6)
    top = o.get("height", 61.0)
    lw = o.get("leg_w", 5.0)

    hip = m.bone("hip", [0, 28, 0])
    hip.cube([-6.2, 26, -3.6], [12.4, 6, 7.2], o["pelvis"], deco=o.get("pelvis_deco", {}))
    torso = m.bone("torso", [0, 31, 0], "hip")
    torso.cube([-cw / 2 + 1.6, 31, -3.4], [cw - 3.2, 8, 6.8], o["waist"], deco=o.get("waist_deco", {}))
    torso.cube([-cw / 2, 38, -cd / 2], [cw, 12, cd], o["chest"], deco=o.get("chest_deco", {}))
    # neck and head
    neck = m.bone("neck", [0, 50, 0], "torso")
    neck.cube([-2.2, 49.5, -2.2], [4.4, 3.2, 4.4], o.get("neck", o["skin"]), deco=o.get("neck_deco", {}))
    head = m.bone("head", [0, 52, 0], "neck")
    hw, hh, hd = o.get("head", (8.0, 9.0, 8.0))
    head.cube([-hw / 2, 52, -hd / 2], [hw, hh, hd], o["skin"], deco=o["head_deco"])
    # arms
    ua = m.pair("uarm", [sh, 48, 0], "torso", rot=[0, 0, o.get("arm_out", -3)])
    ua.cube([sh - aw / 2, 36.5, -aw / 2], [aw, 12.5, aw], o["uarm"], deco=o.get("uarm_deco", {}))
    fa = m.pair("farm", [sh + 0.4, 36.5, 0], ("uarm_l", "uarm_r"))
    fa.cube([sh + 0.4 - (aw - 0.6) / 2, 26, -(aw - 0.6) / 2], [aw - 0.6, 11, aw - 0.6], o["farm"], deco=o.get("farm_deco", {}))
    hand = m.pair("hand", [sh + 0.6, 26, 0], ("farm_l", "farm_r"))
    hand.cube([sh + 0.6 - 1.9, 21.5, -2.0], [3.8, 5.2, 4.0], o.get("hand", o["skin"]), deco=o.get("hand_deco", {}))
    # legs
    th = m.pair("thigh", [4.0, 27, 0], "hip")
    th.cube([4.0 - lw / 2, 15, -lw / 2], [lw, 12.5, lw], o["thigh"], deco=o.get("thigh_deco", {}))
    shin = m.pair("shin", [4.0, 15.5, 0], ("thigh_l", "thigh_r"))
    shin.cube([4.0 - (lw - 0.6) / 2, 4.2, -(lw - 0.6) / 2], [lw - 0.6, 11.6, lw - 0.6], o["shin"], deco=o.get("shin_deco", {}))
    ft = m.pair("foot", [4.0, 4.5, 0], ("shin_l", "shin_r"))
    ft.cube([4.0 - (lw + 0.4) / 2, 0, -lw / 2 - 2.6], [lw + 0.4, 4.6, lw + 3.4], o["boot"], deco=o.get("boot_deco", {}))
    return m, dict(hip=hip, torso=torso, neck=neck, head=head, ua=ua, fa=fa, hand=hand, th=th, shin=shin, ft=ft, sh=sh)


def god_anims(o, idle_len=4.0, walk_len=1.3, extra_idle=None, extra_walk=None, extra_attack=None, atk_len=0.9,
              stride=24.0, bob=0.5, arm_swing=14.0):
    idle = Anim(idle_len)
    idle.bob("hip", 0.35, 1).sway("torso", 0, 1.4, 1, 0.1).sway("head", 0, 2.0, 1, 0.3).sway("head", 1, 3.0, 1, 0.0)
    idle.sway("uarm_l", 0, 3, 1, 0.2).sway("uarm_r", 0, 3, 1, 0.7).sway("uarm_l", 2, 2, 1, 0).sway("uarm_r", 2, -2, 1, 0)
    idle.sway("farm_l", 0, -5, 1, 0.45).sway("farm_r", 0, -5, 1, 0.95)
    walk = Anim(walk_len)
    walk.bob("hip", bob, 2, 0.25)
    walk.sway("thigh_l", 0, stride).sway("thigh_r", 0, -stride)
    walk.keys("shin_l", 0, [(0, 0), (walk_len * .25, 4), (walk_len * .5, 36), (walk_len * .75, 6), (walk_len, 0)])
    walk.keys("shin_r", 0, [(0, 36), (walk_len * .25, 6), (walk_len * .5, 0), (walk_len * .75, 4), (walk_len, 36)])
    walk.keys("foot_l", 0, [(0, 0), (walk_len * .5, -20), (walk_len, 0)])
    walk.keys("foot_r", 0, [(0, -20), (walk_len * .5, 0), (walk_len, -20)])
    walk.sway("uarm_l", 0, -arm_swing).sway("uarm_r", 0, arm_swing)
    walk.keys("farm_l", 0, [(0, -6), (walk_len * .5, -20), (walk_len, -6)]).keys("farm_r", 0, [(0, -20), (walk_len * .5, -6), (walk_len, -20)])
    walk.sway("torso", 1, 4).sway("head", 1, -3).sway("torso", 0, 1.5, 2, 0.1)
    atk = Anim(atk_len, loop=False)
    L = atk_len
    for s in ("l", "r"):
        atk.keys("uarm_" + s, 0, [(0, 0), (L * .3, -170 if s == "r" else -130), (L * .5, -20), (L, 0)])
        atk.keys("farm_" + s, 0, [(0, -6), (L * .3, -30), (L * .5, 8), (L, -6)])
    atk.keys("torso", 0, [(0, 0), (L * .3, -8), (L * .5, 14), (L, 0)])
    atk.keys("head", 0, [(0, 0), (L * .3, -10), (L * .5, 10), (L, 0)])
    atk.keys("hip", 0, [(0, 0), (L * .5, 0)], kind="position")
    for fn, an in ((extra_idle, idle), (extra_walk, walk), (extra_attack, atk)):
        if fn:
            fn(an)
    return {"idle": idle, "walk": walk, "attack": atk}
