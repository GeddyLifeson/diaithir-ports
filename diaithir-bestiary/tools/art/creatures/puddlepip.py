"""Puddlepip: a round sky-blue duckling with a big orange bill, tiny wings, a cheeky water-drop tuft and webbed feet."""
import mats
import motifs as M
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "puddlepip"


def make():
    sky = mats.fur((104, 176, 236), clump=2.6, contrast=0.07, steps=3, tone=0.64)
    sky_d = mats.fur((70, 136, 208), clump=2.6, contrast=0.07, steps=3, tone=0.56)
    belly = mats.fur((236, 246, 255), clump=2.6, contrast=0.05, steps=3, tone=0.7)
    orange = mats.cel((250, 164, 52), tone=0.64, grad=0.16)
    orange_d = mats.cel((226, 120, 36), tone=0.58, grad=0.14)
    drop = mats.glass(ramp_of(["#2a6aa8", "#58a8e8", "#a8e0ff", "#ffffff"]), tone=0.7)

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            T.eye(f, w / 2 + sx * w * 0.26, h * 0.38, 3.6, 4.4, look=(-sx * 0.25, 0.2))
            T.blush(f, w / 2 + sx * w * 0.42, h * 0.72, 2.4, 1.6)

    def bill_top(f):
        for sx in (-1, 1):
            f.paint(f.rect(f.w / 2 + sx * 3, 3, f.w / 2 + sx * 3 + 1, 5), T.INK, 0.2, -0.4)

    m = Model(NAME)
    body = m.bone("body", [0, 5, 0])
    T.rbox(body, [-6.0, 3.6, -6.0], [12, 10, 12], sky, r=3.0)
    body.cube([-4.0, 4.0, -6.4], [8, 6.6, 0.7], belly, hide=("south", "up", "down"))
    T.rbox(body, [-3.4, 3.7, -5.6], [6.8, 5.8, 3.0], belly, r=1.2) if False else None
    tail = m.bone("tail", [0, 9, 6], "body")
    tail.cube([-2.0, 8.0, 5.6], [4, 2.4, 3.4], sky_d)
    tail.cube([-1.2, 10.2, 7.4], [2.4, 1.6, 2.6], sky_d)
    head = m.bone("head", [0, 11.5, -2], "body")
    T.rbox(head, [-5.0, 10.6, -6.6], [10, 8.6, 9], sky, r=2.6, deco={"north": face})
    T.rbox(head, [-1.6, 18.8, -3.4], [3.2, 2.6, 3.0], sky_d, r=0.9)           # tuft
    tuft = m.bone("drop", [0, 21, -2], "head")
    tuft.cube([-1.0, 21.2, -2.6], [2.0, 3.0, 2.0], drop)
    bill = m.bone("bill", [0, 13.4, -6.6], "head")
    bill.cube([-3.4, 12.6, -11.4], [6.8, 1.8, 5.0], orange, deco={"up": bill_top})
    bill.cube([-3.2, 11.4, -11.0], [6.4, 1.4, 4.6], orange_d, hide=("up",))
    for s, sg in (("l", 1), ("r", -1)):
        w = m.bone("wing_" + s, [sg * 6.0, 11.5, 0], "body")
        w.cube([sg * 6.0 + (0 if sg > 0 else -1.8), 6.4, -3.6], [1.8, 6.4, 6.8], sky_d)
        w.cube([sg * 6.0 + (0.0 if sg > 0 else -1.2), 5.4, -2.6], [1.2, 1.4, 4.4], sky_d)
        ft = m.bone("foot_" + s, [sg * 2.6, 4.0, 0], "body" if False else None)
        ft.cube([sg * 2.6 - 0.6, 1.4, -0.6], [1.2, 3.0, 1.2], orange_d)
        ft.cube([sg * 2.6 - 2.2, 0.0, -4.2], [4.4, 1.4, 5.4], orange, deco={"up": lambda f: None})
        for dx in (-1.4, 0, 1.4):
            ft.cube([sg * 2.6 + dx - 0.4, 0.0, -5.2], [0.8, 1.2, 1.2], orange_d)
    anims = {}
    idle = Anim(2.0)
    idle.bob("body", 0.35, 1).sway("wing_l", 2, 10, 2).sway("wing_r", 2, -10, 2).sway("head", 2, 5, 1, 0.2).sway("drop", 0, 12, 2, 0.1).sway("tail", 0, 6, 2)
    anims["idle"] = idle
    walk = Anim(0.5)
    walk.bob("body", 0.8, 2, 0.25).sway("foot_l", 0, 40).sway("foot_r", 0, -40).sway("body", 2, 5).sway("head", 2, 6, 1, 0.5)
    walk.sway("wing_l", 2, 16, 2).sway("wing_r", 2, -16, 2).sway("tail", 0, 10, 2).sway("drop", 0, 15, 2)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("head", 0, [(0, 0), (0.12, 20), (0.24, -16), (0.5, 0)])
    atk.keys("bill", 0, [(0, 0), (0.2, -18), (0.3, 6), (0.5, 0)])
    atk.keys("wing_l", 2, [(0, 0), (0.12, 40), (0.5, 0)]).keys("wing_r", 2, [(0, 0), (0.12, -40), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
