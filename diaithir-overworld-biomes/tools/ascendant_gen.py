#!/usr/bin/env python3 -I
# S6857 Generates the Ascendant biomes (Thornwake, Cinderfen, The Unwound) and their hidden rite sites
# (structure templates with an Ascendant altar and nine ogham stones) into diaithir-overworld-biomes data.
# Usage: python3 -I tools/ascendant_gen.py [DATA_VERSION]   (run from the module root; additive, rewrites only its own files)
import gzip, json, struct, sys
from pathlib import Path

DV = int(sys.argv[1]) if len(sys.argv) > 1 else 4800
ROOT = Path("src/main/resources")
NS = "diaithir_overworld_biomes"
D = ROOT / "data" / NS
A = ROOT / "assets" / NS

def put(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, separators=(",", ":")) + "\n")

def st(name, **props):
    s = {"Name": name}
    if props: s["Properties"] = props
    return s

def simple(name, **props):
    return {"type": "minecraft:simple_state_provider", "state": st(name, **props)}

def disk(block, targets, rmin=2, rmax=4):
    return {"type": "minecraft:disk", "config": {"state_provider": simple(block),
            "target": {"type": "minecraft:matching_blocks", "blocks": targets},
            "radius": {"type": "minecraft:uniform", "min_inclusive": rmin, "max_inclusive": rmax}, "half_height": 1}}

def patch(block, tries=24, props=None):
    return {"type": "minecraft:simple_block", "config": {"to_place": simple(block, **(props or {}))}}

AIR = {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}
TOP = [{"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"}, AIR]

def tree(log, leaves, base, rand, radius=2):
    return {"type": "minecraft:tree", "config": {
        "dirt_provider": simple("minecraft:dirt"),
        "trunk_provider": simple(log, axis="y"),
        "foliage_provider": simple(leaves, distance="7", persistent="false", waterlogged="false"),
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": base, "height_rand_a": rand, "height_rand_b": 1},
        "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": radius, "offset": 0, "height": 3},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 1},
        "decorators": [], "ignore_vines": True}}

