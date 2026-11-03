"""Cockpit gauges (speed, fuel, engine revs) for the Immersive Vehicles packs, so the HUD is not an empty grey panel.
Usage: gen_instruments.py <assets/packid dir> <packid> [top_kmh] [redline_rpm]. Writes textures/instruments.png (1024x1024),
jsondefs/instruments/{speedo,fuel,tacho}.json and adds an `instruments` list + default gauges to every vehicle def lacking one."""
import json, math, os, sys
from PIL import Image, ImageDraw

root, pack = sys.argv[1], sys.argv[2]
TOP_KMH = float(sys.argv[3]) if len(sys.argv) > 3 else 120.0

SIZE = 128
sheet = Image.new("RGBA", (1024, 1024), (0, 0, 0, 0))
d = ImageDraw.Draw(sheet)

def face(ox, oy, ticks, labels):
    c = (ox + 64, oy + 64)
    d.ellipse([ox + 2, oy + 2, ox + 125, oy + 125], fill=(20, 20, 22, 255), outline=(150, 150, 155, 255), width=3)
    for i in range(ticks + 1):
        a = math.radians(225 - 270 * i / ticks)
        r0, r1 = (50, 60) if i % 2 == 0 else (54, 60)
        d.line([c[0] + r0 * math.cos(a), c[1] - r0 * math.sin(a), c[0] + r1 * math.cos(a), c[1] - r1 * math.sin(a)], fill=(235, 235, 235, 255), width=2)
    for i, t in enumerate(labels):
        a = math.radians(225 - 270 * i / (len(labels) - 1))
        d.text((c[0] + 38 * math.cos(a) - 6, c[1] - 38 * math.sin(a) - 5), t, fill=(235, 235, 235, 255))

def needle(ox, oy):
    # pointing straight down-left at rotation 0 is awkward; draw pointing up (-y) and rotate from the 225-degree start
    c = (ox + 64, oy + 64)
    d.polygon([(c[0] - 2, c[1] + 8), (c[0] + 2, c[1] + 8), (c[0], c[1] - 52)], fill=(230, 40, 30, 255))
    d.ellipse([c[0] - 5, c[1] - 5, c[0] + 5, c[1] + 5], fill=(90, 90, 95, 255))

step = max(10, int(round(TOP_KMH / 6 / 10)) * 10)
n = int(TOP_KMH // step)
inst = {
    "speedo": (0, 0, "Speedometer", "velocity", TOP_KMH, n * 2, [str(i * step) for i in range(n + 1)]),
    "fuel": (128, 0, "Fuel gauge", "fuel", 1.0, 4, ["E", "", "1/2", "", "F"]),
    "tacho": (256, 0, "Tachometer", "engine_rpm_1", 1.0, 8, [str(i) for i in range(9)]),
}
defs = {}
for key, (ox, oy, label, var, top, ticks, labels) in inst.items():
    face(ox, oy, ticks, labels)
    needle(ox + 0, oy + 128)
    # the IV "velocity" variable is metres per second (measured: 4.2 shown, about 3.5 blocks/s travelled while accelerating); km/h = m/s * 3.6. Needle sweeps 270 degrees over the scale.
    factor = 270.0 / (top / 3.6) if key == "speedo" else (270.0 if key == "fuel" else 270.0 / 8000.0)
    defs[key] = {
        "general": {"name": label, "materialLists": [["minecraft:iron_ingot:0:1"]]},
        "textureName": "instruments.png",
        "components": [
            {"xCenter": 0, "yCenter": 0, "scale": 1.0, "textureXCenter": ox + 64, "textureYCenter": oy + 64, "textureWidth": 128, "textureHeight": 128},
            {"xCenter": 0, "yCenter": 0, "scale": 1.0, "textureXCenter": ox + 64, "textureYCenter": oy + 192, "textureWidth": 128, "textureHeight": 128,
             "rotationVariable": var, "rotationOffset": -135.0, "rotationFactor": factor, "rotationClampMin": -135.0, "rotationClampMax": 135.0 + 0.0},
        ],
    }
os.makedirs(os.path.join(root, "textures"), exist_ok=True)
sheet.save(os.path.join(root, "textures", "instruments.png"))
os.makedirs(os.path.join(root, "textures", "item", "instruments"), exist_ok=True)
for key, (ox, oy, *_rest) in inst.items():
    sheet.crop((ox, oy, ox + 128, oy + 128)).resize((32, 32), Image.LANCZOS).save(os.path.join(root, "textures", "item", "instruments", key + ".png"))
os.makedirs(os.path.join(root, "models", "item", "instruments"), exist_ok=True)
for key in inst:
    json.dump({"parent": "mts:item/basic", "textures": {"layer0": f"{pack}:item/instruments/{key}"}}, open(os.path.join(root, "models", "item", "instruments", key + ".json"), "w"), indent=1)
os.makedirs(os.path.join(root, "jsondefs", "instruments"), exist_ok=True)
for k, v in defs.items():
    json.dump(v, open(os.path.join(root, "jsondefs", "instruments", k + ".json"), "w"), indent=1)

layout = [("speedo", 78, 40), ("fuel", 190, 40), ("tacho", 302, 40)]
vdir = os.path.join(root, "jsondefs", "vehicles")
for f in sorted(os.listdir(vdir)):
    p = os.path.join(vdir, f)
    v = json.load(open(p))
    if v.get("instruments"):
        continue
    v["instruments"] = [
        {"pos": [0, 1.0, 0.8], "rot": [0, 0, 0], "scale": 0.1, "hudX": x, "hudY": y, "hudScale": 0.85, "defaultInstrument": f"{pack}:{k}"} for k, x, y in layout]
    v.get("motorized", {}).pop("instruments", None)
    json.dump(v, open(p, "w"), indent=1)
    print("gauges ->", f)
