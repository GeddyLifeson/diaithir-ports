"""Library entries for the combat mobs that really exist in the pack, one list per Age, read from the mod jars and data.
Writes library/mobs.json and merges library/gear.json + library/mobs.json into <out> (library-sync/arms.json).
Run: python3 -I tools/library_mobs.py <mods dir> <nemesis.json> <out.json>
Sources: diaithir-bestiary creature json (min_age, stats, biome), diaithir-nemesis nemesis.json (warbands and ranks per Age),
Ice and Fire / Mutant Monsters / Untamed Wilds entity names from their own lang files (every id is checked to exist).
"""
import glob, json, os, re, sys, zipfile

MODS, NEM, OUT = sys.argv[1:4]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ROM = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7}
out = []


def jar(prefix):
    return zipfile.ZipFile(glob.glob(os.path.join(MODS, prefix + "*.jar"))[0])


def lang(z, ns):
    return json.loads(z.read(f"assets/{ns}/lang/en_us.json"))


def ent(name, age, text):
    out.append([name, "being", age, "bestiary", text])


# ---- diaithir-bestiary
z = jar("diaithir-bestiary")
L = lang(z, "diaithirbestiary")
PRESET = {"stalker": "A lurker of the dark that hunts only in low light.",
          "avatar": "A rare avatar-boss, immune to being pushed about.",
          "mech": "A walker of metal that runs on burning fuel.",
          "flyer": "A winged hunter.", "beast": "A wild beast."}
for n in sorted(z.namelist()):
    if "/creature/" not in n or not n.endswith(".json"):
        continue
    cid = n.rsplit("/", 1)[1][:-5]
    d = json.loads(z.read(n))
    sp = d.get("spawn")
    st = d.get("stats", {})
    if not sp or "min_age" not in sp:
        continue  # never spawns by itself (ironstrider, aurelith): not filed
    fight = st.get("damage", 0) >= 5 or d["preset"] == "mech"
    if not fight:
        continue
    biome = sp["biomes"].split(":")[1].replace("is_", "")
    bits = [f"{PRESET[d['preset']]}", f"Health {st.get('health', 0):g}"]
    if st.get("armor"):
        bits.append(f"armour {st['armor']:g}")
    if st.get("damage"):
        bits.append(f"damage {st['damage']:g}")
    txt = bits[0] + " " + ", ".join(bits[1:]) + f". Roams the {biome} from Age {sp['min_age']}" + (", rare" if sp.get("rare") else "") + "."
    if "fuel" in d:
        txt += f" Burns {d['fuel']['item'].split(':')[1]}."
    ent(L[f"entity.diaithirbestiary.{cid}"], ROM[sp["min_age"]], txt)

# ---- diaithir-nemesis warbands
nem = json.load(open(NEM))
seen = {}
ages = nem["ages"]
for age, d in ages.items():
    if not isinstance(d, dict):
        continue
    ranks = d["ranks"]
    ent(f"Kogad {age}", 0 + ROM[age],
        f"Warbands of Age {age}. A monster that kills a player is named, ranked ({ranks[0]} up to {ranks[-1]}) and comes back to ambush them; "
        f"its heir is '{d['heir']}' it. Bands of this Age: " + ", ".join(d["warbands"]) + ".")
    for w in d["warbands"]:
        if w in seen:
            seen[w][4] += f" Also in Age {age}."
            continue
        e = [w, "being", ROM[age], "bestiary",
             f"A warband name borne by named monsters (nemesis) from Age {age}. Ranks run from {ranks[0]} to {ranks[-1]}, and each Age Turn passes the grudge to an heir."]
        seen[w] = e
        out.append(e)

