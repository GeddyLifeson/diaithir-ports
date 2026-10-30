"""Butchery mapping for every species. Run from diaithir-fauna: python3 tools/gen_butchery.py
Writes data/diaithirfauna/butchery/fauna.json (classes + entities, read by diaithir-butchery) and
data/diaithirfauna/butchery_species.json (species id -> carcass class, read by FaunaMob).
Class = archetype (from the body plan) x mass tier (from kg). Fossil taxa butcher like their body plan."""
import json, math, os
RES = "src/main/resources/data/diaithirfauna"
sp = json.load(open(f"{RES}/species.json"))["species"]

ARCH = {}
for p in "fish_deep fish_flat fish_fusiform fish_primitive eel ray placoderm syngnathid".split(): ARCH[p] = "fish"
for p in "ichthyosaur mosasaur plesiosaur dolphin archaic_whale pinniped sea_turtle".split(): ARCH[p] = "marine"
for p in "armadillo bat giraffid lagomorph macropod marsupial_stout monotreme mustelid insectivore primate_ape primate_monkey primate_small rodent_large rodent_small sloth_tree synapsid_sail tapir therapsid viverrid".split(): ARCH[p] = "mammal"
for p in "corvid galliform owl parrot penguin pigeon raptor ratite seabird songbird terror_bird wader waterfowl".split(): ARCH[p] = "bird"
for p in "crocodilian early_tetrapod frog lizard".split(): ARCH[p] = "reptile"
for p in "ankylosaur ceratopsian dromaeosaur hadrosaur ornithopod_small sauropod stegosaur theropod pterosaur".split(): ARCH[p] = "archosaur"
for p in "crab giant_arthropod scorpion eurypterid trilobite beetle insect_flying".split(): ARCH[p] = "arthropod"
for p in "cephalopod ammonite jellyfish starfish".split(): ARCH[p] = "mollusc"
missing = {s["plan"] for s in sp} - set(ARCH)
assert not missing, missing

# reference yields for a body of REF kg; every item is [min, max] and scales with (kg/REF)^0.55
REF = 200.0
T = {
 "mammal": ({"raw_hide": [2, 3], "sinew": [2, 4]}, {"blood_flask": [2, 4], "liver": [1, 1], "heart": [1, 1], "kidney": [2, 2], "tripe": [1, 1], "tallow": [2, 5], "tongue": [1, 1], "brain": [1, 1]},
           {"haunch": [2, 4], "shoulder": [2, 4], "loin": [3, 4], "ribs": [3, 5], "flank": [2, 3], "tenderloin": [2, 2], "neck": [2, 2], "hock": [4, 4], "tail": [1, 1], "scraps": [4, 6], "minecraft:bone": [5, 8], "marrow_bone": [2, 3]}),
 "archosaur": ({"raw_hide": [3, 4], "sinew": [2, 4]}, {"blood_flask": [2, 4], "liver": [1, 1], "heart": [1, 1], "kidney": [1, 2], "tallow": [2, 4], "brain": [1, 1]},
           {"haunch": [2, 4], "shoulder": [2, 3], "loin": [3, 4], "ribs": [3, 5], "flank": [2, 3], "tenderloin": [1, 2], "neck": [2, 3], "tail": [3, 5], "scraps": [4, 6], "minecraft:bone": [8, 12], "marrow_bone": [2, 4]}),
 "reptile": ({"raw_hide": [2, 3]}, {"blood_flask": [1, 2], "liver": [1, 1], "heart": [1, 1]},
           {"loin": [2, 4], "tail": [2, 4], "ribs": [1, 3], "scraps": [2, 4], "minecraft:bone": [4, 8]}),
 "bird": ({"minecraft:feather": [8, 16]}, {"blood_flask": [1, 2], "liver": [1, 1], "heart": [1, 1], "tallow": [1, 2]},
           {"bird_breast": [2, 4], "drumstick": [2, 2], "wing": [2, 2], "neck": [1, 1], "scraps": [2, 4], "minecraft:bone": [3, 6]}),
 "fish": ({"raw_hide": [1, 2]}, {"blood_flask": [1, 3], "liver": [1, 2], "heart": [1, 1], "tallow": [2, 4]},
           {"fish_fillet": [6, 12], "scraps": [2, 5], "minecraft:bone": [3, 7]}),
 "marine": ({"raw_hide": [3, 5], "sinew": [1, 3]}, {"blood_flask": [3, 6], "liver": [2, 3], "heart": [1, 1], "tallow": [6, 12], "kidney": [1, 2]},
           {"fish_fillet": [4, 8], "loin": [2, 4], "ribs": [2, 4], "tail": [1, 2], "scraps": [4, 8], "minecraft:bone": [6, 12]}),
 "arthropod": ({}, {"tallow": [0, 1]}, {"fish_fillet": [2, 5], "scraps": [2, 4]}),
 "mollusc": ({}, {"tallow": [0, 1]}, {"fish_fillet": [2, 5], "scraps": [1, 3]}),
}
EXP = 0.55
CAP = 64
EDGES = [0.05, 0.5, 2, 8, 25, 80, 250, 800, 3000, 10000, 40000]  # tier i holds kg < EDGES[i]; last tier is above
MID = [0.02, 0.2, 1, 4, 14, 45, 140, 450, 1700, 6000, 20000, 80000]

def tier(kg):
    for i, e in enumerate(EDGES):
        if kg < e: return i
    return len(EDGES)

def scaled(d, f):
    out = {}
    for k, (lo, hi) in d.items():
        a, b = lo * f, hi * f
        if b < 0.25: continue
        lo2, hi2 = max(1 if b >= 0.5 else 0, min(CAP, round(a))), min(CAP, max(1, round(b)))
        out[k] = [lo2, max(lo2, hi2)]
    return out

classes, usage = {}, {}
for arch, (skin, gut, cut) in T.items():
    for t in range(len(MID)):
        f = (MID[t] / REF) ** EXP
        c = {"skin": scaled(skin, f), "gut": scaled(gut, f), "cut": scaled(cut, f)}
        # a body too small for any meat cut is not butchered
        if not c["cut"]: continue
        classes[f"fauna_{arch}_{t}"] = c

species_map = {}
for s in sp:
    cid = f"fauna_{ARCH[s['plan']]}_{tier(s['kg'])}"
    species_map[s["id"]] = cid if cid in classes else ""

# default per entity type (plan): the class of its median species, for butchery's own kill handler
ents = {}
by_plan = {}
for s in sp: by_plan.setdefault(s["plan"], []).append(s)
for plan, lst in by_plan.items():
    lst = sorted(lst, key=lambda s: s["kg"])
    ents[f"diaithirfauna:{plan}"] = species_map[lst[len(lst) // 2]["id"]] or "none"

os.makedirs(f"{RES}/butchery", exist_ok=True)
used = set(species_map.values()) | set(ents.values())
json.dump({"classes": {k: v for k, v in classes.items()}, "entities": ents}, open(f"{RES}/butchery/fauna.json", "w"), indent=0, separators=(",", ":"))
json.dump(species_map, open(f"{RES}/butchery_species.json", "w"), separators=(",", ":"))
print(len(classes), "classes,", len(ents), "entity types,", sum(1 for v in species_map.values() if v), "of", len(species_map), "species butchered")
