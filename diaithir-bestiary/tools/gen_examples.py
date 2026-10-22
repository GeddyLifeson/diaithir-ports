"""Writes the five example creatures' GeckoLib models, animations and textures (cube-based placeholders).

Run from diaithir-bestiary/: python3 tools/gen_examples.py
Each creature is a list of bones (name, parent, pivot, cubes, colour) in Blockbench pixel units, 16 to a block, y up from the feet.
"""
import json
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/diaithirbestiary"


def bone(name, pivot, cubes, colour, parent=None, eyes=False):
    return {"name": name, "parent": parent, "pivot": pivot, "cubes": cubes, "colour": colour, "eyes": eyes}


def pair(name, pivot, origin, size, colour, parent=None):
    """A left and right copy of a bone, mirrored across x = 0."""
    mx = lambda p: [-p[0]] + list(p[1:])
    mo = [-origin[0] - size[0]] + list(origin[1:])
    return [bone(name + "_l", pivot, [(origin, size)], colour, parent), bone(name + "_r", mx(pivot), [(mo, size)], colour, parent)]


def loop(length, frames):
    return {"loop": True, "animation_length": length, "bones": frames}


def swing(bones, angle, length, axis=0, phase=False):
    """Rotation keyframes that swing each bone back and forth; every other bone in the list runs in opposite phase."""
    out = {}
    for i, name in enumerate(bones):
        a = angle * (-1 if (i % 2) ^ phase else 1)
        rot = lambda v: [v if axis == 0 else 0, v if axis == 1 else 0, v if axis == 2 else 0]
        out[name] = {"rotation": {"0.0": rot(a), str(length / 2): rot(-a), str(length): rot(a)}}
    return out


def bob(name, height, length):
    return {name: {"position": {"0.0": [0, 0, 0], str(length / 2): [0, height, 0], str(length): [0, 0, 0]}}}


def attack(name, angle):
    return {"loop": False, "animation_length": 0.5, "bones": {name: {"rotation": {"0.0": [0, 0, 0], "0.2": [angle, 0, 0], "0.5": [0, 0, 0]}}}}


