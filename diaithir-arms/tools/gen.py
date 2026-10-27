"""Writes everything data-driven for diaithir-arms: arms.json, item definitions, models, textures, equipment assets, lang,
recipes, tags, age_gates.json, names_map.json. Textures are made by script from vanilla sprites (recoloured by luminance
ramps, worn edges, grain, scratches, per-material details); nothing else is copied.
Run: python3 -I tools/gen.py <dir holding the extracted vanilla 'assets/minecraft/textures' tree>
"""
import colorsys, json, os, random, shutil, sys
import numpy as np
from PIL import Image

VAN = os.path.join(sys.argv[1], "assets/minecraft/textures")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
NS = "diaithirarms"
AS = os.path.join(RES, "assets", NS)
DA = os.path.join(RES, "data", NS)


def W(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)


# ---------------------------------------------------------------- content tables
# key, gate (Age key in tech_ages.json), library age (1..7; Rodos and the Future are filed under 7), adjective, ingredient
SLOTS = ["helmet", "chestplate", "leggings", "boots"]
ARMOR = [
    # key  gate    lib adj          ingredient                       dur boots legs chest helm ench tough kb  sound     lore
    ("bone", "I", 1, "knàim", "diaithirarms:bone_plate", 8, 1, 2, 3, 1, 12, 0.0, 0.0, "leather"),
    ("copper", "II", 2, "uma", "#c:ingots/copper", 11, 1, 3, 4, 2, 8, 0.0, 0.0, "copper"),
    ("bronze", "III", 3, "krèuma", "#c:ingots/bronze", 14, 1, 4, 5, 2, 10, 0.0, 0.0, "iron"),
    ("mail", "IV", 4, "iarund", "#c:ingots/iron", 17, 2, 5, 6, 2, 9, 0.0, 0.0, "chain"),
    ("steel", "V", 5, "kruoid", "#c:ingots/steel", 24, 3, 6, 7, 3, 10, 1.0, 0.0, "iron"),
    ("ballistic", "VI", 6, "snàitlech", "diaithirarms:ballistic_weave", 28, 3, 6, 8, 3, 12, 1.5, 0.05, "leather"),
    ("composite", "VII", 7, "kriadach", "diaithirarms:ceramic_plate", 36, 4, 7, 9, 4, 14, 2.5, 0.05, "diamond"),
    ("rodos", "RODOS", 7, "kumask", "diaithirarms:rodos_alloy_ingot", 46, 4, 8, 10, 4, 16, 3.5, 0.10, "netherite"),
    ("future", "RODOS", 7, "riondag", "diaithirarms:star_lattice", 60, 5, 9, 11, 5, 18, 4.5, 0.15, "diamond"),
]
# tool-like materials: key, gate, lib, adjective, ingredient, durability(tool), speed, bonus, enchant, incorrect tag,
#                      bow dur, bow mult, crossbow dur (0 = none), crossbow mult, shield dur, shield block factor
TOOLS = [
    ("bone", "I", 1, "knàim", "diaithirarms:bone_plate", 100, 2.0, 0.0, 12, "iron", 300, 0.90, 0, 0, 160, 0.80),
    ("copper", "II", 2, "uma", "#c:ingots/copper", 190, 5.0, 1.0, 13, "iron", 330, 0.95, 0, 0, 260, 0.85),
    ("bronze", "III", 3, "krèuma", "#c:ingots/bronze", 320, 5.75, 2.0, 12, "iron", 384, 1.00, 0, 0, 330, 0.90),
    ("mail", "IV", 4, "iarund", "#c:ingots/iron", 250, 6.0, 2.0, 14, "iron", 420, 1.05, 465, 1.15, 420, 0.95),
    ("steel", "V", 5, "kruoid", "#c:ingots/steel", 900, 7.0, 3.0, 12, "diamond", 560, 1.15, 600, 1.30, 600, 1.00),
    ("tempered", "VI", 6, "doirbi", "diaithirarms:tempered_ingot", 1300, 8.0, 3.5, 12, "diamond", 700, 1.25, 750, 1.45, 800, 1.00),
    ("composite", "VII", 7, "mieskaichti", "diaithirarms:ceramic_plate", 1900, 9.0, 4.0, 14, "netherite", 900, 1.35, 950, 1.60, 1100, 1.00),
    ("rodos", "RODOS", 7, "kumask", "diaithirarms:rodos_alloy_ingot", 2900, 10.0, 5.0, 16, "netherite", 1400, 1.50, 1500, 1.80, 1700, 1.00),
    ("future", "RODOS", 7, "riondag", "diaithirarms:star_lattice", 4200, 12.0, 6.5, 18, "netherite", 2000, 1.70, 2100, 2.00, 2600, 1.00),
]
# ballistic armour shares the 'ballistic' tool material for its bow, shield and repair needs (ballistic_weave); the VI weapon
# tier is 'tempered'. Both are listed so each id has a material.
EXTRA_TOOL = ("ballistic", "VI", 6, "snàitlech", "diaithirarms:ballistic_weave", 700, 8.0, 3.5, 12, "diamond", 0, 0, 0, 0, 0, 0)
MELEE_TIERS = ["steel", "tempered", "composite", "rodos", "future"]
# spear tuning: attackDuration, damageMultiplier, delay, dismountTime, dismountThr, knockbackTime, knockbackThr, damageTime, damageThr
SPEAR = {
    "steel": [1.00, 1.00, 0.55, 2.8, 10.5, 6.0, 5.1, 10.5, 4.6],
    "tempered": [1.05, 1.06, 0.50, 2.7, 10.0, 5.8, 5.1, 10.0, 4.6],
    "composite": [1.10, 1.12, 0.45, 2.6, 9.5, 5.6, 5.1, 9.5, 4.6],
    "rodos": [1.15, 1.22, 0.40, 2.5, 9.0, 5.4, 5.1, 8.75, 4.6],
    "future": [1.20, 1.35, 0.33, 2.3, 8.5, 5.0, 5.1, 8.0, 4.6],
}
SLOT_NAME = {"helmet": "Klogaid", "chestplate": "Broildech", "leggings": "Kas", "boots": "Bòtund"}
SLOT_EN = {"helmet": "Helmet", "chestplate": "Chestplate", "leggings": "Leggings", "boots": "Boots"}
KIND_NAME = {"sword": "Klaidem", "axe": "Tuog", "spear": "Slieg", "bow": "Umlaig", "crossbow": "Umlaig kroisi", "shield": "Skiad"}
KIND_EN = {"sword": "Sword", "axe": "Axe", "spear": "Spear", "bow": "Bow", "crossbow": "Crossbow", "shield": "Shield"}
EN_MAT = {"bone": "Bone", "copper": "Copper", "bronze": "Bronze", "mail": "Iron Mail", "steel": "Steel", "ballistic": "Ballistic",
          "tempered": "Tempered", "composite": "Composite", "rodos": "Rodos Alloy", "future": "Future"}
