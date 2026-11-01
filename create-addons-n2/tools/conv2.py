"""Convert 1.20.1/1.21.1 Create-addon datapack content to the 26.1 Diaithir Create port formats."""
import json, re, sys, zipfile, os, glob

MB = 81  # port fluid amounts are droplets (81 per mB)
SINGLE = {"create:cutting", "create:crushing", "create:milling", "create:splashing", "create:haunting",
          "create:pressing", "create:sandpaper_polishing"}
VANILLA_HOME = {"minecraft", "create", "c", "neoforge", "forge"}
DIRS = {"recipes": "recipe", "advancements": "advancement", "loot_tables": "loot_table", "structures": "structure",
        "functions": "function", "predicates": "predicate", "item_modifiers": "item_modifier"}
TAGDIRS = {"items": "item", "blocks": "block", "fluids": "fluid", "entity_types": "entity_type",
           "game_events": "game_event", "biomes": "worldgen/biome"}
warn = []


def tagname(t):
    t = t.lstrip("#")
    if t.startswith("forge:"):
        t = "c:" + t[6:]
    return t


def is_fluid(x):
    return isinstance(x, dict) and ("fluid" in x or "fluidTag" in x or "fluid_tag" in x or
                                    str(x.get("type", "")).startswith("fluid") or "neoforge:fluid" in str(x.get("type", "")))


def ing(x):
    """item ingredient -> 26.1 plain string / list"""
    if isinstance(x, str):
        return "#" + tagname(x) if x.startswith("#") else x
    if isinstance(x, list):
        r = [ing(i) for i in x]
        return r[0] if len(r) == 1 else r
    if isinstance(x, dict):
        if "item" in x and isinstance(x["item"], str):
            return x["item"]
        if "tag" in x:
            return "#" + tagname(x["tag"])
        if "id" in x and isinstance(x["id"], str):
            return x["id"]
        if x.get("type") in ("neoforge:compound",) and "children" in x:
            return ing(x["children"])
        if "ingredient" in x:
            return ing(x["ingredient"])
    warn.append(("ingredient shape", json.dumps(x)[:120]))
    return x


def fluid_in(x):
    amt = int(x.get("amount", 1000)) * MB
    if "fluid" in x and x.get("type", "fluid_stack") not in ("fluid_tag",):
        f = x["fluid"]
        if isinstance(f, str) and f.startswith("#"):
            return {"type": "fluid_tag", "amount": amt, "fluid_tag": "#" + tagname(f)}
        return {"type": "fluid_stack", "amount": amt, "fluid": f}
    t = x.get("fluidTag") or x.get("fluid_tag") or x.get("tag") or x.get("fluid")
    return {"type": "fluid_tag", "amount": amt, "fluid_tag": "#" + tagname(t)}


def comp(c):
    if not isinstance(c, dict):
        return c
    c = dict(c)
    d = c.get("minecraft:dyed_color")
    if isinstance(d, dict):
        c["minecraft:dyed_color"] = d.get("rgb", 0)
    return c


def res(x):
    """item result -> {id,count,chance}"""
    if isinstance(x, str):
        return {"id": x}
    o = {}
    it = x.get("id") or x.get("item")
    if isinstance(it, dict):  # {"item": {"id":..,"count":..}}
        o.update({k: v for k, v in it.items() if k in ("id", "count", "components")})
    else:
        o["id"] = it
    for k in ("count", "chance", "components"):
        if k in x:
            o[k] = comp(x[k]) if k == "components" else x[k]
    if "nbt" in x and "components" not in o:
        warn.append(("nbt result dropped", o["id"]))
    if isinstance(o.get("chance"), (int, float)) and o["chance"] > 1:
        o["chance"] = o["chance"] / 100.0
    if o.get("count") == 1:
        o.pop("count")
    return o


def fres(x):
    return {"id": x["fluid"], "amount": int(x.get("amount", 1000)) * MB}


def heat(v):
    if isinstance(v, bool):
        return "heated" if v else "none"
    return v


def split_ing(lst):
    items, fl = [], []
    for i in lst:
        (fl if is_fluid(i) else items).append(i)
    return [ing(i) for i in items], [fluid_in(i) for i in fl]


def split_res(lst):
    items, fl = [], []
    for i in lst:
        (fl if isinstance(i, dict) and "fluid" in i else items).append(i)
    return [res(i) for i in items], [fres(i) for i in fl]


def seq_step(s, trans):
    t = s["type"]
    ings = s.get("ingredients")
    if ings is None:  # already single-style
        ings = [s["ingredient"]] if "ingredient" in s else []
    items, fl = split_ing(ings)
    held = [i for i in items if i != trans and i != "$ingredient"]
    o = {"type": t}
    if t == "create:deploying":
        o.update({"target": "$ingredient", "ingredient": held[0] if held else items[-1]})
        if s.get("keep_held_item") or s.get("keepHeldItem"):
            o["keep_held_item"] = True
        o["results"] = ["$result"]
    elif t == "create:filling":
        o.update({"ingredient": "$ingredient", "fluid_ingredient": fl[0], "result": "$result"})
    elif t == "create:pressing" or t == "create:cutting":
        o.update({"ingredient": "$ingredient", "results": ["$result"]})
        if "processingTime" in s or "processing_time" in s:
            o["processing_time"] = s.get("processing_time", s.get("processingTime"))
        elif t == "create:cutting":
            o["processing_time"] = 50
    else:
        o.update({"ingredient": "$ingredient", "results": ["$result"]})
    return o


