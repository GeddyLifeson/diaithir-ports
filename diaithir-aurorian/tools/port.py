#!/usr/bin/env python3
"""Port data from shiroroku/TheAurorian (MIT, 1.19.2 Forge) into this 26.1.2 NeoForge module.

usage: port.py <TheAurorian checkout> <module dir>
Writes data/theaurorian/aurorian.json, converts recipes, tags, loot tables, worldgen, models, textures, lang.
Behaviour-heavy items and blocks (see DEFERRED) are not registered; the README lists them.
"""
import json, re, shutil, sys
from pathlib import Path

SRC, OUT = Path(sys.argv[1]), Path(sys.argv[2])
MAIN, GEN = SRC / "src/main/resources", SRC / "src/generated/resources"
JAVA = SRC / "src/main/java/shiroroku/theaurorian"
NS = "theaurorian"
RES = OUT / "src/main/resources"
A, D = RES / "assets" / NS, RES / "data" / NS

TOOL_KINDS = ("sword", "axe", "pickaxe", "shovel", "hoe", "greatsword")
SLOTS = {"helmet": "helmet", "chestplate": "chestplate", "leggings": "leggings", "boots": "boots"}
LEVEL_TAG = {0: "wood", 1: "stone", 2: "iron", 3: "diamond"}
EFFECTS = {"DAMAGE_RESISTANCE": "resistance", "DAMAGE_BOOST": "strength", "MOVEMENT_SPEED": "speed", "MOVEMENT_SLOWDOWN": "slowness", "DIG_SPEED": "haste", "JUMP": "jump_boost", "HEAL": "instant_health", "HARM": "instant_damage", "CONFUSION": "nausea"}
AGE = {"SILENTWOOD": "I", "AURORIAN_STONE": "I", "CERULEAN": "II", "MOONSTONE": "II", "KNIGHT": "II", "SPECTRAL": "III",
       "AURORIAN_STEEL": "III", "AURORIANITE": "IV", "UMBRA": "IV", "CRYSTALLINE": "IV"}
# Items whose behaviour lives in Java we have not ported yet; they are listed, not registered.
DEFERRED_ITEMS = {"absorption_orb", "living_divining_rod", "mirror_of_guidance", "amulet_of_chroma", "emerald_amulet", "keepers_amulet",
                  "ruby_amulet", "sapphire_amulet", "cerulean_arrow", "crystal_arrow", "strange_meat", "silentwood_bow",
                  "umbra_chestplate", "umbra_greatsword", "crystalline_sword", "spectral_helmet", "spectral_chestplate",
                  "spectral_leggings", "spectral_boots"}
DEFERRED_BLOCK_CLASS = {"AurorianPortal", "AurorianPortalFrame",
                        "AurorianFurnaceBlock", "BossSpawnerBlock", "MoonlightForgeBlock", "ScrapperBlock", "SilentwoodChestBlock",
                        "SilentwoodCraftingTableBlock", "ChimneyBlock", "CrystalBlock", "FogWallBlock", "SaplingBlock"}
BLOCK_KIND = {"Block": "full", "DungeonGateKeyHole": "keyhole", "AurorianGrassBlock": "full", "AurorianDeepslateBlock": "full", "SlabBlock": "slab", "StairBlock": "stairs", "WallBlock": "wall", "FenceBlock": "fence",
              "IronBarsBlock": "bars", "RotatedPillarBlock": "log", "LeavesBlock": "leaves", "BushBlock": "plant", "TallGrassBlock": "plant"}


def num(s, default=0.0):
    try:
        return float(s.strip().rstrip("fF"))
    except ValueError:
        return default


def parse_items():
    s = (JAVA / "Registry/ItemRegistry.java").read_text()
    items = []
    for m in re.finditer(r'ITEMS(\w*)\.register\("(\w+)",\s*(.*?)\);\n', s, re.S):
        group, iid, body = m.groups()
        items.append((group, iid, body))
    return items


