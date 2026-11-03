#!/usr/bin/env python3 -I
"""Fills the thin vendor ages (Age I, Age II) from real priced items, by role. Rule: every pool is an id-pattern over real items already
in prices.json, within the role's mod namespaces; items that another vendor already sells, or that open in a later Age (tech_ages.json),
are skipped; each vendor takes the cheapest first, then by id, until it holds CAP items. Price is the table's base price; stock follows the
existing rows (max(4, round(32 / price))). No names, vendors or items are invented; drink vendors are left alone. Run from diaithir-worldsys."""
import json, re, glob
D = "src/main/resources/data/diaithirworldsys/economy/"
prices = json.load(open(D + "prices.json")); tech = json.load(open(D + "tech_ages.json"))
ORDER = tech["order"]
def age_of(i):
    a = tech["items"].get(i) or tech["namespaces"].get(i.split(":")[0]) or "I"
    return ORDER.index(a)
used = set()
for f in glob.glob(D + "vendors*.json"):
    for v in json.load(open(f))["vendors"]: used |= set(v["items"])
# role -> [(namespaces, regex)] pools, tried in order
AGE1 = {"salt_trader": [({"minecraft"}, r"kelp|sea_|seagrass|ink_sac|nautilus|prismarine|sponge")],
        "hide_worker": [({"minecraft"}, r"leather|rabbit_|string|feather|wool|bone|cobweb|scute")],
        "fisher": [({"minecraft"}, r"cod|salmon|fish|bucket|fishing")],
        "hunter": [({"minecraft"}, r"beef|porkchop|mutton|chicken|rabbit|spider_eye|rotten|arrow|^bow$")],
        "herb_healer": [({"minecraft"}, r"dandelion|poppy|orchid|allium|azure|tulip|oxeye|cornflower|lily|rose|peony|lilac|sunflower|mushroom|fern|berr|honey|apple|moss")],
        "flint_smith": [({"minecraft"}, r"flint|stone|cobble|gravel|sand|andesite|diorite|granite|tuff|basalt|dripstone|calcite|stick|coal")],
        "timber_worker": [({"minecraft"}, r"_log|_planks|stick|sapling|bamboo|_door|_fence|_slab|_stairs|_sign|_boat")],
        "forager": [({"minecraft"}, r"carrot|potato|beetroot|egg|pumpkin|melon|wheat|seeds|cocoa|sugar_cane|mushroom")]}
AGE2 = {"potter": [({"minecraft"}, r"terracotta|brick|clay|flower_pot|mud|decorated_pot"), ({"mcwroofs"}, r"clay|terracotta|brick")],
        "farmer": [({"minecraft"}, r"seeds|wheat|carrot|potato|beetroot|melon|pumpkin|sugar_cane|hay|hoe|bone_meal|sapling|berries|cocoa|bamboo|kelp"),
                   ({"farmingforblockheads"}, r"."), ({"mysticalagriculture"}, r"seeds$")],
        "baker": [({"minecraft"}, r"bread|cake|cookie|pie|stew|soup|beef|pork|chicken|mutton|rabbit|cod|salmon|apple|honey|egg|milk|sugar|cooked"),
                  ({"cookingforblockheads"}, r".")]}
def fill(path, plan, cap):
    r = json.load(open(path)); gate = ORDER.index(r["age"]); n0 = 0; n1 = 0
    for v in r["vendors"]:
        n0 += len(v["items"])
        for ns, rx in plan.get(v["id"], []):
            if len(v["items"]) >= cap: break
            for k in sorted((k for k in prices if k.split(":")[0] in ns and re.search(rx, k.split(":", 1)[1])), key=lambda k: (prices[k][0], k)):
                if len(v["items"]) >= cap: break
                if k in used or k in v["items"] or age_of(k) > gate: continue
                p = prices[k][0]; v["items"][k] = {"price": p, "stock": max(4, round(32 / p))}; used.add(k)
        n1 += len(v["items"])
    json.dump(r, open(path, "w"), indent=1, ensure_ascii=False)
    print(path, n0, "->", n1)
fill(D + "vendors.json", AGE1, 34)
fill(D + "vendors_age2.json", AGE2, 70)
