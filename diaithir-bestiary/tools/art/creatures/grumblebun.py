"""Grumblebun: a lilac bunny with a permanent scowl, long floppy ears, big feet and a fluffy white tail."""
import mats
import motifs as M
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "grumblebun"


def make():
    lilac = mats.fur((176, 150, 214), clump=2.6, contrast=0.07, steps=3, tone=0.62)
    lilac_d = mats.fur((140, 112, 188), clump=2.6, contrast=0.07, steps=3, tone=0.54)
    cream = mats.fur((250, 242, 250), clump=2.6, contrast=0.05, steps=3, tone=0.7)
    pinkin = mats.cel((240, 170, 200), tone=0.62, grad=0.12)

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            T.eye(f, w / 2 + sx * w * 0.26, h * 0.46, 3.2, 3.6, look=(-sx * 0.1, 0.3), lid=0.5)
            f.paint(f.line([(w / 2 + sx * w * 0.06, h * 0.36), (w / 2 + sx * w * 0.44, h * 0.20)], 2.0, ss=1), T.INK, 0.15, 0.2, nolight=True)
        f.paint(f.ellipse(w / 2, h * 0.68, 2.0, 1.4), ramp((240, 140, 170)), 0.6, 0.4, nolight=True)
        T.frown(f, w / 2, h * 0.84, w * 0.34, curve=1.8)
        for sx in (-1, 1):
            T.blush(f, w / 2 + sx * w * 0.42, h * 0.72, 2.2, 1.4)

    m = Model(NAME)
    body = m.bone("body", [0, 6, 0])
    T.rbox(body, [-5.6, 4.2, -5.4], [11.2, 9.4, 12], lilac, r=2.6)
    body.cube([-3.2, 4.8, -5.4], [6.4, 5.4, 0.7], cream, hide=("south", "up", "down"))
    tail = m.bone("tail", [0, 8, 6], "body")
    T.rbox(tail, [-2.4, 6.4, 5.6], [4.8, 4.8, 4.6], cream, r=1.6)
    head = m.bone("head", [0, 12, -5], "body")
    T.rbox(head, [-4.6, 11.4, -9.8], [9.2, 7.8, 8.2], lilac, r=2.4, deco={"north": face})
    nose = head.cube([-1.6, 11.0, -9.9], [3.2, 2.0, 0.8], pinkin) if False else None
    for s, sg in (("l", 1), ("r", -1)):
        e1 = m.bone("ear_" + s, [sg * 2.6, 18.0, -4.6], "head", rot=[0, 0, sg * 9])
        e1.cube([sg * 2.6 + (-1.4 if sg > 0 else -1.4), 18.0, -5.2], [2.8, 7.0, 1.6], lilac_d)
        e1.cube([sg * 2.6 - 0.8, 18.4, -5.4], [1.6, 6.0, 0.5], pinkin)
        e2 = m.bone("ear2_" + s, [sg * 2.6, 25.0, -4.4], "ear_" + s, rot=[12, 0, sg * 12])
        e2.cube([sg * 2.6 - 1.4, 25.0, -5.0], [2.8, 5.0, 1.6], lilac_d)
        e2.cube([sg * 2.6 - 1.4, 29.4, -5.0], [2.8, 1.0, 1.6], lilac_d)
    # arms and legs
    for s, sg in (("l", 1), ("r", -1)):
        a = m.bone("arm_" + s, [sg * 5.0, 10.0, -3.0], "body")
        a.cube([sg * 5.0 + (0 if sg > 0 else -1.8), 5.0, -4.2], [1.8, 5.4, 2.4], lilac_d)
        lg = m.bone("leg_" + s, [sg * 3.2, 5.0, 3.0], "body")
        T.rbox(lg, [sg * 3.2 - 2.0, 0.0, 1.4], [4.0, 5.6, 4.4], lilac_d, r=1.2)
        ft = m.bone("foot_" + s, [sg * 3.2, 1.2, 3.0], "leg_" + s)
        T.rbox(ft, [sg * 3.2 - 2.0, 0.0, -2.4], [4.0, 1.8, 6.4], cream, r=0.8)
    anims = {}
    idle = Anim(2.5)
    idle.bob("body", 0.25, 1).sway("ear_l", 2, 6, 1, 0.1).sway("ear_r", 2, -6, 1, 0.1).sway("ear2_l", 0, 10, 2, 0.2).sway("ear2_r", 0, 10, 2, 0.2)
    idle.sway("tail", 2, 8, 2).keys("head", 0, [(0, 0), (1.0, 3), (1.25, -2), (2.5, 0)])
    anims["idle"] = idle
    walk = Anim(0.45)
    walk.bob("body", 1.4, 2, 0.5).sway("leg_l", 0, 38).sway("leg_r", 0, -38).sway("arm_l", 0, -30).sway("arm_r", 0, 30)
    walk.sway("ear2_l", 0, 18, 1, 0.2).sway("ear2_r", 0, 18, 1, 0.2).sway("tail", 2, 14, 2).sway("body", 0, 3, 2)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, 18), (0.24, -20), (0.5, 0)])
    atk.keys("arm_l", 0, [(0, 0), (0.15, -80), (0.3, 20), (0.5, 0)]).keys("arm_r", 0, [(0, 0), (0.15, -80), (0.3, 20), (0.5, 0)])
    atk.keys("ear2_l", 0, [(0, 0), (0.15, -30), (0.5, 0)]).keys("ear2_r", 0, [(0, 0), (0.15, -30), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
