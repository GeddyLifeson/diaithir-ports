#!/usr/bin/env python3
"""Writes one Heavy Yard definition per mega vehicle plus the runtime catalog copy.
Source: diaithir-ships/keelwright/megamachines/catalog.json. Usage: python3 -I gen_mega_yards.py"""
import json, pathlib
R = pathlib.Path(__file__).resolve().parent.parent
src = R.parent / "diaithir-ships/keelwright/megamachines/catalog.json"
out = R / "src/main/resources/data/diaithirmega"
cat = json.loads(src.read_text())
TIER = {"I": "WORKSHOP", "II": "WORKSHOP", "III": "WORKSHOP", "IV": "ERECTING_SHOP", "V": "ERECTING_SHOP", "VI": "ASSEMBLY_LINE", "VII": "AUTOMATED", "VIII": "AUTOMATED", "RODOS": "AUTOMATED"}
# working-gear kind and crew-seat kind per class; a stage is (id, slot kind, count, base ticks)
WORK = {"shovel": "bucket", "dragline": "boom", "bwe": "wheel_boom", "tbm": "cutter_head", "truck": "hold", "crane": "boom", "transporter": "deck", "builder": "printer_head"}
def stages(v):
    m = v["mass_t"]
    scale = max(1, round(m ** 0.5))          # heavier machine, longer stages
    st = [("powertrain", "engine", 1, 200 * scale), ("running_gear", "track", 2 if m > 500 else 1, 150 * scale),
          ("working_gear", WORK[v["class"]], 1, 250 * scale), ("crew", "seat", v["crew"], 40 * v["crew"])]
    if v["autonomy_capable"]:
        st.append(("autonomy", "autonomy_module", 1, 100))
    return [{"id": i, "order": n + 1, "slots": [{"kind": k, "count": c}], "base_ticks": t, "ghost": f"diaithirmega:ghost/{v['id'].split(':')[1]}_{i}"} for n, (i, k, c, t) in enumerate(st)]
(out / "yards").mkdir(parents=True, exist_ok=True)
for v in cat["vehicles"]:
    vid = v["id"].split(":")[1]
    side = max(5, round(v["deck_m2"] ** 0.5))
    y = {"id": vid, "tier": TIER[v["age"]], "site": "ground", "dimensions": [], "footprint": {"w": side, "l": side * 2, "h": max(6, side // 2)},
         "result": "diaithirmega:" + vid, "stages": stages(v), "crane_speedup": 1.0}
    (out / "yards" / f"{vid}.json").write_text(json.dumps(y, indent=1) + "\n")
(out / "catalog.json").write_text(src.read_text())
print(len(cat["vehicles"]), "yards")
