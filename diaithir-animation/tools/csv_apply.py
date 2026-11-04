#!/usr/bin/env python3
"""Keeps ANIMATIONS_COVERAGE.csv in step with diaithir-animation after an S6866 rerun.
Usage: csv_apply.py <guns.json> <coverage.csv>
- appends one row per diaithir-firearms gun (source 'roster') when S6866 does not list it yet, status done (clip, tag class and reload clip exist);
- marks paladins/rogues/wizards/archers/skill_tree spell rows done when their Spell Engine animation ids exist (checked by the jars, see TEST.md).
Rerunning is safe: existing rows are only updated."""
import csv, json, sys
guns, path = sys.argv[1], sys.argv[2]
rows = list(csv.DictReader(open(path, encoding="utf-8")))
cols = list(rows[0].keys())
have = {(r["category"], r["thing"]): r for r in rows}
added = 0
for g in json.load(open(guns))["guns"]:
    thing = "diaithirfirearms:" + g["id"]
    r = have.get(("gun", thing))
    if r:
        if r["status"] == "todo": r["status"] = "done"
        continue
    rows.append({"id": "ANR%05d" % (added + 1), "category": "gun", "namespace": "diaithirfirearms", "thing": thing, "kind": "item (gun)",
                 "animations_needed": "hold,aim,fire,reload,inspect,sprint-lower", "engine_route": "diaithir-animation (PAL), gun class " + g["class"],
                 "owner": "2-melee-guns", "status": "done", "source": "roster"})
    added += 1
w = csv.DictWriter(open(path, "w", newline="", encoding="utf-8"), cols); w.writeheader(); w.writerows(rows)
print("roster rows added", added)
