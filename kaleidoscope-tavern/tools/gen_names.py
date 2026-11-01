"""Writes the Diaithiris name of every block and item in lang/en_us.json as kennings (head noun, then genitive describers, one word) to a JSON map id -> name;
English stays the main display name and the pack shows the Diaithiris name in grey.
Words come from /mnt/project-files/diaithiris-archaic/LEXICON.json. Ids never change. Run: python3 -I tools/gen_names.py [OUT.json] [LEXICON.json]
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
LANG = os.path.join(HERE, "..", "src", "main", "resources", "assets", "kaleidoscope_tavern", "lang", "en_us.json")
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "diaithiris_names.json")
LEX = sys.argv[2] if len(sys.argv) > 2 else "/mnt/project-files/diaithiris-archaic/LEXICON.json"

COLOURS = {"White": "white", "Light Gray": "gray", "Gray": "grey", "Black": "black", "Brown": "brown", "Red": "red", "Orange": "saffron",
           "Yellow": "yellow", "Lime": "lime-green", "Green": "green", "Cyan": "teal", "Light Blue": "turquoise", "Blue": "blue",
           "Purple": "purple", "Magenta": "magenta", "Pink": "pink", "Colorless": "clear"}

# English name -> head noun, then describers (nouns take the genitive, adjectives stay)
KENNINGS = {
    "Bar Cabinet": ("cabinet", ["bar"]), "Bar Counter": ("counter", ["bar"]), "Bar Stool": ("stool", ["bar"]), "Barrel": ("barrel", []),
    "Brandy": ("brandy", []), "Carignan": ("wine", ["red"]), "Chalkboard": ("board", ["chalk"]), "Champagne": ("champagne", []),
    "Dragon Breath Bottle": ("bottle", ["dragon", "breath"]), "Empty Bottle": ("bottle", ["empty"]),
    "Glass Bar Cabinet": ("cabinet", ["clear", "bar"]), "Glow Berries Bucket": ("bucket", ["glow", "berry"]),
    "Glow Berries Juice": ("juice", ["glow", "berry"]), "Glowflower Brew": ("ale", ["glow", "flower"]),
    "Gold Grape": ("grape", ["gold"]), "Gold Grape Bucket": ("bucket", ["gold", "grape"]), "Gold Grape Juice": ("juice", ["gold", "grape"]),
    "Gold Grapevine Trellis": ("frame", ["gold", "grapevine"]), "Grape": ("grape", []), "Grape Bucket": ("bucket", ["grape"]),
    "Grape Juice": ("juice", ["grape"]), "Grapevine": ("grapevine", []), "Grapevine Trellis": ("frame", ["grapevine"]),
    "Honey Bottle": ("bottle", ["honey"]), "Honey Wine": ("wine", ["honey"]), "Ice Grape": ("grape", ["ice"]),
    "Ice Grape Bucket": ("bucket", ["ice", "grape"]), "Ice Grape Juice": ("juice", ["ice", "grape"]),
    "Ice Grapevine Trellis": ("frame", ["ice", "grapevine"]), "Ice Wine": ("wine", ["ice"]), "Luminous Bride": ("wine", ["glow", "bride"]),
    "Madame Shexiang": ("wine", ["queen", "flower"]), "Miner's Star": ("wine", ["miner", "star"]), "Molotov Cocktail": ("cocktail", ["fire"]),
    "Mother Snow": ("wine", ["mother", "snow"]), "Painting": ("painting", []), "Plum Wine": ("wine", ["plum"]),
    "Polaris Sweet White": ("wine", ["star", "sweet", "white"]), "Pressing Tub": ("tub", ["press"]), "Queen": ("wine", ["queen"]),
    "Riesling Dry White": ("wine", ["dry", "white"]), "Rum": ("rum", []), "Sakura Wine": ("wine", ["cherry", "bloom"]),
    "Sandwich Board": ("board", ["sign"]), "Sauvignon Blanc Dry White": ("wine", ["white", "dry", "grape"]), "Sherry": ("sherry", []),
    "Sofa": ("sofa", []), "Stepladder": ("ladder", []), "String Lights": ("string", ["light"]), "Sunset Glow": ("wine", ["glow", "sky"]),
    "Sweet Berries Bucket": ("bucket", ["sweet", "berry"]), "Sweet Berries Juice": ("juice", ["sweet", "berry"]),
    "Sweet Berry Wine": ("wine", ["sweet", "berry"]), "Table": ("table", []), "Tap": ("tap", []), "Trellis": ("frame", []),
    "Vinegar": ("vinegar", []), "Vodka": ("vodka", []), "Water Bottle": ("bottle", ["water"]), "Watermelon Juice": ("juice", ["watermelon"]),
    "Whiskey": ("whiskey", []), "Wild Grapevine": ("grapevine", ["wild"]), "Wine": ("wine", []),
}

lex = json.load(open(LEX))["entries"]
by_en = {}
for e in lex:
    by_en.setdefault(e["en"].lower(), []).append(e)


def word(en, genitive):
    if en == "lime-green":
        return word("green", False) + word("yellow", False)
    cands = by_en.get(en, [])
    pick = next((c for c in cands if c["pos"] == "n"), None) or next((c for c in cands if c["pos"] == "adj"), None)
    if pick is None:
        raise SystemExit("no lexicon entry for " + en)
    form = pick["rod"]
    if genitive and pick["pos"] == "n" and pick.get("gen"):
        form = pick["gen"]
    return re.sub(r"\s+", "", form)


def kenning(head, describers):
    return word(head, False) + "".join(word(d, True) for d in describers)


def convert(english):
    m = re.fullmatch(r"String Lights \((.+)\)", english)
    if m:
        english, colour = "String Lights", m.group(1)
    else:
        colour = None
        for c in sorted(COLOURS, key=len, reverse=True):
            if english.startswith(c + " ") and english[len(c) + 1:] in KENNINGS:
                english, colour = english[len(c) + 1:], c
                break
    head, describers = KENNINGS[english]
    name = kenning(head, describers) + (word(COLOURS[colour], False) if colour else "")
    return name[0].upper() + name[1:]


lang = json.load(open(LANG))
names = {}
for key, value in lang.items():
    m = re.fullmatch(r"(?:block|item)\.([^.]+)\.(.+)", key)
    if m:
        names[m.group(1) + ":" + m.group(2)] = convert(value)
with open(OUT, "w") as f:
    json.dump(names, f, ensure_ascii=False, indent=1)
    f.write("\n")
