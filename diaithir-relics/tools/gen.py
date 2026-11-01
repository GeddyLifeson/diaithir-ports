"""Writes everything data-driven for diaithir-relics: relics.json, item definitions, models, textures, lang, recipes, tags,
loot tables and loot modifiers, age_gates.json, names_map.json, library and codex files. Textures are made by script from
vanilla sprites (recoloured by luminance ramps, worn edges, grain, patina, enamel studs); the trinkets are drawn by code and
take their grain from vanilla ingots. Nothing else is copied.
Run: python3 -I tools/gen.py <dir holding the extracted vanilla 'assets/minecraft/textures' tree>
"""
import colorsys, json, os, shutil, sys
import numpy as np
from PIL import Image

VAN = os.path.join(sys.argv[1], "assets/minecraft/textures")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src/main/resources")
NS = "diaithirrelics"
AS = os.path.join(RES, "assets", NS)
DA = os.path.join(RES, "data", NS)


def W(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)


# ---------------------------------------------------------------- tiers (Ages III to VII)
# gate, library age, tool material: durability, speed, bonus, enchant, incorrect-blocks tag, repair tag, ramp, wood ramp
TIERS = {
    "III": dict(lib=3, dur=700, speed=6.0, bonus=2.5, ench=14, inc="iron", repair="#c:ingots/bronze", ramp="bronze", wood="yew", spear=[0.95, 0.95, 0.60, 3.0, 11.0, 6.2, 5.1, 11.0, 4.6]),
    "IV": dict(lib=4, dur=1000, speed=6.5, bonus=3.0, ench=14, inc="iron", repair="#c:ingots/iron", ramp="iron", wood="wood", spear=[1.00, 1.00, 0.55, 2.8, 10.5, 6.0, 5.1, 10.5, 4.6]),
    "V": dict(lib=5, dur=1500, speed=7.5, bonus=3.5, ench=15, inc="diamond", repair="#c:ingots/steel", ramp="steel", wood="wood", spear=[1.05, 1.06, 0.50, 2.7, 10.0, 5.8, 5.1, 10.0, 4.6]),
    "VI": dict(lib=6, dur=2200, speed=8.5, bonus=4.0, ench=15, inc="diamond", repair="#c:ingots/steel", ramp="tempered", wood="wood", spear=[1.10, 1.12, 0.45, 2.6, 9.5, 5.6, 5.1, 9.5, 4.6]),
    "VII": dict(lib=7, dur=3200, speed=10.0, bonus=5.0, ench=17, inc="netherite", repair="#minecraft:netherite_tool_materials", ramp="rodos", wood="wood", spear=[1.15, 1.22, 0.40, 2.5, 9.0, 5.4, 5.1, 8.75, 4.6]),
}

# ---------------------------------------------------------------- the relics
# id, kind, tier, Diaithiris name, English name, lore, accent colour, extra damage, attack speed, durability,
# active (key, cooldown s, text) or None, passive (key, text), source ("craft" recipe | "loot" structure chests),
# recipe (tier ingredient, catalyst) for crafted ones
R = []
# one-line lore comes from the batch job (compute/jobs/relics_lore) via tools/lore.json {id: text}; nothing is hand-written here
LORE = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "lore.json"), encoding="utf8")) if os.path.exists(os.path.join(os.path.dirname(os.path.abspath(__file__)), "lore.json")) else {}


def relic(**kw):
    R.append(kw)


relic(id="dawn_sword", kind="sword", tier="III", dn="Klaidem Kamunaig", en="Sword of Dawn",
      acc=(214, 70, 28), extra=0.5, speed=-2.4, dur=900,
      active=("sunburst", 30, "Sunburst: flame bursts in a ring of 5 blocks, igniting foes for 6 seconds and burning them for 6 damage."),
      passive=("ignite", "Foes you strike catch fire for 3 seconds."),
      src="craft", rec=("#c:ingots/bronze", "minecraft:blaze_powder"))
relic(id="salmon_spear", kind="spear", tier="III", dn="Slieg Braduin", en="Spear of the Salmon",
      acc=(60, 150, 190), extra=0.5, speed=-2.4, dur=900,
      active=("lunge", 12, "Lunge (sneak and use): dash 7 blocks forward, striking foes in your path for 8 damage."),
      passive=("aqua", "While held in water you swim fast, as if with a dolphin's grace."),
      src="craft", rec=("#c:ingots/bronze", "minecraft:salmon"))
relic(id="boar_shield", kind="shield", tier="III", dn="Skiad Toirk", en="Shield of the Boar",
      acc=(236, 228, 200), extra=0, speed=0, dur=700,
      active=None, passive=("reflect", "Blocking a melee blow throws back 30% of its damage at the attacker."),
      src="craft", rec=("#c:ingots/bronze", "minecraft:leather"))
relic(id="well_ring", kind="ring", tier="III", dn="Fàindi Tobuir", en="Ring of the Well",
      acc=(40, 110, 200), extra=0, speed=0, dur=0,
      active=None, passive=("wellspring", "Below half health you slowly regenerate."),
      src="loot", slot="ring")
relic(id="gold_torque", kind="necklace", tier="III", dn="Moinki Òiri", en="Gold Torque",
      acc=(200, 40, 40), extra=0, speed=0, dur=0,
      active=None, passive=("arrowward", "Arrows, bolts and other missiles do 25% less damage to you."),
      src="craft", slot="necklace", rec=("#c:ingots/bronze", "minecraft:gold_ingot"))

relic(id="storm_axe", kind="axe", tier="IV", dn="Tuog Stoirmi", en="Axe of the Storm",
      acc=(120, 160, 230), extra=1.0, speed=-3.0, dur=1200,
      active=("skybolt", 40, "Skybolt: call lightning down on the foe you are looking at, up to 24 blocks away."),
      passive=("chain", "One blow in five arcs lightning to up to 3 foes within 4 blocks of the target."),
      src="loot")