CREATURES = {
    "bumblesnout": {
        "bones": [
            bone("body", [0, 4, 0], [([-6, 4, -6], [12, 10, 12])], (240, 168, 104), eyes=True),
            bone("snout", [0, 8, -6], [([-3, 5, -10], [6, 4, 4]), ([-1, 7, -11], [2, 2, 1])], (250, 204, 170), "body"),
            *pair("ear", [4, 14, -2], [3, 14, -3], [2, 5, 3], (196, 116, 84), "body"),
            *pair("leg_front", [3, 4, -3], [2, 0, -4], [3, 4, 3], (176, 106, 76)),
            *pair("leg_back", [3, 4, 3], [2, 0, 2], [3, 4, 3], (176, 106, 76)),
            bone("tail", [0, 9, 6], [([-1, 8, 6], [2, 2, 3])], (196, 116, 84), "body"),
        ],
        "anims": {
            "idle": loop(2.0, {**bob("body", 0.4, 2.0), **swing(["ear_l", "ear_r"], 8, 2.0, axis=2, phase=True)}),
            "walk": loop(0.6, {**swing(["leg_front_l", "leg_front_r"], 35, 0.6), **swing(["leg_back_r", "leg_back_l"], 35, 0.6),
                               **bob("body", 0.8, 0.3), **swing(["tail"], 25, 0.6, axis=1)}),
            "attack": attack("snout", -25),
        },
    },
    "emberwyrm": {
        "bones": [
            bone("body", [0, 12, 0], [([-6, 12, -10], [12, 10, 20])], (150, 40, 30)),
            bone("neck", [0, 18, -10], [([-3, 16, -20], [6, 6, 10])], (165, 48, 34), "body"),
            bone("head", [0, 19, -20], [([-4, 15, -28], [8, 7, 8]), ([-3, 22, -23], [1, 3, 1]), ([2, 22, -23], [1, 3, 1])], (175, 55, 38), "neck", eyes=True),
            bone("tail", [0, 16, 10], [([-3, 14, 10], [6, 6, 10])], (150, 40, 30), "body"),
            bone("tail_tip", [0, 16, 20], [([-2, 15, 20], [4, 4, 10])], (130, 34, 26), "tail"),
            *pair("wing", [6, 21, -4], [6, 21, -6], [20, 1, 14], (210, 96, 44), "body"),
            *pair("leg_front", [4, 12, -6], [3, 0, -8], [3, 12, 3], (120, 32, 24)),
            *pair("leg_back", [4, 12, 6], [3, 0, 5], [3, 12, 3], (120, 32, 24)),
        ],
        "anims": {
            "idle": loop(3.0, {**bob("body", 0.3, 3.0), **swing(["wing_l", "wing_r"], 6, 3.0, axis=2), **swing(["tail"], 10, 3.0, axis=1)}),
            "walk": loop(1.0, {**swing(["leg_front_l", "leg_front_r"], 30, 1.0), **swing(["leg_back_r", "leg_back_l"], 30, 1.0),
                               **swing(["tail"], 15, 1.0, axis=1)}),
            "fly": loop(0.8, {**swing(["wing_l", "wing_r"], 35, 0.8, axis=2), **swing(["tail", "tail_tip"], 8, 0.8, axis=1),
                              **{leg: {"rotation": {"0.0": [60, 0, 0]}} for leg in ["leg_front_l", "leg_front_r", "leg_back_l", "leg_back_r"]}}),
            "attack": attack("head", -35),
        },
    },
    "ironstrider": {
        "bones": [
            bone("cabin", [0, 30, 0], [([-10, 30, -9], [20, 16, 18])], (110, 118, 130), eyes=True),
            *pair("gun", [10, 38, 0], [10, 34, -12], [4, 4, 16], (80, 86, 96), "cabin"),
            *pair("leg", [6, 30, 0], [3, 3, -3], [6, 27, 6], (128, 132, 140)),
            *pair("foot", [6, 3, 0], [2, 0, -6], [8, 3, 10], (96, 100, 108), None),
        ],
        "anims": {
            "idle": loop(2.0, bob("cabin", 0.3, 2.0)),
            "walk": loop(1.2, {**swing(["leg_l", "leg_r"], 20, 1.2), **swing(["foot_l", "foot_r"], 20, 1.2), **bob("cabin", 0.8, 0.6)}),
            "attack": attack("gun_l", -20),
        },
    },
    "aurelith": {
        "bones": [
            *pair("leg", [2, 28, 0], [0.5, 0, -2], [3, 28, 4], (240, 234, 218)),
            bone("torso", [0, 28, 0], [([-7, 28, -3], [14, 20, 6])], (240, 234, 218)),
            *pair("arm", [9, 46, 0], [7, 26, -2], [4, 22, 4], (230, 190, 80), "torso"),
            bone("head", [0, 48, 0], [([-4, 48, -4], [8, 9, 8])], (236, 206, 150), "torso", eyes=True),
            bone("halo", [0, 61, 0], [([-7, 60, -7], [14, 1, 2]), ([-7, 60, 5], [14, 1, 2]), ([-7, 60, -5], [2, 1, 10]), ([5, 60, -5], [2, 1, 10])],
                 (255, 238, 140), "head"),
        ],
        "anims": {
            "idle": loop(4.0, {"halo": {"rotation": {"0.0": [0, 0, 0], "4.0": [0, 360, 0]}}, **swing(["arm_l", "arm_r"], 4, 4.0, axis=2)}),
            "walk": loop(1.4, {**swing(["leg_l", "leg_r"], 25, 1.4), **swing(["arm_r", "arm_l"], 20, 1.4),
                               "halo": {"rotation": {"0.0": [0, 0, 0], "1.4": [0, 180, 0]}}}),
            "attack": {"loop": False, "animation_length": 0.8, "bones": {
                arm: {"rotation": {"0.0": [0, 0, 0], "0.3": [-150, 0, 0], "0.8": [0, 0, 0]}} for arm in ["arm_l", "arm_r"]}},
        },
    },
    "hollowmourn": {
        "bones": [
            *pair("leg", [2, 22, 0], [1, 0, -1], [2, 22, 2], (36, 32, 42)),
            bone("torso", [0, 22, 0], [([-4, 22, -2], [8, 14, 4])], (30, 27, 36)),
            *pair("arm", [5, 35, 0], [4, 12, -1], [2, 23, 2], (36, 32, 42), "torso"),
            bone("head", [0, 36, 0], [([-3, 36, -3], [6, 7, 6])], (212, 206, 200), "torso", eyes=True),
        ],
        "anims": {
            "idle": loop(5.0, {"head": {"rotation": {"0.0": [0, 0, 0], "2.5": [0, 0, 12], "5.0": [0, 0, 0]}}}),
            "walk": loop(0.8, swing(["leg_l", "leg_r"], 30, 0.8)),
            "attack": {"loop": False, "animation_length": 0.5, "bones": {
                arm: {"rotation": {"0.0": [0, 0, 0], "0.2": [-90, 0, 0], "0.5": [0, 0, 0]}} for arm in ["arm_l", "arm_r"]}},
        },
    },
}


