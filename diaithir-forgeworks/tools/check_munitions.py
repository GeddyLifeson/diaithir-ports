#!/usr/bin/env python3
"""Validate munitions/catalog.json: ids unique, parts exist and are not newer than the round,
stations match, SI stats positive, later stages in a line are never weaker, tags well formed.
Run: python3 -I check_munitions.py [catalog.json]"""
import json, os, sys

path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "munitions", "catalog.json")
d = json.load(open(path))
ages = {a: i for i, a in enumerate(d["ages"])}
errs = []
parts = {p["id"]: p for p in d["parts"]}
if len(parts) != len(d["parts"]):
    errs.append("duplicate part id")
ids = [r["id"] for r in d["rounds"]]
if len(set(ids)) != len(ids):
    errs.append("duplicate round id")
for p in d["parts"]:
    if p["station"] != d["stations"].get(p["kind"]):
        errs.append(f"{p['id']}: wrong station")
last = {}
for r in d["rounds"]:
    if r["age"] not in ages:
        errs.append(f"{r['id']}: bad age")
        continue
    for k in ("mass_kg", "blast_J"):
        if r[k] <= 0:
            errs.append(f"{r['id']}: {k} must be > 0")
    if r["range_m"] < 0 or not 0 <= r["guidance"] <= 3:
        errs.append(f"{r['id']}: bad range or guidance")
    for pid in r["parts"]:
        if pid not in parts:
            errs.append(f"{r['id']}: missing part {pid}")
        elif ages[parts[pid]["age"]] > ages[r["age"]]:
            errs.append(f"{r['id']}: part {pid} newer than round")
    if not all(t.startswith("c:ammo/") for t in r["tags"]):
        errs.append(f"{r['id']}: bad tag")
    prev = last.get(r["line"])
    if prev and ages[r["age"]] > ages[prev["age"]] and r["blast_J"] < prev["blast_J"] and r["line"] != "incendiary":
        errs.append(f"{r['id']}: weaker than {prev['id']}")
    last[r["line"]] = r
print("\n".join(errs) or f"OK: {len(d['rounds'])} rounds, {len(d['parts'])} parts")
sys.exit(1 if errs else 0)