def parse_tiers():
    s = (JAVA / "Registry/MaterialTiers.java").read_text()
    tools, armor = {}, {}
    for m in re.finditer(r'ForgeTier (\w+) = new ForgeTier\((\d+), (\d+), ([\d.]+)f?, ([\d.]+)f?, (\d+), [\w.]+, \(\) -> Ingredient\.of\(([\w.]+)\.get\(\)\)', s):
        n, lvl, dur, spd, bonus, ench, rep = m.groups()
        rid = rep.split(".")[1]
        tools[n] = dict(level=int(lvl), durability=int(dur), speed=float(spd), bonus=float(bonus), enchant=int(ench), repair=NS + ":" + rid)
    for m in re.finditer(r'ArmorMaterial (\w+) = armorBuilder\((?:(\w+), )?"(\w+)", (\d+), new int\[\]\{([\d, ]+)\}, (?:(\d+), )?SoundEvents\.ARMOR_EQUIP_(\w+), ([\d.]+)F, ([\d.]+)F(?:, \(\) -> Ingredient\.(\w+)(?:\(([\w.]+)\.get\(\)\))?)?', s):
        n, tier, name, mult, prot, ench, snd, tough, kb, ing, rep = m.groups()
        p = [int(x) for x in prot.split(",")]
        e = tools[tier]["enchant"] if tier else int(ench)
        if tier:
            r = tools[tier]["repair"]
        elif rep:
            r = NS + ":" + rep.split(".")[1]
        else:
            r = None
        armor[n] = dict(name=name, durability=int(mult), boots=p[0], legs=p[1], chest=p[2], helmet=p[3], enchant=e,
                        sound=snd.lower(), toughness=float(tough), knockback=float(kb), repair=r)
    return tools, armor


def tool_class(sel_id):
    for k in ("pickaxe", "shovel", "hoe", "greatsword", "sword", "axe"):
        if sel_id.endswith("_" + k):
            return k
    return None


def build_items(tools, armor):
    out = []
    used_t, used_a = set(), set()
    for group, iid, body in parse_items():
        if iid in DEFERRED_ITEMS or group in ("_SPAWN_EGGS", "_GEN_TEA") and False:
            continue
        mat = re.search(r"MaterialTiers\.(\w+)", body)
        mat = mat.group(1) if mat else None
        rarity = re.search(r"Rarity\.(\w+)", body)
        dur = re.search(r"durability\((\d+)\)", body)
        burn = re.search(r"basicItemWithBurntime\(.*?, (\d+)\)", body)
        if group == "_SPAWN_EGGS" or "BaseAurorianArrow" in body or "Curio" in body:
            continue
        spec = dict(id=iid)
        if rarity and rarity.group(1) != "COMMON":
            spec["rarity"] = rarity.group(1).lower()
        if "TEA" in group:
            spec["kind"] = "drink"
        effs = re.findall(r"effect\(\(\) -> new MobEffectInstance\(MobEffects\.(\w+), (\d+)(?:, (\d+))?\), ([\d.]+)F?\)", body)
        if effs:
            spec["effects"] = [dict(id=EFFECTS.get(e, e.lower()), ticks=int(t), amp=int(a or 0), chance=float(c)) for e, t, a, c in effs]
        food = re.search(r"nutrition\((\d+)\)\.saturationMod\(([\d.]+)F\)", body)
        if food:
            spec.setdefault("kind", "food")
            spec["nutrition"] = int(food.group(1))
            spec["saturation"] = round(int(food.group(1)) * float(food.group(2)) * 2, 2)
            spec["fast"] = ".fast()" in body
        if spec.get("kind") == "drink" and "nutrition" not in spec:
            spec["nutrition"], spec["saturation"], spec["fast"] = 0, 0.0, True
        if "_SHIELD" in group or iid.endswith("_shield"):
            spec["kind"], spec["mat"] = "shield", mat
            spec["durability"] = int(dur.group(1)) if dur else tools[mat]["durability"] * 2 if mat in tools else 336
            used_t.add(mat)
        elif iid.endswith(tuple("_" + k for k in SLOTS)):
            slot = iid.rsplit("_", 1)[1]
            amat = re.search(r"MaterialTiers\.(\w+_ARMOR)", body).group(1)
            spec.update(kind="armor", mat=amat, slot=SLOTS[slot])
            used_a.add(amat)
        elif tool_class(iid) and mat and mat in tools:
            k = tool_class(iid)
            nums = re.search(r"MaterialTiers\.\w+,\s*(-?[\d.]+)F?,\s*(-?[\d.]+)F?", body)
            spec.update(kind="sword" if k == "greatsword" else k, mat=mat)
            if nums:
                spec["damage"], spec["speed"] = num(nums.group(1)), num(nums.group(2))
            used_t.add(mat)
        elif iid.endswith("_sickle"):
            continue
        else:
            spec.setdefault("kind", "item")
            if dur:
                spec["durability"] = int(dur.group(1))
            if "Rarity" not in body and False:
                pass
        if burn:
            spec["burn"] = int(burn.group(1))
        if iid.startswith(("silentwood_",)) and "200" in body and spec["kind"] in TOOL_KINDS:
            spec["burn"] = 200
        if iid.endswith("_key") and "durability" in spec:
            spec["kind"] = "item"
        out.append(spec)
    return out, used_t, used_a


