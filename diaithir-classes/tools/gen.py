#!/usr/bin/env python3
"""Write every data and asset JSON of diaithir-classes from tools/content.py. MIT.

Run from the module root:  python3 -I tools/gen.py <diaithir-arms module dir>
Textures are made by tools/textures.py. Prose (lore, Library text) is NOT written here: names_map.json and the job
stubs under /mnt/project-files/compute/jobs/classes_* carry structured facts for the text API.
"""
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import content as C  # noqa: E402
import names as N  # noqa: E402

NS = C.NS
RES = os.path.join(ROOT, "src/main/resources")
DATA = os.path.join(RES, "data", NS)
ASSET = os.path.join(RES, "assets", NS)
ARMS = sys.argv[1] if len(sys.argv) > 1 else "/home/user/wt/arms/diaithir-arms"


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=1)
        f.write("\n")


def tagfile(values):
    return {"replace": False, "values": values}


arms = json.load(open(os.path.join(ARMS, "src/main/resources/data/diaithirarms/arms.json")))
armor_mats = {k: arms["armor"][k] for _, k in [(0, t[0]) for t in C.TIERS]}
tool_mats = {k: arms["tools"][k] for k in [t[0] for t in C.TIERS]}
SPEAR = {"bronze": [0.95, 0.95, 0.60, 3.0, 11.0, 6.2, 5.1, 11.0, 4.6]}
for it in arms["items"]:
    if it["kind"] == "spear" and it["mat"] in ("steel", "composite"):
        SPEAR[it["mat"]] = it["spear"]
BOW_DUR = {t: next(i["durability"] for i in arms["items"] if i["id"] == f"{t}_bow") for t, _, _ in C.TIERS}
SLING_DUR = {"bronze": 250, "steel": 400, "composite": 650}

items = []      # Spec dicts for Java
lang = {}
english = {}    # id -> english name
dnames = {}     # id -> Diaithiris name
ages = {NS: "III"}
recipes = {}
models = {}     # id -> kind of item definition
ex = {c: C.EXTRA[c] for c in C.EXTRA}


def reg(iid, en, rod, age):
    rod = rod[:1].upper() + rod[1:]
    english[iid] = en
    dnames[iid] = rod
    lang[f"item.{NS}.{iid}"] = rod
    ages[f"{NS}:{iid}"] = age


def shapeless(name, ingredients, result, count=1):
    recipes[name] = {"type": "minecraft:crafting_shapeless", "category": "equipment", "ingredients": ingredients,
                     "result": {"id": f"{NS}:{result}", "count": count}}


def shaped(name, pattern, key, result, count=1):
    recipes[name] = {"type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern, "key": key,
                     "result": {"id": f"{NS}:{result}", "count": count}}


parts = []
EMB = {}
for cls, cd in C.CLASSES.items():
    eid = f"{cls}_emblem"
    parts.append(eid)
    EMB[cls] = f"{NS}:{eid}"
    reg(eid, f"{cd['word'].title()} Emblem", N.phrase("emblem", cd["word"]), "III")
    shapeless(eid, ["minecraft:leather"] + cd["emblem"], eid)

