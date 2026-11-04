#!/usr/bin/env python3
"""Generate munitions/catalog.json from the compact table below (plans/MUNITIONS.md).
Game-balance numbers only, SI units: mass kg, blast J, range m. Run: python3 -I gen_munitions.py"""
import json, os

AGES = ["ancient", "ailean", "holy", "sundering", "strangers", "kingdom", "dubhan", "future"]
# station that makes each part kind (Tinkers-style: cast or press a part, then assemble on the table)
STATIONS = {
    "casing": "casting_bay", "charge": "powder_works", "filler": "filler_station",
    "fuze": "fuze_bench", "guidance": "guidance_bench", "motor": "casting_bay",
    "round": "assembly_table",
}
# line: (tag, parts kinds needed, [(age, id, mass, blast, range, guidance)])
LINES = {
    "shot": ("c:ammo/shot", ["casing", "charge"], [
        (0, "stone_shot", 8, 2e3, 120, 0), (1, "iron_ball", 10, 6e3, 200, 0),
        (2, "chain_shot", 11, 7e3, 220, 0)]),
    "shell": ("c:ammo/shell", ["casing", "charge", "filler", "fuze"], [
        (3, "fused_shell", 14, 2e5, 800, 0), (4, "steel_shell", 18, 4e5, 1500, 0),
        (5, "he_shell", 22, 9e5, 2500, 0), (6, "guided_shell", 24, 1.2e6, 4000, 2)]),
    "incendiary": ("c:ammo/incendiary", ["casing", "filler"], [
        (0, "pitch_arrow", 0.1, 1e3, 60, 0), (1, "greek_fire_pot", 4, 8e4, 40, 0),
        (3, "fire_shell", 14, 1.5e5, 900, 0), (5, "cluster_incendiary", 20, 4e5, 2000, 0)]),
    "rocket": ("c:ammo/rocket", ["casing", "charge", "fuze", "motor"], [
        (0, "fire_arrow_rocket", 0.3, 2e3, 100, 0), (1, "war_rocket", 6, 5e4, 600, 0),
        (4, "spin_rocket", 12, 3e5, 1800, 0), (5, "rocket_pod_round", 9, 4e5, 2500, 0),
        (6, "guided_rocket", 14, 7e5, 4000, 2)]),
    "bomb": ("c:ammo/bomb", ["casing", "filler", "fuze"], [
        (0, "thrown_pot", 2, 3e4, 20, 0), (1, "powder_keg", 25, 4e5, 15, 0),
        (4, "aerial_bomb", 120, 6e6, 0, 0), (5, "cluster_bomb", 160, 8e6, 0, 0),
        (6, "smart_bomb", 150, 9e6, 0, 3)]),
    "torpedo": ("c:ammo/torpedo", ["casing", "charge", "filler", "fuze", "motor"], [
        (3, "spar_torpedo", 60, 1e6, 10, 0), (4, "self_propelled_torpedo", 400, 5e6, 3000, 0),
        (5, "wire_guided_torpedo", 450, 6e6, 6000, 1), (6, "homing_torpedo", 480, 7e6, 8000, 2)]),
    "missile": ("c:ammo/missile", ["casing", "charge", "filler", "fuze", "motor", "guidance"], [
        (3, "early_guided_rocket", 40, 8e5, 3000, 1), (4, "radio_missile", 90, 2e6, 8000, 1),
        (5, "heat_seeker", 85, 2.5e6, 12000, 2), (6, "radar_missile", 150, 4e6, 40000, 3),
        (7, "shard_missile", 160, 8e6, 80000, 3)]),
}
# rail cars carry the same rounds; cars themselves belong to the transport module
# containers (MUNITIONS_CONTAINERS.md): id, kind, class, capacity in rounds (box: kg), age, parts made at casting_bay then assembled
CONTAINERS = [
    ("magazine_pistol", "magazine", "pistol", 15, 5), ("magazine_smg", "magazine", "smg", 30, 6),
    ("magazine_rifle", "magazine", "rifle", 30, 6), ("magazine_sniper", "magazine", "sniper", 5, 6),
    ("magazine_lmg_box", "magazine", "lmg", 100, 6), ("drum_smg", "drum", "smg", 70, 6), ("drum_lmg", "drum", "lmg", 150, 6),
    ("clip_rifle", "clip", "rifle", 5, 5), ("speedloader_pistol", "speedloader", "pistol", 6, 5),
    ("belt_lmg", "belt", "lmg", 200, 6), ("cartridge_musket", "cartridge", "musket", 1, 3),
    ("cell_energy", "cell", "energy", 20, 7), ("ammo_box_small", "box", "any", 40, 4), ("ammo_box_large", "box", "any", 200, 5),
]
out = {"ages": AGES, "stations": STATIONS, "parts": [], "rounds": []}
seen = set()
for line, (tag, kinds, rows) in LINES.items():
    for age, rid, mass, blast, rng, guid in rows:
        parts = [f"{rid}_{k}" for k in kinds]
        for p, k in zip(parts, kinds):
            if p not in seen:
                seen.add(p)
                out["parts"].append({"id": p, "kind": k, "station": STATIONS[k], "age": AGES[age]})
        out["rounds"].append({"id": rid, "line": line, "age": AGES[age], "mass_kg": mass,
                              "blast_J": blast, "range_m": rng, "guidance": guid,
                              "parts": parts, "station": STATIONS["round"],
                              "tags": [tag, f"{tag}/{AGES[age]}"]})
out["containers"] = [{"id": i, "kind": k, "class": c, "capacity": n, "age": AGES[a], "station": STATIONS["casing"],
                      "link": "link_lmg" if i == "belt_lmg" else None, "tags": [f"c:ammo_container/{k}", f"diaithircrafting:ammo_class/{c}"]}
                     for i, k, c, n, a in CONTAINERS]
path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "munitions", "catalog.json")
with open(path, "w") as f:
    json.dump(out, f, indent=1)
print(len(out["rounds"]), "rounds,", len(out["parts"]), "parts")
