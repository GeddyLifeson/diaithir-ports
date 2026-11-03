#!/usr/bin/env python3 -I
"""Writes age_gates.json and codex/*.json from the real Not Enough Trials lang file.
Usage: gen.py PATH_TO_NotEnoughTrials_jar   (the jar is third-party and never committed)."""
import json, sys, zipfile, pathlib

AGE = "IV"
root = pathlib.Path(__file__).resolve().parent.parent
lang = json.loads(zipfile.ZipFile(sys.argv[1]).read("assets/notenoughtrials/lang/en_us.json"))

ench = [(k.split(".")[-1], v) for k, v in lang.items()
        if k.startswith("enchantment.notenoughtrials.") and not k.endswith(".desc")]
items = []
for name, title in ench:
    items.append({"id": f"notenoughtrials:{name}", "name": title, "age": AGE,
                  "text": lang[f"enchantment.notenoughtrials.{name}.desc"]})
tips = [v for k, v in lang.items() if k.startswith("notenoughtrials.tip.")]

(root / "age_gates.json").write_text(json.dumps({
    "_doc": "Same shape as DiaithirRPG content/tech_ages.json. The whole notenoughtrials namespace wakes at Age IV (iron): the chambers need wind charges, copper scraping and trial keys.",
    "namespaces": {"notenoughtrials": AGE},
    "items": {"minecraft:heavy_core": "V"}}, indent=1) + "\n")

(root / "codex" / "trials_handbook.json").write_text(json.dumps({
    "id": "diaithirtrials:handbook", "title": "Trial Chambers",
    "chapters": [
        {"tier": "chambers", "name": "The overhauled chambers", "age": AGE,
         "text": " ".join(tips), "items": [
             {"id": "minecraft:trial_key", "name": "Trial Key", "age": AGE, "text": ""},
             {"id": "minecraft:ominous_trial_key", "name": "Ominous Trial Key", "age": AGE, "text": ""},
             {"id": "minecraft:heavy_core", "name": "Heavy Core", "age": "V", "text": lang["notenoughtrials.tip.boss_reward"]}]},
        {"tier": "enchantments", "name": "Rare enchantments", "age": AGE, "text": "", "items": items}]},
    indent=1) + "\n")

(root / "codex" / "trials_quests.json").write_text(json.dumps({"quests": [
    {"id": "diaithirtrials:first_chamber", "title": "Enter a trial chamber",
     "obtain": ["minecraft:trial_key"], "requires": None, "live": True},
    {"id": "diaithirtrials:boss_chamber", "title": "Beat the boss chamber",
     "obtain": ["minecraft:heavy_core"], "requires": "diaithirtrials:first_chamber", "live": True},
    {"id": "diaithirtrials:ominous", "title": "Open an ominous vault",
     "obtain": ["minecraft:ominous_trial_key"], "requires": "diaithirtrials:first_chamber", "live": True}]},
    indent=1) + "\n")