weapon_spells = {}  # item id -> list of spell ids
for cls, cd in C.CLASSES.items():
    for ti, (tk, age, n) in enumerate(C.TIERS):
        part = C.PART[tk]
        adj = N.rod(tk)
        # armour
        for slot in C.SLOTS:
            iid = f"{cls}_{slot}_{tk}"
            items.append(dict(id=iid, kind="armor", mat=tk, slot=slot, damage=0, speed=0, spear=None, durability=0, extras=None))
            reg(iid, f"{cd['word'].title()} {tk.title()} {slot.title()}", f"{N.rod(slot)} {N.rod(cd['word'])} {adj}", age)
            shapeless(iid, [f"diaithirarms:{tk}_{slot}", EMB[cls]], iid)
        ext = [dict(attribute=a, amount=v, op=op, slot="mainhand") for a, v, op in ex[cls][ti]]
        cum = [s for lo in C.LOADOUT[cls][: ti + 1] for s in lo]
        for kind, word in C.WEAPONS[cls]:
            iid = f"{cls}_{kind}_{tk}"
            base = dict(id=iid, mat=tk, slot=None, damage=0, speed=0, spear=None, durability=0, extras=ext)
            if kind == "sword":
                base.update(kind="sword", damage=3.0, speed=-2.4)
                models[iid] = "handheld"
                steel = f"diaithirarms:{tk}_sword"
                shapeless(iid, [steel, EMB[cls]] if tk != "bronze" else [part, part, "minecraft:stick", EMB[cls]], iid)
            elif kind == "spear":
                base.update(kind="spear", spear=SPEAR[tk])
                models[iid] = "spear"
                shapeless(iid, [f"diaithirarms:{tk}_spear", EMB[cls]] if tk != "bronze" else [part, "minecraft:stick", "minecraft:stick", EMB[cls]], iid)
            elif kind == "dagger":
                base.update(kind="sword", damage=1.5, speed=-1.6)
                models[iid] = "handheld"
                shapeless(iid, [part] * n + ["minecraft:stick", EMB[cls]], iid)
            elif kind == "crozier":
                base.update(kind="sword", damage=2.0, speed=-2.8)
                models[iid] = "handheld"
                shapeless(iid, [part] * (n + 1) + ["minecraft:stick", "minecraft:stick", EMB[cls]], iid)
            elif kind == "bow":
                base.update(kind="bow", durability=BOW_DUR[tk])
                models[iid] = "bow"
                shapeless(iid, [f"diaithirarms:{tk}_bow", EMB[cls]], iid)
            elif kind == "sling":
                base.update(kind="sling", durability=SLING_DUR[tk])
                models[iid] = "handheld"
                shapeless(iid, ["minecraft:leather", "minecraft:string"] + [part] * n + [EMB[cls]], iid)
            items.append(base)
            reg(iid, f"{cd['word'].title()} {kind.title()} ({tk})", f"{N.rod(word)} {N.rod(cd['word'])} {adj}", age)
            sp = [s for s in cum if s != "sleagh_volley" or kind == "bow"]
            if kind == "sling":
                sp = [s for s in sp if s != "sleagh_volley"] + C.SLING_EXTRA
                sp = list(dict.fromkeys(sp))
            if kind == "bow":
                sp = [s for s in sp if s != "sleagh_sling_shot"]
            if kind == "spear":
                pass
            weapon_spells[iid] = (sp, "ARCHERY" if kind == "bow" else "MAGIC")

# runes, rings
runes = []
for rid, tree, log, attr, amt, op, eff_text in C.RUNES:
    runes.append(dict(id=rid, attribute=attr, amount=amt, op=op))
    iid = f"rune_{rid}"
    reg(iid, f"Ogham Rune ({tree})", N.phrase("rune", tree), "III")
    lang[f"rune.{NS}.{rid}.effect"] = eff_text
    english[iid] = f"Ogham Rune {rid.title()} ({tree})"
    shapeless(iid, ["minecraft:flint", log, "minecraft:gold_nugget"], iid, 2)
rings = []
for rid, kind, tk, sockets, slot in C.RINGS:
    age = dict((t[0], t[1]) for t in C.TIERS)[tk]
    part = C.PART[tk]
    rings.append(dict(id=rid, sockets=sockets, curio=slot))
    reg(rid, f"{kind.title()} ({tk})", f"{N.rod(kind)} {N.rod(tk)}", age)
    if kind == "ring":
        shaped(rid, [" P ", "P P", " P "], {"P": part}, rid)
    else:
        shaped(rid, [" P ", "PLP", " P "], {"P": part, "L": "minecraft:lapis_lazuli"}, rid)

# ------------------------------------------------------------------ spells, assignments, sets
spell_ids = []
spell_names = {}
for sp in C.SPELLS:
    sid = sp["id"]
    wj(os.path.join(DATA, "spell", sid + ".json"), sp["spell"])
    rodn = N.phrase(*sp["words"])
    spell_names[sid] = rodn
    lang[f"spell.{NS}.{sid}.name"] = rodn
    lang[f"spell.{NS}.{sid}.description"] = sp["desc"]
    ages[f"{NS}:{sid}"] = sp["age"]
    spell_ids.append(f"{NS}:{sid}")
for iid, (sp, access) in weapon_spells.items():
    wj(os.path.join(DATA, "spell_assignments", iid + ".json"),
       {"access": access, "max_spell_count": len(sp), "spell_ids": [f"{NS}:{s}" for s in sp]})

