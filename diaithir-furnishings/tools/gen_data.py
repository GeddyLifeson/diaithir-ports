"""Writes the per-wood and per-jar data files for diaithir-furnishings (blockstates, models, item definitions, recipes, loot, tags, names).
Run: python3 -I tools/gen_data.py. Deterministic. The wood list mirrors Woods.java."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
A = ROOT / "assets/diaithirfurnishings"
D = ROOT / "data/diaithirfurnishings"
NS = "diaithirfurnishings"

# id, stripped log texture, planks, Diaithiris name of the wood (genitive or adjective, from LEXICON_EN.md)
WOODS = [
    ("oak", "stripped_oak_log", "oak_planks", "Dauraig"),
    ("spruce", "stripped_spruce_log", "spruce_planks", "Giutais Lochlundaig"),
    ("birch", "stripped_birch_log", "birch_planks", "Beiti"),
    ("jungle", "stripped_jungle_log", "jungle_planks", "Dlùtchuildi"),
    ("acacia", "stripped_acacia_log", "acacia_planks", "Akakia"),
    ("dark_oak", "stripped_dark_oak_log", "dark_oak_planks", "Dauraig Dourcha"),
    ("mangrove", "stripped_mangrove_log", "mangrove_planks", "Kraeibesàili"),
    ("cherry", "stripped_cherry_log", "cherry_planks", "Sierist"),
    ("pale_oak", "stripped_pale_oak_log", "pale_oak_planks", "Dauraig Glasneilach"),
    ("bamboo", "stripped_bamboo_block", "bamboo_planks", "Kraindchuilki"),
    ("crimson", "stripped_crimson_stem", "crimson_planks", "Kròderg"),
    ("warped", "stripped_warped_stem", "warped_planks", "Toindti"),
]


def dump(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=1, ensure_ascii=False) + "\n")


def box(frm, to, tex, uv=None):
    faces = {f: {"texture": "#" + tex} for f in ("north", "south", "east", "west", "up", "down")}
    return {"from": frm, "to": to, "faces": faces}


def drop_self(name, copy_container=False):
    entry = {"type": "minecraft:item", "name": f"{NS}:{name}"}
    if copy_container:
        entry["functions"] = [{"function": "minecraft:copy_components", "source": "block_entity", "include": ["minecraft:container"]}]
    return {"type": "minecraft:block", "random_sequence": f"{NS}:blocks/{name}",
            "pools": [{"rolls": 1, "bonus_rolls": 0, "conditions": [{"condition": "minecraft:survives_explosion"}], "entries": [entry]}]}


def block_files(name, model_ref):
    dump(A / f"blockstates/{name}.json", {"variants": {"": {"model": f"{NS}:block/{model_ref}"}}})
    dump(A / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{model_ref}"}})


def main():
    lang = json.loads((A / "lang/en_us.json").read_text())
    axe = ["diaithirfurnishings:rope", "diaithirfurnishings:sack", "diaithirfurnishings:hanging_sack"]
    for wid, stripped, planks, label in WOODS:
        name = f"{wid}_signpost"
        block_files(name, name)
        dump(A / f"models/block/{name}.json", {
            "textures": {"side": f"minecraft:block/{stripped}", "end": f"minecraft:block/{stripped}_top", "particle": f"minecraft:block/{stripped}"},
            "elements": [{"from": [6, 0, 6], "to": [10, 16, 10], "faces": {
                "north": {"texture": "#side"}, "south": {"texture": "#side"}, "east": {"texture": "#side"}, "west": {"texture": "#side"},
                "up": {"texture": "#end"}, "down": {"texture": "#end"}}}]})
        dump(D / f"loot_table/blocks/{name}.json", drop_self(name))
        dump(D / f"recipe/{name}.json", {
            "type": "minecraft:crafting_shaped", "category": "misc",
            "key": {"P": f"minecraft:{planks}", "S": "minecraft:stick"},
            "pattern": ["P", "S", "S"], "result": {"id": f"{NS}:{name}", "count": 2}})
        lang[f"block.{NS}.{name}"] = f"Komurra {label}"
        axe.append(f"{NS}:{name}")

    # clay crock: open mouth, the stored item shows over the rim
    block_files("clay_jar", "clay_jar")
    dump(A / "models/block/clay_jar.json", {
        "textures": {"clay": f"{NS}:block/clay_jar", "inner": f"{NS}:block/clay_jar_inner", "particle": f"{NS}:block/clay_jar"},
        "elements": [
            {"from": [3, 0, 3], "to": [13, 9, 13], "faces": {
                "north": {"texture": "#clay"}, "south": {"texture": "#clay"}, "east": {"texture": "#clay"}, "west": {"texture": "#clay"},
                "up": {"texture": "#inner"}, "down": {"texture": "#clay"}}},
            {"from": [4, 9, 4], "to": [12, 11, 5], "faces": {f: {"texture": "#clay"} for f in ("north", "south", "east", "west", "up")}},
            {"from": [4, 9, 11], "to": [12, 11, 12], "faces": {f: {"texture": "#clay"} for f in ("north", "south", "east", "west", "up")}},
            {"from": [4, 9, 5], "to": [5, 11, 11], "faces": {f: {"texture": "#clay"} for f in ("north", "south", "east", "west", "up")}},
            {"from": [11, 9, 5], "to": [12, 11, 11], "faces": {f: {"texture": "#clay"} for f in ("north", "south", "east", "west", "up")}},
        ]})
    # glass jar: see-through body, cork lid
    block_files("glass_jar", "glass_jar")
    dump(A / "models/block/glass_jar.json", {
        "textures": {"glass": f"{NS}:block/glass_jar", "cork": f"{NS}:block/jar_lid", "particle": f"{NS}:block/glass_jar"},
        "elements": [box([4, 0, 4], [12, 11, 12], "glass"), box([5, 11, 5], [11, 13, 11], "cork")]})
    for jar, label, mats in (("clay_jar", "Krogun Krèada", ("minecraft:brick",)), ("glass_jar", "Krogun Glaindech", ("minecraft:glass_pane",))):
        dump(D / f"loot_table/blocks/{jar}.json", drop_self(jar, True))
        lang[f"block.{NS}.{jar}"] = label
    dump(D / "recipe/clay_jar.json", {"type": "minecraft:crafting_shaped", "category": "misc",
        "key": {"B": "minecraft:brick"}, "pattern": ["B B", "B B", " B "], "result": {"id": f"{NS}:clay_jar", "count": 2}})
    dump(D / "recipe/glass_jar.json", {"type": "minecraft:crafting_shaped", "category": "misc",
        "key": {"P": "minecraft:glass_pane", "W": "#minecraft:planks"}, "pattern": [" W ", "P P", "PPP"],
        "result": {"id": f"{NS}:glass_jar", "count": 2}})

    # hanging sack: the sack model lifted 4 px with a cord to the top of the block
    sack = json.loads((A / "models/block/sack.json").read_text())
    for el in sack["elements"]:
        el["from"][1] += 4
        el["to"][1] += 4
    sack["textures"]["rope"] = f"{NS}:block/rope"
    sack["elements"].append({"from": [7.5, 15, 7.5], "to": [8.5, 16, 8.5], "faces": {f: {"uv": [0, 0, 1, 1], "texture": "#rope"} for f in ("north", "south", "east", "west", "up", "down")}})
    dump(A / "models/block/hanging_sack.json", sack)
    block_files("hanging_sack", "hanging_sack")
    dump(D / "loot_table/blocks/hanging_sack.json", drop_self("hanging_sack", True))
    dump(D / "recipe/hanging_sack.json", {"type": "minecraft:crafting_transmute", "category": "misc",
        "input": f"{NS}:sack", "material": f"{NS}:rope", "result": {"id": f"{NS}:hanging_sack"}})
    lang[f"block.{NS}.hanging_sack"] = "Sak Krochti"

    lang["gui.diaithirfurnishings.signpost"] = "Komurra"
    lang["gui.diaithirfurnishings.signpost.line1"] = "First line"
    lang["gui.diaithirfurnishings.signpost.line2"] = "Second line"
    dump(A / "lang/en_us.json", dict(sorted(lang.items())))
    dump(ROOT / "data/minecraft/tags/block/mineable/axe.json", {"values": axe})
    dump(ROOT / "data/minecraft/tags/block/mineable/pickaxe.json", {"values": [f"{NS}:sconce", f"{NS}:clay_jar", f"{NS}:glass_jar"]})


if __name__ == "__main__":
    main()