relic(id="shadow_dagger", kind="dagger", tier="IV", dn="Biudag Failes", en="Dagger of Shadow",
      acc=(110, 60, 170), extra=-1.0, speed=-1.6, dur=900,
      active=("shadowstep", 15, "Shadow step: slip up to 9 blocks along your gaze and vanish for 3 seconds."),
      passive=("backstab", "Blows to a foe facing away deal 50% more damage."),
      src="craft", rec=("#c:ingots/iron", "minecraft:ender_pearl"))
relic(id="cairn_hammer", kind="hammer", tier="IV", dn="Òrdu Kàirn", en="Hammer of the Cairn",
      acc=(150, 150, 140), extra=2.0, speed=-3.2, dur=1300,
      active=("slam", 20, "Cairnfall: strike the ground, hurling foes within 4 blocks into the air for 8 damage."),
      passive=("stagger", "Foes you strike are slowed for 1.5 seconds."),
      src="craft", rec=("#c:ingots/iron", "minecraft:chiseled_stone_bricks"))
relic(id="mist_bell", kind="charm", tier="IV", dn="Klag Kietaig", en="Bell of Mist",
      acc=(150, 190, 200), extra=0, speed=0, dur=0,
      active=None, passive=("mistveil", "When a blow leaves you below 35% health you turn unseen and swift for 5 seconds (once a minute)."),
      src="loot", slot="charm")
relic(id="bear_belt", kind="belt", tier="IV", dn="Krius Matuin", en="Belt of the Bear",
      acc=(190, 120, 40), extra=0, speed=0, dur=0,
      active=None, passive=("bearhide", "Melee blows do 15% less damage to you."),
      src="craft", slot="belt", rec=("#c:ingots/iron", "minecraft:honey_bottle"))

relic(id="sea_sword", kind="sword", tier="V", dn="Klaidem Moir", en="Sword of the Sea",
      acc=(40, 160, 170), extra=0.5, speed=-2.4, dur=1700,
      active=("tidal", 25, "Tide: a surge throws every foe in a 6-block cone far back, and you breathe water for 30 seconds."),
      passive=("tidebonus", "Blows do 30% more damage while you stand in water or rain."),
      src="loot")
relic(id="lightning_spear", kind="spear", tier="V", dn="Slieg Dielunach", en="Spear of Lightning",
      acc=(250, 240, 120), extra=0.5, speed=-2.4, dur=1700,
      active=("beam", 15, "Bolt (sneak and use): a white arc leaps 24 blocks to the first foe, for 14 damage."),
      passive=("skyhunter", "Blows to foes that are off the ground do 40% more damage."),
      src="loot")
relic(id="frost_shield", kind="shield", tier="V", dn="Skiad Reotad", en="Shield of Frost",
      acc=(170, 220, 250), extra=0, speed=0, dur=1400,
      active=None, passive=("frost", "Blocking a melee blow freezes the attacker: slowness III for 3 seconds."),
      src="craft", rec=("#c:ingots/steel", "minecraft:blue_ice"))
relic(id="sun_ring", kind="ring", tier="V", dn="Fàindi Grianu", en="Ring of the Sun",
      acc=(250, 200, 60), extra=0, speed=0, dur=0,
      active=None, passive=("sunwarmth", "Under open sky by day you slowly regenerate."),
      src="craft", slot="ring", rec=("#c:ingots/gold", "minecraft:glowstone_dust"))
relic(id="moon_torque", kind="necklace", tier="V", dn="Moinki Gielach", en="Torque of the Moon",
      acc=(190, 200, 250), extra=0, speed=0, dur=0,
      active=None, passive=("moonsight", "At night you see in the dark and move faster."),
      src="loot", slot="necklace")

relic(id="star_sword", kind="sword", tier="VI", dn="Klaidem Riondag", en="Sword of Stars",
      acc=(240, 240, 255), extra=1.0, speed=-2.4, dur=2500,
      active=("starfall", 35, "Starfall: a burst of light strikes foes in front of you within 8 blocks for 12 damage and marks them glowing for 10 seconds."),
      passive=("starcrit", "Blows to a foe at full health do 25% more damage."),
      src="loot")
relic(id="serpent_axe", kind="axe", tier="VI", dn="Tuog Natrach", en="Axe of the Serpent",
      acc=(90, 190, 70), extra=1.0, speed=-3.0, dur=2500,
      active=("venom", 20, "Venom burst: foes within 5 blocks are poisoned for 8 seconds and weakened."),
      passive=("poison", "Foes you strike are poisoned for 5 seconds."),
      src="craft", rec=("#c:ingots/steel", "minecraft:fermented_spider_eye"))
relic(id="eagle_spear", kind="spear", tier="VI", dn="Slieg Iuluiri", en="Spear of the Eagle",
      acc=(200, 150, 60), extra=0.5, speed=-2.4, dur=2500,
      active=("leap", 14, "Leap (sneak and use): spring high and forward, then fall slowly for 6 seconds."),
      passive=("plunge", "Falling onto a foe adds 1 damage per block fallen (up to 10)."),
      src="craft", rec=("#c:ingots/steel", "minecraft:feather"))
relic(id="raven_whistle", kind="charm", tier="VI", dn="Fiedag Fitig", en="Whistle of the Raven",
      acc=(60, 60, 90), extra=0, speed=0, dur=0,
      active=None, passive=("ravensight", "Hostile creatures within 20 blocks glow, so you see them through walls."),
      src="loot", slot="charm")
relic(id="giant_belt", kind="belt", tier="VI", dn="Krius Famuir", en="Belt of the Giant",
      acc=(160, 90, 50), extra=0, speed=0, dur=0,
      active=None, passive=("giantsbulk", "You carry four extra hearts of absorption, renewed while it is worn."),
      src="loot", slot="belt")

relic(id="oath_sword", kind="sword", tier="VII", dn="Klaidem Miondun", en="Sword of the Oath",
      acc=(220, 40, 60), extra=1.5, speed=-2.4, dur=3500,
      active=("oathcry", 60, "Oath-cry: you and allies within 8 blocks gain strength II and resistance for 20 seconds; foes nearby are weakened."),
      passive=("lifesteal", "You heal 15% of the damage you deal."),
      src="loot")
