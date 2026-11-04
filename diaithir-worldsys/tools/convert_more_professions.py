#!/usr/bin/env python3
"""Convert the More Professions 1.5.3 datapack (Pepsied, MIT) trade tables to moreprofessions/jobs.json.
Usage: convert_more_professions.py UNZIPPED_DATAPACK_DIR OUT.json
Only trades found in the datapack's set/*.mcfunction files are emitted; nothing is invented."""
import json, re, sys, os

root = os.path.join(sys.argv[1], "data/more_professions/function/jobs")
TOKENS = {"circut_board": "minecraft:redstone_block", "lumber_jack": "minecraft:wooden_axe", "miner": "minecraft:wooden_pickaxe",
          "beekeeper": "minecraft:honeycomb", "hunter": "minecraft:wooden_sword", "explorer": "minecraft:compass"}
ITEM = r'\{id:\s*"([a-z_:0-9]+)",\s*count:\s*(\d+)b?\}'
LINE = re.compile(r'Recipes\[0\] set value \{maxUses:\s*(\d+),\s*buyB:\s*' + ITEM + r',\s*buy:\s*' + ITEM +
                  r',\s*sell:\s*' + ITEM + r',\s*xp:\s*(\d+),\s*uses:\s*0,\s*priceMultiplier:\s*([0-9.]+)f')

def trades_of(job, name, seen=()):
    path = os.path.join(root, job, "set", name + ".mcfunction")
    if not os.path.exists(path):
        print("skip missing set file", job, name, file=sys.stderr)
        return []
    text = open(path).read()
    m = LINE.search(text)
    if m:
        mu, bi, bc, ai, ac, si, sc, xp, pm = m.groups()
        t = {"buy": {"id": ai, "count": int(ac)}, "sell": {"id": si, "count": int(sc)}, "max_uses": int(mu), "xp": int(xp), "price_multiplier": float(pm)}
        if bi != "minecraft:air":
            t["buy_b"] = {"id": bi, "count": int(bc)}
        return [t]
    out = []
    for sub in re.findall(r"function more_professions:jobs/%s/set/(variants/[a-z_0-9/]+)" % job, text):
        sp = os.path.join(root, job, "set", sub + ".mcfunction")
        if not os.path.exists(sp):
            # upstream copy-paste slip: log2/log3/... point at variants/log1/*; use variants/<this set>/<leaf>
            alt = os.path.join(root, job, "set", "variants", name, os.path.basename(sub) + ".mcfunction")
            if not os.path.exists(alt):
                print("skip missing variant", sub, file=sys.stderr)
                continue
            sp = alt
        m2 = LINE.search(open(sp).read())
        if not m2:
            raise SystemExit("no trade in " + sp)
        mu, bi, bc, ai, ac, si, sc, xp, pm = m2.groups()
        t = {"buy": {"id": ai, "count": int(ac)}, "sell": {"id": si, "count": int(sc)}, "max_uses": int(mu), "xp": int(xp), "price_multiplier": float(pm)}
        if bi != "minecraft:air":
            t["buy_b"] = {"id": bi, "count": int(bc)}
        out.append(t)
    if not out and "set/variants/" not in text:
        # flower, boat, button... : dispatch through other set files
        for sub in re.findall(r"function more_professions:jobs/%s/set/([a-z_0-9]+)\b" % job, text):
            out += trades_of(job, sub)
    if not out and "set/variants/" not in text:
        raise SystemExit("unresolved " + path)
    return out

res = {}
for job, token in TOKENS.items():
    levels = []
    for n in range(1, 6):
        names = re.findall(r"check/([a-z_0-9]+)", open(os.path.join(root, job, "trade%d.mcfunction" % n)).read())
        slot = []
        for nm in names:
            # check/<nm> calls set/<nm> (or a set file of another name); resolve through the check file
            chk = open(os.path.join(root, job, "check", nm + ".mcfunction")).read()
            target = re.findall(r"function more_professions:jobs/%s/set/([a-z_0-9]+)\s*$" % job, chk, re.M)
            lst = [t for tgt in (target or [nm]) for t in trades_of(job, tgt)]
            if not lst:
                print("skip empty slot", job, nm, file=sys.stderr)
                continue
            slot.append(lst)
        levels.append(slot)
    res[job] = {"token": token, "levels": levels}
json.dump(res, open(sys.argv[2], "w"), indent=1)
print({j: [sum(len(s) for s in l) for l in v["levels"]] for j, v in res.items()})
