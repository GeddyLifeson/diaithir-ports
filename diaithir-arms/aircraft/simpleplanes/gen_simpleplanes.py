#!/usr/bin/env python3
"""Writes upgrades.json, helicopter.json and ../../age_gates_simpleplanes.json for the Simple Planes fold, then validates them.
Usage: gen_simpleplanes.py [diaithir-arms dir]. Values are read from Simple Planes 5.3.9 (LGPL-3.0-only, commit 3b1617c) and converted to SI.
Tick units: 1 tick = 0.05 s, 1 FE = 1 J, 1 mB = 1 L (plans/UNITS.md)."""
import json, pathlib, sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parents[2])
out = root / "aircraft" / "simpleplanes"

# fuel cost per tick by airframe class (config defaults plane_fuel_cost 3, large 6, cargo 10, helicopter 6)
FUEL_COST = {"light": 3, "large": 6, "cargo": 10, "helicopter": 6}
FRAMES = list(FUEL_COST)
HELI = ["helicopter"]

def watts(fe_per_tick):  # 1 FE/t = 20 W
    return fe_per_tick * 20

U = []
def up(id, kind, age, frames, effect, source, ingredients, **extra):
    U.append(dict(id=id, kind=kind, age=age, frames=frames, effect=effect, source=source, ingredients=ingredients, **extra))

up("seats", "crew", "VI", FRAMES, "Adds passenger seats (light: 1 extra; large and helicopter: up to 3 extra).", "SeatsUpgrade",
   {"shaped": ["CCC", "SPS"], "C": "#minecraft:planks", "S": "#c:slimeballs", "P": "minecraft:phantom_membrane"})
up("floaty_bedding", "hull", "VI", FRAMES, "Plane floats on water: +0.04 blocks/tick^2 lift while the hull is in water, so it takes off from a lake (0.8 m/s^2).", "FloatingUpgrade",
   {"shaped": ["III", "CSC", "III"], "I": "#c:ingots/iron", "C": "#minecraft:wool", "S": "#c:slimeballs"})
up("armor", "defence", "VI", FRAMES, "Armour plating: incoming damage x (1 - 0.04 x armour value of the fitted armour item).", "ArmorUpgrade",
   {"shaped": ["ii", "bib", "ibi"], "i": "#c:ingots/iron", "b": "#c:storage_blocks/iron"})
up("shooter", "weapon", "VI", FRAMES, "Fires arrows, fire charges or firework rockets from the nose (one item per shot, inventory slot of 1 stack).", "ShooterUpgrade",
   {"shapeless": ["minecraft:dispenser", "minecraft:flint_and_steel"]}, hook="diaithir-firearms: replace the three vanilla projectiles with mounted-gun ammunition when that mount exists")
up("healing", "repair", "VII", FRAMES, "One-shot repair kit: restores 1 hit point in flight, 2 on the ground, up to 2x max health; consumed on use.", "HealingUpgrade",
   {"shaped": ["i i", " E ", " i "], "i": "#c:ingots/iron", "E": "minecraft:golden_apple"}, consumable=True)
up("folding", "hull", "VII", FRAMES, "Folds the wings so the plane fits a hangar bay or a ship deck.", "FoldingUpgrade",
   {"shaped": [" s ", "pdp", " s "], "s": "minecraft:shulker_shell", "d": "#c:gems/diamond", "p": "minecraft:piston"},
   hook="diaithir-ships Keelwright deck parts and the hangar yard read the folded footprint")
