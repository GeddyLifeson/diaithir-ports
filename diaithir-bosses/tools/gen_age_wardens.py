"""Age Wardens: one mini-boss per Age (I..VI and VIII). Each warden is a lighter copy of an existing boss of the module:
same body kind, same attack classes (slam, charge, bolt, summon...), per-boss look and texture copied and tinted (no new art),
health about 45 percent of the boss it copies, damage 80 percent, Age-gated loot filtered from the source boss.
Additive: touches boss_index, boss/, looks/, textures, items, models, lang, age_gates, entity tags, names job. Run from the module root:
python3 -I tools/gen_age_wardens.py
English names live in lang; Diaithiris names come from /mnt/project-files/compute/jobs/age_wardens_names (merge.py) into age_wardens_diaithiris.json.
"""
import json, os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NS = "diaithirbosses"
RES = os.path.join(ROOT, "src/main/resources")
AS, DA = os.path.join(RES, "assets", NS), os.path.join(RES, "data", NS)
ROM = ["", "I", "II", "III", "IV", "V", "VI", "VII", "Rodos"]
# Id of the Age-evolving warden altar (block and item diaithirbosses:warden_altar, built by the rites thread on claude/warden-altar). Not the Age VII ascendant_altar.
ALTAR_RITE = "warden_altar"
JOB = "/mnt/project-files/compute/jobs/age_wardens_names"

# age, source boss, English name, hp, tint (rgb), scale factor, minion override, bar colour
SPEC = [
    (1, "tribal_chief", "Warden of the First Fire", 130, (230, 120, 60), 0.85, None, "yellow"),
    (2, "stone_colossus", "Warden of the Cut Stone", 190, (120, 170, 200), 0.75, None, "blue"),
    (3, "bronze_warking", "Warden of the Holy Well", 260, (240, 220, 120), 0.9, None, "white"),
    (4, "iron_walker", "Warden of the Sundering", 330, (150, 90, 90), 0.8, "minecraft:wolf", "red"),
    (5, "steel_marshal", "Warden of the Strangers' Road", 420, (110, 190, 170), 0.9, None, "green"),
    (6, "ceramic_warlord", "Warden of the Crowned Hall", 560, (170, 110, 220), 0.85, None, "purple"),
    (8, "ash_seraph", "Warden of the Far Dawn", 900, (120, 220, 240), 0.8, None, "pink"),
]

