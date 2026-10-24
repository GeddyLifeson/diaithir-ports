import sys, json, os, types
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import render
from render import first_model, item_model, sheet
import gen, design
me = gen
ents = []
for b in gen.BLOCKS:
    if not design.shipped_block(me, b): continue
    kind = gen.classify(b)
    if kind in ("stairs", "slab", "wall", "pane"): continue
    m = first_model(b)
    if b in ("mechanical_press","water_wheel","large_water_wheel","basin","belt","mechanical_mixer"):
        m = {"mechanical_press":"create:block/mechanical_press/item","water_wheel":"create:block/water_wheel/item","large_water_wheel":"create:block/large_water_wheel/item","basin":"create:block/basin/block","belt":"create:block/belt/start","mechanical_mixer":"create:block/mechanical_mixer/block"}[b]
    if m: ents.append((b[:30], ("model", m)))
part = int(sys.argv[1]); n = int(sys.argv[2]) if len(sys.argv) > 2 else 48
if part >= 0:
    sheet(ents[part*n:(part+1)*n], f"/tmp/ship_{part}.png", cols=8, cell=192)
print(len(ents))
its = [(i[:26], ("icon", i)) for i in gen.ITEMS if not gen.ITEMS[i].get("blockItem") and design.shipped_item(me, i)]
sheet(its, "/tmp/ship_items.png", cols=8, cell=192)
print(len(its))
