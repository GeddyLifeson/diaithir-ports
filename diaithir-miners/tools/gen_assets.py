#!/usr/bin/env python3
"""Writes every asset and data file for diaithir-miners: 64 px Industrial Factory textures drawn from scratch
(no pixels from Advanced Ore Deposits, FactoriOres or Factorio), models, blockstates, items, lang, recipes, loot."""
import json, os, sys
import numpy as np
from PIL import Image

NS = "diaithirminers"
R = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
A, D = f"{R}/assets/{NS}", f"{R}/data/{NS}"
S = 64
rng = np.random.default_rng(7)

# type: (name, ore colour, source item, result item, lang)
ORES = {
    "iron": ("Iron", (118, 132, 150), "minecraft:raw_iron"),
    "copper": ("Copper", (196, 104, 56), "minecraft:raw_copper"),
    "gold": ("Gold", (232, 176, 52), "minecraft:raw_gold"),
    "diamond": ("Diamond", (80, 230, 255), "minecraft:diamond"),
    "coal": ("Coal", (38, 34, 32), "minecraft:coal"),
    "andesite": ("Andesite", (130, 130, 128), "minecraft:andesite"),
    "redstone": ("Redstone", (170, 40, 36), "minecraft:redstone"),
    "lapis": ("Lapis Lazuli", (52, 84, 150), "minecraft:lapis_lazuli"),
    "nether_quartz": ("Nether Quartz", (222, 214, 200), "minecraft:quartz"),
}
STEEL, STEEL_D, YEL, RUST, DIRT = (92, 94, 96), (58, 60, 62), (196, 152, 40), (120, 62, 30), (98, 74, 50)