up("furnace_engine", "engine", "VI", FRAMES, "Burns any furnace fuel in slot 1. Burn ticks drain at the airframe fuel cost per tick, so one coal (1600 ticks) lasts %s s on a light plane and %s s on a helicopter." % (1600 // 3 // 20, 1600 // 6 // 20), "FurnaceEngineUpgrade",
   {"shaped": ["iRi", "YBY", "LLL"], "i": "#c:ingots/iron", "R": "propeller", "Y": "#c:dusts/redstone", "B": "minecraft:blast_furnace", "L": "minecraft:light_weighted_pressure_plate"},
   engine=True, fuel_cost_per_tick=FUEL_COST)
up("liquid_engine", "engine", "VII", FRAMES, "Burns liquid fuel from a 4000 L internal tank (fuel values per litre come from the plane_liquid_fuels data folder); fills from buckets and fluid-item containers.", "LiquidEngineUpgrade",
   {"shaped": ["iOi", "BLB", "HcH"], "i": "#c:ingots/iron", "O": "propeller", "B": "minecraft:bucket", "L": "minecraft:blast_furnace", "H": "minecraft:heavy_weighted_pressure_plate", "c": "#c:ingots/copper"},
   engine=True, tank_litres=4000, hook="shared fuel API (avgas, diesel): map plane_liquid_fuels to the cars-thread fuel tags")
up("electric_engine", "engine", "VII", FRAMES, "1.5 MJ battery; draws 12 J per tick of fuel cost while throttled: %s W light, %s W large, %s W cargo, %s W helicopter." % tuple(watts(12 * FUEL_COST[f]) for f in FRAMES), "ElectricEngineUpgrade",
   {"shaped": ["###", "/G/", "R#R"], "#": "#c:ingots/copper", "/": "#c:rods/blaze", "G": "propeller", "R": "#c:storage_blocks/redstone"},
   engine=True, capacity_joules=1_500_000, draw_watts={f: watts(12 * FUEL_COST[f]) for f in FRAMES},
   hook="SI energy capability via diaithir-units Units/Fmt; charging station block charges at any rate the grid offers")
up("solar_panel", "power", "VII", ["light", "large", "cargo"], "Trickle charge for the electric engine in daylight with sky access: up to 100 W on a light plane, 200 W on a large or cargo plane, scaled by sun and cut by rain and thunder. Helicopters cannot fit it.", "SolarPanelUpgrade",
   {"shaped": ["GGG", "QDQ", "WWW"], "G": "#c:glass_blocks/colorless", "Q": "#c:gems/quartz", "D": "minecraft:daylight_detector", "W": "#c:gems/lapis"},
   peak_watts={"light": watts(5), "large": watts(10), "cargo": watts(10)}, requires="electric_engine")
up("booster", "engine", "VII", FRAMES, "Raises the throttle ceiling from 5 to 10 steps for short bursts.", "BoosterUpgrade",
   {"shaped": ["II", "FF", "II"], "I": "#c:ingots/iron", "F": "minecraft:flint_and_steel"}, throttle_steps=10)
up("banner", "cosmetic", "VI", FRAMES, "Mounts a banner on the tail.", "BannerUpgrade", {"shapeless": ["#minecraft:banners"]})
up("payload", "cargo", "VI", ["cargo", "large"], "Carries one payload crate that can be dropped over a target; allowed payloads come from the plane_payload data folder.", "PayloadUpgrade", {})
up("chest", "cargo", "VI", FRAMES, "Adds a chest inventory.", "ChestUpgrade", {"shapeless": ["#c:chests/wooden"]})
up("supply_crate", "cargo", "VI", FRAMES, "A barrel on a parachute that can be released from the air.", "SupplyCrateUpgrade", {"shapeless": ["minecraft:barrel", "parachute"]},
   hook="Ad Astra cargo-rocket fold: same crate/parachute drop reused for planetary drops (survey p1)")
up("jukebox", "cosmetic", "VII", FRAMES, "Plays a disc for everyone in range while flying.", "JukeboxUpgrade", {"shapeless": ["minecraft:jukebox"]})

HEL = {
    "id": "simple_helicopter", "age": "VII", "frames": HELI, "base_class": "large",
    "source": "HelicopterEntity (extends LargePlaneEntity)",
    "seats": {"pilot": [0, 0.5], "rear": [0, -0.5], "left": [-1, 0], "right": [1, 0]},
    "controls": "W/S pitch forward/back (max 20 degrees), A/D yaw (2 degrees per tick) and bank (30 degrees forward, 50 degrees climbing), jump key climbs, no key holds hover.",
    "fuel_cost_per_tick": 6, "camera_distance_multiplier": 1.2,
    "flight_model_ticks": {"passive_engine_push": 0.028, "push": 0.05, "drag_quadratic_x10": True, "drag_x2": True,
                           "forward_push_x": 1.5, "back_push_x": 0.2, "climb_push_per_throttle_step": 0.01, "idle_horizontal_drag": 0.999},
    "forbidden_upgrades": ["solar_panel"],
    "notes": "Flight model is expressed in the source's per-tick push units; the Java side converts at load (1 push = 1 block/tick^2 = 400 m/s^2 before drag). The IV vehicle def is not generated here: IV rotor fields are not verified, see README.",
    "airframe_recipe": {"workbench": "plane_workbench", "propellers": 2, "material_units": 7},
}

AGE_ORDER = "III IV V VI VII VIII".split()
ids = set()
for u in U:
    assert u["id"] not in ids, u["id"]; ids.add(u["id"])
    assert u["age"] in AGE_ORDER and u["frames"] and all(f in FRAMES for f in u["frames"]), u["id"]
    if u.get("requires"): assert u["requires"] in {x["id"] for x in U}
assert not set(HEL["forbidden_upgrades"]) & {u["id"] for u in U if "helicopter" in u["frames"]}, "helicopter lists a forbidden upgrade"
assert sum(1 for u in U if u.get("engine")) == 3

doc = {"_doc": "Simple Planes upgrade catalogue folded into diaithir-arms/aircraft. Derived from Simple Planes 5.3.9 (przemykomo, LGPL-3.0-only, commit 3b1617cbdd6a88c645f9af6fb1ebad6714fbc798); see NOTICE. SI units: W, J, L, s.",
       "airframes": {"light": {"fuel_cost_per_tick": 3}, "large": {"fuel_cost_per_tick": 6}, "cargo": {"fuel_cost_per_tick": 10}, "helicopter": {"fuel_cost_per_tick": 6}},
       "upgrades": U}
(out / "upgrades.json").write_text(json.dumps(doc, indent=1, ensure_ascii=False) + "\n")
(out / "helicopter.json").write_text(json.dumps(HEL, indent=1) + "\n")
gates = {"_doc": "Simple Planes fold: upgrade item ids by Age (ids diaithiraircraft:upgrade_<id>)",
         "items": {"diaithiraircraft:upgrade_" + u["id"]: u["age"] for u in U}}
gates["items"]["diaithiraircraft:simple_helicopter"] = HEL["age"]
(root / "age_gates_simpleplanes.json").write_text(json.dumps(gates, indent=1) + "\n")
print("ok: %d upgrades, helicopter, %d gated ids" % (len(U), len(gates["items"])))
