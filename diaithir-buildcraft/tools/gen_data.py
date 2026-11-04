"""Writes blockstates, models, item defs, recipes, loot, tags and names for diaithir-buildcraft from Materials.java.
Run: python3 -I tools/gen_data.py. Deterministic. Compat: blocks join minecraft stairs/slabs/trapdoors/mineable tags and keep old Macaw ids in data/.../migration.json."""
import json, re
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1] / "src/main"
A = ROOT / "resources/assets/diaithirbuildcraft"; D = ROOT / "resources/data/diaithirbuildcraft"; T = ROOT / "resources/data/minecraft/tags/block"
NS = "diaithirbuildcraft"
src = (ROOT / "java/net/diaithir/buildcraft/Materials.java").read_text()
def table(name):
    body = src.split(f"{name} = List.of(")[1].split(");")[0]
    return re.findall(r'new Mat\("(\w+)", "(\w+)", "(\w+)", "(\w*)"\)', body)
WOODS, STONES = table("WOODS"), table("STONES")
def dump(p, d):
    p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(d, indent=1, ensure_ascii=False) + "\n")
def drop(n, slab=False):
    e = {"type": "minecraft:item", "name": f"{NS}:{n}"}
    if slab: e["functions"] = [{"function": "minecraft:set_count", "count": 2, "conditions": [{"condition": "minecraft:block_state_property", "block": f"{NS}:{n}", "properties": {"type": "double"}}]}, {"function": "minecraft:explosion_decay"}]
    return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{n}", "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}], "entries": [e]}]}
def item(n, m): dump(A / f"items/{n}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{m}"}})
def stairs_state(n):
    v = {}
    for f, y0 in (("east", 0), ("south", 90), ("west", 180), ("north", 270)):
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                model = {"straight": "", "inner_left": "_inner", "inner_right": "_inner", "outer_left": "_outer", "outer_right": "_outer"}[shape]
                y = y0 + (-90 if shape.endswith("left") and shape != "straight" else 0)
                if half == "top": y += 90 if shape.endswith("left") or shape == "straight" else 0
                e = {"model": f"{NS}:block/{n}{model}", "y": y % 360}
                if half == "top": e["x"] = 180
                if y % 360 or half == "top": e["uvlock"] = True
                v[f"facing={f},half={half},shape={shape}"] = e
    return {"variants": v}
lang, axe, pick = {}, [], []
for kind, mats in (("wood", WOODS), ("stone", STONES)):
    for mid, tex, blk, st in mats:
        eng = mid.replace("_", " ").title()
        tx = {"bottom": f"minecraft:block/{tex}", "top": f"minecraft:block/{tex}", "side": f"minecraft:block/{tex}"}
        (axe if kind == "wood" else pick)
        for fam, label in (("roof", "Roof"), ("paving", "Paving")) + ((("shutter", "Shutter"),) if kind == "wood" else ()):
            n = f"{mid}_{fam}"; lang[f"block.{NS}.{n}"] = f"{eng} {label}"; (axe if kind == "wood" else pick).append(f"{NS}:{n}")
            dump(D / f"loot_table/blocks/{n}.json", drop(n, fam == "paving"))
            if fam == "roof":
                dump(A / f"blockstates/{n}.json", stairs_state(n))
                for suf, par in (("", "stairs"), ("_inner", "inner_stairs"), ("_outer", "outer_stairs")):
                    dump(A / f"models/block/{n}{suf}.json", {"parent": f"minecraft:block/{par}", "textures": tx})
                item(n, n)
                dump(D / f"recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"P": f"minecraft:{tex}"}, "pattern": ["P  ", "PP ", "PPP"], "result": {"id": f"{NS}:{n}", "count": 4}})
            elif fam == "paving":
                dump(A / f"blockstates/{n}.json", {"variants": {"type=bottom": {"model": f"{NS}:block/{n}"}, "type=top": {"model": f"{NS}:block/{n}_top"}, "type=double": {"model": f"minecraft:block/{tex}"}}})
                dump(A / f"models/block/{n}.json", {"parent": "minecraft:block/slab", "textures": tx}); dump(A / f"models/block/{n}_top.json", {"parent": "minecraft:block/slab_top", "textures": tx})
                item(n, n)
                dump(D / f"recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"P": f"minecraft:{tex}"}, "pattern": ["PPP"], "result": {"id": f"{NS}:{n}", "count": 6}})
            else:
                v = {}
                for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
                    for half in ("bottom", "top"):
                        for op in ("false", "true"):
                            mod = {"bottom": "bottom", "top": "top"}[half] if op == "false" else "open"
                            e = {"model": f"{NS}:block/{n}_{mod}"}
                            if op == "false" or half == "top": pass
                            if op == "true": e["y"] = (y + 180) % 360 if half == "bottom" else (y + 180) % 360
                            elif y: e["y"] = y
                            v[f"facing={f},half={half},open={op},powered=false"] = e; v[f"facing={f},half={half},open={op},powered=true"] = e
                dump(A / f"blockstates/{n}.json", {"variants": v})
                for s in ("bottom", "top", "open"): dump(A / f"models/block/{n}_{s}.json", {"parent": f"minecraft:block/template_trapdoor_{s}", "textures": {"texture": f"minecraft:block/{tex}"}})
                item(n, n + "_bottom")
                dump(D / f"recipe/{n}.json", {"type": "minecraft:crafting_shaped", "category": "building", "key": {"P": f"minecraft:{tex}"}, "pattern": ["PPP", "PPP"], "result": {"id": f"{NS}:{n}", "count": 3}})
dump(A / "lang/en_us.json", dict(sorted(lang.items())))
allb = [k.split(".")[-1] for k in lang]
dump(T / "mineable/axe.json", {"values": axe}); dump(T / "mineable/pickaxe.json", {"values": pick})
dump(T / "stairs.json", {"values": [f"{NS}:{b}" for b in allb if b.endswith("_roof")]})
dump(T / "slabs.json", {"values": [f"{NS}:{b}" for b in allb if b.endswith("_paving")]})
dump(T.parent / "item/stairs.json", {"values": [f"{NS}:{b}" for b in allb if b.endswith("_roof")]})
dump(T.parent / "item/slabs.json", {"values": [f"{NS}:{b}" for b in allb if b.endswith("_paving")]})
dump(T / "wooden_trapdoors.json", {"values": [f"{NS}:{b}" for b in allb if b.endswith("_shutter")]})
dump(D / "migration.json", {"note": "old id -> new id, consumed by diaithir-compat id migration", "map": {**{f"mcw_roofs:{m[0]}_roof": f"{NS}:{m[0]}_roof" for m in WOODS + STONES}, **{f"mcw_paths:{m[0]}_paving": f"{NS}:{m[0]}_paving" for m in WOODS + STONES}, **{f"mcw_trapdoors:{m[0]}_shutter": f"{NS}:{m[0]}_shutter" for m in WOODS}}})
print(len(allb), "blocks")