PARTS = {  # id: (Diaithiris, English)
    "bone_plate": ("Troindser knàim", "Bone Plate"),
    "ballistic_weave": ("Fieg snàitlech", "Ballistic Weave"),
    "ceramic_plate": ("Troindser kriadach", "Ceramic Plate"),
    "tempered_ingot": ("Kruoid doirbi", "Tempered Ingot"),
    "rodos_alloy_ingot": ("Kumask Rodos", "Rodos Alloy Ingot"),
    "star_lattice": ("Fieg riondag", "Star Lattice"),
}

# ---------------------------------------------------------------- colour ramps (dark to light)
RAMPS = {
    "hide": [(44, 28, 18), (84, 55, 33), (122, 83, 50), (158, 112, 70), (192, 147, 100)],
    "bone": [(128, 116, 94), (176, 164, 138), (212, 202, 178), (234, 226, 204), (250, 245, 228)],
    "copper": [(74, 34, 20), (140, 70, 38), (192, 100, 54), (224, 140, 86), (244, 186, 138)],
    "bronze": [(62, 40, 18), (114, 78, 32), (164, 118, 50), (204, 160, 80), (234, 204, 124)],
    "iron": [(26, 26, 30), (54, 55, 60), (90, 92, 98), (130, 133, 140), (176, 179, 186)],
    "steel": [(34, 40, 48), (76, 84, 94), (120, 130, 140), (170, 180, 190), (218, 226, 232)],
    "olive": [(24, 30, 20), (48, 58, 36), (78, 90, 56), (110, 122, 80), (146, 156, 108)],
    "carbon": [(12, 14, 18), (30, 34, 40), (54, 60, 68), (92, 100, 108), (146, 154, 160)],
    "ceramic": [(96, 100, 104), (150, 154, 156), (196, 198, 196), (226, 226, 220), (246, 245, 238)],
    "rodos": [(18, 40, 52), (34, 88, 100), (64, 140, 142), (150, 190, 160), (232, 226, 176)],
    "rodos2": [(60, 30, 20), (120, 70, 36), (176, 120, 56), (220, 176, 84), (248, 224, 140)],
    "future": [(52, 64, 92), (112, 130, 164), (178, 196, 222), (224, 234, 248), (252, 254, 255)],
    "wood": [(42, 28, 16), (82, 56, 32), (118, 82, 48), (150, 108, 66), (180, 136, 90)],
    "yew": [(70, 38, 22), (110, 62, 34), (146, 90, 50), (178, 122, 70), (206, 158, 100)],
    "pale": [(96, 80, 56), (140, 118, 82), (178, 154, 112), (208, 186, 144), (232, 214, 176)],
    "tempered": [(40, 42, 58), (82, 84, 112), (126, 120, 140), (170, 150, 130), (214, 190, 140)],
}
TIER_RAMP = {"bone": "bone", "copper": "copper", "bronze": "bronze", "mail": "iron", "steel": "steel", "ballistic": "olive",
             "tempered": "tempered", "composite": "carbon", "rodos": "rodos", "future": "future"}
TIER_WOOD = {"bone": "pale", "copper": "yew", "bronze": "yew", "mail": "wood", "steel": "wood", "tempered": "wood",
             "composite": "carbon", "rodos": "wood", "future": "ceramic"}


def ramp_at(name, t):
    st = RAMPS[name]
    t = np.clip(t, 0, 1) * (len(st) - 1)
    i = np.minimum(t.astype(int), len(st) - 2)
    f = (t - i)[..., None]
    a = np.array(st, dtype=float)
    return a[i] * (1 - f) + a[i + 1] * f


# ---------------------------------------------------------------- texture engine
def load(rel):
    return Image.open(os.path.join(VAN, rel)).convert("RGBA")


def arr(im):
    return np.array(im, dtype=float)


def neighbours(mask):
    p = np.pad(mask, 1, constant_values=False)
    return {"l": p[1:-1, :-2], "r": p[1:-1, 2:], "u": p[:-2, 1:-1], "d": p[2:, 1:-1]}


def interior(mask):
    n = neighbours(mask)
    m = mask & n["l"] & n["r"] & n["u"] & n["d"]
    return list(zip(*np.nonzero(m)))


def luminance(a):
    return (0.299 * a[..., 0] + 0.587 * a[..., 1] + 0.114 * a[..., 2]) / 255.0


def normalise(L, mask):
    v = L[mask]
    if v.size == 0:
        return L
    lo, hi = np.percentile(v, 2), np.percentile(v, 98)
    if hi - lo < 1e-3:
        return np.full_like(L, 0.5)
    return np.clip((L - lo) / (hi - lo), 0, 1)