def periodic(scale):
    """Seamless value noise over the 64 px block."""
    n = max(2, S // scale)
    g = rng.random((n, n))
    idx = np.arange(S) * n / S
    i0 = idx.astype(int)
    t = idx - i0
    t = t * t * (3 - 2 * t)
    a = g[np.ix_(i0 % n, i0 % n)]; b = g[np.ix_(i0 % n, (i0 + 1) % n)]
    c = g[np.ix_((i0 + 1) % n, i0 % n)]; d = g[np.ix_((i0 + 1) % n, (i0 + 1) % n)]
    tx = t[None, :]; ty = t[:, None]
    return (a * (1 - tx) + b * tx) * (1 - ty) + (c * (1 - tx) + d * tx) * ty


def img(arr):
    return Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA" if arr.shape[2] == 4 else "RGB")


def terrain():
    n = periodic(16) * .5 + periodic(6) * .3 + periodic(2) * .2
    base = np.array(DIRT, float)[None, None, :] * (.7 + .6 * n[..., None])
    crack = periodic(4) < .12
    base[crack] *= .55
    return base


def facets(rows, cols, color, count, rmax, rmin):
    """Angular flat-faceted rocks (biggest in the middle) painted in the ore colour."""
    yy, xx = np.mgrid[0:rows, 0:cols]
    out = np.zeros((rows, cols, 4))
    pts = []
    for k in range(count):
        r = rmin + (rmax - rmin) * (1 - k / count)
        pts.append((rng.uniform(cols * .2, cols * .8), rng.uniform(rows * .2, rows * .8), r))
    pts.sort(key=lambda p: -abs(p[0] - cols / 2) - abs(p[1] - rows / 2))
    for cx, cy, r in pts:
        ang = rng.uniform(0, 6.28)
        dx, dy = xx - cx, yy - cy
        u = dx * np.cos(ang) + dy * np.sin(ang)
        v = -dx * np.sin(ang) + dy * np.cos(ang)
        m = (np.abs(u) + np.abs(v) * 1.3) < r
        shade = .75 + .5 * ((u + v) / (2 * r)).clip(-1, 1) * -1 + rng.uniform(-.08, .08)
        col = np.array(color, float) * shade[..., None]
        out[m, :3] = col[m]; out[m, 3] = 255
        edge = m & ((np.abs(u) + np.abs(v) * 1.3) > r - 1.2)
        out[edge, :3] *= .55
    return out


def deposit_tex(color):
    t = terrain()
    f = facets(S, S, color, 16, 11, 3)
    # keep heaps off the rim so the tile still reads as a patch cell
    m = f[..., 3:4] / 255
    t = t * (1 - m) + f[..., :3] * m
    grit = rng.random((S, S)) < .03
    t[grit] = np.array(color) * .8
    return img(t)


def chunk_tex(color):
    f = facets(16 * 4, 16 * 4, color, 6, 15, 7)
    return img(f)


def steel_face(hazard=False, grime=.5):
    n = periodic(8) * .5 + periodic(3) * .5
    p = np.array(STEEL, float)[None, None, :] * (.75 + .5 * n[..., None])
    drip = (periodic(32) > .55) & (np.arange(S)[:, None] > 0)
    p[drip] *= .8
    rust = periodic(8) > .62
    p[rust] = p[rust] * .5 + np.array(RUST, float) * .5
    p[:2] = STEEL_D; p[-2:] = STEEL_D; p[:, :2] = STEEL_D; p[:, -2:] = STEEL_D
    for bx in (6, S - 7):
        for by in (6, S - 7):
            yy, xx = np.mgrid[0:S, 0:S]
            p[(xx - bx) ** 2 + (yy - by) ** 2 < 10] = (130, 130, 128)
    if hazard:
        for x in range(S):
            for y in range(S - 9, S - 3):
                if ((x + y) // 6) % 2 == 0:
                    p[y, x] = YEL
                else:
                    p[y, x] = (30, 28, 26)
    return p


def miner_textures(kind):
    side = steel_face(hazard=True)
    side[18:34, 12:52] = (40, 38, 36)  # intake slot
    if kind == "burner":
        yy, xx = np.mgrid[0:S, 0:S]
        side[(xx - 32) ** 2 + (yy - 26) ** 2 < 36] = (24, 22, 20)
    else:
        for i in range(3):
            side[20 + i * 5: 22 + i * 5, 16:48] = (80, 230, 255) if i == 0 else (60, 62, 64)
    top = steel_face()
    top[16:48, 16:48] = (50, 52, 54)
    yy, xx = np.mgrid[0:S, 0:S]
    top[(xx - 32) ** 2 + (yy - 32) ** 2 < 100] = (30, 28, 26)
    bottom = steel_face()
    on = side.copy()
    on[18:34, 12:52] = (255, 150, 50) if kind == "burner" else (80, 230, 255)
    return {"side": side, "top": top, "bottom": bottom, "side_on": on}


def save(im, rel):
    p = f"{A}/textures/{rel}.png"
    os.makedirs(os.path.dirname(p), exist_ok=True)
    im.save(p)


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)


def main():
    lang = {f"itemGroup.{NS}": "Diaithir Miners", "tooltip.diaithirminers.deposit": "Stores a finite surface deposit reserve.",
            f"block.{NS}.burner_miner": "Burner Miner", f"block.{NS}.electric_miner": "Electric Miner",
            f"item.{NS}.drill_head": "Drill Head"}
    for k, (nm, col, res) in ORES.items():
        save(deposit_tex(col), f"block/{k}_deposit")
        save(chunk_tex(col), f"item/raw_{k}_chunk")
        wj(f"{A}/blockstates/{k}_deposit.json", {"variants": {"": {"model": f"{NS}:block/{k}_deposit"}}})
        wj(f"{A}/models/block/{k}_deposit.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{k}_deposit"}})
        wj(f"{A}/models/item/{k}_deposit.json", {"parent": f"{NS}:block/{k}_deposit"})
        wj(f"{A}/items/{k}_deposit.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{k}_deposit"}})
        wj(f"{A}/models/item/raw_{k}_chunk.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/raw_{k}_chunk"}})
        wj(f"{A}/items/raw_{k}_chunk.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/raw_{k}_chunk"}})
        lang[f"block.{NS}.{k}_deposit"] = f"{nm} Deposit"
        lang[f"item.{NS}.raw_{k}_chunk"] = f"Raw {nm} Chunk"
        lang[f"deposit_type.{NS}.{k}"] = nm
        wj(f"{D}/recipe/{k}_from_raw_chunks.json", {"type": "minecraft:crafting_shaped", "category": "misc",
            "pattern": ["CCC", "C C", "CCC"], "key": {"C": f"{NS}:raw_{k}_chunk"}, "result": {"id": res}})
        wj(f"{D}/loot_table/blocks/{k}_deposit.json", {"type": "minecraft:block", "pools": []})
    for kind in ("burner", "electric"):
        tx = miner_textures(kind)
        for part, a in tx.items():
            save(img(a), f"block/{kind}_miner_{part}")
        for st, side in (("", "side"), ("_on", "side_on")):
            wj(f"{A}/models/block/{kind}_miner{st}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
                "top": f"{NS}:block/{kind}_miner_top", "bottom": f"{NS}:block/{kind}_miner_bottom",
                "side": f"{NS}:block/{kind}_miner_{side}"}})
        wj(f"{A}/blockstates/{kind}_miner.json", {"variants": {"enabled=false": {"model": f"{NS}:block/{kind}_miner"},
            "enabled=true": {"model": f"{NS}:block/{kind}_miner_on"}}})
        wj(f"{A}/models/item/{kind}_miner.json", {"parent": f"{NS}:block/{kind}_miner"})
        wj(f"{A}/items/{kind}_miner.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{kind}_miner"}})
        wj(f"{D}/loot_table/blocks/{kind}_miner.json", {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
            {"type": "minecraft:item", "name": f"{NS}:{kind}_miner"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
    # drill head: steel cone with hazard band
    dh = np.zeros((64, 64, 4))
    yy, xx = np.mgrid[0:64, 0:64]
    cone = (np.abs(xx - 32) < (60 - yy) * .45) & (yy > 6) & (yy < 58)
    dh[cone, :3] = np.array(STEEL_D) * (.8 + .4 * periodic(4)[cone][:, None]); dh[cone, 3] = 255
    band = cone & (yy > 14) & (yy < 22)
    dh[band, :3] = YEL; dh[band, 3] = 255
    save(img(dh), "item/drill_head")
    wj(f"{A}/models/item/drill_head.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/drill_head"}})
    wj(f"{A}/items/drill_head.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/drill_head"}})
    wj(f"{D}/recipe/drill_head.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" A ", "III", " I "],
        "key": {"I": "minecraft:iron_ingot", "A": "minecraft:andesite"}, "result": {"id": f"{NS}:drill_head"}})
    wj(f"{D}/recipe/burner_miner.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" S ", "nFn", " D "],
        "key": {"S": "minecraft:cobblestone", "F": "minecraft:furnace", "D": f"{NS}:drill_head", "n": "minecraft:iron_nugget"},
        "result": {"id": f"{NS}:burner_miner"}})
    wj(f"{D}/recipe/electric_miner.json", {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": [" B ", "nRn", " D "],
        "key": {"B": "minecraft:iron_block", "R": "minecraft:redstone_block", "D": f"{NS}:drill_head", "n": "minecraft:iron_nugget"},
        "result": {"id": f"{NS}:electric_miner"}})
    wj(f"{R}/data/minecraft/tags/block/mineable/pickaxe.json", {"replace": False, "values": [f"{NS}:{k}_deposit" for k in ORES] + [f"{NS}:burner_miner", f"{NS}:electric_miner"]})
    wj(f"{R}/data/minecraft/tags/block/needs_stone_tool.json", {"replace": False, "values": [f"{NS}:burner_miner", f"{NS}:electric_miner"]})
    # old ids -> new ids (read by DiaithirMiners id migration)
    wj(f"{D}/id_migration.json", {"namespace_from": "advanced_ore_deposits", "namespace_to": NS})
    wj(f"{A}/lang/en_us.json", lang)


main()