def R(p): return json.load(open(p, encoding="utf8"))
def W(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    json.dump(o, open(p, "w", encoding="utf8"), indent=1, ensure_ascii=False)

def tint(src, dst, rgb, k=0.4):
    im = Image.open(src).convert("RGBA")
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a: px[x, y] = tuple(int(c * (1 - k) + c * t / 255 * k * 1.6) if False else min(255, int(c * (1 - k) + (c * t / 255) * k * 1.8)) for c, t in zip((r, g, b), rgb)) + (a,)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    im.save(dst)

index = R(os.path.join(DA, "boss_index.json"))
lang = R(os.path.join(AS, "lang/en_us.json"))
gates = R(os.path.join(ROOT, "age_gates.json"))
tags = {n: R(os.path.join(RES, p)) for n, p in (("c", "data/c/tags/entity_type/bosses.json"), ("nl", "data/remnant_mobs/tags/entity_type/never_leveled.json"), ("nr", "data/remnant_mobs/tags/entity_type/never_ranked.json"))}
jobrows = []
ids = []
for age, src, en, hp, rgb, sf, minion, bar in SPEC:
    bid = f"age_warden_{age}"
    ids.append(bid)
    p = R(os.path.join(DA, "boss", src + ".json"))
    dm = 0.8
    p.update({"id": bid, "name": en, "name_en": en, "age": age, "entity": f"{NS}:{bid}", "tier": "age_warden", "structure": None, "summon": {"rite": ALTAR_RITE, "requires_kills": []}, "mythic": False,
              "creature_id": None, "xp": min(1200, int(p["xp"] * 0.45)), "equipment": dict(p.get("equipment") or {})})
    p.pop("fallback_own", None)
    o = p["own"]
    o["hp"] = hp
    o["damage"] = round(o["damage"] * dm, 1)
    o["scale"] = round(o["scale"] * sf, 2)
    p["bar"] = {"color": bar, "overlay": "notched_6"}
    # lighter fight: cap on stacking is 4x of the base in every case (hp_per_age 0.2 x 3 steps = 1.6x)
    p["scaling"] = {"hp_per_age": 0.2, "damage_per_age": 0.08, "hp_per_extra_player": 0.4, "max_age_steps": 3}
    p["arena"]["radius"] = max(16, int(p["arena"]["radius"] * 0.75))
    p["arena"]["burn"] = round(p["arena"]["burn"] * 0.6, 1)
    p["max_hit_fraction"] = 0.12
    p["respawn"] = {"mode": "cooldown", "ticks": 36000}
    for a in p["attacks"].values():
        if "damage" in a: a["damage"] = round(a["damage"] * dm, 1)
        if a.get("type") == "summon":
            a["count"] = max(1, a.get("count", 2) - 1)
            a["cap"] = min(a.get("cap", 6), 5)
            if minion: a["minion"] = minion
    if minion:
        # Age IV warden calls a hound pack (creature.hounds)
        for a in p["attacks"].values():
            if a["type"] == "cloud":
                a.update({"type": "summon", "count": 2, "cap": 4, "radius": 6, "minion": minion, "telegraph": 36, "cooldown": 200})
                for k in ("effect", "amp", "ticks", "duration"): a.pop(k, None)
    for ph in p["phases"]:
        ph["invuln"] = min(ph.get("invuln", 0), 30)
    keep = []
    for l in p["loot"]:
        if l["min_age"] > age + 1 or ":boss_" in l["item"]: continue   # no endgame cores from a mini-boss
        l = dict(l)
        l["min"] = max(1, round(l["min"] * 0.6)); l["max"] = max(l["min"], round(l["max"] * 0.6))
        keep.append(l)
    keep.append({"item": f"{NS}:champion_token", "min": 1, "max": 1, "min_age": age, "chance": 0.5, "per_age": 0})
    keep.append({"item": f"{NS}:ageshard", "min": 1, "max": 2, "min_age": age, "chance": 0.35, "per_age": 0})
    p["loot"] = keep
    p["look"] = True
    W(os.path.join(DA, "boss", bid + ".json"), p)
    lk = R(os.path.join(AS, "looks", src + ".json"))
    lk["id"] = bid
    W(os.path.join(AS, "looks", bid + ".json"), lk)
    tint(os.path.join(AS, "textures/entity/planet", src + ".png"), os.path.join(AS, "textures/entity/planet", bid + ".png"), rgb)
    for it in ("spawn_egg", "trophy"):
        tint(os.path.join(AS, "textures/item", f"{src}_{it}.png"), os.path.join(AS, "textures/item", f"{bid}_{it}.png"), rgb)
        W(os.path.join(AS, "items", f"{bid}_{it}.json"), {"model": {"type": "minecraft:model", "model": f"{NS}:item/{bid}_{it}"}})
        W(os.path.join(AS, "models/item", f"{bid}_{it}.json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{bid}_{it}"}})
    lang.update({f"entity.{NS}.{bid}": en, f"boss.{NS}.{bid}": en, f"item.{NS}.{bid}_spawn_egg": en + " Spawn Egg", f"item.{NS}.{bid}_trophy": "Trophy of the " + en})
    if bid not in index: index.append(bid)
    gates["entities"][f"{NS}:{bid}"] = ROM[age]
    gates["items"][f"{NS}:{bid}_trophy"] = gates["items"][f"{NS}:{bid}_spawn_egg"] = ROM[age]
    for t in tags.values():
        if not any(v["id"] == f"{NS}:{bid}" for v in t["values"]): t["values"].append({"id": f"{NS}:{bid}", "required": False})
    jobrows.append({"id": f"{NS}:{bid}", "name": en, "age": ROM[age]})

W(os.path.join(DA, "boss_index.json"), index)
W(os.path.join(AS, "lang/en_us.json"), lang)
W(os.path.join(ROOT, "age_gates.json"), gates)
for n, p in (("c", "data/c/tags/entity_type/bosses.json"), ("nl", "data/remnant_mobs/tags/entity_type/never_leveled.json"), ("nr", "data/remnant_mobs/tags/entity_type/never_ranked.json")):
    W(os.path.join(RES, p), tags[n])

# names job (same format as jobs/alexsmobs_names)
os.makedirs(JOB, exist_ok=True)
SYS = ("You give one modern Irish Gaelic (Gaeilge) word, or a short compound written as one word, naming a guardian figure, for a fantasy RPG on a 1:5 Earth "
       "with Celtic myth flavour. Output only the single lowercase word, no quotes, no explanation.")
with open(os.path.join(JOB, "in.jsonl"), "w", encoding="utf8") as f:
    for r in jobrows:
        f.write(json.dumps({"id": r["id"], "system": SYS, "prompt": f"Creature: {r['name']} (a guardian mini-boss of Age {r['age']}). Give the Irish Gaelic word for it.",
                            "max_tokens": 400, "name": r["name"]}, ensure_ascii=False) + "\n")
print("wardens:", ids)