def shade(src, ramp, rng, grain=0.05, edge=0.14, ao=0.10, scratches=2, contrast=1.0, mask_sel=None, base=None):
    """Recolour src by luminance through a ramp. base: existing RGBA array to write onto (for partial recolours)."""
    a = arr(src)
    mask = a[..., 3] > 0
    sel = mask if mask_sel is None else (mask & mask_sel)
    L = normalise(luminance(a), mask)
    L = np.clip((L - 0.5) * contrast + 0.5, 0, 1)
    n = neighbours(mask)
    L = L + rng.uniform(-grain, grain, L.shape)
    L = L + edge * (~n["l"] | ~n["u"]) - ao * (~n["r"] | ~n["d"])
    h, w = L.shape
    pts = interior(sel)
    for _ in range(scratches * max(1, (h * w) // 256)):
        if not pts:
            break
        y, x = pts[rng.integers(len(pts))]
        for k in range(rng.integers(2, 5)):
            yy, xx = y + k, x + k
            if 0 <= yy < h and 0 <= xx < w and sel[yy, xx]:
                L[yy, xx] += 0.12
    rgb = ramp_at(ramp, L)
    out = (arr(src).copy() if base is None else base.copy())
    out[sel, :3] = rgb[sel]
    out[sel, 3] = a[sel, 3]
    return out


def save(a, rel):
    path = os.path.join(AS, "textures", rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA").save(path)


def put(a, y, x, rgb):
    a[y, x, :3] = rgb
    a[y, x, 3] = 255


# ---- per-material details, applied after shading; each takes (a, rng, kind)
def det_bone(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    # lashing: dark stitch pixels in diagonal runs
    for (y, x) in interior(mask)[:: max(3, len(interior(mask)) // 18 or 3)]:
        put(a, y, x, RAMPS["hide"][0])


def det_copper(a, rng):
    mask = a[..., 3] > 0
    pts = interior(mask)
    for _ in range(max(2, len(pts) // 40)):  # verdigris flecks in the recesses
        if pts:
            y, x = pts[rng.integers(len(pts))]
            put(a, y, x, (72, 142, 112))


def det_bronze(a, rng):
    mask = a[..., 3] > 0
    pts = interior(mask)
    if len(pts) > 12:  # La Tene enamel studs, deep red and blue (STYLE 11)
        ys = [p[0] for p in pts]
        xs = [p[1] for p in pts]
        cy, cx = int(np.mean(ys)), int(np.mean(xs))
        cands = sorted(pts, key=lambda p: (p[0] - cy) ** 2 + (p[1] - cx) ** 2)
        put(a, cands[0][0], cands[0][1], (150, 36, 30))
        far = [p for p in cands if abs(p[0] - cands[0][0]) + abs(p[1] - cands[0][1]) >= 4]
        if far:
            put(a, far[0][0], far[0][1], (34, 62, 130))
    for _ in range(max(1, len(pts) // 60)):
        y, x = pts[rng.integers(len(pts))]
        put(a, y, x, (92, 120, 84))


def det_mail(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x] and (x + y) % 2 == 0:
                a[y, x, :3] *= 0.86  # ring pattern
    pts = interior(mask)
    for _ in range(max(2, len(pts) // 30)):
        y, x = pts[rng.integers(len(pts))]
        put(a, y, x, (116, 64, 34))  # rust bloom in the recesses


def det_steel(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    for x in range(w):
        if x % 3 == 0:
            for y in range(h):
                if mask[y, x]:
                    a[y, x, :3] *= 1.05  # brushed grain
    pts = interior(mask)
    for _ in range(max(2, len(pts) // 50)):
        y, x = pts[rng.integers(len(pts))]
        put(a, y, x, (232, 238, 242))
        if x + 1 < w and mask[y, x + 1]:
            put(a, y, x + 1, (40, 46, 54))  # rivet with its shadow


def det_ballistic(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x] and (x % 4 == 0 or y % 4 == 0):
                a[y, x, :3] *= 0.80  # webbing grid


def det_composite(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                a[y, x, :3] *= 1.10 if ((x // 2) + (y // 2)) % 2 == 0 else 0.92  # carbon twill
    pts = interior(mask)
    for _ in range(max(2, len(pts) // 36)):  # ceramic insert tiles
        y, x = pts[rng.integers(len(pts))]
        for dy in (0, 1):
            for dx in (0, 1):
                if y + dy < h and x + dx < w and mask[y + dy, x + dx]:
                    put(a, y + dy, x + dx, RAMPS["ceramic"][3 - dy])


def det_rodos(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    L = normalise(luminance(a), mask)
    warm = ramp_at("rodos2", L)
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                t = ((x * 7 + y * 5) % 31) / 31.0
                if t > 0.62:  # bands of warm gold through the teal
                    a[y, x, :3] = 0.35 * a[y, x, :3] + 0.65 * warm[y, x]


def det_future(a, rng):
    mask = a[..., 3] > 0
    h, w = mask.shape
    pts = interior(mask)
    if not pts:
        return
    for y in range(h):
        for x in range(w):
            if mask[y, x] and (x - y) % 7 == 0 and rng.random() < 0.8:
                put(a, y, x, (92, 226, 252))  # lattice lines, cold light


DET = {"bone": det_bone, "copper": det_copper, "bronze": det_bronze, "mail": det_mail, "steel": det_steel,
       "ballistic": det_ballistic, "composite": det_composite, "rodos": det_rodos, "future": det_future}

# armour: tier -> (item source prefix, layer source name, ramp, contrast)
ARM_SRC = {
    "bone": ("leather", "leather", "hide", 1.0),
    "copper": ("copper", "copper", "copper", 1.0),
    "bronze": ("copper", "copper", "bronze", 1.0),
    "mail": ("chainmail", "chainmail", "iron", 1.1),
    "steel": ("iron", "iron", "steel", 1.1),
    "ballistic": ("leather", "leather", "olive", 1.0),
    "composite": ("diamond", "diamond", "carbon", 1.1),
    "rodos": ("netherite", "netherite", "rodos", 1.2),
    "future": ("diamond", "diamond", "future", 1.0),
}


def merged_leather(rel_base, slot=None):
    """leather item sprite: base plus its overlay, so the strap detail survives."""
    base = load(rel_base + ".png")
    ov = rel_base + "_overlay.png"
    if os.path.exists(os.path.join(VAN, ov)):
        o = load(ov)
        base = Image.alpha_composite(base, o)
    return base


def make_armor_item(key, slot, rng):
    pre, _, ramp, contrast = ARM_SRC[key]
    rel = f"item/{pre}_{slot}"
    src = merged_leather(rel) if pre == "leather" else load(rel + ".png")
    a = shade(src, ramp, rng, contrast=contrast)
    if key == "bone":  # bone plates: checker of bone on the hide
        mask = a[..., 3] > 0
        h, w = mask.shape
        for y in range(h):
            for x in range(w):
                if mask[y, x] and ((x // 3) + (y // 2)) % 2 == 0 and luminance(a)[y, x] > 0.25:
                    t = luminance(a)[y, x]
                    a[y, x, :3] = ramp_at("bone", np.array(min(1.0, t * 1.2 + 0.1)))
    DET[key](a, rng)
    save(a, f"item/{key}_{slot}.png")


def make_armor_layers(key, rng):
    _, lay, ramp, contrast = ARM_SRC[key]
    for folder in ("humanoid", "humanoid_leggings", "humanoid_baby"):
        p = f"entity/equipment/{folder}/{lay}.png"
        if not os.path.exists(os.path.join(VAN, p)):
            continue
        src = load(p)
        if lay == "leather":
            ov = f"entity/equipment/{folder}/leather_overlay.png"
            if os.path.exists(os.path.join(VAN, ov)):
                src = Image.alpha_composite(src, load(ov))
        a = shade(src, ramp, rng, contrast=contrast, grain=0.04, scratches=3)
        if key == "bone":
            mask = a[..., 3] > 0
            h, w = mask.shape
            Ln = luminance(a)
            for y in range(h):
                for x in range(w):
                    if mask[y, x] and ((x // 3) + (y // 3)) % 2 == 0 and Ln[y, x] > 0.2:
                        a[y, x, :3] = ramp_at("bone", np.array(min(1.0, Ln[y, x] * 1.3 + 0.1)))
        if key == "ballistic" and folder == "humanoid":
            # the helmet block (top 16 rows, left 32 columns of the adult sheet) is a painted steel pot
            hh = 16 if a.shape[0] == 32 else 8
            sub = a[:hh, :32]
            msk = sub[..., 3] > 0
            Lh = normalise(luminance(sub), msk)
            sub[msk, :3] = ramp_at("steel", Lh * 0.7)[msk] * np.array([0.55, 0.62, 0.45]) + 40
        DET[key](a, rng)
        save(a, f"entity/equipment/{folder}/{key}.png")


def classify_wood(a):
    mask = a[..., 3] > 0
    h, w = mask.shape
    wood = np.zeros(mask.shape, bool)
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                r, g, b = a[y, x, :3] / 255.0
                hh, s, v = colorsys.rgb_to_hsv(r, g, b)
                if s > 0.25 and 0.02 < hh < 0.14:
                    wood[y, x] = True
    return mask, wood


def two_tone(src, key, rng, metal_ramp=None, wood_ramp=None, keep_string=False):
    a = arr(src)
    mask, wood = classify_wood(a)
    out = shade(src, metal_ramp or TIER_RAMP[key], rng, mask_sel=~wood, scratches=2)
    out = shade(src, wood_ramp or TIER_WOOD[key], rng, mask_sel=wood, base=out, scratches=1, grain=0.04)
    if keep_string:
        L = luminance(a)
        S = np.array([[colorsys.rgb_to_hsv(*(a[y, x, :3] / 255.0))[1] for x in range(a.shape[1])] for y in range(a.shape[0])])
        st = mask & (L > 0.62) & (S < 0.12)
        out[st, :3] = a[st, :3]
    return out


def make_melee(key, rng):
    for kind in ("sword", "axe"):
        out = two_tone(load(f"item/iron_{kind}.png"), key, rng, wood_ramp="wood")
        DET[key if key in DET else "steel"](out, rng) if key != "tempered" else det_steel(out, rng)
        save(out, f"item/{key}_{kind}.png")
    for suffix in ("", "_in_hand"):
        out = two_tone(load(f"item/iron_spear{suffix}.png"), key, rng, wood_ramp="wood")
        save(out, f"item/{key}_spear{suffix}.png")


def make_ranged(key, rng):
    kk = key
    for nm in ("bow", "bow_pulling_0", "bow_pulling_1", "bow_pulling_2"):
        out = two_tone(load(f"item/{nm}.png"), kk, rng, metal_ramp=TIER_RAMP[kk], wood_ramp=("wood" if kk not in ("composite", "future") else TIER_WOOD[kk]), keep_string=True)
        save(out, f"item/{key}_{nm}.png")
    if tool_row(key)[12] > 0:
        for nm in ("crossbow_standby", "crossbow_pulling_0", "crossbow_pulling_1", "crossbow_pulling_2", "crossbow_arrow", "crossbow_firework"):
            out = two_tone(load(f"item/{nm}.png"), kk, rng, keep_string=True)
            save(out, f"item/{key}_{nm}.png")


def tool_row(key):
    for t in TOOLS + [EXTRA_TOOL]:
        if t[0] == key:
            return t


SHIELD_HEATER = [11, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 8, 6, 4, 2]
SHIELD_RECT = [10] * 15


def make_shield(key, rng):
    a = np.zeros((16, 16, 4))
    rect = key in ("tempered", "composite", "future")
    prof = SHIELD_RECT if rect else SHIELD_HEATER
    for i, wd in enumerate(prof):
        y = i
        x0 = 8 - wd // 2
        for x in range(x0, x0 + wd):
            if 0 <= y < 16:
                a[y, x] = (0, 0, 0, 255)
    mask = a[..., 3] > 0
    n = neighbours(mask)
    rim = mask & ~(n["l"] & n["r"] & n["u"] & n["d"])
    field_ramp = {"bone": "hide", "copper": "wood", "bronze": "wood", "mail": "wood", "steel": "steel", "tempered": "carbon",
                  "composite": "carbon", "rodos": "rodos", "future": "future"}[key]
    rim_ramp = {"bone": "bone", "copper": "copper", "bronze": "bronze", "mail": "iron", "steel": "steel", "tempered": "carbon",
                "composite": "ceramic", "rodos": "rodos2", "future": "ceramic"}[key]
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                t = 0.55 + 0.35 * (1 - abs(x - 7.5) / 7.5) - 0.12 * (y / 15.0) + rng.uniform(-0.05, 0.05)
                if rim[y, x]:
                    c = ramp_at(rim_ramp, np.array(0.45 + 0.4 * (1 if (x < 8 and y < 8) else 0) + rng.uniform(-0.06, 0.06)))
                else:
                    c = ramp_at(field_ramp, np.array(t))
                    if field_ramp == "wood" and x % 3 == 0:
                        c = c * 0.82  # plank joints
                a[y, x, :3] = c
    # boss
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            y, x = 6 + dy, 8 + dx
            if mask[y, x]:
                t = 0.75 - 0.2 * (dy + dx > 0)
                a[y, x, :3] = ramp_at(rim_ramp, np.array(t))
    if key == "bronze":
        put(a, 3, 5, (150, 36, 30)); put(a, 3, 10, (34, 62, 130)); put(a, 11, 8, (150, 36, 30))
    if key == "bone":
        for yy in (3, 9):
            for xx in (4, 11):
                if mask[yy, xx]:
                    put(a, yy, xx, RAMPS["bone"][3])
    if key == "tempered":  # riot shield: clear visor panel behind a dark frame
        for y in range(2, 10):
            for x in range(5, 11):
                if mask[y, x] and not rim[y, x]:
                    a[y, x, :3] = (130, 170, 190) if (x + y) % 2 == 0 else (150, 188, 206)
    if key == "composite":
        for y in range(h):
            for x in range(w):
                if mask[y, x] and not rim[y, x] and (x // 2 + y // 2) % 2 == 0:
                    a[y, x, :3] = np.clip(a[y, x, :3] * 1.25, 0, 255)
    if key == "future":
        for y in range(h):
            for x in range(w):
                if mask[y, x] and not rim[y, x] and (x - y) % 5 == 0:
                    put(a, y, x, (92, 226, 252))
    if key == "rodos":
        for y in range(h):
            for x in range(w):
                if mask[y, x] and not rim[y, x] and ((x - 8) ** 2 + (y - 6) ** 2) in (13, 14, 16, 17):
                    a[y, x, :3] = ramp_at("rodos2", np.array(0.7))
    if key == "steel":  # centre ridge
        for y in range(h):
            if mask[y, 8]:
                a[y, 8, :3] = ramp_at("steel", np.array(0.92));
    # outline darkening
    for y in range(h):
        for x in range(w):
            if rim[y, x] and (not n["r"][y, x] or not n["d"][y, x]):
                a[y, x, :3] *= 0.7
    save(a, f"item/{key}_shield.png")


def sprite_from(src_rel, ramp, rng, key, extra=None):
    out = shade(load(src_rel), ramp, rng, contrast=1.2)
    if extra:
        extra(out, rng)
    return out


def make_parts(rng):
    # bone plate: three bone slabs lashed with hide cord
    a = np.zeros((16, 16, 4))
    for i, (y0, x0) in enumerate([(3, 2), (6, 5), (9, 8)]):
        for y in range(y0, y0 + 4):
            for x in range(x0, x0 + 6):
                t = 0.6 + 0.3 * ((x - x0) / 6) - 0.1 * (y - y0) / 3 + rng.uniform(-0.05, 0.05)
                a[y, x] = (*ramp_at("bone", np.array(t)), 255)
        for x in range(x0, x0 + 6):
            a[y0 + 3, x, :3] *= 0.7
    for (y, x) in [(5, 6), (8, 9), (11, 12), (5, 3), (8, 6), (11, 9)]:
        put(a, y, x, RAMPS["hide"][1])
    save(a, "item/bone_plate.png")
    # ballistic weave: olive folded cloth with a woven grid
    a = np.zeros((16, 16, 4))
    for y in range(3, 14):
        for x in range(2, 14):
            t = 0.45 + 0.25 * (((x // 2) + (y // 2)) % 2) + rng.uniform(-0.05, 0.05) - 0.1 * (y - 3) / 11
            a[y, x] = (*ramp_at("olive", np.array(t)), 255)
    for x in range(2, 14):
        a[8, x, :3] *= 0.7
    for y in range(3, 14):
        a[y, 8, :3] *= 0.8
    save(a, "item/ballistic_weave.png")
    # ceramic plate: pale tile with hex-ish facets and a chipped corner
    a = np.zeros((16, 16, 4))
    for y in range(2, 14):
        for x in range(3, 13):
            if (x, y) in ((3, 2), (12, 13)):
                continue
            t = 0.62 + 0.18 * (((x // 3) + (y // 3)) % 2) + rng.uniform(-0.04, 0.04) - 0.15 * (y - 2) / 12
            a[y, x] = (*ramp_at("ceramic", np.array(t)), 255)
    for y in range(2, 14):
        a[y, 12, :3] *= 0.75
    for x in range(3, 13):
        a[13, x, :3] *= 0.75
    save(a, "item/ceramic_plate.png")
    # ingots: vanilla ingot silhouette recoloured
    for nm, src, ramp, ex in (("tempered_ingot", "item/iron_ingot.png", "tempered", None),
                              ("rodos_alloy_ingot", "item/netherite_ingot.png", "rodos", det_rodos)):
        out = sprite_from(src, ramp, rng, nm, ex)
        save(out, f"item/{nm}.png")
    # star lattice: a diamond-shaped lattice of pale threads with cyan nodes
    a = np.zeros((16, 16, 4))
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) + abs(y - 7.5)
            if d <= 6.5:
                t = 0.75 - d / 14 + rng.uniform(-0.04, 0.04)
                a[y, x] = (*ramp_at("future", np.array(t)), 255)
                if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                    a[y, x, :3] *= 0.78
    for (y, x) in [(7, 7), (8, 8), (4, 8), (11, 7), (7, 4), (8, 11)]:
        put(a, y, x, (92, 226, 252))
    save(a, "item/star_lattice.png")


# ---------------------------------------------------------------- build all
def main():
    rng = np.random.default_rng(26)
    # fresh output
    for d in (os.path.join(AS), os.path.join(DA)):
        shutil.rmtree(d, ignore_errors=True)
    items = []
    lang, names = {}, {}
    gates_items = {}
    armor_mats, tool_mats = {}, {}
    for (key, gate, lib, adj, ing, dur, b, l, c, hm, ench, tough, kb, snd) in ARMOR:
        armor_mats[key] = dict(durability=dur, boots=b, legs=l, chest=c, helmet=hm, enchant=ench, sound=snd,
                               toughness=tough, knockback=kb, repair=f"{NS}:repair/{key}")
        for slot in SLOTS:
            iid = f"{key}_{slot}"
            items.append(dict(id=iid, kind="armor", mat=key, slot=slot, damage=0, speed=0, mult=0, shield=0, spear=[], durability=0))
            dn = f"{SLOT_NAME[slot]} {adj}"
            lang[f"item.{NS}.{iid}"] = dn
            names[iid] = dict(diaithiris=dn, english=f"{EN_MAT[key]} {SLOT_EN[slot]}")
            gates_items[f"{NS}:{iid}"] = gate
    for t in TOOLS + [EXTRA_TOOL]:
        key, gate, lib, adj, ing, dur, spd, bonus, ench, inc, bdur, bmult, cdur, cmult, sdur, sblock = t
        tool_mats[key] = dict(incorrect=inc, durability=dur, speed=spd, bonus=bonus, enchant=ench, repair=f"{NS}:repair/{key}")
        if key == "ballistic":
            continue
        def add(kind, damage=0, speed=0, mult=0, shield=0, spear=None, durability=0):
            iid = f"{key}_{kind}"
            items.append(dict(id=iid, kind=kind, mat=key, slot="", damage=damage, speed=speed, mult=mult, shield=shield,
                              spear=spear or [], durability=durability))
            dn = f"{KIND_NAME[kind]} {adj}"
            lang[f"item.{NS}.{iid}"] = dn
            names[iid] = dict(diaithiris=dn, english=f"{EN_MAT[key]} {KIND_EN[kind]}")
            gates_items[f"{NS}:{iid}"] = gate
        if key in MELEE_TIERS:
            add("sword", 3.0, -2.4)
            add("axe", 6.0, -3.1)
            add("spear", spear=SPEAR[key])
        add("bow", mult=bmult, durability=bdur)
        if cdur:
            add("crossbow", mult=cmult, durability=cdur)
        add("shield", shield=sblock, durability=sdur)
    # the ballistic bow/shield belong to tier VI too: reuse 'tempered' entries (VI) - ballistic only owns armour and weave
    for pid, (dn, en) in PARTS.items():
        lang[f"item.{NS}.{pid}"] = dn
        names[pid] = dict(diaithiris=dn, english=en)
        gates_items[f"{NS}:{pid}"] = {"bone_plate": "I", "ballistic_weave": "VI", "ceramic_plate": "VII", "tempered_ingot": "VI",
                                      "rodos_alloy_ingot": "RODOS", "star_lattice": "RODOS"}[pid]
    W(os.path.join(DA, "arms.json"), dict(armor=armor_mats, tools=tool_mats, items=items, parts=list(PARTS)))
    W(os.path.join(AS, "lang", "en_us.json"), lang)
    W(os.path.join(ROOT, "names_map.json"), names)
    W(os.path.join(ROOT, "age_gates.json"), {
        "_doc": "Same shape as DiaithirRPG content/tech_ages.json: namespaces and per-item entries (items beat namespaces). Hide and bone I, copper II, bronze III, iron mail IV, steel V, tempered alloy and ballistic weave VI, ceramic composite VII, Rodos alloy and the Future ring-lattice RODOS (the Future follows Rodos and has no Age key of its own yet, so it shares RODOS).",
        "namespaces": {NS: "I"}, "items": gates_items})

    # ---- textures
    for t in ARMOR:
        key = t[0]
        for slot in SLOTS:
            make_armor_item(key, slot, rng)
        make_armor_layers(key, rng)
    for t in TOOLS:
        key = t[0]
        if key in MELEE_TIERS:
            make_melee(key, rng)
        make_ranged(key, rng)
        make_shield(key, rng)
    # ballistic (VI armour) shares no weapon sprites; the VI weapon tier is 'tempered'
    make_parts(rng)

    # ---- equipment assets
    for t in ARMOR:
        key = t[0]
        lay = lambda: [{"texture": f"{NS}:{key}"}]
        W(os.path.join(AS, "equipment", f"{key}.json"), {"layers": {"humanoid": lay(), "humanoid_leggings": lay(), "humanoid_baby": lay()}})

    # ---- models and item definitions
    def model(name, obj):
        W(os.path.join(AS, "models", "item", f"{name}.json"), obj)

    def idef(name, obj):
        W(os.path.join(AS, "items", f"{name}.json"), {"model": obj})

    def m(name):
        return {"type": "minecraft:model", "model": f"{NS}:item/{name}"}

    for it in items:
        iid, kind = it["id"], it["kind"]
        if kind in ("armor", "sword", "axe", "shield"):
            model(iid, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
            idef(iid, m(iid))
            if kind in ("sword", "axe"):
                model(iid, {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/{iid}"}})
        elif kind == "spear":
            model(iid, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
            model(iid + "_in_hand", {"parent": "minecraft:item/spear_in_hand", "textures": {"layer0": f"{NS}:item/{iid}_in_hand"}})
            W(os.path.join(AS, "items", f"{iid}.json"), {
                "model": {"type": "minecraft:select", "property": "minecraft:display_context",
                          "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": m(iid)}], "fallback": m(iid + "_in_hand")},
                "swap_animation_scale": 1.95})
        elif kind == "bow":
            for i, nm in enumerate(("bow", "bow_pulling_0", "bow_pulling_1", "bow_pulling_2")):
                parent = "minecraft:item/bow" if nm == "bow" else f"minecraft:item/{nm}"
                model(f"{iid[:-3]}{nm}", {"parent": parent, "textures": {"layer0": f"{NS}:item/{iid[:-3]}{nm}"}})
            p = iid[:-3]
            idef(iid, {"type": "minecraft:condition", "property": "minecraft:using_item", "on_false": m(p + "bow"),
                       "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 0.05,
                                   "entries": [{"threshold": 0.65, "model": m(p + "bow_pulling_1")},
                                               {"threshold": 0.9, "model": m(p + "bow_pulling_2")}],
                                   "fallback": m(p + "bow_pulling_0")}})
        elif kind == "crossbow":
            p = iid[:-8]
            for nm in ("crossbow_standby", "crossbow_pulling_0", "crossbow_pulling_1", "crossbow_pulling_2", "crossbow_arrow", "crossbow_firework"):
                parent = "minecraft:item/crossbow"
                model(p + nm, {"parent": parent, "textures": {"layer0": f"{NS}:item/{p}{nm}"}})
            idef(iid, {"type": "minecraft:select", "property": "minecraft:charge_type",
                       "cases": [{"when": "arrow", "model": m(p + "crossbow_arrow")}, {"when": "rocket", "model": m(p + "crossbow_firework")}],
                       "fallback": {"type": "minecraft:condition", "property": "minecraft:using_item", "on_false": m(p + "crossbow_standby"),
                                    "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:crossbow/pull",
                                                "entries": [{"threshold": 0.58, "model": m(p + "crossbow_pulling_1")},
                                                            {"threshold": 1.0, "model": m(p + "crossbow_pulling_2")}],
                                                "fallback": m(p + "crossbow_pulling_0")}}})
    # shields: flat sprite held in the off hand
    for it in items:
        if it["kind"] == "shield":
            model(it["id"], {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{it['id']}"},
                             "display": {
                                 "firstperson_lefthand": {"rotation": [0, -90, 0], "translation": [2, 3, 2], "scale": [1.3, 1.3, 0.7]},
                                 "firstperson_righthand": {"rotation": [0, -90, 0], "translation": [2, 3, 2], "scale": [1.3, 1.3, 0.7]},
                                 "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [1.1, 1.1, 0.7]},
                                 "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [1.1, 1.1, 0.7]}}})
    for pid in PARTS:
        model(pid, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{pid}"}})
        idef(pid, m(pid))

    # ---- tags
    def tag(path, values):
        W(os.path.join(RES, "data", path + ".json"), {"replace": False, "values": values})

    ids = lambda pred: [f"{NS}:{i['id']}" for i in items if pred(i)]
    tag("minecraft/tags/item/head_armor", ids(lambda i: i["slot"] == "helmet"))
    tag("minecraft/tags/item/chest_armor", ids(lambda i: i["slot"] == "chestplate"))
    tag("minecraft/tags/item/leg_armor", ids(lambda i: i["slot"] == "leggings"))
    tag("minecraft/tags/item/foot_armor", ids(lambda i: i["slot"] == "boots"))
    tag("minecraft/tags/item/enchantable/head_armor", ids(lambda i: i["slot"] == "helmet"))
    tag("minecraft/tags/item/enchantable/chest_armor", ids(lambda i: i["slot"] == "chestplate"))
    tag("minecraft/tags/item/enchantable/leg_armor", ids(lambda i: i["slot"] == "leggings"))
    tag("minecraft/tags/item/enchantable/foot_armor", ids(lambda i: i["slot"] == "boots"))
    tag("minecraft/tags/item/enchantable/armor", ids(lambda i: i["kind"] == "armor"))
    tag("minecraft/tags/item/enchantable/equippable", ids(lambda i: i["kind"] in ("armor", "shield")))
    tag("minecraft/tags/item/swords", ids(lambda i: i["kind"] == "sword"))
    tag("minecraft/tags/item/axes", ids(lambda i: i["kind"] == "axe"))
    tag("minecraft/tags/item/spears", ids(lambda i: i["kind"] == "spear"))
    tag("minecraft/tags/item/enchantable/sword", ids(lambda i: i["kind"] == "sword"))
    tag("minecraft/tags/item/enchantable/melee_weapon", ids(lambda i: i["kind"] in ("sword", "axe", "spear")))
    tag("minecraft/tags/item/enchantable/sharp_weapon", ids(lambda i: i["kind"] in ("sword", "axe")))
    tag("minecraft/tags/item/enchantable/weapon", ids(lambda i: i["kind"] in ("sword", "axe", "spear")))
    tag("minecraft/tags/item/enchantable/bow", ids(lambda i: i["kind"] == "bow"))
    tag("minecraft/tags/item/enchantable/crossbow", ids(lambda i: i["kind"] == "crossbow"))
    tag("minecraft/tags/item/enchantable/durability", ids(lambda i: True))
    # repair tags
    repair = {"bone": [f"{NS}:bone_plate", "minecraft:leather"], "copper": ["#c:ingots/copper"], "bronze": ["#c:ingots/bronze"],
              "mail": ["#c:ingots/iron"], "steel": ["#c:ingots/steel"], "ballistic": [f"{NS}:ballistic_weave"],
              "tempered": [f"{NS}:tempered_ingot"], "composite": [f"{NS}:ceramic_plate"], "rodos": [f"{NS}:rodos_alloy_ingot"],
              "future": [f"{NS}:star_lattice"]}
    for k, v in repair.items():
        tag(f"{NS}/tags/item/repair/{k}", v)
    # empty optional ingot tags so the recipes always parse
    for nm in ("bronze", "steel"):
        tag(f"c/tags/item/ingots/{nm}", [])

    # ---- recipes
    def recipe(name, obj):
        W(os.path.join(DA, "recipe", f"{name}.json"), obj)

    def ing(s):
        return {"tag": s[1:]} if s.startswith("#") else {"item": s}

    def shaped(name, pattern, key, result, count=1):
        k = {}
        for sym, val in key.items():
            k[sym] = val if isinstance(val, str) else val
        recipe(name, {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern, "key": k,
                      "result": {"id": f"{NS}:{result}", "count": count}})

    pat = {"helmet": ["MMM", "M M"], "chestplate": ["M M", "MMM", "MMM"], "leggings": ["MMM", "M M", "M M"], "boots": ["M M", "M M"]}
    ingr = {t[0]: t[4] for t in ARMOR}
    for t in ARMOR:
        key = t[0]
        for slot in SLOTS:
            p = pat[slot]
            k = {"M": ingr[key]}
            if key == "bone":  # hide base with bone plates: leather in the lower row
                p = {"helmet": ["MMM", "L L"], "chestplate": ["L L", "MMM", "LLL"], "leggings": ["MMM", "L L", "L L"], "boots": ["L L", "M M"]}[slot]
                k["L"] = "minecraft:leather"
            shaped(f"{key}_{slot}", p, k, f"{key}_{slot}")
    tool_ing = {t[0]: t[4] for t in TOOLS}
    stick = "#c:rods/wooden"
    for key in MELEE_TIERS:
        m_ = tool_ing[key]
        shaped(f"{key}_sword", ["M", "M", "|"], {"M": m_, "|": stick}, f"{key}_sword")
        shaped(f"{key}_axe", ["MM", "M|", " |"], {"M": m_, "|": stick}, f"{key}_axe")
        shaped(f"{key}_spear", ["  M", " | ", "|  "], {"M": m_, "|": stick}, f"{key}_spear")
    for t in TOOLS:
        key = t[0]
        m_ = tool_ing[key]
        shaped(f"{key}_bow", [" MS", "M S", " MS"], {"M": m_, "S": "minecraft:string"}, f"{key}_bow")
        if t[12]:
            shaped(f"{key}_crossbow", ["|M|", "STS", " | "], {"M": m_, "|": stick, "S": "minecraft:string", "T": "minecraft:tripwire_hook"}, f"{key}_crossbow")
        shaped(f"{key}_shield", ["PMP", "PMP", " M "], {"M": m_, "P": "#minecraft:planks"}, f"{key}_shield")
    # parts
    shaped("bone_plate", ["BB", "BL"], {"B": "minecraft:bone", "L": "minecraft:leather"}, "bone_plate", 2)
    recipe("ballistic_weave", {"type": "minecraft:crafting_shapeless", "category": "misc",
                               "ingredients": ["minecraft:string"] * 4 + ["#c:ingots/steel"], "result": {"id": f"{NS}:ballistic_weave", "count": 2}})
    recipe("ceramic_plate", {"type": "minecraft:crafting_shapeless", "category": "misc",
                             "ingredients": ["minecraft:brick", "minecraft:brick", "minecraft:quartz", f"{NS}:ballistic_weave"],
                             "result": {"id": f"{NS}:ceramic_plate", "count": 2}})
    recipe("tempered_ingot", {"type": "minecraft:crafting_shapeless", "category": "misc",
                              "ingredients": ["#c:ingots/steel", "#c:ingots/steel", "minecraft:blaze_powder"],
                              "result": {"id": f"{NS}:tempered_ingot", "count": 2}})
    recipe("rodos_alloy_ingot", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                 "ingredients": ["minecraft:netherite_ingot", f"{NS}:tempered_ingot", "minecraft:amethyst_shard", "minecraft:echo_shard"],
                                 "result": {"id": f"{NS}:rodos_alloy_ingot", "count": 1}})
    recipe("star_lattice", {"type": "minecraft:crafting_shapeless", "category": "misc",
                            "ingredients": [f"{NS}:rodos_alloy_ingot", f"{NS}:ceramic_plate", "minecraft:nether_star"],
                            "result": {"id": f"{NS}:star_lattice", "count": 1}})
    print("items", len(items), "parts", len(PARTS))


if __name__ == "__main__":
    main()