sets = []
for cls, cd in C.CLASSES.items():
    for ti, (tk, age, n) in enumerate(C.TIERS):
        sid = f"{cls}_{tk}"
        pieces = [f"{NS}:{cls}_{s}_{tk}" for s in C.SLOTS]
        a2, a4 = C.SET_ATTR[cls]

        def attrs(lst, count):
            return [{"type": a, "id": f"{NS}:set_{sid}_{count}_{i}", "amount": vals[ti], "operation": op, "slot": "any"}
                    for i, (a, vals, op) in enumerate(lst)]
        passive = next(s for s in C.SPELLS if s["id"].startswith(cls + "_") and s["id"].endswith("_" + tk) and s["spell"].get("type") == "PASSIVE")
        wj(os.path.join(DATA, "equipment_set", sid + ".json"), {
            "name": f"{N.rod(cd['word'])} {N.rod(tk)}",
            "items": pieces,
            "bonuses": [
                {"required_piece_count": 2, "attributes": attrs(a2, 2)},
                {"required_piece_count": 4, "attributes": attrs(a4, 4),
                 "spells": {"access": "NONE", "max_spell_count": 1, "spell_ids": [f"{NS}:{passive['id']}"]}},
            ]})
        lang[f"equipment_set.{NS}.{sid}"] = f"{N.rod(cd['word'])} {N.rod(tk)}"
        sets.append(f"{NS}:{sid}")

# ------------------------------------------------------------------ main data file
wj(os.path.join(DATA, "classes.json"), dict(
    armor={k: dict(v) for k, v in armor_mats.items()}, tools={k: dict(v) for k, v in tool_mats.items()},
    items=items, parts=parts, runes=runes, rings=rings, spells=spell_ids, sets=sets,
    containers=[f"{NS}:{i}" for i in weapon_spells]))

for name, r in recipes.items():
    wj(os.path.join(DATA, "recipe", name + ".json"), r)
wj(os.path.join(RES, "data/curios/tags/item/ring.json"), tagfile([f"{NS}:{r['id']}" for r in rings if r["curio"] == "ring"]))
wj(os.path.join(RES, "data/curios/tags/item/charm.json"), tagfile([f"{NS}:{r['id']}" for r in rings if r["curio"] == "charm"]))

# ------------------------------------------------------------------ assets: item definitions and models
for sub in ("items", "models"):
    shutil.rmtree(os.path.join(ASSET, sub), ignore_errors=True)


def model(iid, parent="minecraft:item/generated", tex=None):
    wj(os.path.join(ASSET, "models/item", iid + ".json"), {"parent": parent, "textures": {"layer0": f"{NS}:item/{tex or iid}"}})


def simple_def(iid, extra=None):
    d = {"model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}}
    if extra:
        d.update(extra)
    wj(os.path.join(ASSET, "items", iid + ".json"), d)


allids = [i["id"] for i in items] + parts + [f"rune_{r['id']}" for r in runes] + [r["id"] for r in rings]
for iid in allids:
    kind = models.get(iid, "generated")
    if kind == "handheld":
        model(iid, "minecraft:item/handheld")
        simple_def(iid)
    elif kind == "spear":
        model(iid)
        model(iid + "_in_hand", "minecraft:item/spear_in_hand")
        wj(os.path.join(ASSET, "items", iid + ".json"), {
            "model": {"type": "minecraft:select", "property": "minecraft:display_context",
                      "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"}}],
                      "fallback": {"type": "minecraft:model", "model": f"{NS}:item/{iid}_in_hand"}},
            "swap_animation_scale": 1.95})
    elif kind == "bow":
        model(iid, "minecraft:item/bow")
        for i in range(3):
            model(f"{iid}_pulling_{i}", f"minecraft:item/bow_pulling_{i}")
        wj(os.path.join(ASSET, "items", iid + ".json"), {"model": {
            "type": "minecraft:condition", "property": "minecraft:using_item",
            "on_false": {"type": "minecraft:model", "model": f"{NS}:item/{iid}"},
            "on_true": {"type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": 0.05,
                        "entries": [{"threshold": 0.65, "model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}_pulling_1"}},
                                    {"threshold": 0.9, "model": {"type": "minecraft:model", "model": f"{NS}:item/{iid}_pulling_2"}}],
                        "fallback": {"type": "minecraft:model", "model": f"{NS}:item/{iid}_pulling_0"}}}})
    else:
        model(iid)
        simple_def(iid)

# spell icons are textures/spell/<id>.png (made by textures.py)
lang.update({
    "message.diaithir_classes.socket_full": "Every socket is filled.",
    "message.diaithir_classes.socket_duplicate": "That rune is already set in it.",
    "message.diaithir_classes.socket_not_applicable": "That does not fit.",
    "tooltip.diaithir_classes.sockets": "Sockets %s/%s",
    "tooltip.diaithir_classes.hint": "Right-click a rune onto it in your inventory to set it.",
})
wj(os.path.join(ASSET, "lang/en_us.json"), lang)

