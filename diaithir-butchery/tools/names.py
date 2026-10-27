"""Diaithiris display names (ids never change). Words come from diaithiris-archaic/LEXICON_EN.md; compounds follow the lexicon's own (skianfeòla)."""
NAMES = {
    "carcass": "Klosach", "flint_knife": "Skianklachteini", "copper_knife": "Skianuma", "bronze_knife": "Skiankrèuma", "steel_knife": "Skiankruoid", "tempered_knife": "Skianfaguirti", "ceramic_knife": "Skiankriadach", "diamond_edge_knife": "Landdaeimen", "crystal_thread_knife": "Skiankriustul", "shard_knife": "Skianspielg", "butcher_knife": "Skianfeòladuir", "butcher_cleaver": "Skianfeòlad", "meat_hook": "Dubunfeòlad",
    "raw_hide": "Kraikend", "sinew": "Fèitlùtaid", "tallow": "Saild", "blood_flask": "Foilsierrag", "marrow_bone": "Smiurknàim",
    "haunch": "Sliasaidi", "shoulder": "Guoluind", "loin": "Kùlfeòlad", "ribs": "Ausna", "flank": "Slius", "tenderloin": "Kùlfeòladmaed",
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

SPECIES = {
    "cow": "Bòi", "mooshroom": "Bòibalgunbuochuir", "pig": "Muk", "sheep": "Kaera", "goat": "Gobur", "horse": "Iech", "donkey": "Asul", "mule": "Moileid",
    "llama": "Lauma", "trader_llama": "Lauma", "camel": "Kàmul", "camel_husk": "Kàmul", "chicken": "Kierk", "rabbit": "Koinenach", "cod": "Trosk",
    "salmon": "Bradun", "tropical_fish": "Iaskachiumadatach", "squid": "Gibernach", "glow_squid": "Gibernachloisni", "turtle": "Sligenach",
    "fox": "Siondach", "polar_bear": "Matungiel", "ocelot": "Kat", "hoglin": "Tork", "frog": "Loskund", "dolphin": "Leimaduir", "nautilus": "Seòladuirsligi",
    "ravager": "Oilebeist", "spider": "Damunuldaid", "armadillo": "Aurmachdbietach", "sniffer": "Snòtaduir", "strider": "Srutteinibietach",
}
