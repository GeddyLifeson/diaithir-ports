"""Diaithiris names, Library lines and the Codex handbook for the everyday objects.
Irish terms: roster/irish_terms.json (modern Irish, from a Haiku pass; needs a native-speaker review). The Diaithiris form comes
from /mnt/project-files/diaithiris-archaic/tools/archaic.py. Run: python3 -I tools/gen_names_codex.py"""
import csv, json, sys
from pathlib import Path

sys.path.insert(0, "/mnt/project-files/diaithiris-archaic/tools")
import archaic

M = Path(__file__).resolve().parents[1]
LANG = M / "src/main/resources/assets/diaithirfurnishings/lang/en_us.json"
NS = "diaithirfurnishings"
rows = [r for r in csv.DictReader(open(M / "roster/roster.csv")) if r["id"] not in ("rope", "sack")]
irish = json.load(open(M / "roster/irish_terms.json"))
age = {i["id"]: i["age"] for i in json.load(open(M / "src/main/resources/data/diaithirfurnishings/everyday.json"))["items"]}
lang = json.load(open(LANG))
names = {}
for r in rows:
    term = irish.get(r["id"], "").strip().lower()
    if not term or not all(c.isalpha() or c in " -'" for c in term):
        continue
    native = archaic.text(term)
    if native.lower() == term:
        continue
    native = native[:1].upper() + native[1:]
    lang[f"item.{NS}.{r['id']}.native"] = native
    names[r["id"]] = native
json.dump(lang, open(LANG, "w"), indent=1, ensure_ascii=False)
json.dump({r["name"]: names[r["id"]] for r in rows if r["id"] in names}, open(M / "roster/names_map.json", "w"), indent=1, ensure_ascii=False)

chapters, lib = {}, []
for r in rows:
    nm = names.get(r["id"], r["name"])
    text = f"{r['name']}: earliest widespread use {r['earliest']} ({r['region']}). {r['summary']}"
    chapters.setdefault(r["category"], []).append({"id": f"{NS}:{r['id']}", "name": nm, "age": age[r["id"]], "text": text})
    lib.append([nm, "item", ["I", "II", "III", "IV", "V", "VI"].index(age[r["id"]]) + 1, "everyday", text])
(M / "codex").mkdir(exist_ok=True); (M / "library").mkdir(exist_ok=True)
json.dump({"id": f"{NS}:handbook", "title": "Everyday objects",
           "chapters": [{"tier": c, "name": c.replace("_", " ").title(), "age": "I", "text": f"Everyday objects: {c.replace('_', ' ')}.", "items": v}
                        for c, v in sorted(chapters.items())]},
          open(M / "codex/everyday_handbook.json", "w"), indent=1, ensure_ascii=False)
json.dump(lib, open(M / "library/everyday_library.json", "w"), indent=1, ensure_ascii=False)
print(len(names), "native names,", len(lib), "library lines,", len(chapters), "chapters")
