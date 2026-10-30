"""Diaithiris display names by script: English headwords are looked up in the Diaithiris lexicon
(/mnt/project-files/diaithiris-archaic/LEXICON.json); nothing is typed in by hand. Noun first, modifier after
(Lùirech knàim = armour of hide), as the lexicon's grammar notes give."""
import json
import os

LEX = os.environ.get("DIAITHIRIS_LEXICON", "/mnt/project-files/diaithiris-archaic/LEXICON.json")
_by = {}
for _e in json.load(open(LEX, encoding="utf8"))["entries"]:
    _by.setdefault(_e["en"].lower(), []).append(_e)

# armour-piece and material words already fixed by diaithir-arms (names_map.json there); reused so a Fian helm
# reads like its arms helm
ARMS_WORDS = {"helmet": "Klogaid", "chestplate": "Broildech", "leggings": "Kas", "boots": "Bòtund",
              "bronze": "krèuma", "steel": "kruoid", "composite": "kriadach",
              "sword": "Klaidem", "spear": "Slieg", "bow": "Umlaig"}


def rod(en, pos=None):
    if en in ARMS_WORDS:
        return ARMS_WORDS[en]
    es = _by.get(en.lower())
    if not es:
        raise KeyError("no Diaithiris entry for " + en)
    if pos:
        for e in es:
            if e["pos"] == pos:
                return e["rod"]
    return es[0]["rod"]


def phrase(*words):
    """words: English headwords (str) or (headword, pos). First word is capitalised."""
    out = []
    for w in words:
        out.append(rod(*w) if isinstance(w, tuple) else rod(w))
    s = " ".join(out)
    return s[:1].upper() + s[1:]
