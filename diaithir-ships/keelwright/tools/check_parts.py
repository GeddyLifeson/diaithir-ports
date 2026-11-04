#!/usr/bin/env python3
"""Validate the part catalog and print per-class balance (usage: check_parts.py [parts.json])."""
import json, sys, pathlib

KINDS = {"hull", "control", "drive", "power", "storage", "link", "weapon", "shield", "crew", "life", "lift", "dock", "attitude"}
path = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).parent.parent / "data" / "parts.json")
cat = json.loads(path.read_text())
classes = cat["hull_classes"]
errors, seen = [], set()
for p in cat["parts"]:
    pid = p["id"]
    if pid in seen: errors.append(f"duplicate id {pid}")
    seen.add(pid)
    if p["kind"] not in KINDS: errors.append(f"{pid}: bad kind {p['kind']}")
    if not 1 <= p["age"] <= 8: errors.append(f"{pid}: age {p['age']} outside 1-8")
    if p["mass"] <= 0 or p["hp"] <= 0: errors.append(f"{pid}: mass/hp must be positive")
    for c in p["classes"]:
        if c not in classes: errors.append(f"{pid}: unknown class {c}")
    if p["kind"] == "drive" and p.get("thrust", 0) <= 0: errors.append(f"{pid}: drive needs thrust")
    if p["kind"] == "weapon" and p.get("damage", 0) <= 0: errors.append(f"{pid}: weapon needs damage")
    if p["kind"] == "dock" and p.get("range", 0) <= 0: errors.append(f"{pid}: dock needs range")
    if p["kind"] == "attitude" and p.get("torque", 0) <= 0: errors.append(f"{pid}: attitude part needs torque")
    if "fuel_table" in p and not p.get("fuel_per_tick"): errors.append(f"{pid}: fuel_table without fuel_per_tick")

fuels = json.loads((path.parent / "thruster_fuels.json").read_text())["fuels"]
tags = [f["tag"] for f in fuels]
if len(tags) != len(set(tags)): errors.append("duplicate fuel tag")
for f in fuels:
    if not f["tag"].startswith("c:") or not f["upstream"] or not (0.5 <= f["thrust_mult"] <= 1.5 and 0.5 <= f["burn_mult"] <= 1.5):
        errors.append(f"fuel {f['tag']}: bad tag, upstream list or multiplier")
print(f"{len(fuels)} fuels merged from {sum(len(f['upstream']) for f in fuels)} upstream ids")

def thrust_per_mass(parts):
    m = sum(p["mass"] for p in parts)
    t = sum(p.get("thrust", 0) for p in parts)
    return t / m if m else 0

for cname, c in classes.items():
    members = [p for p in cat["parts"] if cname in p["classes"]]
    if not any(p["kind"] == "control" for p in members): errors.append(f"{cname}: no control part")
    if not any(p["kind"] == "drive" for p in members): errors.append(f"{cname}: no drive part")
    for drive in (p for p in members if p["kind"] == "drive"):
        # reference ship: 30 hull of best-age plate + helm + this drive x2
        hulls = [p for p in members if p["kind"] == "hull" and p["age"] <= drive["age"]]
        if not hulls: errors.append(f"{cname}: no hull available at age {drive['age']} for {drive['id']}"); continue
        hull = max(hulls, key=lambda p: p["hp"])
        ship = [hull] * 30 + [next(p for p in members if p["kind"] == "control")] + [drive] * 2
        r = thrust_per_mass(ship)
        flag = "" if c["min_thrust_per_mass"] <= r <= c["max_thrust_per_mass"] else "  <-- OUT OF RANGE"
        if flag: errors.append(f"{cname}/{drive['id']}: thrust/mass {r:.3f} outside {c['min_thrust_per_mass']}-{c['max_thrust_per_mass']}")
        print(f"{cname:6} {drive['id']:14} age {drive['age']} thrust/mass {r:.3f}{flag}")
    pw = sum(-p["power"] for p in members if p["kind"] == "power")
    print(f"{cname:6} total generation across catalog: {pw} PU/tick")

print(f"{len(cat['parts'])} parts, {len(errors)} errors")
for e in errors: print("ERROR", e)
sys.exit(1 if errors else 0)
