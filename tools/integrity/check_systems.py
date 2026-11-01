#!/usr/bin/env python3 -I
"""System checks across pack + DiaithirRPG. Usage: check_systems.py <pack_root> <DiaithirRPG_root> [items.tsv]
Culture starts (130 master units each have a spawn + giver + name unit), Age order and gating tables, quest item refs,
calendar/timeline config, economy script test, transport mods present. Exit 1 on any ERROR."""
import sys, os, re, csv, json, glob, subprocess, collections
pack, rpg = sys.argv[1], sys.argv[2]
items_f = sys.argv[3] if len(sys.argv) > 3 else "/mnt/project-files/pack_item_ids.tsv"
E, W = [], []
J = lambda p: json.load(open(p, encoding="utf-8-sig"))
items = {l.split("\t")[0] for l in open(items_f, encoding="utf-8")}
nspaces = {i.split(":")[0] for i in items}

# culture starts
master = [r["unit"] for r in csv.DictReader(open("/mnt/project-files/cultures/MASTER_CULTURES.csv", encoding="utf-8"))]
spawns, givers = J(f"{pack}/kubejs/data/diaithir/culture_spawns.json"), J(f"{pack}/kubejs/data/diaithir/culture_givers.json")
units = J(f"{rpg}/content/names/units.json")
for nm, d in (("spawns", spawns), ("givers", givers), ("name units", units)):
    if len(d) != len(master) + 1: E.append(f"{nm}: {len(d)} entries, expected {len(master)} master units + diaithir")
for u in set(units) ^ set(spawns) | set(units) ^ set(givers): E.append(f"culture slug {u} not present in spawns, givers and name units alike")
for u, s in spawns.items():
    if not isinstance(s.get("x"), int) or not isinstance(s.get("z"), int): E.append(f"spawn {u}: bad coordinates")
    elif abs(s["x"]) > 6_400_000 or abs(s["z"]) > 6_400_000: E.append(f"spawn {u}: outside 1:5 world")
if spawns.get("diaithir", {}).get("x") != -251201 or spawns["diaithir"].get("z") != -1541408: E.append("diaithir spawn is not Creag Ruadh -251201,-1541408")
print(f"cultures: {len(master)} master, {len(spawns)} spawns, {len(givers)} givers, {len(units)} name units")

# Age order and gating
ta, tt = J(f"{rpg}/content/tech_ages.json"), J(f"{rpg}/content/tech_tree.json")
if ta["order"][1:] != [e for e in tt["eraOrder"] if e in ta["order"]] and set(ta["order"]) - {"TELLING"} != set(tt["eraOrder"]) - {"VIII"}:
    W.append(f"Age order differs: ages {ta['order']} vs tree {tt['eraOrder']}")
src = open(f"{pack}/kubejs/server_scripts/speuran/age_gate.js", encoding="utf-8").read()
m = re.search(r"SPEURAN_AGES = \[(.*?)\]", src)
ag = re.findall(r"'([^']+)'", m.group(1)) if m else []
if ag != ta["order"][:len(ag)]: E.append(f"age_gate AGES {ag} not a prefix of tech_ages order {ta['order']}")
for k, v in re.findall(r"'([a-z_]+:[a-z_]+)': '([IVX]+|RODOS)'", src):
    if v not in ta["order"]: E.append(f"age_gate {k}: unknown Age {v}")
    if f"{k}.json" and not os.path.exists(f"{pack}/kubejs/data/{k.split(':')[0]}/dimension/{k.split(':')[1]}.json"): E.append(f"age_gate world {k}: no dimension json")
badns = [n for n in ta["namespaces"] if n not in nspaces and n not in ta["nontech"]]
if badns: W.append(f"{len(badns)} gated namespaces absent from the pack item dump: {badns[:12]}")
for it, a in ta["items"].items():
    if a not in ta["order"]: E.append(f"tech_ages item {it}: unknown Age {a}")
unmapped = sum(1 for k in ta["items"] if J(f"{rpg}/content/tech_items.json")["items"].get(k) is None)
if unmapped: E.append(f"{unmapped} aged items have no tech")
print(f"ages: {len(ta['items'])} per-item ages, {len(ta['namespaces'])} namespaces, tree {len(tt['techs'])} techs")

# quests
ids = set(); bad = collections.Counter(); n = 0
for p in glob.glob(f"{rpg}/content/history_quests/*.json"):
    for q in J(p)["quests"]:
        n += 1
        for st in q.get("stages", []):
            g = st.get("goal", "")
            mm = re.match(r"(?:supply|have|craft|mine):([a-z0-9_.-]+:[a-z0-9_/.-]+?)(?::\d+)?$", g)
            if mm and not mm.group(1).split(':')[1].isdigit() and mm.group(1) not in items: bad[mm.group(1)] += 1
for i, c in bad.most_common(): E.append(f"quest item {i} not in pack (x{c})")
print(f"quests: {n} history quests, {len(bad)} unknown items")
r = subprocess.run([sys.executable, "-I", f"{rpg}/tools/history/check_history_quests.py"], capture_output=True, text=True)
if r.returncode: E.append("history quest check: " + r.stdout[-200:])

# calendar / timeline
tl = J(f"{pack}/kubejs/config/earth_timeline.json")
if not isinstance(tl, dict): E.append("earth_timeline.json not an object")
for f in glob.glob(f"{pack}/kubejs/server_scripts/earth_timeline/*.js"):
    if subprocess.run(["node", "--check", f], capture_output=True).returncode: E.append(f"timeline script syntax: {os.path.basename(f)}")
cy = f"{rpg}/content/calendar/kubejs/server_scripts/calendar/calendar_year.js"
if subprocess.run(["node", "--check", cy], capture_output=True).returncode: E.append("calendar_year.js syntax")

# economy
r = subprocess.run(["node", f"{rpg}/tools/economy/test_economy.js"], capture_output=True, text=True)
if r.returncode: E.append("economy test: " + r.stderr[-300:])
else: print("economy:", r.stdout.strip())

# ships: Valkyrien Skies and vs_eureka are gated in tech_ages but absent from pack-latest mods (warned above); not an error here
# transport: rail, road, sea, air mods in the pack
mods = " ".join(os.listdir(f"{pack}/mods")).lower()
for need, label in (("railcraft", "rail"), ("immersive_vehicles", "road/air vehicles"), ("create", "create"), ("lightmans", "company/economy")):
    if need not in mods: E.append(f"transport/economy mod missing: {label} ({need})")
for w in W: print("WARN", w)
for e in E: print("ERROR", e)
print("ERRORS", len(E), "WARNINGS", len(W)); sys.exit(1 if E else 0)
