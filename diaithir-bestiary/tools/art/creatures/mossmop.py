"""Mossmop: a big shaggy moss mop on tiny feet, a sleepy cream face peeking out, mushrooms and a flower in its fringe."""
import mats
import motifs as M
import toon as T
from model import Model, Anim
from pix import ramp, ramp_of

NAME = "mossmop"


def make():
    moss = mats.foliage((98, 148, 62), size=6.5, tone=0.6, lo=0.4, hi=1.4)
    moss_d = mats.foliage((66, 108, 50), size=6.5, tone=0.55, lo=0.4, hi=1.4)
    moss_l = mats.foliage((140, 182, 80), size=6.5, tone=0.62, lo=0.4, hi=1.35)
    core = mats.fur((84, 120, 54), clump=3, contrast=0.1, steps=3, tone=0.5)
    cream = mats.cel((246, 224, 184), tone=0.66, grad=0.16)
    brown = mats.cel((84, 60, 46), tone=0.5, grad=0.1)
    capm = mats.cel((226, 80, 72), tone=0.6, grad=0.16)
    flowerm = mats.cel((255, 214, 90), tone=0.7, grad=0.1)
    petalm = mats.cel((250, 140, 190), tone=0.68, grad=0.1)

    def face(f):
        w, h = f.w, f.h
        for sx in (-1, 1):
            T.eye(f, w / 2 + sx * w * 0.24, h * 0.42, 3.4, 3.8, look=(0, 0.4), lid=0.42)
            T.blush(f, w / 2 + sx * w * 0.38, h * 0.74, 2.2, 1.4)
        f.paint(f.ellipse(w / 2, h * 0.66, 1.6, 1.2), ramp((190, 120, 100)), 0.5, 0.3, nolight=True)
        T.smile(f, w / 2, h * 0.84, w * 0.28, curve=1.2)

    def dots(f):
        for (x, y) in ((4, 4), (f.w - 5, 6), (f.w / 2, f.h - 5)):
            f.paint(f.ellipse(x, y, 1.4, 1.4), cream.rp, 0.8, 0.5)

    import math
    import random
    rnd = random.Random(7)
    hair_a = mats.hair((104, 156, 66), tone=0.58, strand=2.0, contrast=0.3)
    hair_b = mats.hair((70, 116, 54), tone=0.54, strand=2.0, contrast=0.3)
    hair_c = mats.hair((146, 188, 84), tone=0.6, strand=2.0, contrast=0.3)
    m = Model(NAME)
    body = m.bone("body", [0, 8, 0])
    body.cube([-5.0, 6.0, -5.0], [10, 11, 10], core)

    def tier(nm, ytop, ybot, r, n, mats_, lean, gap=0.0, thick=2.6):
        b = m.bone(nm, [0, ytop, 0], "body")
        for i in range(n):
            th = 2 * math.pi * i / n + rnd.uniform(-0.08, 0.08)
            ang = (math.degrees(th) + 180) % 360 - 180          # 0 = front (-z)
            if gap and abs(ang) < gap:
                continue
            x, z = r * math.sin(th), -r * math.cos(th)
            ln = (ytop - ybot) * rnd.uniform(0.78, 1.0)
            lx = lean * rnd.uniform(0.7, 1.3)
            b.cube([x - thick / 2, ytop - ln, z - thick / 2], [thick, ln, thick], mats_[i % len(mats_)],
                   rot=[math.cos(th) * lx, 0, -math.sin(th) * lx], pivot=[x, ytop, z])
        return b

    tier("moss_lo", 9.0, 1.6, 6.6, 15, [hair_b, hair_a, hair_c], 8, gap=34)
    tier("moss_mid", 16.0, 6.5, 5.6, 14, [hair_a, hair_c, hair_b], 6, gap=30)
    tier("moss_hi", 21.0, 12.0, 4.0, 10, [hair_c, hair_a], 5)
    # the face peeks out between the front strands
    head = m.bone("head", [0, 15, -5], "body")
    T.rbox(head, [-4.2, 9.2, -8.4], [8.4, 7.0, 4.2], cream, r=1.6, deco={"north": face})
    fr = m.bone("fringe", [0, 17.5, -7], "head")
    for i, (x, ln) in enumerate(((-3.6, 3.2), (-1.8, 4.0), (0.0, 2.8), (1.8, 4.2), (3.6, 3.0))):
        fr.cube([x - 0.9, 16.8 - ln + 1.6, -9.6], [1.8, ln, 1.8], [hair_a, hair_c][i % 2])
    # tuft with mushrooms and a flower
    tuft = m.bone("tuft", [0, 22, 0], "body")
    tuft.cube([-4.0, 21.4, -4.0], [8, 3.6, 8], moss_l)
    tuft.cube([-1.8, 21.4, -1.8], [3.6, 6.0, 3.6], moss_d)
    mush = m.bone("mush", [3.0, 24, 1], "tuft")
    mush.cube([2.4, 24.8, 0.4], [1.4, 3.0, 1.4], cream)
    T.rbox(mush, [1.2, 27.2, -0.8], [3.8, 1.8, 3.8], capm, r=0.8, deco={"up": dots})
    mush2 = m.bone("mush2", [-3.0, 24, -1], "tuft")
    mush2.cube([-3.6, 24.4, -1.4], [1.0, 2.2, 1.0], cream)
    T.rbox(mush2, [-4.8, 26.2, -2.6], [3.2, 1.4, 3.2], capm, r=0.7, deco={"up": dots})
    fl = m.bone("flower", [-1, 27, 1.6], "tuft")
    fl.cube([-1.4, 24.0, 1.0], [0.8, 4.0, 0.8], moss_d)
    fl.cube([-1.4 - 1.4, 27.4, 0.0], [3.6, 1.0, 2.8], petalm)
    fl.cube([-1.4 - 0.4, 27.4, -1.2], [2.0, 1.0, 5.2], petalm)
    fl.cube([-1.4 - 0.4, 27.9, 0.2], [2.0, 0.8, 2.0], flowerm)
    for s, sg in (("l", 1), ("r", -1)):
        ft = m.bone("foot_" + s, [sg * 3.2, 2.0, -1.0], "body" if False else None)
        T.rbox(ft, [sg * 3.2 - 2.0, 0.0, -4.0], [4.0, 2.6, 5.6], brown, r=0.9)
    anims = {}
    idle = Anim(3.2)
    idle.bob("body", 0.5, 1).sway("moss_hi", 2, 2, 1, 0.1).sway("moss_mid", 2, -3, 1, 0.3).sway("moss_lo", 2, 3, 1, 0.5).sway("tuft", 2, 6, 1, 0.2).sway("flower", 2, 8, 2).sway("mush", 2, 5, 2, 0.3)
    idle.sway("head", 0, 3, 1, 0.4)
    anims["idle"] = idle
    walk = Anim(0.8)
    walk.bob("body", 1.1, 2, 0.25).sway("foot_l", 0, 30).sway("foot_r", 0, -30).sway("moss_hi", 0, 4, 2).sway("moss_mid", 0, -5, 2, 0.1).sway("moss_lo", 0, 6, 2, 0.2)
    walk.sway("moss_hi", 2, 3, 1).sway("moss_lo", 2, -4, 1).sway("tuft", 2, 8, 2).sway("flower", 2, 10, 2).sway("body", 2, 4, 1)
    anims["walk"] = walk
    atk = Anim(0.5, loop=False)
    atk.keys("body", 0, [(0, 0), (0.12, -10), (0.24, 8), (0.5, 0)])
    atk.keys("head", 0, [(0, 0), (0.12, 14), (0.5, 0)])
    atk.keys("moss_hi", 0, [(0, 0), (0.15, -14), (0.3, 10), (0.5, 0)]).keys("moss_lo", 0, [(0, 0), (0.15, 16), (0.3, -8), (0.5, 0)])
    anims["attack"] = atk
    return m, anims
