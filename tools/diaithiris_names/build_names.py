"""Diaithiris names for every item id: head-first kenning phrase from the archaic lexicon.
usage: build_names.py LEXICON.json(archaic) ITEMS.json REF_DIR OUT.json [REPORT.md]
REF_DIR holds the Irish and Scottish Gaelic Hunspell lists (*.dic) for the collision check."""
import json, re, sys, glob, unicodedata, collections

LEX, ITEMS, REF, OUT = sys.argv[1:5]
REPORT = sys.argv[5] if len(sys.argv) > 5 else None
STOP = set("of the a an and with from to in on for or by is as are it this s".split())
LEVEL = {"A1": 0, "A2": 1, "B1": 2, "B2": 3, "C1": 4, "C2": 5}
# words the lexicon lacks, said with lexicon words; first match wins
TERMS = {
    "glass": "clear stone", "ingot": "metal bar", "ingots": "metal bar", "nugget": "metal lump", "planks": "board",
    "pickaxe": "stone digger", "chestplate": "chest armor", "leggings": "leg armor", "trapdoor": "floor door",
    "leaves": "leaf", "orange": "yellow red", "cyan": "blue green", "stained": "dye", "dyed": "dye",
    "cracked": "broken", "mossy": "moss", "old": "ancient", "stripped": "bare", "potted": "pot",
    "cobblestone": "round stone", "cobbled": "round stone", "deepslate": "deep slate", "blackstone": "black stone",
    "redstone": "red stone", "lodestone": "guide stone", "glowstone": "light stone", "terracotta": "baked clay",
    "obsidian": "black glass", "paving": "road stone", "flagstone": "flat stone", "framed": "frame", "encased": "case",
    "carved": "carve", "polished": "polish", "smooth": "smooth", "chiseled": "carve", "waxed": "wax", "gilded": "gold",
    "inlayed": "inlay", "indented": "notch", "inverted": "turn", "weathered": "weather", "exposed": "bare",
    "oxidized": "rust", "fused": "melt", "crafted": "make", "crafter": "maker", "crafting": "make",
    "autocrafter": "self maker", "teleporter": "far door", "spawner": "birth cage", "minecart": "mine cart",
    "spyglass": "far glass", "shulker": "shell box", "piglin": "pig man", "ghast": "ghost",
}
ROMAN = {"ii", "iii", "iv", "vi", "vii", "viii", "ix", "xi", "xii"}
WORDS = lambda s: re.findall(r"[^\W\d_]+", s.lower())
tok = lambda s: re.findall(r"[a-z]+", s.lower())

lex = json.load(open(LEX, encoding="utf8"))["entries"]
BY = collections.defaultdict(list)
for x in lex:
    BY[x["en"].lower()].append(x)

def pick(en, mod):
    es = [x for x in BY.get(en, []) if x["pos"] in ("n", "adj", "v")]
    if not es:
        return None
    order = ("adj", "n", "v") if mod else ("n", "adj", "v")
    es.sort(key=lambda x: (order.index(x["pos"]), LEVEL.get(x["level"], 9)))
    return es[0]["rod"].lower()

def stems(w):
    yield w
    for suf, rep in (("ies", "y"), ("es", ""), ("s", ""), ("ed", ""), ("ed", "e"), ("ing", ""), ("ing", "e"), ("en", ""), ("y", ""), ("ier", "y")):
        if w.endswith(suf) and len(w) > len(suf) + 2:
            yield w[:-len(suf)] + rep

def lookup(w, mod):
    for s in stems(w):
        r = pick(s, mod)
        if r:
            return r

LOAN_N = {"ph": "f", "th": "t", "ck": "k", "qu": "ku", "sh": "s", "x": "ks", "w": "u", "y": "i", "oo": "u", "ee": "i", "z": "s", "j": "g", "v": "b"}

