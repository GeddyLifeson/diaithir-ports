#!/usr/bin/env python3 -I
"""Fills the thin vendor ages (Age I, Age II) from real priced items, by role. Rule: every pool is an id-pattern over real items already
in prices.json, within the role's mod namespaces; items that another vendor already sells, or that open in a later Age (tech_ages.json),
are skipped; each vendor takes the cheapest first, then by id, until it holds CAP items. Price is the table's base price; stock follows the
existing rows (max(4, round(32 / price))). No names, vendors or items are invented; drink vendors are left alone. Run from diaithir-worldsys."""
import json, re, glob
D = "src/main/resources/data/diaithirworldsys/economy/"
prices = json.load(open(D + "prices.json", encoding="utf-8")); tech = json.load(open(D + "tech_ages.json", encoding="utf-8"))
ORDER = tech["order"]
def age_of(i):
    a = tech["items"].get(i) or tech["namespaces"].get(i.split(":")[0]) or "I"
    return ORDER.index(a)
used = set()
for f in glob.glob(D + "vendors*.json"):
    for v in json.load(open(f, encoding="utf-8"))["vendors"]: used |= set(v["items"])
# role -> [(namespaces, regex)] pools, tried in order. Every regex is anchored on whole item names (or whole name parts), so "egg" cannot match "leggings".
WOOD = r"(oak|spruce|birch|jungle|acacia|dark_oak|mangrove|cherry|pale_oak|bamboo|crimson|warped)"
AGE1 = {"salt_trader": [({"minecraft"}, r"^(kelp|dried_kelp|sea_pickle|seagrass|ink_sac|glow_ink_sac|nautilus_shell|sponge|prismarine_shard|prismarine_crystals)$")],
        "hide_worker": [({"minecraft"}, r"^(leather|rabbit_hide|rabbit_foot|string|feather|bone|cobweb|turtle_scute|[a-z_]+_wool)$")],
        "fisher": [({"minecraft"}, r"^(cod|salmon|pufferfish|tropical_fish|cooked_cod|cooked_salmon|fishing_rod|cod_bucket|salmon_bucket|tropical_fish_bucket|pufferfish_bucket)$")],
        "hunter": [({"minecraft"}, r"^(beef|cooked_beef|porkchop|cooked_porkchop|mutton|cooked_mutton|chicken|cooked_chicken|rabbit|cooked_rabbit|spider_eye|rotten_flesh|arrow|bow)$")],
        "herb_healer": [({"minecraft"}, r"^(dandelion|poppy|blue_orchid|allium|azure_bluet|[a-z]+_tulip|oxeye_daisy|cornflower|lily_of_the_valley|rose_bush|peony|lilac|sunflower|brown_mushroom|red_mushroom|fern|large_fern|sweet_berries|glow_berries|honey_bottle|honeycomb|apple|moss_block)$")],
        "flint_smith": [({"minecraft"}, r"^(flint|cobblestone|mossy_cobblestone|stone|stone_bricks|gravel|sand|red_sand|sandstone|red_sandstone|andesite|diorite|granite|polished_andesite|polished_diorite|polished_granite|tuff|calcite|dripstone_block|basalt|smooth_basalt|deepslate|cobbled_deepslate|stick|coal|charcoal)$")],
        "timber_worker": [({"minecraft"}, r"^((stripped_)?" + WOOD + r"_(log|wood|planks)|" + WOOD + r"_(door|fence|fence_gate|slab|stairs|sign|boat|button|pressure_plate|trapdoor|sapling)|stick|bamboo)$")],
        "forager": [({"minecraft"}, r"^(carrot|potato|baked_potato|poisonous_potato|beetroot|egg|pumpkin|melon|melon_slice|wheat|[a-z]+_seeds|cocoa_beans|sugar_cane|brown_mushroom|red_mushroom|sweet_berries)$")]}
AGE2 = {"potter": [({"minecraft"}, r"^([a-z_]*terracotta|brick|bricks|[a-z_]+_bricks|clay_ball|clay|flower_pot|mud|packed_mud|decorated_pot)$"), ({"mcwroofs"}, r"(^|_)(clay|terracotta|brick)(_|$)")],
        "farmer": [({"minecraft"}, r"^(wheat|[a-z]+_seeds|carrot|potato|beetroot|melon|melon_slice|pumpkin|sugar_cane|hay_block|[a-z]+_hoe|bone_meal|[a-z_]*sapling|sweet_berries|cocoa_beans|bamboo|kelp|composter)$"),
                   ({"farmingforblockheads"}, r"."), ({"mysticalagriculture"}, r"seeds$")],
        "baker": [({"minecraft"}, r"^(bread|cake|cookie|pumpkin_pie|mushroom_stew|rabbit_stew|beetroot_soup|suspicious_stew|cooked_[a-z_]+|beef|porkchop|chicken|mutton|rabbit|cod|salmon|apple|honey_bottle|honeycomb|egg|milk_bucket|sugar|baked_potato|cocoa_beans|sweet_berries|glow_berries|dried_kelp|wheat)$"),
                  ({"cookingforblockheads"}, r".")]}
def fill(path, plan, cap):
    r = json.load(open(path, encoding="utf-8")); gate = ORDER.index(r["age"]); n0 = 0; n1 = 0
    for v in r["vendors"]:
        n0 += len(v["items"])
        for ns, rx in plan.get(v["id"], []):
            if len(v["items"]) >= cap: break
            for k in sorted((k for k in prices if k.split(":")[0] in ns and re.search(rx, k.split(":", 1)[1])), key=lambda k: (prices[k][0], k)):
                if len(v["items"]) >= cap: break
                if k in used or k in v["items"] or age_of(k) > gate: continue
                p = prices[k][0]; v["items"][k] = {"price": p, "stock": max(4, round(32 / p))}; used.add(k)
        n1 += len(v["items"])
    json.dump(r, open(path, "w", encoding="utf-8"), indent=1, ensure_ascii=False)
    print(path, n0, "->", n1)
fill(D + "vendors.json", AGE1, 34)
fill(D + "vendors_age2.json", AGE2, 70)