def build_blocks():
    s = (JAVA / "Registry/BlockRegistry.java").read_text()
    blocks, deferred = [], []
    for m in re.finditer(r'regBlockItem(?:WithBurntime)?\(BLOCKS\w*, "(\w+)", \(\) -> new (\w+)\(([^\n]*)\)(?:, (\d+))?\);\n', s):
        bid, cls, args, burn = m.groups()
        if cls in DEFERRED_BLOCK_CLASS or False:
            deferred.append(bid)
            continue
        kind = BLOCK_KIND.get(cls)
        if not kind:
            deferred.append(bid)
            continue
        cp = re.search(r"copy\((Blocks\.(\w+)|(?:BlockRegistry\.)?(\w+)\.get\(\))\)", args)
        spec = dict(id=bid, kind=kind)
        if cp:
            spec["copy"] = ("minecraft:" + cp.group(2).lower()) if cp.group(2) else NS + ":" + cp.group(3)
        if "strength(-1.0F" in args:
            spec["unbreakable"] = True
        lt = re.search(r"lightLevel\(\(state\) -> (\d+)\)", args)
        if lt:
            spec["light"] = int(lt.group(1))
        if burn:
            spec["burn"] = int(burn)
        if cls == "DungeonGateKeyHole":
            spec["key"] = NS + ":" + re.search(r"ItemRegistry\.(\w+)", args).group(1)
            spec["lockpick"] = bool(re.search(r"\btrue\)", args))
        if cls == "StairBlock":
            base = re.search(r"(?:Blocks\.(\w+)::defaultBlockState|(\w+)\.get\(\)\.defaultBlockState\(\))", args)
            spec["base"] = ("minecraft:" + base.group(1).lower()) if base.group(1) else NS + ":" + base.group(2)
        blocks.append(spec)
    ids = {b["id"] for b in blocks}
    keep = []
    for b in blocks:
        deps = [b.get("copy"), b.get("base")]
        if any(d and d.startswith(NS + ":") and d[len(NS) + 1:] not in ids for d in deps):
            deferred.append(b["id"])
        else:
            keep.append(b)
    byid, order, seen = {b["id"]: b for b in keep}, [], set()

    def visit(b):
        if b["id"] in seen:
            return
        seen.add(b["id"])
        for d in (b.get("copy"), b.get("base")):
            if d and d.startswith(NS + ":"):
                visit(byid[d[len(NS) + 1:]])
        order.append(b)
    for b in keep:
        visit(b)
    return order, deferred


def conv_ing(i):
    if isinstance(i, list):
        return [conv_ing(x) for x in i]
    if "item" in i:
        return i["item"]
    if "tag" in i:
        return "#" + conv_tag(i["tag"])
    raise ValueError(i)


def conv_tag(t):
    if t.startswith("#"):
        return "#" + conv_tag(t[1:])
    if t.startswith("forge:"):
        return "c:" + t[6:]
    return t


def conv_recipe(r, known):
    t = r["type"]
    if t == "minecraft:crafting_shaped":
        out = dict(type=t, pattern=r["pattern"], key={k: conv_ing(v) for k, v in r["key"].items()}, result=dict(id=r["result"]["item"], count=r["result"].get("count", 1)))
    elif t == "minecraft:crafting_shapeless":
        out = dict(type=t, ingredients=[conv_ing(x) for x in r["ingredients"]], result=dict(id=r["result"]["item"], count=r["result"].get("count", 1)))
    elif t in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"):
        res = r["result"]
        out = dict(type=t, ingredient=conv_ing(r["ingredient"]), result=dict(id=res if isinstance(res, str) else res["item"]),
                   experience=r.get("experience", 0), cookingtime=r.get("cookingtime", 200))
    else:
        return None
    refs = [out["result"]["id"]] if "result" in out else []
    ing = []
    if "key" in out:
        ing = list(out["key"].values())
    elif "ingredients" in out:
        ing = out["ingredients"]
    else:
        ing = [out["ingredient"]]
    for x in ing:
        for y in (x if isinstance(x, list) else [x]):
            if not y.startswith("#"):
                refs.append(y)
    if any(x.startswith(NS + ":") and x not in known for x in refs):
        return None
    return out


