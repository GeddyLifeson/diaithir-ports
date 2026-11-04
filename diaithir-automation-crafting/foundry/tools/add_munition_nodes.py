#!/usr/bin/env python3
"""Adds/refreshes one progression node per munition round and container (ids munition.*) in graph.json.
Usage: add_munition_nodes.py [graph.json]  then python3 -I check_progression.py graph.json --report"""
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_munitions_content as M

path = sys.argv[1] if len(sys.argv) > 1 else "/mnt/project-files/plans/progression/graph.json"
g = json.load(open(path))
g["nodes"] = [n for n in g["nodes"] if not n["id"].startswith("munition.")]
cat = json.load(open(M.CATALOG))
new = []


def node(iid, name, age, station, makes, form, line, container=False):
    new.append({"id": "munition." + iid, "system": "combat", "age": M.ROMAN[M.AGES.index(age)], "name": name, "needs": [station],
                "makes": makes, "module": "diaithir-automation-crafting", "quest": "q.munition." + iid,
                "item": f"{M.NS}:" + (iid if container else "munition_" + iid), "station": station, "form": form, "item_final": True})


for r in cat["rounds"]:
    node(r["id"], M.title(r["id"]), r["age"], M.STATION[M.AGES.index(r["age"])], [r["tags"][0]], "assembly", r["line"])
for c in cat["containers"]:
    node(c["id"], M.title(c["id"]), c["age"], M.STATION[M.AGES.index(c["age"])], [c["tags"][0]], "assembly", c["kind"], True)
# consumers: one mount/rack node per round line (Keelwright, vehicle packs), one feed node per container kind (guns)
def consumer(nid, name, age_i, needs, makes, module):
    new.append({"id": nid, "system": "ships", "age": M.ROMAN[age_i], "name": name, "needs": needs, "makes": [makes], "terminal": makes,
                "module": module, "quest": "q." + nid})


for line in sorted({r["line"] for r in cat["rounds"]}):
    first = min(M.AGES.index(r["age"]) for r in cat["rounds"] if r["line"] == line)
    consumer(f"munition.mount.{line}", f"Vehicle mounts for {line}", first, [f"c:ammo/{line}"], f"mount.{line}", "diaithir-ships")
for kind in sorted({c["kind"] for c in cat["containers"]}):
    first = min(M.AGES.index(c["age"]) for c in cat["containers"] if c["kind"] == kind)
    consumer(f"munition.feed.{kind}", f"Guns fed by {kind}", first, [f"c:ammo_container/{kind}"], f"feed.{kind}", "diaithir-combat-classes")
g["nodes"].extend(new)
json.dump(g, open(path, "w"), indent=1)
print(len(new), "munition nodes")