def box_uv(size):
    w, h, d = (int(round(v)) for v in size)
    return 2 * (d + w), d + h


def pack(cubes, width):
    """Shelf-packs each cube's box UV region; returns uv origins and the texture height."""
    x = y = shelf = 0
    uvs = []
    for size in cubes:
        cw, ch = box_uv(size)
        if x + cw > width:
            x, y, shelf = 0, y + shelf, 0
        uvs.append((x, y))
        x, shelf = x + cw, max(shelf, ch)
    return uvs, y + shelf


def shade(colour, factor, rng):
    n = rng.randint(-10, 10)
    return tuple(max(0, min(255, int(c * factor) + n)) for c in colour) + (255,)


def paint(img, uv, size, colour, eyes, rng):
    w, h, d = (int(round(v)) for v in size)
    u, v = uv
    faces = [((u + d, v), (w, d), 1.15), ((u + d + w, v), (w, d), 0.7), ((u, v + d), (d, h), 0.85),
             ((u + d, v + d), (w, h), 1.0), ((u + d + w, v + d), (d, h), 0.85), ((u + 2 * d + w, v + d), (w, h), 0.9)]
    for (fx, fy), (fw, fh), light in faces:
        for px in range(fw):
            for py in range(fh):
                edge = 0.85 if px in (0, fw - 1) or py in (0, fh - 1) else 1.0
                img.putpixel((fx + px, fy + py), shade(colour, light * edge, rng))
    if eyes and w >= 4 and h >= 3:
        fx, fy = u + d, v + d
        ey = fy + max(1, h // 3)
        for ex in (fx + w // 4, fx + w - 1 - w // 4):
            img.putpixel((ex, ey), (12, 10, 14, 255))


def geometry(name, spec):
    cubes = [c for b in spec["bones"] for c in b["cubes"]]
    width = 64
    while max(box_uv(s)[0] for _, s in cubes) > width:
        width *= 2
    uvs, height = pack([s for _, s in cubes], width)
    tex_h = 16
    while tex_h < height:
        tex_h *= 2
    rng = random.Random(name)
    img = Image.new("RGBA", (width, tex_h), (0, 0, 0, 0))
    bones, i = [], 0
    for b in spec["bones"]:
        out = {"name": b["name"], "pivot": b["pivot"], "cubes": []}
        if b["parent"]:
            out["parent"] = b["parent"]
        for k, (origin, size) in enumerate(b["cubes"]):
            out["cubes"].append({"origin": origin, "size": size, "uv": list(uvs[i])})
            paint(img, uvs[i], size, b["colour"], b["eyes"] and k == 0, rng)
            i += 1
        bones.append(out)
    tall = max(o[1] + s[1] for o, s in cubes) / 16
    wide = max(max(abs(o[0]), abs(o[0] + s[0]), abs(o[2]), abs(o[2] + s[2])) for o, s in cubes) / 8
    geo = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry." + name, "texture_width": width, "texture_height": tex_h,
                        "visible_bounds_width": round(wide + 1, 1), "visible_bounds_height": round(tall + 1, 1),
                        "visible_bounds_offset": [0, round(tall / 2, 2), 0]},
        "bones": bones}]}
    return geo, img


def main():
    for name, spec in CREATURES.items():
        geo, img = geometry(name, spec)
        for sub, data in (("geckolib/models/entity/" + name + ".geo.json", geo),
                          ("geckolib/animations/entity/" + name + ".animation.json", {"format_version": "1.8.0", "animations": spec["anims"]})):
            path = ROOT / sub
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(data, indent=1) + "\n")
        tex = ROOT / "textures/entity" / (name + ".png")
        tex.parent.mkdir(parents=True, exist_ok=True)
        img.save(tex)
        print(name, img.size)


if __name__ == "__main__":
    main()