def hexcol(n):
    return "#%06x" % n


def flat_uniform(o):
    """1.19 {"type":"uniform","value":{min,max}} -> 26.1 {"type":"uniform","min_inclusive","max_inclusive"}."""
    if isinstance(o, dict):
        if o.get("type") == "minecraft:uniform" and isinstance(o.get("value"), dict):
            v = o.pop("value")
            o.update(v)
        return {k: flat_uniform(v) for k, v in o.items()}
    if isinstance(o, list):
        return [flat_uniform(x) for x in o]
    return o


def convert_worldgen(known):
    wg = D / "worldgen"
    if not wg.exists():
        return
    # dimension type: 26.1 moved sky and fog to attributes and time to clocks; this one is fixed eternal night
    dt = json.loads((D / "dimension_type/the_aurorian.json").read_text())
    (D / "dimension_type/the_aurorian.json").write_text(json.dumps({
        "ambient_light": dt["ambient_light"],
        "attributes": {
            "minecraft:gameplay/bed_rule": {"can_set_spawn": "always", "can_sleep": "never", "explodes": False},
            "minecraft:gameplay/respawn_anchor_works": False,
            "minecraft:visual/ambient_light_color": "#1a1530",
            "minecraft:visual/fog_color": "#c0c0ff",
            "minecraft:visual/sky_color": "#79a0ff",
            "minecraft:visual/sky_light_color": "#6a5acd",
            "minecraft:visual/sky_light_factor": 0.25},
        "coordinate_scale": 1.0, "has_ceiling": False, "has_ender_dragon_fight": False, "has_fixed_time": True, "has_skylight": True,
        "height": dt["height"], "infiniburn": "#minecraft:infiniburn_overworld", "logical_height": dt["logical_height"],
        "min_y": dt["min_y"], "monster_spawn_block_light_limit": 0,
        "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": 0, "max_inclusive": 7}}, indent=1) + "\n")
    # biomes
    for f in (wg / "biome").glob("*.json"):
        b = json.loads(f.read_text())
        e = b["effects"]
        out = {"has_precipitation": b.get("precipitation", "none") != "none", "temperature": b["temperature"], "downfall": b["downfall"],
               "attributes": {"minecraft:visual/sky_color": hexcol(e["sky_color"]), "minecraft:visual/fog_color": hexcol(e["fog_color"])},
               "effects": {"water_color": hexcol(e["water_color"]), "water_fog_color": hexcol(e["water_fog_color"])},
               "carvers": b["carvers"].get("air", []), "features": b["features"], "spawners": {}, "spawn_costs": {}}
        f.write_text(json.dumps(out, indent=1) + "\n")
    # features: random_patch / flower become simple_block plus placement modifiers
    pf = wg / "placed_feature"
    for f in (wg / "configured_feature").glob("*.json"):
        c = json.loads(f.read_text())
        if c["type"] in ("minecraft:random_patch", "minecraft:flower"):
            cfg = c["config"]
            f.write_text(json.dumps(cfg["feature"]["feature"], indent=1) + "\n")
            pp = json.loads((pf / f.name).read_text())
            pp["placement"] += [{"type": "minecraft:count", "count": cfg["tries"]},
                                {"type": "minecraft:random_offset", "xz_spread": {"type": "minecraft:trapezoid", "min": -cfg["xz_spread"], "max": cfg["xz_spread"], "plateau": 0},
                                 "y_spread": {"type": "minecraft:trapezoid", "min": -cfg["y_spread"], "max": cfg["y_spread"], "plateau": 0}}] + cfg["feature"]["placement"]
            (pf / f.name).write_text(json.dumps(pp, indent=1) + "\n")
        elif c["type"] == "minecraft:forest_rock":
            c = {"type": "minecraft:block_blob", "config": {"can_place_on": {"type": "minecraft:matching_block_tag", "tag": "minecraft:forest_rock_can_place_on"}, "state": c["config"]["state"]}}
            f.write_text(json.dumps(c, indent=1) + "\n")
        elif c["type"] == "minecraft:tree":
            cfg = c["config"]
            dirt = cfg.pop("dirt_provider")
            cfg["below_trunk_provider"] = dirt
            cfg.pop("force_dirt", None)
            f.write_text(json.dumps(flat_uniform(c), indent=1) + "\n")
    for f in pf.glob("*.json"):
        t = f.read_text().replace("theaurorian:silentwood_sapling", "minecraft:spruce_sapling")
        f.write_text(t)
    # noise settings: 26.1 router needs preliminary_surface_level
    ns = wg / "noise_settings/the_aurorian.json"
    n = json.loads(ns.read_text())
    n["noise_router"].setdefault("preliminary_surface_level", {"type": "minecraft:flat_cache", "argument": {"type": "minecraft:cache_2d", "argument": 0.0}})
    ns.write_text(json.dumps(n, indent=1) + "\n")