def loan(w):
    """Foreign or invented word: respell with Diaithiris letters, as the archaic laws would show it."""
    s = w
    for a, b in LOAN_N.items():
        s = s.replace(a, b)
    s = re.sub(r"c(?=[eiy])", "s", s)
    s = s.replace("c", "k").replace("kh", "ch")
    s = re.sub(r"([bdfgklmnprst])\1+", r"\1", s)
    s = re.sub(r"(?<=[^aeiou])e$", "i", s)
    return s

def split2(w):
    for i in range(3, len(w) - 2):
        a, b = w[:i], w[i:]
        ra, rb = lookup(a, True), lookup(b, False)
        if ra and rb:
            return rb + ra   # head last in English, first here
    return None

def tr(w, mod, seen=0):
    if w in ROMAN:
        return w.upper(), "r"
    if w in TERMS and seen < 2:
        return " ".join(tr(t, mod, seen + 1)[0] for t in reversed(tok(TERMS[w])))[:], "k"
    r = lookup(w, mod)
    if r:
        return r, "l"
    r = split2(w)
    if r:
        return r, "c"
    return loan(w), "x"

def phrase(name):
    t = [w for w in tok(name) if (w not in STOP or w == "of") and (len(w) > 2 or w in ROMAN)]
    if "of" in t:
        k = t.index("of")
        L, R = t[:k], t[k + 1:]
        ws = [("m" if i else "h", w) for i, w in enumerate(reversed(L))] + [("m", w) for w in reversed(R)]
    else:
        ws = [("m" if i else "h", w) for i, w in enumerate(reversed(t))]
    out, kinds = [], set()
    for role, w in ws:
        if w == "of" or len(w) == 0:
            continue
        r, k = tr(w, role == "m")
        out.append(r); kinds.add(k)
    s = " ".join(out)
    return (s[:1].upper() + s[1:]) if s else "", kinds

def fold(w):
    return unicodedata.normalize("NFD", w).encode("ascii", "ignore").decode()

REAL = set()
for p in glob.glob(REF + "/*.dic"):
    for l in open(p, encoding="utf8", errors="ignore").read().splitlines()[1:]:
        w = l.split("/")[0].strip().lower()
        REAL.add(w); REAL.add(fold(w))
OI = set(open(REF + "/oldirish_known.txt", encoding="utf8").read().split()) if glob.glob(REF + "/oldirish_known.txt") else set()
REAL |= OI | {fold(w) for w in OI}

# a coined or lexicon word that is a living Gaelic word takes the archaic layer's second shift
sys.path.insert(0, "/mnt/project-files/diaithiris-archaic/tools")
import archaic as A
A.REAL = REAL
def clear(w):
    if w.upper() == w or w not in REAL and fold(w) not in REAL:
        return w
    for c in (A.fallback(w), w + "u", w + "ad", w + "ir"):
        if c not in REAL and fold(c) not in REAL:
            return c
    return w + "ur"

items = json.load(open(ITEMS, encoding="utf8"))
names, stat, bad = {}, collections.Counter(), collections.Counter()
for i, n in items.items():
    nm = n if n else i.split(":", 1)[1].replace("_", " ").replace("/", " ")
    p, kinds = phrase(nm)
    if not p:
        continue
    p = " ".join(clear(w.lower()) if w[:1].islower() or w.isupper() else clear(w.lower()).capitalize() for w in p.split())
    names[i] = p
    stat["+".join(sorted(kinds))] += 1
    for w in WORDS(p):
        if w in REAL or fold(w) in REAL:
            bad[w] += 1
json.dump(names, open(OUT, "w", encoding="utf8"), ensure_ascii=False, separators=(",", ":"), sort_keys=True)
print(len(names), "names;", dict(stat))
print("words equal to a real Gaelic word:", len(bad), "of", len({w for p in names.values() for w in WORDS(p)}), "distinct;", bad.most_common(40))
if REPORT:
    open(REPORT, "w", encoding="utf8").write("collisions:\n" + "\n".join(f"{w} {n}" for w, n in bad.most_common()) + "\n")
