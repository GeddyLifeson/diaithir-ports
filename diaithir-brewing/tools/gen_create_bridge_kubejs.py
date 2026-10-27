#!/usr/bin/env python3
"""Make Create mixing recipes from the drinks KubeJS data (a_data.js + the recipe rules of brewing.js).

usage: gen_create_bridge_kubejs.py <a_data.js> <out data dir> [--namespaces a,b,...] [--exclude item,item,...]

Mirrors drkRecipeFor() and the age I/II 'vat' filter of brewing.js, one heated-basin Create mixing
recipe per brew. Each ingredient group becomes an alternatives list (any item of the group), counts
are repeated slots (Create allows 9). Items from namespaces not listed in --namespaces are dropped
from the alternatives; a group left empty skips the recipe. Water is the basin's fluid.
"""
import json, re, sys, pathlib

DAY = 24000
BUCKET = 81000
ITEM = "diaithir_drinks:"


def load(path):
    s = pathlib.Path(path).read_text()
    def const(name):
        m = re.search(r"^const %s = (.*)$" % name, s, re.M)
        return json.loads(m.group(1))
    keys, rows, ing, extras = const("DRINK_KEYS"), const("DRINK_ROWS"), const("DRINK_ING"), const("DRINK_EXTRAS")
    groups = dict(ing)
    groups["grain"] = ["croptopia:barley", "biomesoplenty:barley", "minecraft:wheat", "croptopia:rye", "croptopia:oat", "croptopia:corn"]
    groups["young_wine"] = [ITEM + "young_wine"]
    groups["neutral"] = [ITEM + "neutral_spirit"]
    for k in ("grain", "corn", "cane", "grape", "fruit", "agave"):
        groups["nm_" + k] = [ITEM + "new_make_" + k]
    return [dict(zip(keys, r)) for r in rows], groups


def recipe_for(d):
    a, base = d["abv"], d["bases"][0]
    n = max(2, min(6, round(2 + a / 4)))
    r = {"id": d["id"], "inputs": [], "ticks": DAY, "station": d["station"], "age": d["age"]}
    grainy = base in ("rice", "millet", "rye", "corn", "oat", "wheat")
    ins = r["inputs"]
    route = d["route"]
    if route == "ferment":
        if d["cat"] == "beer":
            if base in ("barley", "malt"): ins.append(["malt", n])
            else: ins += [[base, n], ["malt", 1]]
            if d["hopped"]: ins.append(["hops", 1])
            ins.append(["yeast", 1])
        elif base == "honey":
            ins.append(["honey", n])
            if re.search("metheglin", d["id"]): ins.append(["herb", 1])
            if re.search("melomel|cyser|pyment", d["id"]):
                ins.append([("apple" if "cyser" in d["id"] else "grape" if "pyment" in d["id"] else "berry"), 2])
        elif grainy or base == "barley":
            ins += [[base, n], ["yeast", 1]]
        else:
            ins.append([base, n + 1])
            if a > 9: ins.append(["sugar", 1])
        r["ticks"] = DAY * min(4, 1 + int(a // 4))
    elif route == "sparkle":
        ins += [["grape", 5], ["sugar", 1]]; r["ticks"] = DAY * 3
    elif route == "lager":
        ins += [["malt", 3], ["hops", 1], ["yeast", 1]]; r["ticks"] = DAY * 6
    elif route == "distill":
        if d["station"] == "cask":
            ins.append(["nm_" + d["new_make"], 3]); r["ticks"] = DAY * max(1, d["cask_days"])
        else:
            ins.append([base, min(6, 3 + int(a // 30))])
            if grainy or base == "barley": ins.append(["yeast", 1])
            r["ticks"] = 6000
    elif route == "fortify":
        ins += [["young_wine", 3], ["neutral", 1]]; r["ticks"] = DAY * max(1, d["cask_days"])
    elif route == "steep":
        ins += [["neutral", 1], [base, 3], ["sugar", 2]]; r["ticks"] = DAY * 2
    return r


def main(src, out, namespaces=None, exclude=()):
    rows, groups = load(src)
    ok_ns = set(namespaces.split(",")) if namespaces else None
    out = pathlib.Path(out) / "create" / "recipe" / "mixing"
    out.mkdir(parents=True, exist_ok=True)
    made, skipped = 0, []
    for d in rows:
        r = recipe_for(d)
        if r["station"] != "vat" or r["age"] not in ("I", "II"):
            continue
        slots, why = [], None
        for g, c in r["inputs"]:
            alts = [i for i in groups.get(g, []) if (not ok_ns or i.split(":")[0] in ok_ns) and i not in exclude]
            if not alts:
                why = "group %s has no usable item" % g
                break
            slots += [alts[0] if len(alts) == 1 else alts] * c
        if why is None and len(slots) > 9:
            why = "%d ingredient slots, Create allows 9" % len(slots)
        if why:
            skipped.append((d["id"], why))
            continue
        rec = {
            "neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "diaithir_create"}],
            "type": "create:mixing",
            "heat_requirement": "heated",
            "processing_time": min(2400, max(20, r["ticks"] // 20)),
            "ingredients": slots,
            "fluid_ingredients": [{"type": "fluid_stack", "amount": BUCKET, "fluid": "minecraft:water"}],
            "results": [{"id": ITEM + d["id"], "count": 3}],
        }
        (out / ("brew_" + d["id"] + ".json")).write_text(json.dumps(rec, indent=1) + "\n")
        made += 1
    for i, w in skipped:
        print("skip", i, w)
    print(made, "written,", len(skipped), "skipped")


if __name__ == "__main__":
    ns = sys.argv[sys.argv.index("--namespaces") + 1] if "--namespaces" in sys.argv else None
    ex = sys.argv[sys.argv.index("--exclude") + 1].split(",") if "--exclude" in sys.argv else []
    main(sys.argv[1], sys.argv[2], ns, set(ex))