def write_integration(data, blocks):
    """c: convention tags, Age gates, Codex handbook and the quest chain (data only; the Codex reads them)."""
    ing = {"aurorian_steel": "aurorian_steel_ingot", "aurorianite": "aurorianite_ingot", "cerulean": "cerulean_ingot",
           "crystalline": "crystalline_ingot", "moonstone": "moonstone_ingot", "umbra": "umbra_ingot"}
    nug = {"aurorian_steel": "aurorian_steel_nugget", "cerulean": "cerulean_nugget", "moonstone": "moonstone_nugget"}
    ores = {"cerulean": ["cerulean_ore", "deepslate_cerulean_ore"], "moonstone": ["moonstone_ore", "deepslate_moonstone_ore"], "aurorian_coal": ["aurorian_coal_ore"]}
    have = {i["id"] for i in data["items"]} | {b["id"] for b in blocks}
    def tag(kind, path, vals):
        vals = [NS + ":" + v for v in vals if v in have]
        if vals:
            f = RES / "data/c/tags" / kind / (path + ".json")
            f.parent.mkdir(parents=True, exist_ok=True)
            f.write_text(json.dumps(dict(replace=False, values=vals), indent=1) + "\n")
    for m, i in ing.items():
        tag("item", f"ingots/{m}", [i])
    for m, i in nug.items():
        tag("item", f"nuggets/{m}", [i])
    for m, bl in ores.items():
        tag("block", f"ores/{m}", bl)
        tag("item", f"ores/{m}", bl)
    tag("item", "ores", sum(ores.values(), []))
    tag("block", "ores", sum(ores.values(), []))
    gates = {"namespaces": {NS: "I"}, "items": {}}
    for i in data["items"]:
        m = i.get("mat")
        if i["kind"] in ("sword", "axe", "pickaxe", "shovel", "hoe", "shield"):
            gates["items"][f"{NS}:{i['id']}"] = data["tools"][m]["age"]
        elif i["kind"] == "armor":
            gates["items"][f"{NS}:{i['id']}"] = data["armor"][m]["age"]
    for m, i in ing.items():
        gates["items"][f"{NS}:{i}"] = data["tools"].get(m, {"age": "III"})["age"]
    (OUT / "age_gates.json").write_text(json.dumps(gates, indent=1) + "\n")

    lang = json.loads((MAIN / "assets" / NS / "lang/en_us.json").read_text())
    def nm(i):
        return lang.get(f"item.{NS}.{i}") or lang.get(f"block.{NS}.{i}") or i.replace("_", " ").title()
    tiers = []
    for m, t in data["tools"].items():
        its = [dict(id=f"{NS}:{i['id']}", name=nm(i["id"]), age=t["age"], text="") for i in data["items"] if i.get("mat") == m]
        if its:
            tiers.append(dict(tier=m, name=m.replace("_", " ").title(), age=t["age"], items=its))
    (OUT / "codex").mkdir(exist_ok=True)
    (OUT / "codex/aurorian_handbook.json").write_text(json.dumps(dict(id=NS + ":handbook", title="The Aurorian", chapters=tiers), indent=1, ensure_ascii=False) + "\n")
    # Quest chain from the upstream progression (README: runestone -> aurorianite, darkstone -> umbra, moon temple -> crystalline).
    # "live" is false where the boss, key block or dungeon behaviour is not ported yet; the Codex shows only live quests.
    chain = [
        ("silentwood_age", "Silentwood and Aurorian stone", ["silentwood_pickaxe", "aurorian_stone_pickaxe"], None, True),
        ("ore_hunt", "Cerulean and moonstone", ["cerulean_ingot", "moonstone_ingot"], "silentwood_age", True),
        ("moonstone_gear", "Moonstone and cerulean gear", ["moonstone_sword", "cerulean_chestplate"], "ore_hunt", True),
        ("steel", "Aurorian steel", ["aurorian_steel_sword", "aurorian_steel_chestplate"], "moonstone_gear", True),
        ("runestone_dungeon", "Runestone dungeon and its keeper", ["aurorianite_ingot"], "steel", False),
        ("darkstone_dungeon", "Darkstone dungeon", ["umbra_ingot"], "runestone_dungeon", False),
        ("moon_temple", "Moon temple", ["crystalline_ingot"], "darkstone_dungeon", False)]
    quests = [dict(id=f"{NS}:{q}", title=t, obtain=[f"{NS}:{x}" for x in it], requires=(f"{NS}:{r}" if r else None), live=live) for q, t, it, r, live in chain]
    (OUT / "codex/aurorian_quests.json").write_text(json.dumps(dict(quests=quests), indent=1) + "\n")


