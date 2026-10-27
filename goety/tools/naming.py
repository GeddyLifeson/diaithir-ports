"""English -> Diaithiris names. Modern Gaelic roots are chosen per English word, reordered noun-first,
then run through the pack's archaic layer (archaic.py) so no word equals a modern Gaelic spelling."""
import json, os, re, sys
sys.path.insert(0, "/mnt/project-files/diaithiris-archaic/tools")
import archaic
_here = "/mnt/project-files/diaithiris-archaic"
archaic.MEMO.update(json.load(open(os.path.join(_here, "archaic_map.json"), encoding="utf8")))
archaic.OVERRIDES.update(json.load(open(os.path.join(_here, "tools/overrides.json"), encoding="utf8")))

# English word -> modern Gaelic root (nouns first when composing)
G = {
 "haunted": "taibhseach", "rotten": "lobhte", "marsh": "boglach", "windswept": "gaothach", "pine": "giuthas",
 "planks": "clàran", "log": "stoc", "wood": "fiodh", "stripped": "rùisgte", "stairs": "staidhre", "slab": "leac",
 "wall": "balla", "fence": "callaid", "gate": "geata", "button": "putan", "pressure": "bruthadh", "plate": "truinnsear",
 "trapdoor": "cuidhteas", "door": "doras", "leaves": "duilleach", "bookshelf": "sgeilp-leabhraichean",
 "stone": "clach", "bricks": "breigean", "brick": "breigean", "tiles": "leacagan", "tile": "leacag", "pillar": "colbh",
 "chiseled": "snaidhte", "polished": "lìomhte", "cracked": "sgàinte", "mossy": "caonaich", "ominous": "mì-fhortanach",
 "crypt": "uaigh", "shade": "sgàil", "marble": "màrmor", "highrock": "àrd-chreag", "crag": "creag", "cragrocks": "creagan",
 "siltstone": "clach-shalachair", "silt": "salachar", "jade": "seud", "ore": "mèinn", "block": "bloc", "metal": "meatailt",
 "cursed": "mallaichte", "pale": "bàn", "steel": "stàilinn", "dark": "dorcha", "alloy": "measgachadh", "ingot": "ingot",
 "dirty": "salach", "snowy": "sneachdach", "cobbled": "cloichte", "smooth": "rèidh", "red": "dearg", "blue": "gorm",
 "green": "uaine", "gold": "òr", "indented": "eagaichte", "slate": "leug", "worn": "caithte", "weathered": "caithte",
 "washed": "nighte", "patterned": "pàtrannach", "cornered": "ceàrnach", "glazed": "gleusta", "connected": "ceangailte",
 "banded": "bann", "arch": "bogha", "holder": "gleidheadair", "covered": "còmhdaichte", "plated": "plàtaichte",
 "heavy": "trom", "slight": "aotrom", "path": "slighe", "pavement": "ceum", "braced": "taiceil", "studded": "tacaichte",
 "moss": "còinneach", "carved": "gearrte", "soul": "anam", "ectoplasm": "ecto", "essence": "brìgh", "fabric": "aodach",
 "magic": "draoidheach", "occult": "falaichte", "spirit": "spiorad", "gale": "gaoth-mhòr", "chill": "fuachd",
 "unholy": "mì-naomh", "tooth": "fiacail", "savage": "borb", "fang": "fiacail", "venomous": "nimheil", "egg": "ugh",
 "spider": "damhan-allaidh", "warped": "cam", "dust": "duslach", "grave": "uaigh", "raging": "garg", "matter": "stuth",
 "ice": "deigh", "cube": "ciùb", "ruby": "ruban", "empty": "falamh", "focus": "fòcas", "hunger": "acras", "core": "cridhe",
 "wind": "gaoth", "mystic": "dìomhair", "void": "falamh", "shard": "criomag", "heart": "cridhe", "night": "oidhche",
 "forbidden": "toirmisgte", "piece": "pios", "fragment": "criomag", "jar": "crogan", "pottery": "criadaireachd",
 "sherd": "criomag", "cross": "crois", "dead": "marbh", "haunt": "taibhs", "echo": "mac-talla", "withered": "seargte",
 "manuscript": "làmh-sgrìobhadh", "shrouded": "falaichte", "blueprint": "dealbh", "henbane": "puinnsean-eich",
 "nightshade": "lus-na-h-oidhche", "blossom": "flùr", "bone": "cnàimh", "sword": "claidheamh", "shovel": "spaid",
 "pickaxe": "tairnge", "axe": "tuagh", "hoe": "sgrìob", "helmet": "clogaid", "chestplate": "uchd-bhrat",
 "leggings": "casbhrat", "boots": "bòtannan", "iron": "iarann", "black": "dubh", "knight": "ridire", "paladin": "curaidh",
 "emerald": "smaragd", "awakened": "dùisgte", "totem": "tòtam", "spent": "caithte", "of": "", "the": "", "souls": "anman",
 "staff": "bata", "wand": "slat", "ring": "fàinne", "amulet": "seun", "necklace": "usgar", "amethyst": "aimiteist",
 "charm": "seun", "belt": "crios", "scroll": "sgrìobhainn", "crown": "crùn", "robe": "èideadh", "hat": "ad",
 "alarming": "clisgeach", "feline": "cait", "spiteful": "gamhlasach", "rune": "rùn", "necro": "marbh", "geo": "talmhaidh",
 "storm": "stoirm", "frost": "reothadh", "wild": "fiadhaich", "abyss": "domhainn", "nether": "ifrinn", "pale_steel": "stàilinn bhàn", "apparition": "taibhse", "basalt": "basalt", "blooming": "fàs", "blossoming": "fo-fhlùr", "bottom": "bun", "chorus": "ceòl", "corrupted": "truaillte", "corrupt": "truaillte", "detritus": "smùr", "diamond": "daoimean", "dirt": "salchar", "end": "crìoch", "grown": "fàsta", "infused": "lìonta", "limestone": "clach-aoil", "mixed": "measgaichte", "mold": "molltair", "oak": "darach", "purpur": "purpur", "rock": "creag", "rod": "slat", "sky": "speur", "slated": "leugach", "soiled": "salach", "spruce": "giuthas-dorcha", "top": "mullach", "weathering": "caitheadh", "summoning": "gairm", "zombie": "marbhan", "skeleton": "cnàimhneach", "husk": "plaosg", "stray": "seachranaiche", "wither": "seargadh", "cave": "uaimh", "servant": "tràill", "souls": "anman", "school": "sgoil", "right-click": "cliog-deas", "it": "e", "onto": "air", "a": "", "to": "", "load": "luchdaich", "no": "gun", "loaded": "luchdaichte", "not": "chan", "enough": "gu leòr", "common": "cumanta", "ill-omened": "droch-chomharraichte", "necromancy": "marbh-dhraoidheachd", "geomancy": "talamh-dhraoidheachd", "bolt": "saighead", "magic": "draoidheachd", "ignite": "las", "heal": "slànachadh", "hide": "seiche", "bulwark": "dìon", "weakening": "lagachadh", "sonic": "fuaimneach", "boom": "sgailc", "shockwave": "tonn-clisgidh", "arrow": "saighead", "rain": "uisge", "corruption": "truaillidheachd", "biting": "greimeachadh", "teeth": "fiaclan", "shredding": "reubadh", "rotting": "lobhadh", "osseous": "cnàimheach", "killing": "marbhadh", "leeching": "deoghal", "temblor": "crith-thalmhainn", "clap": "bualadh", "earth": "talamh", "punch": "dòrn", "smack": "sgleog", "eruption": "brùchdadh", "quaking": "crith", "breath": "anail", "spike": "bior", "hail": "clachan-meallain", "blizzard": "cathadh", "chilling": "fuarachadh", "nova": "spreadhadh", "launch": "tilgeil", "cushion": "cluasag", "updraft": "gaoth-suas", "blast": "spreadhadh", "whirlwind": "cuairt-ghaoth", "cyclone": "cuairt-ghaoth mhòr", "razor": "ealtainn", "thunderbolt": "tàirneanach", "lightning": "dealanach", "thunderstorm": "stoirm-thàirneanaich", "discharge": "leigeil-às", "sprightly": "sùgach", "shocking": "clisgeach", "fireball": "ball-teine", "blazing": "lasrach", "ghastly": "oillteil", "lavaball": "ball-làbha", "skull": "claigeann", "fire": "teine", "flame": "lasair", "strike": "buille", "bombardment": "bomadh", "meteor": "dreag", "shower": "fras", "blink": "priobadh", "flash": "boillsgeadh", "water": "uisge", "jet": "steall", "steaming": "smùid", "prisma": "priosam", "beam": "gath", "tidal": "làn-mara", "poison": "puinnsean", "dart": "dairt", "entangling": "rib", "leaping": "leum", "wand": "slat", "totem": "tòtam", "focus": "rùn", "staff": "bata", "necro": "marbh", "geo": "talmhaidh", "ominous": "mì-fhortanach", "souls_of": "", "bomb": "spreadhadh", "earth's": "talmhainn", "launching": "tilgeil", "lava": "làbha", "altar": "altair", "pedestal": "bun-colbh", "compacted": "dùmhail", "concentrated": "dlùth", "dense": "dlùth", "flower": "flùr", "plank": "clàr", "shadow": "faileas", "steep": "cas", "thatched": "tughaichte", "wartful": "foinneach", "clàr-bruthaidh": "clàr-bruthaidh", "doras-tuill": "doras-tuill",
}
# multi-word phrase overrides: key is the lower-case English phrase
PHR = {
 "pressure plate": "clàr-bruthaidh", "trapdoor": "doras-tuill", "dark alloy": "measgachadh dorcha",
}

def gaelic(english):
    s = english.lower()
    for k, v in PHR.items():
        s = s.replace(k, v.replace(" ", "~"))
    words = s.split()
    out, miss = [], []
    for w in words:
        if "~" in w:
            out.append(w.replace("~", " ")); continue
        g = G.get(w)
        if g is None:
            miss.append(w); g = w
        if g:
            out.append(g)
    # noun-first order: Gaelic puts the head noun first, so reverse English adjective-noun order
    out.reverse()
    return " ".join(out), miss

def diaithiris(english):
    g, miss = gaelic(english)
    d = archaic.text(g)
    return d[:1].upper() + d[1:], miss


def archaic_text(sentence):
    """Plain sentence with %s placeholders: each Gaelic-rooted word is mapped by G where known, then archaic."""
    parts = re.split(r"(%s|%d)", sentence)
    out = []
    for p in parts:
        if p in ("%s", "%d"):
            out.append(p); continue
        g, _ = gaelic(p) if p.strip() else (p, [])
        out.append(archaic.text(g))
    r = "".join(out)
    return r[:1].upper() + r[1:]
