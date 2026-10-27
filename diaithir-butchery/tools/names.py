"""Diaithiris display names (ids never change). Words come from diaithiris-archaic/LEXICON_EN.md; compounds follow the lexicon's own (skianfeòla)."""
NAMES = {
    "carcass": "Klosach", "butcher_knife": "Skianfeòladuir", "butcher_cleaver": "Skianfeòla", "meat_hook": "Dubunfeòla",
    "raw_hide": "Kraikend", "sinew": "Fèitlùtaid", "tallow": "Saild", "blood_flask": "Foilsierrag", "marrow_bone": "Smiurknàim",
    "haunch": "Sliasaidi", "shoulder": "Guoluind", "loin": "Kùlfeòla", "ribs": "Ausna", "flank": "Slius", "tenderloin": "Kùlfeòlamaed",
    "neck": "Amachu", "tail": "Ierbuld", "hock": "Speira", "fish_fillet": "Sliseg", "bird_breast": "Broildech", "drumstick": "Kasein",
    "wing": "Skiad", "scraps": "Foidlech", "liver": "Grùtun", "heart": "Kridi", "kidney": "Dubag", "tripe": "Staumag", "tongue": "Tienga", "brain": "Ienchuind",
}
COOKED = "Ròstad"
CONCEPT = "Feòladuirechd"
CARCASS_OF = "Klosach %s"
SIZES = {"tiny": "Bìudach", "small": "Bieg", "medium": "Miedunach", "large": "Mòru", "huge": "Àibeisech"}
CLASS = {"land": "Tìar", "fish": "Moir", "bird": "Ein", "predator": "Kriechaduir", "reptile": "Snàguir", "monster_hide": "Oilebeist"}

def item(n):
    if n.startswith("cooked_"):
        return NAMES[n[7:]] + " " + COOKED
    return NAMES[n]

def body(k):
    p = k.split("_")
    if k in ("predator", "reptile", "monster_hide"):
        return "Klosach " + CLASS[k]
    return "Klosach " + CLASS[p[0]] + " " + SIZES[p[1]]
