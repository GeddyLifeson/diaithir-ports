"""Smoke test over rcon: summons every registered goety item (ids from names_map.json + entities.json when present).
Usage: python3 -I tools/smoke.py <rcon-port>"""
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import rcon
root = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
ids = list(json.load(open(os.path.join(root, "names_map.json"))))
cmds = ["gamerule spawn_mobs false"] if False else []
cmds += [f'summon item 0 100 0 {{Item:{{id:"goety:{i}",count:1}}}}' for i in ids]
out = rcon.run(int(sys.argv[1]), cmds)
bad = [(c, o) for c, o in zip(cmds, out) if "Summoned" not in o]
print("summoned", len(cmds) - len(bad), "failed", len(bad))
for c, o in bad[:20]: print(c, "->", o)