relic(id="smith_hammer", kind="hammer", tier="VII", dn="Òrdu Gobund", en="Hammer of the Smith",
      acc=(240, 150, 40), extra=2.0, speed=-3.2, dur=3500,
      active=("mend", 120, "Remaking: restore a quarter of the wear on everything you wear and hold."),
      passive=("breaker", "Blows to armoured foes do 2 extra damage per 5 points of their armour."),
      src="craft", rec=("minecraft:netherite_ingot", "minecraft:diamond"))
relic(id="light_shield", kind="shield", tier="VII", dn="Skiad Soulais", en="Shield of Light",
      acc=(255, 240, 150), extra=0, speed=0, dur=3000,
      active=None, passive=("ward", "While blocking, missiles do no damage to you and melee attackers are blinded for 3 seconds."),
      src="loot")
relic(id="fate_ring", kind="ring", tier="VII", dn="Fàindi Dànu", en="Ring of Fate",
      acc=(180, 60, 200), extra=0, speed=0, dur=0,
      active=None, passive=("fatesave", "A fatal blow leaves you at 3 hearts with regeneration (once every 10 minutes)."),
      src="loot", slot="ring")
relic(id="apple_branch", kind="charm", tier="VII", dn="Geig Ubul", en="Apple Branch",
      acc=(230, 190, 60), extra=0, speed=0, dur=0,
      active=None, passive=("feast", "Your hunger refills slowly while it is worn."),
      src="loot", slot="charm")
relic(id="dragon_torque", kind="necklace", tier="VII", dn="Moinki Drieguin", en="Torque of the Dragon",
      acc=(230, 100, 30), extra=0, speed=0, dur=0,
      active=None, passive=("dragonward", "You take no damage from fire or lava."),
      src="loot", slot="necklace")

# ---------------------------------------------------------------- loot sources (Ages III to VII, vanilla and pack chests)
LOOT = {
    "III": ["minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft", "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple",
            "minecraft:chests/shipwreck_treasure", "minecraft:chests/buried_treasure", "minecraft:chests/pillager_outpost", "minecraft:chests/igloo_chest",
            "dungeoncrawl:chests/stage_1", "dungeoncrawl:chests/secret_room"],
    "IV": ["minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing", "minecraft:chests/underwater_ruin_big",
           "minecraft:chests/ruined_portal", "minecraft:chests/woodland_mansion", "minecraft:chests/desert_pyramid", "dungeoncrawl:chests/stage_2",
           "dungeoncrawl:chests/treasure"],
    "V": ["minecraft:chests/stronghold_library", "minecraft:chests/woodland_mansion", "minecraft:chests/nether_bridge", "minecraft:chests/bastion_other",
          "minecraft:chests/ancient_city", "dungeoncrawl:chests/stage_3", "dungeoncrawl:chests/treasure"],
    "VI": ["minecraft:chests/bastion_treasure", "minecraft:chests/ancient_city", "minecraft:chests/ancient_city_ice_box", "minecraft:chests/trial_chambers/reward",
           "dungeoncrawl:chests/stage_4", "dungeoncrawl:chests/treasure"],
    "VII": ["minecraft:chests/end_city_treasure", "minecraft:chests/trial_chambers/reward_ominous", "minecraft:chests/ancient_city",
            "minecraft:chests/bastion_treasure", "dungeoncrawl:chests/stage_5", "dungeoncrawl:chests/treasure"],
}
LOOT_CHANCE = {"III": 0.10, "IV": 0.10, "V": 0.12, "VI": 0.15, "VII": 0.20}

SLOT_CURIOS = {"ring": "ring", "necklace": "necklace", "charm": "charm", "belt": "belt"}
KIND_EN = {"sword": "sword", "axe": "axe", "spear": "spear", "dagger": "dagger", "hammer": "hammer", "shield": "shield",
           "ring": "ring", "necklace": "necklace", "charm": "charm", "belt": "belt"}

# ---------------------------------------------------------------- colour ramps (dark to light)
RAMPS = {
    "hide": [(44, 28, 18), (84, 55, 33), (122, 83, 50), (158, 112, 70), (192, 147, 100)],
    "bronze": [(62, 40, 18), (114, 78, 32), (164, 118, 50), (204, 160, 80), (234, 204, 124)],
    "iron": [(26, 26, 30), (54, 55, 60), (90, 92, 98), (130, 133, 140), (176, 179, 186)],
    "steel": [(34, 40, 48), (76, 84, 94), (120, 130, 140), (170, 180, 190), (218, 226, 232)],
    "tempered": [(40, 42, 58), (82, 84, 112), (126, 120, 140), (170, 150, 130), (214, 190, 140)],
    "rodos": [(18, 40, 52), (34, 88, 100), (64, 140, 142), (150, 190, 160), (232, 226, 176)],
    "rodos2": [(60, 30, 20), (120, 70, 36), (176, 120, 56), (220, 176, 84), (248, 224, 140)],
    "gold": [(84, 54, 8), (150, 104, 18), (208, 158, 36), (240, 204, 80), (255, 240, 160)],
    "silver": [(60, 64, 76), (112, 118, 132), (166, 172, 184), (210, 214, 224), (244, 246, 252)],
    "wood": [(42, 28, 16), (82, 56, 32), (118, 82, 48), (150, 108, 66), (180, 136, 90)],
    "yew": [(70, 38, 22), (110, 62, 34), (146, 90, 50), (178, 122, 70), (206, 158, 100)],
    "leaf": [(20, 44, 18), (40, 84, 30), (66, 124, 44), (100, 160, 62), (150, 200, 96)],
    "bone": [(128, 116, 94), (176, 164, 138), (212, 202, 178), (234, 226, 204), (250, 245, 228)],
}
TIER_TRINKET = {"III": "bronze", "IV": "iron", "V": "silver", "VI": "tempered", "VII": "gold"}


def ramp_at(name, t):
    st = RAMPS[name] if isinstance(name, str) else name
    t = np.clip(t, 0, 1) * (len(st) - 1)
    i = np.minimum(np.asarray(t).astype(int), len(st) - 2)
    f = (t - i)[..., None] if np.ndim(t) else (t - i)
    a = np.array(st, dtype=float)
    return a[i] * (1 - f) + a[i + 1] * f