def conv_recipe(d):
    if not isinstance(d, dict):
        return d
    t = d.get("type", "")
    if "conditions" in d:
        d["neoforge:conditions"] = d.pop("conditions")
    for c in d.get("neoforge:conditions", []):
        _fixcond(c)
    for k in ("processingTime", "heatRequirement"):
        if k in d:
            d[re.sub("([A-Z])", lambda m: "_" + m.group(1).lower(), k)] = d.pop(k)
    if "heat_requirement" in d:
        d["heat_requirement"] = heat(d["heat_requirement"])
    if "superheated" in d:
        if d.pop("superheated"):
            d["heat_requirement"] = "superheated"
    if t == "create:sequenced_assembly":
        trans = d.pop("transitional_item", None) or d.pop("transitionalItem", None)
        tid = trans.get("id") or trans.get("item")
        d["transitional_item"] = {"id": tid}
        d["ingredient"] = ing(d["ingredient"])
        rs = d.pop("results", None)
        if rs is not None:
            main = res(rs[0])
            d["result"] = main
            if len(rs) > 1:
                d["junks"] = [res(r) for r in rs[1:]]
        else:
            d["result"] = res(d["result"])
        if "junks" in d:
            d["junks"] = [res(r) for r in d["junks"]]
        d["sequence"] = [seq_step(s, tid) for s in d["sequence"]]
        if "loops" not in d:
            d["loops"] = 5
        return d
    if t in ("minecraft:crafting_shaped", "create:mechanical_crafting"):
        d["key"] = {k: ing(v) for k, v in d["key"].items()}
        d["result"] = res(d["result"])
        return d
    if t == "minecraft:crafting_shapeless":
        d["ingredients"] = [ing(i) for i in d["ingredients"]]
        d["result"] = res(d["result"])
        return d
    if t in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"):
        d["ingredient"] = ing(d["ingredient"])
        d["result"] = res(d["result"])
        return d
    if t == "minecraft:stonecutting":
        d["ingredient"] = ing(d["ingredient"])
        d["result"] = res(d["result"])
        return d
    if t.startswith("create:"):
        ingredients = d.pop("ingredients", None)
        if ingredients is None and "ingredient" in d:
            ingredients = [d.pop("ingredient")]
        items, fl = split_ing(ingredients or [])
        results = d.pop("results", None)
        if results is None and "result" in d:
            r = d.pop("result")
            results = r if isinstance(r, list) else [r]
        ritems, rfl = split_res(results or [])
        if t == "create:sandpaper_polishing":
            d["ingredient"] = items[0]
            d["result"] = ritems[0]
        elif t in SINGLE:
            d["ingredient"] = items[0]
            d["results"] = ritems
            dflt = {"create:crushing": 200, "create:milling": 50, "create:cutting": 50}.get(t)
            if dflt and "processing_time" not in d:
                d["processing_time"] = dflt
        elif t == "create:filling":
            d["ingredient"] = items[0]
            d["fluid_ingredient"] = fl[0]
            d["result"] = ritems[0]
        elif t == "create:emptying":
            d["ingredient"] = items[0]
            d["fluid_result"] = rfl[0]
            d["result"] = ritems[0] if ritems else {"id": "minecraft:air"}
        elif t == "create:deploying":
            d["target"], d["ingredient"] = items[0], items[1]
            d["results"] = ritems
        elif t == "create:item_application":
            d["target"], d["ingredient"] = items[0], items[1]
            d["results"] = ritems
        else:  # mixing, compacting, and unknown create processing
            d["ingredients"] = items
            if fl:
                d["fluid_ingredients"] = fl
            d["results"] = ritems
            if rfl:
                d["fluid_results"] = rfl
        return d
    # foreign recipe types: generic upgrade of obvious item shapes
    return gen(d)


def gen(o, key=""):
    if isinstance(o, dict):
        if set(o) <= {"item", "count", "nbt", "components", "chance"} and "item" in o and key in ("result", "results"):
            return res(o)
        if set(o) <= {"item"} or set(o) <= {"tag"}:
            return ing(o)
        return {k: gen(v, k if k in ("result", "results") else key) for k, v in o.items()}
    if isinstance(o, list):
        return [gen(v, key) for v in o]
    return o


def _fixcond(c):
    if not isinstance(c, dict):
        return
    t = c.get("type", "")
    if t.startswith("forge:"):
        c["type"] = "neoforge:" + t[6:]
    for v in c.get("values", []) or []:
        _fixcond(v)
    if "value" in c:
        _fixcond(c["value"])


