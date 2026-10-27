"""Lists item, block-only and entity ids with their English names from the client jar and every mod jar (and the jars nested in them).
usage: scan_items.py MC_CLIENT_JAR OUT_DIR MODS_DIR [MODS_DIR ...]   writes OUT_DIR/items.json, blocks_only.json, entities.json, dimensions.json, biomes.json, structures.json
Items come from assets/<ns>/items/*.json (item definitions) and item.* lang keys; the English name is the lang value (null if none)."""
import glob, io, json, re, sys, zipfile

MC, OUT, *DIRS = sys.argv[1:]
items, lang_item, lang_block, ents = {}, {}, {}, {}
lang_biome, places = {}, {"dimension": {}, "biome": {}, "structure": {}}
LANG = re.compile(r"assets/([a-z0-9_.-]+)/lang/en_us\.json$")
PLACE = re.compile(r"data/([a-z0-9_.-]+)/(dimension|worldgen/biome|worldgen/structure)/(.+)\.json$")
ITEM = re.compile(r"assets/([a-z0-9_.-]+)/items/(.+)\.json$")


def scan(z):
    for n in z.namelist():
        if n.endswith(".jar") and n.startswith("META-INF/jarjar/"):
            try:
                scan(zipfile.ZipFile(io.BytesIO(z.read(n))))
            except Exception:
                pass
            continue
        m = PLACE.match(n)
        if m:
            places[m[2].split("/")[-1]].setdefault(f"{m[1]}:{m[3]}", None)
        m = ITEM.match(n)
        if m:
            items.setdefault(f"{m[1]}:{m[2]}", None)
        if not LANG.match(n):
            continue
        try:
            d = json.loads(z.read(n).decode("utf8", "ignore"))
        except Exception:
            continue
        for k, v in d.items():
            p = k.split(".", 2)
            if len(p) == 3 and "." not in p[2] and isinstance(v, str) and len(v) < 48 and "%" not in v:
                t = {"item": lang_item, "block": lang_block, "entity": ents, "biome": lang_biome}.get(p[0])
                if t is not None:
                    t.setdefault(f"{p[1]}:{p[2]}", v)


for f in [MC] + [j for d in DIRS for j in sorted(glob.glob(d + "/*.jar"))]:
    try:
        scan(zipfile.ZipFile(f))
    except Exception:
        print("bad", f)
for i in items:
    items[i] = lang_item.get(i) or lang_block.get(i)
for k, v in lang_item.items():
    items.setdefault(k, v)  # registered in code without an item definition
for kind, d in places.items():
    for i in d:
        d[i] = lang_biome.get(i) if kind == "biome" else {"minecraft:overworld": "Overworld", "minecraft:the_nether": "The Nether", "minecraft:the_end": "The End"}.get(i)
places["dimension"].update({"minecraft:overworld": None, "minecraft:the_nether": None, "minecraft:the_end": None})  # vanilla has no dimension files in the client jar
blocks_only = {k: v for k, v in lang_block.items() if k not in items}
for name, d in (("items", items), ("blocks_only", blocks_only), ("entities", ents), ("dimensions", places["dimension"]), ("biomes", places["biome"]), ("structures", places["structure"])):
    json.dump(d, open(f"{OUT}/{name}.json", "w", encoding="utf8"), ensure_ascii=False)
print(len(items), "items,", len(blocks_only), "blocks without an item,", len(ents), "entities")