def placed(feature, count, extra=None):
    p = [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"}]
    p += extra or [{"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}]
    p.append({"type": "minecraft:biome"})
    return {"feature": f"{NS}:{feature}", "placement": p}

GROUND = ["minecraft:dirt", "minecraft:grass_block", "minecraft:podzol", "minecraft:mud", "minecraft:sand", "minecraft:stone", "minecraft:coarse_dirt"]
SURF = {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR_WG"}

BIOMES = {
    "thornwake": dict(
        tier=1, age="VII", temp=0.6, down=0.7, fog=0x3b4a2c, sky=0x7ea052, water=0x3f6a4f, wfog=0x1d3326, particle="minecraft:spore_blossom_air",
        mood="minecraft:ambient.crimson_forest.mood", stone="minecraft:mossy_stone_bricks", stone2="minecraft:cracked_stone_bricks",
        configured={
            "asc_thornwake_trees": tree("minecraft:dark_oak_log", "minecraft:dark_oak_leaves", 5, 3, 2),
            "asc_thornwake_thorns": patch("minecraft:sweet_berry_bush", 32, {"age": "3"}),
            "asc_thornwake_deadwood": patch("minecraft:dead_bush", 20),
            "asc_thornwake_webs": patch("minecraft:cobweb", 6),
            "asc_thornwake_floor": disk("minecraft:podzol", GROUND, 3, 5)},
        placed={"asc_thornwake_trees": (7, [SURF]), "asc_thornwake_thorns": (24, TOP),
                "asc_thornwake_deadwood": (12, TOP), "asc_thornwake_webs": (4, TOP), "asc_thornwake_floor": (3, [SURF])},
        steps={2: ["asc_thornwake_floor"], 9: ["asc_thornwake_trees", "asc_thornwake_thorns", "asc_thornwake_deadwood", "asc_thornwake_webs"]},
        en="Thornwake", dia="Fàs-droighinn",
        body="A wood grown in the wake of something that did not stop growing. Thorn and berry choke the ground, and old stones stand under the briars where an Ascendant rite was once kept. Tier I rites wake here."),
    "cinderfen": dict(
        tier=2, age="Rodos", temp=1.1, down=0.4, fog=0x3a3330, sky=0x6b5a50, water=0x4a4036, wfog=0x241c18, particle="minecraft:ash",
        mood="minecraft:ambient.soul_sand_valley.mood", stone="minecraft:blackstone", stone2="minecraft:polished_blackstone_bricks",
        configured={
            "asc_cinderfen_ash": disk("minecraft:soul_soil", GROUND, 3, 5),
            "asc_cinderfen_mud": disk("minecraft:mud", GROUND, 2, 4),
            "asc_cinderfen_embers": disk("minecraft:magma_block", GROUND, 1, 2),
            "asc_cinderfen_reeds": patch("minecraft:dead_bush", 24),
            "asc_cinderfen_trees": tree("minecraft:mangrove_log", "minecraft:mangrove_leaves", 4, 2, 2)},
        placed={"asc_cinderfen_ash": (3, [SURF]), "asc_cinderfen_mud": (4, [SURF]), "asc_cinderfen_embers": (1, [SURF]),
                "asc_cinderfen_reeds": (14, TOP), "asc_cinderfen_trees": (1, [SURF])},
        steps={2: ["asc_cinderfen_ash", "asc_cinderfen_mud", "asc_cinderfen_embers"], 9: ["asc_cinderfen_reeds", "asc_cinderfen_trees"]},
        en="Cinderfen", dia="Lòn-luaithre",
        body="A wetland that burns without flame: black water, soul-soil, and magma under the mud. Slagiron is smelted from what the fen gives up. Tier II rites wake here."),
    "unwound": dict(
        tier=3, age="Rodos", temp=0.2, down=0.0, fog=0xc9c4d8, sky=0xb8b2d0, water=0x9a96b8, wfog=0x4a4766, particle="minecraft:white_ash",
        mood="minecraft:ambient.warped_forest.mood", stone="minecraft:deepslate_bricks", stone2="minecraft:cracked_deepslate_bricks",
        configured={
            "asc_unwound_pale": disk("minecraft:calcite", GROUND, 3, 5),
            "asc_unwound_basalt": disk("minecraft:smooth_basalt", GROUND, 2, 3),
            "asc_unwound_rods": patch("minecraft:end_rod", 6, {"facing": "up"}),
            "asc_unwound_amethyst": patch("minecraft:amethyst_cluster", 4, {"facing": "up", "waterlogged": "false"})},
        placed={"asc_unwound_pale": (3, [SURF]), "asc_unwound_basalt": (2, [SURF]),
                "asc_unwound_rods": (3, TOP), "asc_unwound_amethyst": (2, TOP)},
        steps={2: ["asc_unwound_pale", "asc_unwound_basalt"], 9: ["asc_unwound_rods", "asc_unwound_amethyst"]},
        en="The Unwound", dia="An Dì-chasaid",
        body="Ground where the Ages' clock has run down: pale, still, and wrong underfoot. Few trees, no weather. The last rites wake here, and The Last Wheel with them. Tier III rites wake here."),
}

# ---------- template NBT ----------
def nbt_str(s):
    b = s.encode(); return struct.pack(">H", len(b)) + b
def tag(t, name, payload): return bytes([t]) + nbt_str(name) + payload
def t_int(n, v): return tag(3, n, struct.pack(">i", v))
def t_str(n, v): return tag(8, n, nbt_str(v))
def t_list(n, ty, items): return tag(9, n, bytes([ty]) + struct.pack(">i", len(items)) + b"".join(items))
def comp(parts): return b"".join(parts) + b"\0"
def int3(n, a): return t_list(n, 3, [struct.pack(">i", x) for x in a])

def template(path, palette, blocks, size):
    names = {}
    pal = []
    for b in palette:
        pal.append(b)
    pl = [comp([t_str("Name", n)] + ([tag(10, "Properties", comp([t_str(k, v) for k, v in p.items()]))] if p else [])) for n, p in pal]
    bl = [comp([t_int("state", s), int3("pos", pos)]) for s, pos in blocks]
    root = tag(10, "", comp([t_int("DataVersion", DV), int3("size", size),
                              t_list("palette", 10, pl), t_list("blocks", 10, bl), t_list("entities", 10, [])]))
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "wb") as f:
        f.write(gzip.compress(root, mtime=0))

OGHAM = [(-3, -3), (0, -4), (3, -3), (-4, 0), (4, 0), (-3, 3), (0, 4), (3, 3), (0, -2)]  # nine stones, ring + inner

def build_site(key, cfg):
    sz = 11; c = 5
    palette = [(cfg["stone"], None), (cfg["stone2"], None), ("diaithirbosses:ascendant_altar", None), ("diaithirbosses:ogham_stone", None),
               (cfg["stone"].replace("bricks", "brick_wall") if "bricks" in cfg["stone"] else "minecraft:cobblestone_wall", None)]
    blocks = []
    for x in range(sz):
        for z in range(sz):
            dx, dz = x - c, z - c
            r2 = dx * dx + dz * dz
            if r2 <= 30:  # floor disc
                blocks.append((1 if (x * 7 + z * 13) % 5 == 0 else 0, (x, 0, z)))
    # broken wall ring posts (hidden/ruined look)
    for dx, dz in [(5, 0), (-5, 0), (0, 5), (0, -5), (4, 3), (-4, -3)]:
        blocks.append((4, (c + dx, 1, c + dz)))
    blocks.append((2, (c, 1, c)))
    for dx, dz in OGHAM:
        blocks.append((3, (c + dx, 1, c + dz)))
    template(D / "structure" / f"ascendant_{key}_site.nbt", palette, blocks, [sz, 3, sz])

def main():
    ids_biome = []
    for key, cfg in BIOMES.items():
        bid = f"{NS}:{key}"; ids_biome.append(bid)
        feats = [[] for _ in range(11)]
        for step, names in cfg["steps"].items():
            feats[step] = [f"{NS}:{n}" for n in names]
        put(D / "worldgen/biome" / f"{key}.json", {
            "has_precipitation": cfg["down"] > 0.1, "temperature": cfg["temp"], "downfall": cfg["down"],
            "effects": {"fog_color": cfg["fog"], "sky_color": cfg["sky"], "water_color": cfg["water"], "water_fog_color": cfg["wfog"],
                        "mood_sound": {"sound": cfg["mood"], "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0},
                        "particle": {"probability": 0.003, "options": {"type": cfg["particle"]}}},
            "spawners": {k: [] for k in ["monster", "creature", "ambient", "axolotls", "underground_water_creature", "water_creature", "water_ambient", "misc"]},
            "spawn_costs": {}, "carvers": [], "features": feats})
        for n, f in cfg["configured"].items():
            put(D / "worldgen/configured_feature" / f"{n}.json", f)
        for n, (count, extra) in cfg["placed"].items():
            put(D / "worldgen/placed_feature" / f"{n}.json", placed(n, count, extra))
        build_site(key, cfg)
        cond = {"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "diaithirbosses"}]}
        put(D / "tags/worldgen/biome" / f"has_rite_site_{key}.json", {"values": [bid]})
        put(D / "worldgen/template_pool" / f"ascendant_{key}_site.json", {"fallback": "minecraft:empty", "elements": [
            {"weight": 1, "element": {"element_type": "minecraft:single_pool_element", "location": f"{NS}:ascendant_{key}_site",
                                      "processors": "minecraft:empty", "projection": "rigid"}}]})
        put(D / "worldgen/structure" / f"ascendant_{key}_site.json", dict(cond, **{
            "type": "minecraft:jigsaw", "biomes": f"#{NS}:has_rite_site_{key}", "step": "surface_structures",
            "spawn_overrides": {}, "terrain_adaptation": "beard_thin", "start_pool": f"{NS}:ascendant_{key}_site", "size": 1,
            "start_height": {"absolute": -1}, "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 16,
            "use_expansion_hack": False}))
        put(D / "worldgen/structure_set" / f"ascendant_{key}_site.json", dict(cond, **{
            "structures": [{"structure": f"{NS}:ascendant_{key}_site", "weight": 1}],
            "placement": {"type": "minecraft:random_spread", "spacing": 20, "separation": 8, "salt": 1708100 + cfg["tier"]}}))
        entry = {"id": bid, "category": "biome", "title": f"codex.{NS}.{key}.title", "body": f"codex.{NS}.{key}.body",
                 "age": cfg["age"], "quest": f"{NS}:quests/visit_{key}", "upgrade_rule": "unlock_on_visit"}
        put(D / "codex/entries/biome" / f"{key}.json", entry)
        put(D / "quests" / f"visit_{key}.json", {"id": f"{NS}:quests/visit_{key}", "age": cfg["age"],
            "task": {"type": "visit_biome", "biome": bid}, "codex_link": bid, "reward": {"energy_J": 5000 * cfg["tier"]}, "global_event": "biomes_ready"})
    put(D / "tags/worldgen/biome/ascendant_biomes.json", {"values": ids_biome})
    # fold into the module's biome sets
    for tg in ("all_biomes",):
        p = D / "tags/worldgen/biome" / f"{tg}.json"
        v = json.loads(p.read_text()); v["values"] = sorted(set(v["values"]) | set(ids_biome)); put(p, v)
    # lang
    lp = A / "lang/en_us.json"
    lang = json.loads(lp.read_text())
    for key, cfg in BIOMES.items():
        lang[f"biome.{NS}.{key}"] = cfg["en"]
        lang[f"codex.{NS}.{key}.title"] = cfg["en"]
        lang[f"codex.{NS}.{key}.body"] = cfg["body"]
    lp.write_text(json.dumps(lang, separators=(",", ":"), ensure_ascii=False) + "\n")
    print("wrote", len(BIOMES), "biomes + rite sites; DataVersion", DV)

main()