def conv_name(n):
    p = n.split("/")
    if p[0] != "data" or len(p) < 3:
        return n
    if p[2] == "tags" and len(p) > 3:
        p[3] = TAGDIRS.get(p[3], p[3])
        if p[3] == "worldgen/biome":
            p[3:4] = ["worldgen", "biome"]
    else:
        p[2] = DIRS.get(p[2], p[2])
    return "/".join(p)


def conv_tag(d):
    d["values"] = [("#" + tagname(v) if isinstance(v, str) and v.startswith("#") else v) for v in d.get("values", [])]
    return d


def foreign_ns(d):
    ns = set()
    if isinstance(d, str) and d.startswith('#'): d = d[1:]

    def w(o):
        if isinstance(o, str):
            m = re.match(r"^#?([a-z0-9_.-]+):[a-z0-9_./-]+$", o)
            if m:
                ns.add(m.group(1))
        elif isinstance(o, dict):
            for k, v in o.items():
                if k in ("type", "neoforge:conditions", "category", "group"):
                    continue
                w(v)
        elif isinstance(o, list):
            for v in o:
                w(v)
    w(d)
    return ns - VANILLA_HOME - {"computercraft"} if False else ns - VANILLA_HOME


def add_conditions(d, own_ns):
    ns = foreign_ns(d) - own_ns
    if not ns:
        return d
    conds = d.setdefault("neoforge:conditions", [])
    have = {c.get("modid") for c in conds if isinstance(c, dict)}
    for n in sorted(ns):
        if n in have:
            continue
        conds.append({"type": "neoforge:or", "values": [{"type": "neoforge:mod_loaded", "modid": n},
                                                          {"type": "neoforge:mod_loaded", "modid": "diaithir_" + n}]})
    return d


def convert_jar(src, outdir, slug, ver="1.0.0"):
    zin = zipfile.ZipFile(src)
    files = {}
    own = set()
    for n in zin.namelist():
        if n.endswith("/") or n.startswith("__MACOSX") or n.endswith(".DS_Store"):
            continue
        p = n.split("/")
        if p[0] == "data" and len(p) > 2:
            own.add(p[1])
    own = set(os.environ.get('OWN','').split(','))
    prune = set(json.load(open('/nonexistent'))) if os.path.exists('/nonexistent') else set()
    for n in zin.namelist():
        if n.endswith("/") or n.startswith("__MACOSX") or n.endswith(".DS_Store"):
            continue
        if n.startswith("META-INF/") and not n.startswith("META-INF/services"):
            continue
        if n in ("fabric.mod.json", "quilt.mod.json", "pack.mcmeta") or n.endswith(".mixins.json"):
            continue
        b = zin.read(n)
        nn = conv_name(n)
        if nn in prune:
            continue
        if nn.startswith("data/") and nn.endswith(".json"):
            try:
                d = json.loads(b.decode("utf-8-sig"))
            except Exception as e:
                warn.append(("badjson", n))
                continue
            seg = nn.split("/")
            if seg[2] == "recipe" and isinstance(d, dict):
                try:
                    d = conv_recipe(d)
                except Exception as e:
                    warn.append(("recipe fail %r" % e, n))
                    continue
                d = add_conditions(d, own)
            elif seg[2] == "tags":
                d = conv_tag(d)
                d["values"] = [({"id": v, "required": False} if isinstance(v, str) and foreign_ns(v) else v) for v in d["values"]]
            elif seg[2] in ("advancement", "loot_table") and isinstance(d, dict):
                d = add_conditions(gen(d), own)
            elif isinstance(d, dict) and "conditions" in d:
                d["neoforge:conditions"] = d.pop("conditions")
            b = json.dumps(d, indent=1).encode()
        files[nn] = b
    mid = re.sub(r"[^a-z0-9_]", "_", slug.lower())
    toml = f'''modLoader="javafml"
loaderVersion="[1,)"
license="See upstream"
[[mods]]
modId="diaithir_{mid}"
version="{ver}-diaithir.1"
displayName="{slug} (Diaithir port)"
authors="Original authors via Modrinth; datapack rewritten for Diaithir Create 26.1.2"
description=\'\'\'Data-only Create addon "{slug}" rewritten for the Diaithir Create port (26.1 formats, create -> diaithir_create).\'\'\'
[[dependencies.diaithir_{mid}]]
modId="diaithir_create"
type="required"
versionRange="[6.0.9,)"
ordering="AFTER"
side="BOTH"
'''
    files["META-INF/neoforge.mods.toml"] = toml.encode()
    os.makedirs(outdir, exist_ok=True)
    out = os.path.join(outdir, f"diaithir-{slug}-26.1.2-{ver}-diaithir.1.jar")
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for n, b in files.items():
            z.writestr(n, b)
    return out, len([f for f in files if "/recipe/" in f])


if __name__ == "__main__":
    for j in sorted(glob.glob(sys.argv[1] + "/*.jar")):
        slug = os.path.basename(j)[:-4]
        print(slug, convert_jar(j, sys.argv[2], slug))
    for w in warn[:60]:
        print("WARN", w)
    print(len(warn), "warnings")
