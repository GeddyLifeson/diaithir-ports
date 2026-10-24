import sys, json, os, render
from render import *
reg = json.load(open(os.path.dirname(__file__)+"/data/registries.json"))
ids = [b["id"] for b in reg["blocks"]]
ents = []
for i in ids:
    m = first_model(i)
    if m: ents.append((i[:28], ("model", m)))
n = int(sys.argv[2]) if len(sys.argv) > 2 else 64
part = int(sys.argv[1])
sheet(ents[part*n:(part+1)*n], f"/tmp/all_{part}.png", cols=8, cell=192)
print(len(ents))