def gem_ramp(c):
    c = np.array(c, dtype=float)
    return [tuple(np.clip(c * k, 0, 255)) for k in (0.30, 0.55, 0.85, 1.12, 1.45)]


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
    if 0 <= y < a.shape[0] and 0 <= x < a.shape[1]:
        a[y, x, :3] = rgb
        a[y, x, 3] = 255


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


def two_tone(src, metal, wood, rng):
    a = arr(src)
    mask, wd = classify_wood(a)
    out = shade(src, metal, rng, mask_sel=~wd, scratches=2)
    return shade(src, wood, rng, mask_sel=wd, base=out, scratches=1, grain=0.04), wd


def patina(a, rng, tier, mask):
    """Age marks by material: verdigris on bronze, rust bloom on iron, brushed grain on steel, heat tint, cold glow."""
    pts = interior(mask)
    h, w = mask.shape
    if not pts:
        return
    if tier == "III":
        for _ in range(max(2, len(pts) // 28)):
            y, x = pts[rng.integers(len(pts))]
            put(a, y, x, (72, 142, 112))
    elif tier == "IV":
        for _ in range(max(2, len(pts) // 26)):
            y, x = pts[rng.integers(len(pts))]
            put(a, y, x, (116, 64, 34))
    elif tier == "V":
        for x in range(0, w, 3):
            for y in range(h):
                if mask[y, x]:
                    a[y, x, :3] = np.clip(a[y, x, :3] * 1.06, 0, 255)
    elif tier == "VI":
        for _ in range(max(2, len(pts) // 30)):
            y, x = pts[rng.integers(len(pts))]
            put(a, y, x, (176, 138, 110))
    else:
        for y in range(h):
            for x in range(w):
                if mask[y, x] and ((x * 7 + y * 5) % 31) / 31.0 > 0.7:
                    a[y, x, :3] = 0.4 * a[y, x, :3] + 0.6 * ramp_at("rodos2", np.array(0.6))


def accent(a, rng, mask, wd, acc, tier):
    """Enamel stud or gem on the grip (Age III and IV: red and blue enamel studs in the island style), accent etching on the blade."""
    pts = interior(mask & wd) or interior(mask)
    if pts:
        cands = sorted(pts, key=lambda p: -(p[0] - p[1]))  # low-left end of the grip
        y, x = cands[0]
        g = gem_ramp(acc)
        put(a, y, x, g[3])
        for dy, dx in ((0, 1), (1, 0)):
            if 0 <= y + dy < 16 and 0 <= x + dx < 16 and mask[y + dy, x + dx]:
                put(a, y + dy, x + dx, g[1])
        if tier in ("III", "IV") and len(cands) > 4:
            y2, x2 = cands[min(4, len(cands) - 1)]
            put(a, y2, x2, (150, 36, 30) if tier == "III" else (34, 62, 130))
    blade = mask & ~wd
    bp = interior(blade)
    for k, (y, x) in enumerate(sorted(bp, key=lambda p: p[0] + p[1])):
        if k % 3 == 0:
            a[y, x, :3] = 0.55 * a[y, x, :3] + 0.45 * np.array(acc)


# ---------------------------------------------------------------- weapon sprites
def make_sword(r, rng):
    src = load("item/iron_sword.png")
    out, wd = two_tone(src, TIERS[r["tier"]]["ramp"], TIERS[r["tier"]]["wood"], rng)
    mask = out[..., 3] > 0
    patina(out, rng, r["tier"], mask & ~wd)
    accent(out, rng, mask, wd, r["acc"], r["tier"])
    save(out, f"item/{r['id']}.png")


def make_axe(r, rng):
    src = load("item/iron_axe.png")
    out, wd = two_tone(src, TIERS[r["tier"]]["ramp"], TIERS[r["tier"]]["wood"], rng)
    mask = out[..., 3] > 0
    patina(out, rng, r["tier"], mask & ~wd)
    accent(out, rng, mask, wd, r["acc"], r["tier"])
    save(out, f"item/{r['id']}.png")


def make_spear(r, rng):
    for suffix in ("", "_in_hand"):
        src = load(f"item/iron_spear{suffix}.png")
        out, wd = two_tone(src, TIERS[r["tier"]]["ramp"], TIERS[r["tier"]]["wood"], rng)
        mask = out[..., 3] > 0
        patina(out, rng, r["tier"], mask & ~wd)
        accent(out, rng, mask, wd, r["acc"], r["tier"])
        save(out, f"item/{r['id']}{suffix}.png")


def make_hammer(r, rng):
    src = load("item/mace.png")
    out, wd = two_tone(src, TIERS[r["tier"]]["ramp"], TIERS[r["tier"]]["wood"], rng)
    mask = out[..., 3] > 0
    patina(out, rng, r["tier"], mask & ~wd)
    accent(out, rng, mask, wd, r["acc"], r["tier"])
    save(out, f"item/{r['id']}.png")


def make_dagger(r, rng):
    a = np.zeros((16, 16, 4))
    lum = np.zeros((16, 16))

    def px(y, x, v):
        if 0 <= y < 16 and 0 <= x < 16:
            a[y, x] = (255, 255, 255, 255)
            lum[y, x] = v
    # blade runs from the tip (13,2) down-left to the guard
    for i in range(8):
        px(2 + i, 13 - i, 0.85)
        px(2 + i, 12 - i, 0.5)
    px(1, 14, 0.9)
    wood = np.zeros((16, 16), bool)
    for t in range(-2, 3):  # cross-guard
        px(9 + t, 6 + t, 0.7)
    for i in range(1, 4):  # grip
        px(9 + i, 6 - i, 0.4)
        wood[9 + i, 6 - i] = True
        px(9 + i, 5 - i, 0.25)
        wood[9 + i, 5 - i] = True
    mask = a[..., 3] > 0
    src = np.dstack([lum * 255] * 3 + [a[..., 3]])
    img = Image.fromarray(src.astype(np.uint8), "RGBA")
    tr = TIERS[r["tier"]]
    out = shade(img, tr["ramp"], rng, mask_sel=~wood, contrast=1.2)
    out = shade(img, "hide", rng, mask_sel=wood, base=out, contrast=1.1)
    patina(out, rng, r["tier"], mask & ~wood)
    g = gem_ramp(r["acc"])
    put(out, 13, 2, g[3]); put(out, 13, 3, g[2]); put(out, 12, 2, g[1])
    for k, (y, x) in enumerate([(3 + i, 12 - i) for i in range(1, 6)]):
        if k % 2 == 0:
            out[y, x, :3] = 0.55 * out[y, x, :3] + 0.45 * np.array(r["acc"])
    save(out, f"item/{r['id']}.png")


# ---------------------------------------------------------------- shield sprites
SHIELD_HEATER = [11, 13, 13, 13, 13, 13, 13, 13, 12, 11, 10, 8, 6, 4, 2]


def make_shield(r, rng):
    a = np.zeros((16, 16, 4))
    for i, wd in enumerate(SHIELD_HEATER):
        x0 = 8 - wd // 2
        for x in range(x0, x0 + wd):
            a[i, x] = (0, 0, 0, 255)
    mask = a[..., 3] > 0
    n = neighbours(mask)
    rim = mask & ~(n["l"] & n["r"] & n["u"] & n["d"])
    tier = r["tier"]
    field = {"boar_shield": "hide", "frost_shield": "silver", "light_shield": "gold"}[r["id"]]
    rim_ramp = TIERS[tier]["ramp"]
    for y in range(16):
        for x in range(16):
            if mask[y, x]:
                t = 0.55 + 0.35 * (1 - abs(x - 7.5) / 7.5) - 0.12 * (y / 15.0) + rng.uniform(-0.05, 0.05)
                if rim[y, x]:
                    c = ramp_at(rim_ramp, np.array(0.45 + 0.4 * (1 if (x < 8 and y < 8) else 0) + rng.uniform(-0.06, 0.06)))
                else:
                    c = ramp_at(field, np.array(t))
                    if field == "hide" and x % 3 == 0:
                        c = c * 0.84
                a[y, x, :3] = c
    g = gem_ramp(r["acc"])
    if r["id"] == "boar_shield":  # two curved tusks and a snout boss
        for (y, x) in [(7, 4), (8, 3), (9, 3), (10, 4), (7, 11), (8, 12), (9, 12), (10, 11)]:
            put(a, y, x, RAMPS["bone"][3])
        for (y, x) in [(6, 7), (6, 8), (7, 7), (7, 8)]:
            put(a, y, x, (92, 60, 52))
        put(a, 6, 7, (30, 20, 20)); put(a, 6, 8, (30, 20, 20))
        put(a, 3, 5, (150, 36, 30)); put(a, 3, 10, (34, 62, 130))  # enamel studs
    elif r["id"] == "frost_shield":  # ice crystals
        for (y, x) in [(4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (6, 6), (6, 10), (7, 7), (7, 9), (5, 6), (5, 10), (8, 6), (8, 10)]:
            put(a, y, x, g[2] if (x + y) % 2 else g[4])
        for (y, x) in [(2, 4), (11, 7), (3, 11)]:
            put(a, y, x, (240, 250, 255))
    else:  # sun: rays round a boss
        for (y, x) in [(4, 8), (5, 8), (9, 8), (10, 8), (6, 5), (6, 11), (7, 4), (7, 12), (5, 6), (5, 10), (8, 6), (8, 10)]:
            put(a, y, x, g[3])
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                put(a, 7 + dy, 8 + dx, g[4] if dy + dx <= 0 else g[2])
    patina(a, rng, tier, mask & ~rim)
    for y in range(16):
        for x in range(16):
            if rim[y, x] and (not n["r"][y, x] or not n["d"][y, x]):
                a[y, x, :3] *= 0.7
    save(a, f"item/{r['id']}.png")


# ---------------------------------------------------------------- trinket sprites (drawn by code; grain from a vanilla ingot)
def grain_field(rng):
    ing = load("item/iron_ingot.png")
    L = luminance(arr(ing))
    L = (L - L.min()) / max(1e-3, (L.max() - L.min()))
    return np.clip(L, 0, 1)


def paint(mask, lum, ramp, rng, gem_px=(), gem=None, extra=None):
    a = np.zeros((16, 16, 4))
    n = neighbours(mask)
    gr = grain_field(rng)
    for y in range(16):
        for x in range(16):
            if mask[y, x]:
                t = lum[y, x] + 0.12 * (gr[y, x] - 0.5) + rng.uniform(-0.04, 0.04)
                t += 0.14 * (not n["l"][y, x] or not n["u"][y, x]) - 0.12 * (not n["r"][y, x] or not n["d"][y, x])
                a[y, x, :3] = ramp_at(ramp, np.array(np.clip(t, 0, 1)))
                a[y, x, 3] = 255
    if gem is not None:
        g = gem_ramp(gem)
        for k, (y, x) in enumerate(gem_px):
            put(a, y, x, g[3] if k == 0 else (g[4] if k == 1 else g[2]))
    if extra:
        extra(a)
    return a


def disc(cx, cy, r0, r1):
    yy, xx = np.mgrid[0:16, 0:16]
    d = np.hypot(xx - cx, yy - cy)
    return (d >= r0) & (d <= r1), d, xx, yy


def make_ring(r, rng):
    mask, d, xx, yy = disc(7.5, 9.5, 2.6, 5.3)
    lum = 0.5 + 0.35 * (-(xx - 7.5) - (yy - 9.5)) / 6.0
    gem_px = [(3, 7), (3, 8), (4, 7), (4, 8)]
    for (y, x) in gem_px:
        mask[y, x] = True
    mask[2, 7] = mask[2, 8] = False
    a = paint(mask, lum, TIER_TRINKET[r["tier"]], rng, gem_px, r["acc"])
    patina(a, rng, r["tier"], mask)
    for (y, x) in [(3, 6), (3, 9), (4, 6), (4, 9)]:  # claws
        put(a, y, x, ramp_at(TIER_TRINKET[r["tier"]], np.array(0.8)))
    g = gem_ramp(r["acc"])
    put(a, 3, 7, g[4]); put(a, 3, 8, g[3]); put(a, 4, 7, g[2]); put(a, 4, 8, g[1])
    save(a, f"item/{r['id']}.png")


def make_necklace(r, rng):
    mask, d, xx, yy = disc(7.5, 7.0, 4.4, 6.0)
    ang = np.degrees(np.arctan2(yy - 7.0, xx - 7.5))
    mask &= ~(np.abs(ang + 90) < 28)  # the open throat of the torque
    mask &= yy < 14
    lum = 0.5 + 0.3 * (-(xx - 7.5) - (yy - 7.0)) / 6.0
    twist = ((xx + yy) % 3 == 0)
    lum = lum + 0.12 * twist  # twisted-rope look
    gem_px = [(13, 7), (13, 8), (14, 7), (14, 8)]
    for (y, x) in gem_px:
        mask[y, x] = True
    # end knobs
    for (y, x) in [(1, 4), (1, 5), (2, 4), (2, 5), (1, 10), (1, 11), (2, 10), (2, 11)]:
        mask[y, x] = True
        lum[y, x] = 0.85
    a = paint(mask, lum, TIER_TRINKET[r["tier"]], rng, gem_px, r["acc"])
    patina(a, rng, r["tier"], mask)
    save(a, f"item/{r['id']}.png")


def make_belt(r, rng):
    mask = np.zeros((16, 16), bool)
    lum = np.zeros((16, 16))
    for x in range(16):
        sag = int(round(1.2 * np.sin(x / 15.0 * np.pi)))
        for y in range(5 + sag, 10 + sag):
            mask[y, x] = True
            lum[y, x] = 0.62 - 0.1 * (y - (5 + sag)) / 4 + (0.06 if (x % 4 == 0) else 0)
    a = paint(mask, lum, "hide", rng)
    metal = TIER_TRINKET[r["tier"]]
    for y in range(4, 12):  # buckle
        for x in range(6, 10):
            if 4 <= y <= 11 and (y in (4, 11) or x in (6, 9)):
                put(a, y, x, ramp_at(metal, np.array(0.55 + 0.3 * (x == 6 or y == 4))))
    g = gem_ramp(r["acc"])
    for (y, x) in [(7, 7), (7, 8), (8, 7), (8, 8)]:
        put(a, y, x, g[3])
    put(a, 7, 7, g[4])
    for x in (2, 4, 11, 13):  # studs
        put(a, 7, x, ramp_at(metal, np.array(0.85)))
    patina(a, rng, r["tier"], a[..., 3] > 0)
    save(a, f"item/{r['id']}.png")


ASCII = {
    "mist_bell": [
        ".......KK.......", "......KMMK......", ".....KHMMMK.....", "....KHHMMMMK....", "....KHMMMMMK....", "...KHHMMMMMSK...",
        "...KHMMMMMMSK...", "...KHMMMMMMSK...", "..KHHMMMMMMMSK..", "..KHMMMMMMMMSK..", ".KKKKKKKKKKKKKK.", "..SSSSSSSSSSSS..",
        ".......KK.......", "......KggK......", "......KggK......", ".......KK......."],
    "raven_whistle": [
        "................", "................", "..........KKK...", "........KKHMMK..", ".......KHHMMSK..", "......KHHMMSK...", ".....KHMMMSK....",
        "....KHMMMSK.....", "...KHMMMSK......", "..KHMMMSK.......", ".KgKMMSK........", "KggKKSK.........", "KggK............", ".KK.............", "................", "................"],
    "apple_branch": [
        "..............K.", ".............KMK", "...........KKMK.", "..........KMMK..", "...gg....KMK....", "..gggg..KMK.....", "..gggg.KMK..ll..",
        "...gg.KMKKllll..", ".....KMK..lll...", "....KMK.........", "...KMK..........", "..KMK...........", ".KMK............", "KMK.............", "................", "................"],
}


def make_charm(r, rng):
    rows = ASCII[r["id"]]
    mask = np.zeros((16, 16), bool)
    lum = np.zeros((16, 16))
    tok = {"K": 0.08, "H": 0.9, "M": 0.58, "S": 0.28}
    gem_px, leaf = [], []
    for y, row in enumerate(rows):
        for x, ch in enumerate(row[:16]):
            if ch in tok:
                mask[y, x] = True
                lum[y, x] = tok[ch]
            elif ch == "g":
                mask[y, x] = True
                gem_px.append((y, x))
            elif ch == "l":
                mask[y, x] = True
                leaf.append((y, x))
                lum[y, x] = 0.6
    ramp = TIER_TRINKET[r["tier"]] if r["id"] != "apple_branch" else "silver"
    a = paint(mask, lum, ramp, rng, gem_px, r["acc"])
    for (y, x) in leaf:
        put(a, y, x, ramp_at("leaf", np.array(0.45 + 0.4 * ((x + y) % 2))))
    patina(a, rng, r["tier"], mask)
    save(a, f"item/{r['id']}.png")


MAKE = {"sword": make_sword, "axe": make_axe, "spear": make_spear, "dagger": make_dagger, "hammer": make_hammer,
        "shield": make_shield, "ring": make_ring, "necklace": make_necklace, "belt": make_belt, "charm": make_charm}


# ---------------------------------------------------------------- main
def main():
    rng = np.random.default_rng(1926)
    for d in (AS, DA):
        shutil.rmtree(d, ignore_errors=True)
    ids = [r["id"] for r in R]
    assert len(ids) == len(set(ids)) and 20 <= len(ids) <= 30
    lang, names, gates, items = {}, {}, {}, []
    lib = []
    for r in R:
        tr = TIERS[r["tier"]]
        a_key, a_cd, a_txt = r["active"] if r["active"] else (None, 0, "")
        p_key, p_txt = r["passive"]
        items.append(dict(id=r["id"], kind=r["kind"], tier=r["tier"], extra=r["extra"], speed=r["speed"], durability=r["dur"],
                          active=a_key, cooldown=a_cd, passive=p_key, slot=r.get("slot", "")))
        lang[f"item.{NS}.{r['id']}"] = r["dn"]
        r["lore"] = LORE.get(r["id"], "")
        if r["lore"]:
            lang[f"item.{NS}.{r['id']}.lore"] = r["lore"]
        lang[f"item.{NS}.{r['id']}.age"] = f"Age {r['tier']}"
        if a_key:
            lang[f"item.{NS}.{r['id']}.active"] = a_txt + f" Cooldown {a_cd} s."
        lang[f"item.{NS}.{r['id']}.passive"] = p_txt
        names[r["id"]] = dict(diaithiris=r["dn"], english=r["en"])
        gates[f"{NS}:{r['id']}"] = r["tier"]
    lang["tooltip.diaithirrelics.active"] = "Active"
    lang["tooltip.diaithirrelics.passive"] = "Passive"
    lang["tooltip.diaithirrelics.worn"] = "Worn in a Curios %s slot"
    lang["itemGroup.diaithirrelics"] = "Iarmadun"
    W(os.path.join(DA, "relics.json"), dict(
        tiers={k: dict(durability=v["dur"], speed=v["speed"], bonus=v["bonus"], enchant=v["ench"], incorrect=v["inc"], spear=v["spear"],
                       repair=f"{NS}:repair/{k.lower()}") for k, v in TIERS.items()},
        items=items))
    W(os.path.join(AS, "lang", "en_us.json"), lang)
    W(os.path.join(ROOT, "names_map.json"), names)
    W(os.path.join(ROOT, "age_gates.json"), {
        "_doc": "Same shape as DiaithirRPG content/tech_ages.json: namespaces and per-item entries (items beat namespaces). Relics belong to Ages III to VII by the age of the metal and the tale.",
        "namespaces": {NS: "III"}, "items": gates})

    # textures
    for r in R:
        MAKE[r["kind"]](r, rng)

    # models and item definitions
    def model(name, obj):
        W(os.path.join(AS, "models", "item", f"{name}.json"), obj)

    def m(name):
        return {"type": "minecraft:model", "model": f"{NS}:item/{name}"}

    for r in R:
        iid, kind = r["id"], r["kind"]
        parent = "minecraft:item/handheld" if kind in ("sword", "axe", "dagger", "hammer") else "minecraft:item/generated"
        if kind == "spear":
            model(iid, {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{iid}"}})
            model(iid + "_in_hand", {"parent": "minecraft:item/spear_in_hand", "textures": {"layer0": f"{NS}:item/{iid}_in_hand"}})
            W(os.path.join(AS, "items", f"{iid}.json"), {
                "model": {"type": "minecraft:select", "property": "minecraft:display_context",
                          "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": m(iid)}], "fallback": m(iid + "_in_hand")},
                "swap_animation_scale": 1.95})
            continue
        obj = {"parent": parent, "textures": {"layer0": f"{NS}:item/{iid}"}}
        if kind == "shield":
            obj["display"] = {
                "firstperson_lefthand": {"rotation": [0, -90, 0], "translation": [2, 3, 2], "scale": [1.3, 1.3, 0.7]},
                "firstperson_righthand": {"rotation": [0, -90, 0], "translation": [2, 3, 2], "scale": [1.3, 1.3, 0.7]},
                "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [1.1, 1.1, 0.7]},
                "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 3, 1], "scale": [1.1, 1.1, 0.7]}}
        model(iid, obj)
        W(os.path.join(AS, "items", f"{iid}.json"), {"model": m(iid)})

    # tags
    def tag(path, values):
        W(os.path.join(RES, "data", path + ".json"), {"replace": False, "values": values})

    sel = lambda *ks: [f"{NS}:{r['id']}" for r in R if r["kind"] in ks]
    tag("minecraft/tags/item/swords", sel("sword", "dagger"))
    tag("minecraft/tags/item/axes", sel("axe", "hammer"))
    tag("minecraft/tags/item/spears", sel("spear"))
    tag("minecraft/tags/item/enchantable/sword", sel("sword", "dagger"))
    tag("minecraft/tags/item/enchantable/melee_weapon", sel("sword", "dagger", "axe", "hammer", "spear"))
    tag("minecraft/tags/item/enchantable/sharp_weapon", sel("sword", "dagger", "axe", "hammer"))
    tag("minecraft/tags/item/enchantable/weapon", sel("sword", "dagger", "axe", "hammer", "spear"))
    tag("minecraft/tags/item/enchantable/equippable", sel("shield"))
    tag("minecraft/tags/item/enchantable/durability", sel("sword", "dagger", "axe", "hammer", "spear", "shield"))
    for slot in ("ring", "necklace", "charm", "belt"):
        tag(f"curios/tags/item/{slot}", [f"{NS}:{r['id']}" for r in R if r["kind"] == slot])
    # Curios: make sure players (and armour stands, for the self test) have the four slots the trinkets use; additive with other packs' data
    W(os.path.join(RES, "data", "curios", "curios", "entities", "diaithir_relics.json"),
      {"entities": ["#curios:player_like"], "slots": ["ring", "necklace", "charm", "belt"]})
    for k, v in TIERS.items():
        tag(f"{NS}/tags/item/repair/{k.lower()}", [v["repair"]])
    for nm in ("bronze", "steel"):
        tag(f"c/tags/item/ingots/{nm}", [])

    # recipes for the crafted relics
    def ing(s):
        return s

    pats = {
        "sword": ([" B ", "BAB", " S "], True), "axe": (["BB ", "BA ", " S "], True), "spear": (["  B", " A ", "S  "], True),
        "dagger": (["B", "A", "S"], True), "hammer": (["BAB", " S ", " S "], True),
        "shield": (["BAB", "BWB", " B "], False), "ring": ([" B ", "BAB", " B "], False), "necklace": (["BBB", "B B", " A "], False),
        "charm": ["B B", " A ", "B B"], "belt": (["LLL", "BAB"], False),
    }
    for r in R:
        if r["src"] != "craft":
            continue
        B, A = r["rec"]
        pat = pats[r["kind"]]
        pat, stick = (pat if isinstance(pat, tuple) else (pat, False))
        key = {"B": B, "A": A}
        if stick:
            key["S"] = "#c:rods/wooden"
        if r["kind"] == "shield":
            key["W"] = "#minecraft:planks"
        if r["kind"] == "belt":
            key["L"] = "minecraft:leather"
        W(os.path.join(DA, "recipe", f"{r['id']}.json"), {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pat,
                                                           "key": key, "result": {"id": f"{NS}:{r['id']}", "count": 1}})

    # loot: one table per Age (the Age's loot-only relics), and global loot modifiers adding it to structure chests
    modifiers = 0
    for tier in TIERS:
        loot_ids = [r["id"] for r in R if r["tier"] == tier and r["src"] == "loot"]
        if not loot_ids:
            continue
        W(os.path.join(DA, "loot_table", "chests", f"age_{tier.lower()}.json"), {
            "type": "minecraft:chest",
            "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"{NS}:{i}", "weight": 1} for i in loot_ids]}]})
        for tbl in LOOT[tier]:
            ns, path = tbl.split(":")
            nm = f"{tier.lower()}_{ns}_{path.replace('/', '_')}"
            W(os.path.join(DA, "loot_modifiers", f"{nm}.json"), {
                "type": "neoforge:add_table",
                "conditions": [{"condition": "neoforge:loot_table_id", "loot_table_id": tbl},
                               {"condition": "minecraft:random_chance", "chance": LOOT_CHANCE[tier]}],
                "table": f"{NS}:chests/age_{tier.lower()}"})
            modifiers += 1

    # library and codex
    ageword = {"III": "Age III", "IV": "Age IV", "V": "Age V", "VI": "Age VI", "VII": "Age VII"}
    lib.append(["Iarmadun", "concept", 3, "relics",
                "Twenty-six unique relics of Ages III to VII: heroes' weapons, shields and the trinkets of the otherworld. Weapons and shields are held; rings, torques, charms and belts sit in Curios slots. Each has one active or passive power; crafted ones need a catalyst, the rest turn up in structure chests."])
    for r in R:
        tr = TIERS[r["tier"]]
        a_key, a_cd, a_txt = r["active"] if r["active"] else (None, 0, "")
        src = ("Crafted from " + ("1 " if False else "") + "a tier metal and a catalyst" if r["src"] == "craft" else f"Found in structure chests of {ageword[r['tier']]}")
        if r["src"] == "craft":
            B, A = r["rec"]
            src = f"Crafted from {B.split(':')[-1].replace('ingots/', '') } and {A.split(':')[-1].replace('_', ' ')}"
        body = []
        if r["kind"] in ("ring", "necklace", "charm", "belt"):
            body.append(f"Curios {r['slot']} slot.")
        elif r["kind"] != "shield":
            body.append(f"Damage {r['extra']:+g} over the Age's base.")
        if a_key:
            body.append(a_txt + f" Cooldown {a_cd} s.")
        body.append(r["passive"][1])
        body.append(src + f"; {ageword[r['tier']]}.")
        lib.append([r["dn"], "item", tr["lib"], "relics", " ".join(body)])
    W(os.path.join(ROOT, "library", "relics_mechanics.json"), lib)  # numbers only; the lore line is merged in by the batch job
    # batch job for the one-line lore (bulk text goes to the API, not to this session)
    job = "/mnt/project-files/compute/jobs/relics_lore"
    if os.path.isdir("/mnt/project-files/compute/jobs"):
        os.makedirs(job, exist_ok=True)
        sysmsg = ("You write one short tooltip line of lore for a legendary item in a Minecraft RPG set on a 1:5 Earth across seven Ages, "
                  "with Celtic and Irish myth flavour. Plain words, no em dashes, no real people, no franchise names, no invented gods. "
                  "Output only the line, 8 to 20 words, no quotes.")
        with open(os.path.join(job, "in.jsonl"), "w", encoding="utf8") as f:
            for r, row in zip(R, lib[1:]):
                prompt = (f"Mod: relics. Item: {r['en']} (Diaithiris name: {r['dn']}), a {KIND_EN[r['kind']]} of Age {r['tier']}. "
                          f"Its power: {r['passive'][1]} Write the lore line for this item.")
                f.write(json.dumps(dict(id=f"relics:{r['id']}", system=sysmsg, prompt=prompt, name=r["dn"], max_tokens=70,
                                        age=TIERS[r["tier"]]["lib"], mech=row[4]), ensure_ascii=False) + "\n")
        shutil.copy(os.path.join(ROOT, "tools", "relics_merge.py"), os.path.join(job, "merge.py"))
        open(os.path.join(job, "README"), "w").write(
            "Owner: relics thread (diaithir-relics). Rows: the 26 relics (id = relics:<item id>). One lore line each.\n"
            "Check rule: 8 to 20 words, no em dash, no quotes. Failing rows go to Claude Batch. max_tokens 70.\n"
            "Merge: python3 /mnt/project-files/compute/jobs/relics_lore/merge.py (needs out.jsonl; writes library-sync/relics.json as "
            "[name, 'item', age, 'relics', lore + mechanics] plus the 'Iarmadun' concept row, and the module's tools/lore.json id->lore "
            "if the repo path /home/user/wt/g2/diaithir-relics exists; rerun tools/gen.py afterwards for the tooltips and handbook).\n")
    chapters = []
    for tier in TIERS:
        ch = dict(tier=tier, name=f"Iarmadun {ageword[tier]}", age=tier, text="", items=[])
        for r in R:
            if r["tier"] == tier:
                ch["items"].append(dict(id=f"{NS}:{r['id']}", name=r["dn"], age=tier, text=(r["lore"] + " " if r["lore"] else "") + r["passive"][1]))
        chapters.append(ch)
    W(os.path.join(ROOT, "codex", "relics_handbook.json"), dict(id=f"{NS}:handbook", title="Iarmadun", chapters=chapters))
    print("relics", len(R), "loot modifiers", modifiers)


if __name__ == "__main__":
    main()