# ------------------------------------------------------------------ age gates, names, codex
wj(os.path.join(ROOT, "age_gates.json"), {
    "_doc": "Same shape as DiaithirRPG content/tech_ages.json: namespaces and per-item entries (items beat namespaces). Bronze tier III, steel V, composite VII (the same Ages as the diaithir-arms materials they reuse); runes and emblems III; spells carry the Age of the tier that grants them.",
    "namespaces": {NS: "III"}, "items": {k: v for k, v in ages.items() if k != NS}})
wj(os.path.join(ROOT, "names_map.json"), {k: {"diaithiris": dnames[k], "english": english[k]} for k in dnames})
chapters = []
for cls, cd in C.CLASSES.items():
    chapters.append(dict(
        class_id=cls, name=N.rod(cd["word"]).capitalize(), text="", school=cd["school"],
        weapons=[{"id": f"{NS}:{i['id']}", "name": dnames[i["id"]], "age": ages[f"{NS}:{i['id']}"]} for i in items if i["id"].startswith(cls + "_") and i["kind"] != "armor"],
        armour=[{"id": f"{NS}:{i['id']}", "name": dnames[i["id"]], "age": ages[f"{NS}:{i['id']}"]} for i in items if i["id"].startswith(cls + "_") and i["kind"] == "armor"],
        spells=[{"id": f"{NS}:{s['id']}", "name": spell_names[s["id"]], "age": s["age"], "text": s["desc"]} for s in C.SPELLS if s["cls"] == cls]))
wj(os.path.join(ROOT, "codex/classes_handbook.json"), dict(id=f"{NS}:handbook", title=N.phrase("warrior", "archer").split()[0], chapters=chapters,
                                                        runes=[{"id": f"{NS}:rune_{r[0]}", "name": dnames[f"rune_{r[0]}"], "effect": r[6], "age": "III"} for r in C.RUNES]))

# ------------------------------------------------------------------ text-API job stubs (structured facts only)
SYS_LORE = ("You write one short tooltip line for an item in a Minecraft RPG set on a 1:5 Earth across seven Ages, with Celtic and Irish myth flavour. "
            "Plain words, no em dashes, no real people, no franchise names, no invented gods. Output only the line, 8 to 18 words, no quotes.")
SYS_LIB = ("You write a short Library entry for a Minecraft RPG set on a 1:5 Earth across seven Ages, with Celtic and Irish myth flavour. "
           "Plain words, no em dashes, no real people, no franchise names, no invented gods. Use only the facts given and keep every number. "
           "Output 1 to 3 sentences, 15 to 55 words, no quotes.")
JOBS = "/mnt/project-files/compute/jobs"
lore_rows, lib_rows = [], []
for iid in dnames:
    lore_rows.append(dict(id=f"classes:{iid}", system=SYS_LORE,
                          prompt=f"Mod: classes. Item: {english[iid]} (Diaithiris name: {dnames[iid]}). Write the tooltip line for this item.",
                          name=dnames[iid], max_tokens=60))
AGE_NUM = {"III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7}


def lib(name, kind, age, locale, facts):
    lib_rows.append(dict(id=f"classes:{locale}:{len(lib_rows)}", system=SYS_LIB, name=name, kind=kind, age=AGE_NUM[age], locale=locale,
                         prompt=f"Name: {name}. Kind: {kind}. Age: {age}. Facts: {facts}", max_tokens=140))


for cls, cd in C.CLASSES.items():
    lib(N.rod(cd["word"]).capitalize(), "concept", "III", "classes",
        f"Class kit of the {cd['word']}s. Gear in Ages III (bronze), V (steel), VII (composite); power school {cd['school']}; "
        f"four pieces of one tier give set bonuses at 2 and 4 pieces. Weapons: " + ", ".join(k for k, _ in C.WEAPONS[cls]) + ".")
for sp in C.SPELLS:
    lib(spell_names[sp["id"]], "concept", sp["age"], "magic", f"Spell of the {C.CLASSES[sp['cls']]['word']} class, tier {sp['spell'].get('tier')}, school {sp['spell']['school']}. Effect: {sp['desc']}")
for r in C.RUNES:
    lib(dnames[f"rune_{r[0]}"], "item", "III", "runes", f"Ogham rune of the {r[1]}. Craft: flint, {r[2].split(':')[1]}, gold nugget gives 2. Socketed in a ring or charm it gives {r[6]}.")
for rid, kind, tk, sockets, slot in C.RINGS:
    lib(dnames[rid], "item", dict((t[0], t[1]) for t in C.TIERS)[tk], "runes", f"A {kind} of {tk}, worn in the {slot} slot, with {sockets} rune socket(s).")


def write_job(kind, rows, readme, merge):
    d = os.path.join(JOBS, kind)
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, "in.jsonl"), "w", encoding="utf8") as f:
        for r in rows:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")
    open(os.path.join(d, "README"), "w").write(readme)
    open(os.path.join(d, "merge.py"), "w").write(merge)


