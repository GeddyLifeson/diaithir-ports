#!/usr/bin/env python3
"""Builds the set-family item tags, set bonus data, shared gem tags and Apotheosis gem definitions.
Reads the pack item list (packmap/pack_dump.json.gz) and, for gems, the Silent Gems trait numbers (jar given as argv[1]); writes only our own data."""
import gzip, json, pathlib, re, sys, zipfile
ROOT = pathlib.Path(__file__).resolve().parent.parent
D = ROOT / "src/main/resources/data"
import argparse
ap = argparse.ArgumentParser(description=__doc__)
ap.add_argument("silentgems_jar")
ap.add_argument("pack_dump", help="packmap/pack_dump.json.gz")
ARGS = ap.parse_args()
items = [i if isinstance(i, str) else i["id"] for i in json.load(gzip.open(ARGS.pack_dump))["items"]]
def w(path, obj):
    p = D / path; p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(obj, indent=1, ensure_ascii=False))
def tag(path, vals): w(path, {"replace": False, "values": [{"id": v, "required": False} for v in vals]})

arm = lambda ns, rx: [i for i in items if i.startswith(ns + ":") and re.search(rx, i)]
fam = {
 "holy":    arm("paladins", r"(armor|robe)_(head|chest|legs|feet)$"),
 "shadow":  arm("rogues",   r"(armor|robe)_(head|chest|legs|feet)$"),
 "arcane":  arm("wizards",  r"(armor|robe)_(head|chest|legs|feet)$"),
 "wayfarer": [i for i in items if i.startswith("artifacts:") and not re.search(r"_broken$|spawn_egg|umbrella|eternal_steak|everlasting_beef|onion_ring|chorus_totem|whoopee", i)],
 "gemwork": [i for i in items if re.match(r"jewelry:(.*_ring|.*_necklace)$", i)] + ["irons_jewelry:ring", "irons_jewelry:necklace"],
}
for k, v in fam.items(): tag(f"diaithirgear/tags/item/sets/{k}.json", v)
A = lambda a, amt, op="add_value": {"attribute": a, "operation": op, "amount": amt}
bon = {
 "holy":   (4, False, [(2, [A("minecraft:max_health", 2), A("spell_power:healing", 0.05, "add_multiplied_base")]), (4, [A("minecraft:max_health", 4), A("minecraft:knockback_resistance", 0.1), A("spell_power:healing", 0.1, "add_multiplied_base")])]),
 "shadow": (4, False, [(2, [A("minecraft:movement_speed", 0.05, "add_multiplied_base"), A("apothic_attributes:crit_chance", 0.05)]), (4, [A("minecraft:movement_speed", 0.1, "add_multiplied_base"), A("apothic_attributes:crit_chance", 0.1), A("minecraft:sneaking_speed", 0.2)])]),
 "arcane": (4, False, [(2, [A("spell_power:arcane", 0.05, "add_multiplied_base"), A("spell_power:fire", 0.05, "add_multiplied_base"), A("spell_power:frost", 0.05, "add_multiplied_base")]), (4, [A("spell_power:haste", 0.1, "add_multiplied_base"), A("spell_power:generic", 0.1, "add_multiplied_base"), A("minecraft:max_absorption", 4)])]),
 "wayfarer": (3, True, [(2, [A("minecraft:luck", 1)]), (3, [A("minecraft:step_height", 0.4), A("minecraft:safe_fall_distance", 2)]), (5, [A("minecraft:movement_speed", 0.05, "add_multiplied_base"), A("minecraft:luck", 1)])]),
 "gemwork": (4, True, [(2, [A("minecraft:luck", 1)]), (3, [A("minecraft:max_health", 2)]), (4, [A("minecraft:armor_toughness", 2), A("minecraft:max_health", 2)])]),
}
idx = []
for k, (age, curio, tiers) in bon.items():
    w(f"diaithirgear/set_bonuses/{k}.json", {"id": k, "tag": f"diaithirgear:sets/{k}", "age": age, "curios": curio, "tiers": [{"pieces": n, "attributes": a} for n, a in tiers]})
    idx.append(k)
w("diaithirgear/set_bonuses/index.json", idx)

# ---- gems: shared tags and Apotheosis gems mirroring Silent Gems trait numbers
jar = zipfile.ZipFile(ARGS.silentgems_jar)
SG = ["alexandrite","ammolite","aquamarine","black_diamond","carnelian","citrine","garnet","heliodor","iolite","kyanite","moldavite","opal","pearl","peridot","rose_quartz","ruby","sapphire","tanzanite","topaz","turquoise","white_diamond"]
JEW = ["ruby","topaz","citrine","jade","sapphire","tanzanite"]
IRON = ["garnet","moonstone","onyx","peridot","ruby","sapphire","topaz"]
for g in JEW: tag(f"c/tags/item/gems/{g}.json", [f"jewelry:{g}"])
tag("diaithirgear/tags/item/gems/socketable.json", sorted({f"#c:gems/{g}" for g in SG + JEW + IRON}))
GEM = {  # silentgems trait -> (gem, attribute, apotheosis gem_class)
 "hearty": ("peridot", "minecraft:max_health", {"key": "core_armor", "types": ["apotheosis:chestplate", "apotheosis:leggings"]}),
 "power": ("garnet", "minecraft:attack_damage", {"key": "light_weapon", "types": ["apotheosis:melee_weapon", "apotheosis:trident"]}),
 "hasty": ("opal", "minecraft:mining_efficiency", {"key": "tools", "types": ["apotheosis:breaker", "apotheosis:shears", "apotheosis:melee_weapon"]}),
 "neptunes_blessing": ("pearl", "minecraft:oxygen_bonus", "apotheosis:helmet"),
 "step_up": ("aquamarine", "minecraft:step_height", "apotheosis:boots"),
 "twinkletoes": ("tanzanite", "minecraft:sneaking_speed", "apotheosis:boots"),
}
PUR = ["cracked", "chipped", "flawed", "normal", "flawless", "perfect"]
pairs = {}
for trait, (gem, attr, gc) in GEM.items():
    t = json.loads(jar.read(f"data/silentgems/silentgear_traits/{trait}.json"))
    vals = None
    for e in t["effects"]:
        for lst in e.get("attribute_modifiers", {}).values():
            for m in lst:
                if m["attribute"] == attr: vals = m["values"]; break
            if vals: break
        if vals: break
    n = len(vals)
    lv = [vals[min(i, n - 1)] for i in range(5)] if n >= 5 else [round(vals[0] * (i + 1) / 5, 3) for i in range(5)]  # single-level traits reach the full value at flawless
    lv.append(round(lv[4] * 1.25, 3))
    w(f"diaithirgear/apotheosis/gems/{gem}.json", {
        "weights": {"quality": 0.1, "weight": 60},
        "bonuses": [{"type": "apotheosis:attribute", "attribute": attr, "gem_class": gc, "operation": "add_value", "values": dict(zip(PUR, lv))}]})
    pairs[gem] = {"silentgems_trait": "silentgems:" + trait, "apotheosis_gem": "diaithirgear:" + gem, "attribute": attr, "tag": f"c:gems/{gem}", "levels": lv}
w("diaithirgear/gem_affix_map.json", {"_doc": "One gem tag, the same affix on both sides: the Silent Gems trait and the Apotheosis gem of the same name give the same attribute at matching levels (trait level 1..5 = cracked..flawless, perfect = flawless x1.25; single-level traits ramp to full at flawless). Generated by tools/gen_sets.py.", "pairs": pairs})
print(sorted(fam), {k: len(v) for k, v in fam.items()}, list(pairs))