def main():
    tools, armor = parse_tiers()
    items, ut, ua = build_items(tools, armor)
    blocks, dblocks = build_blocks()
    known = {NS + ":" + i["id"] for i in items} | {NS + ":" + b["id"] for b in blocks}
    # ingredient items needed by recipes of kept things, plus block items
    data = dict(tools={k.lower(): v for k, v in tools.items() if k in ut}, armor={armor[k]["name"]: armor[k] for k in ua},
                items=[dict(i, mat=(armor[i["mat"]]["name"] if i["kind"] == "armor" else i["mat"].lower()) if "mat" in i else None) for i in items],
                blocks=blocks)
    for i in data["items"]:
        if i["mat"] is None:
            del i["mat"]
    for k, v in data["tools"].items():
        v["incorrect"] = LEVEL_TAG[v["level"]]
        v["age"] = AGE.get(k.upper(), "II")
    for k, v in data["armor"].items():
        v["age"] = AGE.get(k.upper(), "II") if k.upper() in AGE else AGE.get(k.upper().split("_")[0], "II")
    D.mkdir(parents=True, exist_ok=True)
    (D / "aurorian.json").write_text(json.dumps(data, indent=1) + "\n")
    # recipes
    rd = D / "recipe"
    rd.mkdir(exist_ok=True)
    kept = skipped = 0
    for p in sorted((MAIN / "data" / NS / "recipes").rglob("*.json")):
        out = conv_recipe(json.loads(p.read_text()), known)
        if out is None:
            skipped += 1
            continue
        (rd / p.name).write_text(json.dumps(out, indent=1) + "\n")
        kept += 1
    # loot tables (generated block drops)
    def prune(o):
        """Drop loot entries naming items that are not registered (deferred behaviour items)."""
        if isinstance(o, dict):
            if "name" in o and isinstance(o["name"], str) and o["name"].startswith(NS + ":") and o["name"] not in known and o.get("type") == "minecraft:item":
                return None
            return {k: prune(v) if not isinstance(v, list) else [y for y in (prune(x) for x in v) if y is not None] for k, v in o.items()}
        return o
    for src, sub, check in ((GEN / "data" / NS / "loot_tables/blocks", "blocks", True), (MAIN / "data" / NS / "loot_tables/chests", "chests", False)):
        if not src.exists():
            continue
        ld = D / "loot_table" / sub
        ld.mkdir(parents=True, exist_ok=True)
        for p in src.rglob("*.json"):
            if check and NS + ":" + p.stem not in known:
                continue
            t = prune(json.loads(p.read_text()))
            t["pools"] = [x for x in t.get("pools", []) if x.get("entries")]
            dst = ld / p.relative_to(src)
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(json.dumps(t, indent=1) + "\n")
    shutil.copy(Path(__file__).parent / "silentwood_leaves_loot.json", D / "loot_table/blocks/silentwood_leaves.json")  # 26.1 shears/silk-touch conditions
    # tags, filtered to known ids
    for root in (GEN, MAIN):
        for p in (root / "data").rglob("tags/*/**/*.json"):
            rel = p.relative_to(root / "data")
            ns, _, kind, *rest = rel.parts
            kind = {"blocks": "block", "items": "item"}.get(kind)
            if not kind:
                continue
            ns = "c" if ns == "forge" else ns
            if p.stem in ("absorption_orb_repairable", "crystalline_shield_repairable"):
                continue
            vals = []
            for v in json.loads(p.read_text())["values"]:
                v = v if isinstance(v, str) else v["id"]
                v = conv_tag(v)
                if v.startswith(NS + ":") and v not in known:
                    continue
                vals.append(v)
            if vals:
                dst = RES / "data" / ns / "tags" / kind / Path(*rest)
                dst.parent.mkdir(parents=True, exist_ok=True)
                dst.write_text(json.dumps(dict(replace=False, values=vals), indent=1) + "\n")
    # worldgen, dimension, structures (validated by a server boot, see README)
    for sub in ("dimension", "dimension_type", "worldgen"):
        s = MAIN / "data" / NS / sub
        if s.exists():
            shutil.copytree(s, D / sub, dirs_exist_ok=True)
    convert_worldgen(known)
    if (MAIN / "data" / NS / "structures").exists():
        shutil.copytree(MAIN / "data" / NS / "structures", D / "structure", dirs_exist_ok=True)
    # assets
    for sub in ("textures", "blockstates", "models"):
        s = MAIN / "assets" / NS / sub
        if s.exists():
            shutil.copytree(s, A / sub, dirs_exist_ok=True)
    for sub in ("blockstates", "models"):
        s = GEN / "assets" / NS / sub
        if s.exists():
            shutil.copytree(s, A / sub, dirs_exist_ok=True)
    # equipment assets: armor layer textures move to entity/equipment
    eq = A / "equipment"
    eq.mkdir(parents=True, exist_ok=True)
    for a in data["armor"].values():
        n = a["name"]
        for old, new in (("layer_1", "humanoid"), ("layer_2", "humanoid_leggings")):
            src = A / "textures/models/armor" / f"{n}_{old}.png"
            if src.exists():
                dst = A / "textures/entity/equipment" / new / f"{n}.png"
                dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy(src, dst)
        layers = {k: [dict(texture=f"{NS}:{n}")] for k in ("humanoid", "humanoid_leggings", "humanoid_baby")}
        (eq / f"{n}.json").write_text(json.dumps(dict(layers=layers), indent=1) + "\n")
    shutil.rmtree(A / "textures/models", ignore_errors=True)
    # client item definitions
    idir = A / "items"
    idir.mkdir(exist_ok=True)
    allids = [i["id"] for i in data["items"]] + [b["id"] for b in blocks]
    for n in allids:
        mdl = "item" if (A / f"models/item/{n}.json").exists() else "block" if (A / f"models/block/{n}.json").exists() else "item"
        (idir / f"{n}.json").write_text(json.dumps(dict(model=dict(type="minecraft:model", model=f"{NS}:{mdl}/{n}")), indent=1) + "\n")
    # lang (keep keys for registered things and shared strings)
    lang = json.loads((MAIN / "assets" / NS / "lang/en_us.json").read_text())
    (A / "lang").mkdir(exist_ok=True)
    (A / "lang/en_us.json").write_text(json.dumps(lang, indent=1, ensure_ascii=False) + "\n")
    # fuel data map
    fuels = {NS + ":" + i["id"]: {"burn_time": i["burn"]} for i in data["items"] if "burn" in i}
    fuels.update({NS + ":" + b["id"]: {"burn_time": b["burn"]} for b in blocks if "burn" in b})
    fd = RES / "data/neoforge/data_maps/item"
    fd.mkdir(parents=True, exist_ok=True)
    (fd / "furnace_fuels.json").write_text(json.dumps(dict(values=fuels), indent=1) + "\n")
    # repair tags
    for k, v in list(data["tools"].items()) + list(data["armor"].items()):
        if v.get("repair"):
            t = D / "tags/item/repair" / f"{k}.json"
            t.parent.mkdir(parents=True, exist_ok=True)
            t.write_text(json.dumps(dict(values=[v["repair"]]), indent=1) + "\n")
    write_integration(data, blocks)
    print(f"items {len(items)} blocks {len(blocks)} deferred blocks {dblocks} recipes {kept} skipped {skipped}")
    print("tools", sorted(data["tools"]), "armor", sorted(data["armor"]))


main()
