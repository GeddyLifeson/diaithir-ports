"""Smoke test over rcon: summons every registered goety item (ids from names_map.json + entities.json when present).
Usage: python3 -I tools/smoke.py <rcon-port>"""
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rcon
root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
names = json.load(open(os.path.join(root, "names_map.json")))
ids = [i for i, v in names.items() if not v.get("entity")]
ents = [i for i, v in names.items() if v.get("entity")]
ids += [e + "_spawn_egg" for e in ents]
cmds = ["gamerule spawn_mobs false"] if False else []
cmds += [f'summon item 0 100 0 {{Item:{{id:"goety:{i}",count:1}}}}' for i in ids]
cmds += [f"summon goety:{e} 0 100 0" for e in ents]
import glob
wg = os.path.join(root, "src/main/resources/data/goety")
structs = sorted(os.path.basename(f)[:-5] for f in glob.glob(wg + "/worldgen/structure/*.json"))
import time
placed = []
for k, n in enumerate(structs):
    x = 200 + k * 160
    rcon.run(int(sys.argv[1]), [f"forceload add {x - 80} 120 {x + 80} 280"]); time.sleep(20)
    placed.append((f"place structure goety:{n} {x} 80 200", rcon.run(int(sys.argv[1]), [f"place structure goety:{n} {x} 80 200"])[0]))
    rcon.run(int(sys.argv[1]), [f"forceload remove {x - 80} 120 {x + 80} 280"])
rcon.run(int(sys.argv[1]), ["forceload add 40 40"]); time.sleep(5)
for c, o in placed: print(c, "->", o[:90])
cmds += [f"place feature goety:{os.path.basename(f)[:-5]} 40 20 40" for f in glob.glob(wg + "/worldgen/placed_feature/*.json")]
loots = sorted(os.path.relpath(f, wg + "/loot_table")[:-5] for f in glob.glob(wg + "/loot_table/**/*.json", recursive=True) if "/blocks/" not in f)
cmds += [f"loot spawn 0 100 0 loot goety:{l}" for l in loots]
cmds += ["goetyselftest"]
out = rcon.run(int(sys.argv[1]), cmds)
bad = [(c, o) for c, o in zip(cmds, out) if not c.startswith(("goetyselftest", "place", "loot")) and "Summoned" not in o]
for c, o in zip(cmds, out):
    if c.startswith(("place", "loot")): print(c, "->", o[:90])
print([o for c, o in zip(cmds, out) if c.startswith("goetyselftest")])
print("summoned", len(cmds) - len(bad), "failed", len(bad))
for c, o in bad[:20]: print(c, "->", o)