BANNED = r"\b(fian|fianna|sleagh|ceilg|cleirech)\b"
RETRY_NOTE = " Never use the words Fian, Fianna, Sleagh, Ceilg or Cleirech; say warrior, archer, hunter or healer instead."
MERGE_COMMON = """import json,pathlib,re
d=pathlib.Path(__file__).parent
rd=d.parent/(d.name+'_retry')
meta={}
for l in open(d/'in.jsonl'):
    o=json.loads(l); meta[o['id']]=o
outs={}
for f in (d/'out.jsonl', rd/'out.jsonl'):
    if f.exists():
        for l in open(f):
            o=json.loads(l); outs[o['id']]=o
BAN=re.compile(r'%s',re.I)
def retry(bad):
    if not bad: return
    rd.mkdir(exist_ok=True)
    with open(rd/'in.jsonl','w') as f:
        for i in bad:
            r=dict(meta[i]); r['system']=r['system']+%r; f.write(json.dumps(r,ensure_ascii=False)+chr(10))
    (rd/'README').write_text('Retry rows of '+d.name+': rows that failed the check (modern Irish class words or length). Same format; merge.py of '+d.name+' reads out.jsonl here too.'+chr(10))
""" % (BANNED, RETRY_NOTE)
MERGE_LORE = MERGE_COMMON + """rows=[]; bad=[]
for i,m in meta.items():
    o=outs.get(i); t=o['text'].strip().strip('"') if o else ''
    if o and 8<=len(t.split())<=18 and '\\u2014' not in t and not BAN.search(t) and not BAN.search(m['name']): rows.append([m['name'],'item',0,'classes_lore',t])
    else: bad.append(i)
retry(bad)
json.dump(rows,open('/mnt/project-files/library-sync/classes_lore.json','w'),ensure_ascii=False)
print(len(rows),'of',len(meta),'; retry rows',len(bad))
"""
MERGE_LIB = MERGE_COMMON + """rows=[]; bad=[]
for i,m in meta.items():
    o=outs.get(i); t=o['text'].strip().strip('"') if o else ''
    n=len(t.split()); s=len(re.findall(r'[.!?](?:\\s|$)',t))
    if o and 15<=n<=55 and 1<=s<=3 and '\\u2014' not in t and not BAN.search(t): rows.append([m['name'],m['kind'],m['age'],m['locale'],t])
    else: bad.append(i)
retry(bad)
json.dump(rows,open('/mnt/project-files/library-sync/classes.json','w'),ensure_ascii=False)
print(len(rows),'of',len(meta),'; retry rows',len(bad))
"""
write_job("classes_lore", lore_rows,
          "Owner: classes thread (diaithir-classes). Rows: every item id of the mod as classes:<item id>. One tooltip line per item.\n"
          "Check rule: 8 to 18 words, no em dash, no quotes, no proper names of real people. Failing rows go to Claude Batch. max_tokens 60.\n"
          "Merge: python3 /mnt/project-files/compute/jobs/classes_lore/merge.py (writes library-sync/classes_lore.json as [name, \"item\", 0, \"classes_lore\", text]).\n",
          MERGE_LORE)
write_job("classes_library", lib_rows,
          "Owner: classes thread (diaithir-classes). Rows: class kits, spells, runes, rings, each with the facts and numbers in the prompt. 1 to 3 sentences.\n"
          "Check rule: 15 to 55 words, 1 to 3 sentences, no em dash, no quotes. Failing rows go to Claude Batch. max_tokens 140.\n"
          "Merge: python3 /mnt/project-files/compute/jobs/classes_library/merge.py (writes library-sync/classes.json as [name, kind, age, locale, text]).\n",
          MERGE_LIB)
print("items", len(items), "parts", len(parts), "runes", len(runes), "rings", len(rings), "spells", len(spell_ids), "sets", len(sets),
      "recipes", len(recipes), "lore rows", len(lore_rows), "lib rows", len(lib_rows))