# ---- Ice and Fire (Ages from the mod's own item Ages: dragon gear IV, lightning V)
z = jar("iceandfire")
L = lang(z, "iceandfire")
IF = [("troll", 3, "A hulking cave brute that turns to stone in sunlight."),
      ("cyclops", 3, "A one-eyed giant that raids herds and farms."),
      ("gorgon", 3, "Its gaze turns the unwary to stone."),
      ("hydra", 3, "A many-headed serpent that grows its heads back and poisons its prey."),
      ("cockatrice", 3, "A crested beast whose gaze petrifies."),
      ("stymphalian_bird", 3, "Flocks of metal-feathered birds that rain feathers on a target."),
      ("amphithere", 3, "A winged serpent of the forests."),
      ("siren", 3, "Lures swimmers into deep water with her song."),
      ("sea_serpent", 3, "A great serpent of the open sea."),
      ("deathworm", 3, "A giant sand worm that bursts up from below."),
      ("ghost", 3, "A restless spirit bound to its grave."),
      ("fire_dragon", 4, "A dragon of the high crags that breathes fire; its bones and scales are Age IV materials."),
      ("ice_dragon", 4, "A dragon of the frozen lands that breathes frost; its bones and scales are Age IV materials."),
      ("lightning_dragon", 5, "A dragon that breathes lightning; its scales are an Age V material."),
      ("dread_thrall", 4, "A shambling soldier of an undead host."),
      ("dread_ghoul", 4, "A fast undead brute of the dread host."),
      ("dread_scuttler", 4, "A skittering undead crawler."),
      ("dread_beast", 4, "A beast raised by the dread host."),
      ("dread_knight", 4, "A mounted undead knight of the dread host."),
      ("dread_lich", 4, "An undead caster that commands the dread host."),
      ("dread_queen", 4, "The rare ruler of a dread host.")]
for eid, age, txt in IF:
    ent(L[f"entity.iceandfire.{eid}"], age, txt)

# ---- Mutant Monsters (Age of the vanilla thing each one grows from)
z = jar("MutantMonsters")
L = lang(z, "mutantmonsters")
MM = [("mutant_zombie", 1, "A zombie grown huge and strong."), ("mutant_skeleton", 1, "A towering skeleton that throws bone volleys."),
      ("mutant_snow_golem", 1, "A snow golem that fires ice."), ("spider_pig", 2, "A pig with spider legs that climbs and bites."),
      ("mutant_enderman", 3, "An enderman grown huge, with extra arms and a teleporting temper."),
      ("mutant_creeper", 4, "A creeper grown huge; its blast is strong and its gunpowder Age IV."),
      ("creeper_minion", 4, "A small creeper that follows and fights for its owner.")]
for eid, age, txt in MM:
    ent(L[f"entity.mutantmonsters.{eid}"], age, txt)

# ---- Untamed Wilds predators (spawn_tables/predators.json): present from Age I
z = jar("untamedwilds")
L = lang(z, "untamedwilds")
pred = json.loads(z.read("data/untamedwilds/spawn_tables/predators.json"))["entries"]
for e in pred:
    fam = e["type"].split(":")[1]
    species = sorted(v for k, v in L.items() if re.fullmatch(rf"entity\.untamedwilds\.{fam}_[a-z_]+", k) and not k.endswith("sciname"))
    name = L[f"entity.untamedwilds.{fam}"]
    sz = f"in groups of {e['size_min']} to {e['size_max']}" if e["size_max"] > 1 else "alone"
    ent(name + " (wild)", 1, f"Wild predator family with {len(species)} kinds, for example {', '.join(species[:4])}. Hunts {sz}; present from Age I.")

json.dump(out, open(os.path.join(ROOT, "library", "mobs.json"), "w"), indent=1, ensure_ascii=False)
gear = json.load(open(os.path.join(ROOT, "library", "gear.json")))
merged = gear + out
os.makedirs(os.path.dirname(OUT), exist_ok=True)
json.dump(merged, open(OUT, "w"), indent=1, ensure_ascii=False)
import collections
print(len(gear), "gear +", len(out), "mobs;", "mobs by age", sorted(collections.Counter(x[2] for x in out).items()))
