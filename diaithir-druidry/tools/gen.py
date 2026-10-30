#!/usr/bin/env python3
"""Generate every data/asset JSON of diaithir-druidry from tools/spells.py. MIT.

Run:  python3 -I tools/gen.py   (from the module root). Output goes to src/main/resources.
"""
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
import spells as SP  # noqa: E402

NS = SP.NS
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data", NS)
ASSET = os.path.join(RES, "assets", NS)

AGE_NUM = {"III": 3, "IV": 4, "V": 5}


def wj(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=1)
        f.write("\n")


def tag(values):
    return {"values": [{"id": v, "required": False} for v in values]}


def main():
    shutil.rmtree(os.path.join(RES, "data"), ignore_errors=True)
    for sub in ("items", "models", "lang"):  # textures are made by tools/textures.py and kept
        shutil.rmtree(os.path.join(ASSET, sub), ignore_errors=True)
    lang = {}
    ages = {NS: "III"}
    items_age = {}
    library = []
    old_irish = set(open("/mnt/project-files/diaithiris-archaic/tools/oldirish_known.txt", encoding="utf8").read().split()) \
        if os.path.exists("/mnt/project-files/diaithiris-archaic/tools/oldirish_known.txt") else set()

    all_ids = []
    for sc in SP.SCHOOLS:
        key = sc["key"]
        ids = []
        t0 = []
        for sd in sc["spells"]:
            sid = sd["id"]
            full = f"{NS}:{sid}"
            wj(os.path.join(DATA, "spell", sid + ".json"), sd["spell"])
            lang[f"spell.{NS}.{sid}.name"] = sd["rod"]
            lang[f"spell.{NS}.{sid}.description"] = sd["desc"]
            ids.append(full)
            all_ids.append(full)
            if sd["spell"]["tier"] == 0:
                t0.append(full)
            items_age[f"{NS}:{sid}"] = sd["age"]
            assert sd["rod"].lower() not in old_irish, sd["rod"]
            library.append([sd["rod"], "concept", AGE_NUM[sd["age"]], "magic",
                            f"A {sc['en']} spell of tier {sd['spell']['tier'] + 1}, school line {sd['spell']['group']}. {sd['desc'].replace('{damage}', 'its damage').replace('{heal}', 'its healing')}"])
        # pools: books and scrolls use every spell of the school; the staff carries its first tier-0 spells
        wj(os.path.join(DATA, "tags", "spell", "spell_book", key + ".json"), tag(ids))
        wj(os.path.join(DATA, "tags", "spell", "spell_scroll", key + ".json"), tag(ids))
        wj(os.path.join(DATA, "tags", "spell", "weapon", key + "_staff.json"), tag(t0))
        wj(os.path.join(DATA, "tags", "spell", key + ".json"),
           {"values": [{"id": f"#{NS}:spell_book/{key}", "required": False},
                       {"id": f"#{NS}:weapon/{key}_staff", "required": False}]})
        # lang for items
        lang[f"item.{NS}.spell_book/{key}"] = f"Liebur {sc['rod']}"
        lang[f"item.{NS}.spell_book/{key}.spell_binding.description"] = sc["blurb"]
        lang[f"item.{NS}.spell_scroll/{key}"] = f"Rolda {sc['rod']}"
        lang[f"item.{NS}.staff_{key}_1"] = f"Bauta {sc['rod']}"
        lang[f"item.{NS}.staff_{key}_2"] = f"Bauta {sc['rod']} Mòru"
        lang[f"school.{NS}.{key}"] = sc["rod"]
        library.append([sc["rod"], "concept", 3, "magic",
                        f"{sc['blurb']} Seven spells from Age III to Age V; learn them from a {'Liebur ' + sc['rod']} made at the spell binding table or from scrolls found in old chambers."])
        library.append([f"Bauta {sc['rod']}", "item", 3, "magic",
                        f"A staff of the {sc['rod']} way. Adds spell power and carries the school's first spell. Crafted with a bronze ingot and 1 {sc['focus'].split(':')[1].replace('_', ' ')}. Age III."])
        library.append([f"Bauta {sc['rod']} Mòru", "item", 5, "magic",
                        f"The greater staff of the {sc['rod']} way: more spell power and 300 uses. Crafted with a steel ingot and 2 {sc['focus'].split(':')[1].replace('_', ' ')}. Age V."])

        # item models
        for kind, folder in (("spell_book", "spell_book"), ("spell_scroll", "spell_scroll")):
            wj(os.path.join(ASSET, "items", folder, key + ".json"),
               {"model": {"type": "minecraft:model", "model": f"{NS}:item/{folder}/{key}"}})
            wj(os.path.join(ASSET, "models", "item", folder, key + ".json"),
               {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{folder}/{key}"}})
        for n in (1, 2):
            wj(os.path.join(ASSET, "items", "staff", f"{key}_{n}.json"),
               {"model": {"type": "minecraft:model", "model": f"{NS}:item/staff/{key}_{n}"}})
            wj(os.path.join(ASSET, "models", "item", "staff", f"{key}_{n}.json"),
               {"parent": "minecraft:item/handheld", "textures": {"layer0": f"{NS}:item/staff/{key}_{n}"}})

        # staves (recipe results with components: Spell Engine gives the container, the model is ours)
        for n in (1, 2):
            comp = staff_components(sc, key, n, t0)
            if n == 1:
                pat = ["  F", " I ", "S  "]
                keys = {"F": sc["focus"], "I": "#c:ingots/bronze", "S": "minecraft:stick"}
            else:
                pat = ["FIF", " S ", " S "]
                keys = {"F": sc["focus"], "I": "#c:ingots/steel", "S": "minecraft:stick"}
            wj(os.path.join(DATA, "recipe", f"staff_{key}_{n}.json"),
               {"type": "minecraft:crafting_shaped", "category": "equipment", "key": keys, "pattern": pat,
                "result": {"id": "minecraft:stick", "count": 1, "components": comp}})
            items_age[f"{NS}:staff_{key}_{n}"] = "III" if n == 1 else "V"
            sc.setdefault("staff_components", {})[n] = comp

    # spell icons are looked up by id
    wj(os.path.join(DATA, "tags", "spell", "all.json"), tag(all_ids))
    # school scroll and staff loot -------------------------------------------------------------
    loot_tables(lang)
    # make sure the ingot tags exist even where no mod fills them (merged with the pack's tags)
    for m in ("bronze", "steel"):
        wj(os.path.join(RES, "data", "c", "tags", "item", "ingots", m + ".json"), {"values": []})
    # lang
    wj(os.path.join(ASSET, "lang", "en_us.json"), lang)
    # age gates
    wj(os.path.join(ROOT, "age_gates.json"),
       {"_doc": "Namespace to Age (tech_ages.json style). Item entries beat namespace entries. Spell entries give the Age a spell is meant to be learnt at: tier 0 and 1 spells Age III, tier 2 Age IV, tier 3 Age V.",
        "namespaces": ages, "items": items_age})
    # library
    wj("/mnt/project-files/library-sync/druidry.json", library) if os.path.isdir("/mnt/project-files/library-sync") else None
    wj(os.path.join(ROOT, "library-druidry.json"), library)
    # codex source: chapters for the Handbooks hook (Modonomicon builder reads this)
    chapters = []
    for sc in SP.SCHOOLS:
        chapters.append({"school": sc["key"], "name": sc["rod"], "text": sc["blurb"], "power": sc["power"],
                         "staff": [f"Bauta {sc['rod']}", f"Bauta {sc['rod']} Mòru"],
                         "spells": [{"id": f"{NS}:{s['id']}", "name": s["rod"], "tier": s["spell"]["tier"] + 1,
                                     "age": s["age"], "text": s["desc"]} for s in sc["spells"]]})
    wj(os.path.join(ROOT, "codex", "druidry_handbook.json"),
       {"id": f"{NS}:handbook", "title": "Draeidechd", "chapters": chapters})
    print("spells", len(all_ids), "lang", len(lang), "library", len(library))


def staff_components(sc, key, n, t0):
    power = sc["power"]
    amt = 3.0 if n == 1 else 6.0
    mods = []
    if power == "physical_melee":
        mods.append({"type": "minecraft:attack_damage", "id": f"{NS}:{key}_{n}_damage", "amount": 3.0 if n == 1 else 5.0,
                     "operation": "add_value", "slot": "mainhand"})
        mods.append({"type": "spell_power:soul", "id": f"{NS}:{key}_{n}_soul", "amount": 2.0 if n == 1 else 4.0,
                     "operation": "add_value", "slot": "mainhand"})
    else:
        mods.append({"type": f"spell_power:{power}", "id": f"{NS}:{key}_{n}_power", "amount": amt,
                     "operation": "add_value", "slot": "mainhand"})
    return {
        "minecraft:item_model": f"{NS}:staff/{key}_{n}",
        "minecraft:item_name": {"translate": f"item.{NS}.staff_{key}_{n}"},
        "minecraft:max_stack_size": 1,
        "minecraft:max_damage": 150 if n == 1 else 300,
        "minecraft:rarity": "uncommon" if n == 1 else "rare",
        "minecraft:attribute_modifiers": mods,
        "minecraft:lore": [{"text": sc["blurb"].split(".")[0] + ".", "color": "gray", "italic": False}],
        "spell_engine:spell_container": {
            "access": "MAGIC",
            "pool": f"{NS}:weapon/{key}_staff",
            "max_spell_count": len(t0),
            "spell_ids": t0,
        },
    }


def loot_tables(lang):
    tiers = {
        "age3": dict(rolls=(1, 2), tier=(0, 1), staff=(1, 8), n=1, targets=[
            "minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft", "minecraft:chests/desert_pyramid",
            "minecraft:chests/jungle_temple", "minecraft:chests/shipwreck_treasure", "minecraft:chests/underwater_ruin_big",
            "minecraft:chests/ruined_portal", "minecraft:chests/igloo_chest", "minecraft:chests/village/village_temple"]),
        "age4": dict(rolls=(1, 2), tier=(1, 2), staff=(1, 6), n=1, targets=[
            "minecraft:chests/stronghold_library", "minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
            "minecraft:chests/woodland_mansion", "minecraft:chests/pillager_outpost", "minecraft:chests/bastion_treasure",
            "minecraft:chests/bastion_other", "minecraft:chests/nether_bridge"]),
        "age5": dict(rolls=(1, 3), tier=(2, 3), staff=(1, 4), n=2, targets=[
            "minecraft:chests/ancient_city", "minecraft:chests/ancient_city_ice_box", "minecraft:chests/end_city_treasure",
            "minecraft:chests/trial_chambers/reward_rare", "minecraft:chests/trial_chambers/reward_ominous_rare",
            "minecraft:chests/trial_chambers/reward_unique", "minecraft:chests/trial_chambers/reward_ominous_unique"]),
    }
    glm = []
    for tname, t in tiers.items():
        scroll_entries = []
        for sc in SP.SCHOOLS:
            scroll_entries.append({
                "type": "minecraft:item", "name": "spell_engine:spell_scroll", "weight": 4,
                "functions": [{"function": "spell_engine:spell_bind_randomly",
                               "pool": f"#{NS}:spell_scroll/{sc['key']}",
                               "tier": {"type": "minecraft:uniform", "min": t["tier"][0], "max": t["tier"][1]},
                               "count": 1}]})
        staff_entries = []
        for sc in SP.SCHOOLS:
            comp = staff_components(sc, sc["key"], t["n"], [f"{NS}:{s['id']}" for s in sc["spells"] if s["spell"]["tier"] == 0])
            staff_entries.append({"type": "minecraft:item", "name": "minecraft:stick", "weight": 1,
                                  "functions": [{"function": "minecraft:set_components", "components": comp}]})
        table = {"type": "minecraft:chest", "pools": [
            {"rolls": {"type": "minecraft:uniform", "min": t["rolls"][0], "max": t["rolls"][1]}, "entries": scroll_entries},
            {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": t["staff"][0] / t["staff"][1]}],
             "entries": staff_entries}]}
        wj(os.path.join(DATA, "loot_table", "chests", tname + ".json"), table)
        wj(os.path.join(DATA, "loot_modifiers", f"inject_{tname}.json"), {
            "type": "neoforge:add_table",
            "conditions": [{"condition": "minecraft:all_of", "terms": [
                {"condition": "minecraft:any_of", "terms": [
                    {"condition": "neoforge:loot_table_id", "loot_table_id": tid} for tid in t["targets"]]},
                {"condition": "minecraft:random_chance", "chance": 0.4 if tname != "age5" else 0.6}]}],
            "table": f"{NS}:chests/{tname}"})
        glm.append(f"{NS}:inject_{tname}")



if __name__ == "__main__":
    main()
