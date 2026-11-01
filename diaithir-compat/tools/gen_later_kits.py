"""Adds kits for Diaithir, the courts and the Age V eras to site_people.json, from later_dress.csv (same columns as culture_dress.csv
plus slug, mainhand, carried, villager_type). Age VI and later keep MCA's own clothes: the painter has no modern or future garments.
python diaithir-compat/tools/gen_later_kits.py    (needs numpy and Pillow; safe to rerun, it replaces its own kits)"""
import csv, json, sys
from pathlib import Path
HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE / "outfits"))
import clothing
sys.path.insert(0, str(HERE))
from gen_people import RES, NS, ROLES, LATER_AGE, STACK

rows = list(csv.DictReader(open(HERE / "later_dress.csv", newline="", encoding="utf-8")))
jf = RES / "data" / NS / "site_people.json"
data = json.loads(jf.read_text(encoding="utf-8"))
ao = clothing.load_ao(HERE / "outfits" / "ao.png")
for r in rows:
    kit = r["slug"][:40].strip("_")
    items = [r["mainhand"]] + r["carried"].split()
    assert not [i for i in items if LATER_AGE.search(i)], (kit, items)
    r["culture"] = kit   # seeds the painter
    entries, ids, roles = [], {}, {}

    def save(img, gender, name):
        path = f"skins/clothing/normal/{kit}/{gender}/{name}.png"
        dest = RES / "assets" / NS / path
        dest.parent.mkdir(parents=True, exist_ok=True)
        clothing.indexed(img, 48).save(dest, optimize=True, transparency=255)
        entries.append({"id": f"{NS}:{path}", "profession": f"{NS}:{kit}", "chance": 1.0, "gender": gender})
        return f"{NS}:{path}"
    for g in ("male", "female"):
        for n in range(3):
            ids.setdefault(g, []).append(save(clothing.render(r, g, n, ao), g, str(n)))
        for role in ROLES:
            roles.setdefault(role, {})[g] = save(clothing.render(r, g, role, ao), g, role)
    lst = RES / "data" / NS / "skins" / "clothing" / f"{kit}.json"
    lst.parent.mkdir(parents=True, exist_ok=True)
    lst.write_text(json.dumps(entries, indent=2) + "\n", encoding="utf-8")
    data["kits"][kit] = {"clothing": ids, "roles": roles}
    data["cultures"]["slug:" + kit] = {"kit": kit, "mainhand": r["mainhand"], "carried": [{"item": i, "count": STACK.get(i, 1)} for i in r["carried"].split()],
                                       "villager_type": "minecraft:" + r["villager_type"]}
data["kits"] = dict(sorted(data["kits"].items())); data["cultures"] = dict(sorted(data["cultures"].items()))
jf.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
print(len(rows), "kits added")
