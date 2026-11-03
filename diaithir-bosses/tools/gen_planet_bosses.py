#!/usr/bin/env python3
"""Planet bosses: a roster of real Library entries per planet, one body plan, model and texture per boss, and a rite per boss.

Usage: gen_planet_bosses.py <planets.tsv> <content.sqlite> <library.sqlite> <rites.tsv> <out resources dir>
Reads only; every name and the archetype-driving words come from the entry's own text. Writes
  data/diaithirbosses/planet_bosses.json      compact roster (expanded into profiles at load)
  data/diaithirbosses/planet_templates.json   attack/phase templates per body plan
  data/diaithirbosses/rite_bosses.json        rite id -> boss id
  assets/diaithirbosses/looks/<id>.json       model parts (cuboids) per boss
  assets/diaithirbosses/textures/entity/planet/<id>.png
  tools/planet_bosses_report.md               counts and uniqueness
"""
import colorsys, hashlib, json, math, os, random, re, sqlite3, sys
from collections import defaultdict
from PIL import Image

planets_tsv, content_db, lib_db, rites_tsv, out = sys.argv[1:6]
FORM_PRI = {"BOSS": 0, "MOB": 1, "NPC": 2}

# ---------------------------------------------------------------- roster
def rng_for(*k):
    return random.Random(int(hashlib.sha256("|".join(map(str, k)).encode()).hexdigest()[:12], 16))

planets = []
for ln in open(planets_tsv, encoding="utf8").read().splitlines()[1:]:
    c = ln.split("\t")
    planets.append(dict(id=c[0], name=c[1], dim=c[2], soil=c[3], realm=c[4], spine=c[5], opens=int(c[6]), entries=int(c[7])))

con = sqlite3.connect(content_db)
lib = sqlite3.connect(lib_db)
cand = defaultdict(list)
for pid, form, eid, tier, name in con.execute(
        "select lower(universe), form, id, tier, name from content where form in ('BOSS','MOB','NPC')"):
    cand[pid].append((FORM_PRI[form], eid, tier, name))

MONSTER = re.compile(r"dragon|wyrm|drake|serpent|snake|worm|leviathan|spider|beetle|swarm|golem|colossus|giant|titan|ogre|troll|"
                     r"beast|monster|horror|abomination|hydra|wraith|spectre|specter|demon|fiend|lich|avatar|guardian|warden|"
                     r"tyrant|devourer|behemoth|elemental|kraken|chimera|wolf|bear|lion|boar|hound|eagle|raven|roc|phoenix", re.I)
ARCH_WORDS = [
    ("hydra", r"\bhydra|many-headed|multi-headed|three-headed|seven-headed|five-headed"),
    ("dragon", r"\b(dragon|wyrm|drake|wyvern|firedrake)s?\b"),
    ("serpent", r"\b(serpent|snake|worm|eel|leviathan|basilisk|naga|python)s?\b"),
    ("arachnid", r"\b(spider|arachnid|scorpion|tarantula)s?\b"),
    ("bird", r"\b(bird|raven|crow|eagle|owl|roc|phoenix|hawk|falcon|harpy|gryphon|griffin|condor|vulture|heron|swan)s?\b"),
    ("insect", r"\b(insect|beetle|mantis|swarm|wasp|locust|hornet|moth|termite|cicada|hive|scarab|ant|bee)s?\b"),
    ("aquatic", r"\b(kraken|shark|whale|crab|squid|octopus|fish|lobster|tentacle|tentacled)s?\b|\bsea (beast|monster|serpent)"),
    ("treant", r"\b(treant|tree|fungus|mushroom|bramble|thorn|ent)s?\b|\bliving (tree|forest|wood)"),
    ("centaur", r"\b(centaur|horse|steed|stallion|unicorn)s?\b"),
    ("golem", r"\b(golem|construct|automaton|colossus|statue|obelisk|monolith|clockwork|boulder)s?\b|\bmechanical\b"),
    ("spirit", r"\b(ghost|spirit|wraith|shade|phantom|spectre|specter|banshee|lich|elemental)s?\b|\bghostly\b"),
    ("quadruped", r"\b(wolf|wolves|bear|lion|boar|hound|stag|bull|ox|tiger|panther|ram|mammoth|rhino|hog|fox|lizard|salamander|crocodile)s?\b"),
    ("giant", r"\b(giant|titan|ogre|troll|jotun|cyclops|behemoth|goliath)s?\b"),
]
ARCH_RX = [(a, re.compile(r, re.I)) for a, r in ARCH_WORDS]
REALM_HUM = {  # humanoid subtype lean per realm
    "Arcane": ["mage", "reaper", "king", "winged"], "Martial": ["knight", "multiarm", "king", "brute"],
    "Ki": ["multiarm", "winged", "reaper", "knight"], "Mythic": ["fiend", "winged", "king", "mage"],
    "Stellar": ["winged", "king", "mage", "multiarm"], "Grounded": ["brute", "knight", "fiend", "reaper"],
    "Outer": ["reaper", "fiend", "mage", "multiarm"]}
HUM_SUBS = ["knight", "mage", "brute", "multiarm", "winged", "king", "reaper", "fiend"]
SOIL_HUE = {"Bloom": 0.30, "Frost": 0.55, "Iron": 0.08, "Hollow": 0.75, "Storm": 0.62, "Ember": 0.02, "Quiet": 0.45}
WORD_HUE = [(r"fire|ember|flame|magma|ash|burn|sun\b|blaze|forge", 0.02, 0.8), (r"ice|frost|snow|cold|winter|glacier", 0.54, 0.6),
            (r"rot|plague|swamp|poison|venom|blight|bog|fungus", 0.22, 0.6), (r"gold|radiant|light|dawn|holy|sacred", 0.12, 0.8),
            (r"shadow|night|dark|void|abyss|dusk|black", 0.78, 0.35), (r"blood|crimson|red", 0.98, 0.7),
            (r"sea|tide|ocean|water|wave|deep", 0.5, 0.6), (r"bone|skeleton|dead|grave|tomb", 0.1, 0.12),
            (r"stone|iron|steel|rock|mountain|granite", 0.6, 0.12), (r"forest|leaf|wild|green|grove|verdant", 0.32, 0.55)]


def tier_gate(opens, tier):
    return max(1, min(8, opens + tier - 1))


def classify(text, name, realm, rng):
    t = f"{name}. {text}"[:260]  # the subject is named in the first sentences
    hits = [(m.start(), i, a) for i, (a, rx) in enumerate(ARCH_RX) for m in [rx.search(t)] if m]
    if hits:
        return min(hits)[2], "text"
    sub = rng.choice(REALM_HUM.get(realm, HUM_SUBS) + HUM_SUBS)
    return "humanoid:" + sub, "realm"


roster = []
for P in planets:
    pool = cand.get(P["id"], [])
    if len(pool) < 3:  # planets with no boss-ish rows draw from every being in the planet's Library
        extra = lib.execute("select id, age, name from entry where lower(universe)=? and kind='being' order by age desc limit 400", (P["id"],)).fetchall()
        pool = pool + [(2, i, a, n) for i, a, n in extra if i not in {x[1] for x in pool}]
    if not pool:
        continue
    n = max(3, min(12, 3 + int(2 * math.log10(max(P["entries"], 10)))))
    # score: form priority, then monstrous text, then hash for a stable shuffle
    txt = {}
    for i in range(0, len(pool), 500):
        ids = [p[1] for p in pool[i:i + 500]]
        for eid, s in lib.execute("select id, summary from entry where id in (%s)" % ",".join("?" * len(ids)), ids):
            txt[eid] = s or ""
    scored = []
    for pri, eid, tier, name in pool:
        mon = 0 if MONSTER.search(txt.get(eid, "") + " " + name) else 1
        scored.append((pri + mon, rng_for(P["id"], eid).random(), pri, eid, tier, name))
    scored.sort()
    by_tier = defaultdict(list)
    for s in scored:
        by_tier[s[4]].append(s)
    chosen = []
    for t in sorted(by_tier, reverse=True):  # one per Age tier first, so the roster spans the planet's Ages
        if len(chosen) < n:
            chosen.append(by_tier[t][0])
    for s in scored:
        if len(chosen) >= n:
            break
        if s not in chosen:
            chosen.append(s)
    for s in chosen:
        _, _, pri, eid, tier, name = s
        roster.append(dict(P=P, eid=eid, tier=int(tier), name=name, text=txt.get(eid, ""), pri=pri))

# ---------------------------------------------------------------- geometry
class Part:
    def __init__(s, name, pivot, rot=(0, 0, 0)):
        s.name, s.pivot, s.rot, s.cubes = name, pivot, rot, []

    def box(s, x0, y0, f0, x1, y1, f1, tag=""):
        """World box in pixels: x lateral, y up from ground, f forward (the face's side)."""
        a = lambda lo, hi: (min(lo, hi), max(lo, hi))
        (x0, x1), (y0, y1), (f0, f1) = a(x0, x1), a(y0, y1), a(f0, f1)
        if min(x1 - x0, y1 - y0, f1 - f0) < 1:
            return s
        s.cubes.append([round(x0), round(y0), round(f0), round(x1), round(y1), round(f1), tag])
        return s


class Build:
    def __init__(s, rng):
        s.r, s.parts, s.feat = rng, [], []

    def part(s, name, px, py, pf, rot=(0, 0, 0)):
        p = Part(name, (px, py, pf), rot)
        s.parts.append(p)
        return p

    def U(s, a, b):
        return s.r.uniform(a, b)

    def sym(s, name, x, py, pf, fn, rot=(0, 0, 0)):
        """Mirrored pair named name_l / name_r; fn(part, sign) adds the boxes for one side."""
        for sg, sd in ((1, "l"), (-1, "r")):
            p = s.part(f"{name}_{sd}", sg * x, py, pf, (rot[0], sg * rot[1], sg * rot[2]))
            fn(p, sg)


def face(b, hx, hy, hf, w, h, d, snout=0, tag="h"):
    """Head box with optional snout; returns head part. Centred on hx; hy = bottom; hf = back of head."""
    p = b.part("head", hx, hy, hf + d * 0.5)
    p.box(hx - w / 2, hy, hf, hx + w / 2, hy + h, hf + d, tag)
    if snout:
        p.box(hx - w * 0.3, hy, hf + d, hx + w * 0.3, hy + h * 0.5, hf + d + snout)
        p.box(hx - w * 0.3, hy + h * 0.5, hf + d, hx + w * 0.3, hy + h * 0.6, hf + d + snout * 0.8)
    return p


def horns(b, head, hx, top, hf, d, w, kind):
    ln = b.U(4, 9)
    if kind == "curved":
        for sg in (1, -1):
            head.box(hx + sg * w * 0.4, top, hf + d * 0.3, hx + sg * (w * 0.4 + 2), top + ln, hf + d * 0.3 + 2)
            head.box(hx + sg * (w * 0.4 + 2), top + ln - 2, hf + d * 0.3, hx + sg * (w * 0.4 + 5), top + ln, hf + d * 0.3 + 2)
    elif kind == "antlers":
        for sg in (1, -1):
            head.box(hx + sg * w * 0.35, top, hf + d * 0.3, hx + sg * (w * 0.35 + 1), top + ln, hf + d * 0.3 + 1)
            for k in range(3):
                y = top + 2 + k * ln / 3.5
                head.box(hx + sg * (w * 0.35 + 1), y, hf + d * 0.3, hx + sg * (w * 0.35 + 4), y + 1, hf + d * 0.3 + 1)
    elif kind == "spikes":
        for k in range(5):
            x = hx - w / 2 + 1 + k * (w - 2) / 4
            head.box(x - 0.5, top, hf + 1, x + 0.5, top + b.U(2, 6), hf + 2)
    elif kind == "crown":
        head.box(hx - w / 2 - 1, top - 1, hf - 1, hx + w / 2 + 1, top + 2, hf + d + 1)
        for k in range(4):
            x = hx - w / 2 + k * (w / 3)
            head.box(x - 0.5, top + 2, hf, x + 0.5, top + 5, hf + 1)
    elif kind == "single":
        head.box(hx - 1, top, hf + d * 0.5, hx + 1, top + ln, hf + d * 0.5 + 2)
    elif kind == "frill":
        head.box(hx - w * 0.9, top - 4, hf - 3, hx + w * 0.9, top + 5, hf - 1)


HORNS = ["none", "curved", "antlers", "spikes", "crown", "single", "frill"]


def spikes_back(b, p, x0, x1, y, f0, f1, n, hmax):
    for k in range(n):
        f = f0 + (f1 - f0) * (k + 0.5) / n
        h = hmax * (0.5 + 0.5 * math.sin(math.pi * (k + 0.5) / n))
        p.box(x0, y, f - 1, x1, y + h, f + 1)


def tail(b, x, y, f, n, w, h, ln, kind):
    """Trailing segments backwards from forward position f (behind = smaller f)."""
    for k in range(n):
        s = 1 - k / (n + 1)
        p = b.part(f"tail_{k}", x, y, f - k * ln)
        p.box(x - w * s / 2, y - h * s / 2, f - (k + 1) * ln, x + w * s / 2, y + h * s / 2, f - k * ln)
        if k == n - 1:
            if kind == "club":
                p.box(x - w * .7, y - h * .7, f - (k + 1) * ln - 5, x + w * .7, y + h * .7, f - (k + 1) * ln)
            elif kind == "fork":
                for sg in (1, -1):
                    p.box(x + sg * 1, y - 1, f - (k + 1) * ln - 6, x + sg * 3, y + 1, f - (k + 1) * ln)
            elif kind == "blade":
                p.box(x - 0.5, y - h * 1.4, f - (k + 1) * ln - 6, x + 0.5, y + h * 1.4, f - (k + 1) * ln)


def hold(p, x, y, f, kind, b):
    """A held prop, built into the arm part so it swings with the arm."""
    L = b.U(16, 26)
    if kind == "staff":
        p.box(x - .5, y - 6, f, x + .5, y + L, f + 1); p.box(x - 2, y + L, f - 1, x + 2, y + L + 4, f + 3, "g")
    elif kind == "greatsword":
        p.box(x - 1.5, y - 2, f, x + 1.5, y + L, f + 1); p.box(x - 4, y + 1, f - .5, x + 4, y + 3, f + 1.5)
    elif kind == "hammer":
        p.box(x - .5, y - 4, f, x + .5, y + L * .8, f + 1); p.box(x - 4, y + L * .8, f - 2, x + 4, y + L * .8 + 7, f + 3)
    elif kind == "axe":
        p.box(x - .5, y - 4, f, x + .5, y + L * .8, f + 1); p.box(x - 1, y + L * .5, f - 5, x + 1, y + L * .8, f + 1)
    elif kind == "bow":
        p.box(x - .5, y - 4, f + 2, x + .5, y + 4, f + 3); p.box(x - .5, y + 4, f + 1, x + .5, y + 10, f + 2); p.box(x - .5, y - 10, f + 1, x + .5, y - 4, f + 2)
    elif kind == "shield":
        p.box(x - 5, y - 2, f + 1, x + 5, y + 12, f + 3)
    elif kind == "scythe":
        p.box(x - .5, y - 6, f, x + .5, y + L, f + 1); p.box(x - 7, y + L, f, x + 1, y + L + 2, f + 1); p.box(x - 8, y + L - 3, f, x - 6, y + L, f + 1)
    elif kind == "lantern":
        p.box(x - .5, y - 4, f, x + .5, y + 10, f + 1); p.box(x - 2, y + 10, f - 1, x + 2, y + 15, f + 3, "g")


def humanoid(b, sub):
    r = b.U
    tw, th, td = r(8, 12), r(11, 15), r(5, 7)
    leg = r(10, 14)
    hs = r(7, 9)
    arm = r(10, 14)
    build = b.r.choice(["lanky", "stocky", "even", "even"])
    if build == "lanky": tw, th, leg = tw * .85, th * 1.15, leg * 1.2
    elif build == "stocky": tw, th, leg = tw * 1.25, th * .95, leg * .9
    by = leg
    prop = b.r.choice(["staff", "greatsword", "hammer", "axe", "bow", "shield", "scythe", "lantern", "none"])
    b.feat += [sub, build, prop]
    if sub in ("reaper", "mage"):  # robed, no legs: hem to the ground
        hem = b.part("robe", 0, by + th, 0)
        steps = 3
        for k in range(steps):
            w = tw + 2 + k * 3
            hem.box(-w / 2, k * (by + 2) / steps, -td / 2 - k, w / 2, (k + 1) * (by + 2) / steps, td / 2 + k)
        b.feat.append(f"robe{steps}")
    else:
        w = b.U(3, 4.5)
        b.sym("leg", tw * 0.27, by, 0, lambda p, s: p.box(s * tw * 0.27 - w / 2, 0, -w / 2, s * tw * 0.27 + w / 2, by, w / 2))
    body = b.part("body", 0, by, 0)
    body.box(-tw / 2, by, -td / 2, tw / 2, by + th, td / 2)
    if sub == "brute":
        body.box(-tw * 0.75, by + th * 0.55, -td * 0.7, tw * 0.75, by + th, td * 0.7)
        hs *= 0.75; arm *= 1.2
    hy = by + th + (1 if sub != "brute" else -1)
    hf = -hs / 2 + (3 if sub == "brute" else 0)
    head = face(b, 0, hy, hf, hs, hs, hs, snout=2 if sub in ("brute", "fiend") else 0)
    if sub in ("knight", "king", "winged"):
        head.box(-hs / 2 - 0.5, hy + hs - 1, hf - 0.5, hs / 2 + 0.5, hy + hs + 1, hf + hs + 0.5)
    if sub == "king":
        horns(b, head, 0, hy + hs, hf, hs, hs, "crown"); b.feat.append("crown")
        cape = b.part("cape", 0, by + th, -td / 2)
        cape.box(-tw / 2 - 1, 0, -td / 2 - 2, tw / 2 + 1, by + th, -td / 2)
    elif sub == "fiend":
        k = b.r.choice(["curved", "antlers", "spikes"]); horns(b, head, 0, hy + hs, hf, hs, hs, k); b.feat.append(k)
        tail(b, 0, by, -td / 2, 4, 3, 3, 4, b.r.choice(["fork", "blade", "club"]))
    elif sub == "reaper":
        hood = b.part("hood", 0, hy, hf)
        hood.box(-hs / 2 - 1.5, hy - 1, hf - 2, hs / 2 + 1.5, hy + hs + 3, hf + hs + 1)
        hood.box(-1, hy + hs + 3, hf + 1, 1, hy + hs + 8, hf + 3)
    elif sub == "mage":
        hat = b.part("hat", 0, hy + hs, hf + hs / 2)
        for k in range(4):
            sz = hs + 5 - k * 3
            hat.box(-sz / 2, hy + hs + k * 3, hf + hs / 2 - sz / 2, sz / 2, hy + hs + (k + 1) * 3, hf + hs / 2 + sz / 2)
        for k in range(3):
            o = b.part(f"orbit_{k}", 0, by + th, 0)
            a = 2 * math.pi * k / 3
            ox, of = math.cos(a) * 11, math.sin(a) * 11
            o.box(ox - 1.5, by + th - 4 + k * 4, of - 1.5, ox + 1.5, by + th - 1 + k * 4, of + 1.5, "g")
    elif sub == "winged":
        for nm in ("wing",):
            def wg(p, s):
                for k in range(3):
                    p.box(s * (tw / 2 + 1 + k * 7), by + th - 2 + k * 4, -td / 2 - 1 - k, s * (tw / 2 + 8 + k * 7), by + th + 8 + k * 4, -td / 2 - 1.5 - k)
            b.sym(nm, 0, by + th, -td / 2, wg)
        halo = b.part("halo", 0, hy + hs, hf)
        halo.box(-hs * 0.7, hy + hs + 2, hf - 2, hs * 0.7, hy + hs + 3, hf + hs + 2, "g")
    k = 2 if sub == "multiarm" else 1
    for j in range(k):
        yy = by + th - 2 - j * 5
        aw = r(3, 4.5)
        sh = sub == "knight" or sub == "king"
        def ar(p, s, yy=yy, aw=aw, j=j):
            x = s * (tw / 2 + aw / 2)
            p.box(x - aw / 2, yy - arm, -aw / 2 + 1, x + aw / 2, yy + 2, aw / 2 + 1)
            if j == 0 and s == 1:
                hold(p, x, yy - arm, aw / 2 + 2, prop, b)
            if sub == "brute":
                p.box(x - aw, yy - arm - 4, -aw / 2, x + aw, yy - arm + 2, aw + 1)
            if sh and j == 0:
                p.box(x - aw, yy - 1, -aw, x + aw, yy + 4, aw + 1)
        b.sym(f"arm{j}", 0, yy, 0, ar)


def quadruped(b, big=False):
    r = b.U
    bl, bw, bh = r(18, 26), r(10, 15), r(10, 15)
    ll, lw = r(8, 13), r(3.5, 5.5)
    y = ll
    body = b.part("body", 0, y, 0)
    body.box(-bw / 2, y, -bl / 2, bw / 2, y + bh, bl / 2)
    for i, (f, sg) in enumerate([(bl / 2 - lw, 1), (bl / 2 - lw, -1), (-bl / 2 + lw, 1), (-bl / 2 + lw, -1)]):
        p = b.part(f"leg_{i}", sg * (bw / 2 - lw / 2), y, f)
        p.box(sg * (bw / 2 - lw / 2) - lw / 2, 0, f - lw / 2, sg * (bw / 2 - lw / 2) + lw / 2, y, f + lw / 2)
    hw = bw * r(.6, .9)
    head = face(b, 0, y + bh * 0.5, bl / 2 - 2, hw, hw, hw * 0.9, snout=r(2, 6))
    horns(b, head, 0, y + bh * 0.5 + hw, bl / 2 - 2, hw * 0.9, hw, k := b.r.choice(HORNS)); b.feat.append(k)
    if b.r.random() < .5:
        spikes_back(b, body, -1, 1, y + bh, -bl / 2, bl / 2, 6, r(3, 8)); b.feat.append("backspikes")
    if b.r.random() < .35:
        for sg in (1, -1):
            head.box(sg * hw * .3 - .5, y + bh * 0.5 - 4, bl / 2 + hw * .6, sg * hw * .3 + .5, y + bh * 0.5, bl / 2 + hw * .6 + 1)
        b.feat.append("tusks")
    tail(b, 0, y + bh * 0.7, -bl / 2, b.r.randint(2, 4), 4, 4, 4, b.r.choice(["none", "club", "fork"]))


def serpent(b, heads=1):
    r = b.U
    n = b.r.randint(7, 11)
    w0 = r(8, 13)
    sw = r(5, 8)
    for k in range(n):
        s = 1 - 0.7 * k / n
        w = w0 * s
        off = math.sin(k * r(.5, .9)) * sw
        p = b.part(f"seg_{k}", off, w / 2, -k * w0 * 0.8)
        p.box(off - w / 2, 0, -k * w0 * 0.8 - w0 * 0.8, off + w / 2, w, -k * w0 * 0.8)
    if heads == 1:
        up = r(6, 16)
        neck = b.part("neck", 0, w0, 0)
        neck.box(-w0 * .4, 0, -2, w0 * .4, w0 + up, w0 * .8)
        hd = face(b, 0, w0 + up - 2, w0 * .5, w0 * .9, w0 * .7, w0 * 1.1, snout=r(2, 6))
        horns(b, hd, 0, w0 + up - 2 + w0 * .7, w0 * .5, w0 * 1.1, w0 * .9, k := b.r.choice(["frill", "spikes", "none", "single", "curved"])); b.feat.append(k)
    else:
        b.feat.append(f"heads{heads}")
        for h in range(heads):
            ang = (h - (heads - 1) / 2) * 0.8
            hx = math.sin(ang) * w0 * 1.6
            up = r(8, 18)
            nk = b.part(f"neck_{h}", hx, w0, 0)
            nk.box(hx - w0 * .25, 0, -2, hx + w0 * .25, w0 + up, w0 * .5 + h % 2 * 3)
            hd = b.part(f"head_{h}", hx, w0 + up, w0 * .4)
            hd.box(hx - w0 * .4, w0 + up - 2, w0 * .2, hx + w0 * .4, w0 + up + w0 * .5, w0 * 1.3, "h")
            hd.box(hx - w0 * .25, w0 + up - 2, w0 * 1.3, hx + w0 * .25, w0 + up + w0 * .25, w0 * 1.3 + 4)


def dragon(b):
    r = b.U
    bl, bw, bh = r(20, 28), r(11, 15), r(11, 15)
    ll = r(8, 12)
    y = ll
    body = b.part("body", 0, y, 0)
    body.box(-bw / 2, y, -bl / 2, bw / 2, y + bh, bl / 2)
    pairs = b.r.choice([1, 2])
    for i in range(pairs * 2):
        f = bl / 2 - 4 - (i // 2) * (bl - 8)
        sg = 1 if i % 2 == 0 else -1
        p = b.part(f"leg_{i}", sg * (bw / 2 - 2), y, f)
        p.box(sg * (bw / 2 - 2) - 2.5, 0, f - 2.5, sg * (bw / 2 - 2) + 2.5, y, f + 2.5)
    ns = b.r.randint(2, 4)
    nl = r(5, 8)
    for k in range(ns):
        p = b.part(f"neck_{k}", 0, y + bh * .6 + k * nl * .8, bl / 2 + k * 3)
        p.box(-bw * .3, y + bh * .6 + k * nl * .8, bl / 2 + k * 3 - 2, bw * .3, y + bh * .6 + (k + 1) * nl * .8 + 2, bl / 2 + k * 3 + 4)
    hy = y + bh * .6 + ns * nl * .8
    hd = face(b, 0, hy, bl / 2 + ns * 3, bw * .6, bw * .5, bw * .8, snout=r(4, 8))
    horns(b, hd, 0, hy + bw * .5, bl / 2 + ns * 3, bw * .8, bw * .6, k := b.r.choice(["curved", "antlers", "spikes", "frill", "single"])); b.feat.append(k)
    wl = r(16, 26)
    segs = b.r.choice([2, 3])
    def wg(p, s):
        for k in range(segs):
            p.box(s * (bw / 2 + k * wl / segs), y + bh - 1 + k * 3, -bl / 3 - k * 2, s * (bw / 2 + (k + 1) * wl / segs), y + bh + 1 + k * 3, bl / 4 - k * 4)
    b.sym("wing", 0, y + bh, 0, wg); b.feat.append(f"wing{segs}")
    spikes_back(b, body, -1, 1, y + bh, -bl / 2, bl / 2, 7, r(3, 7))
    tail(b, 0, y + bh * .5, -bl / 2, b.r.randint(4, 6), bw * .7, bh * .6, 5, b.r.choice(["fork", "blade", "club", "none"]))


def arachnid(b):
    r = b.U
    ab = r(10, 16)
    th = r(7, 10)
    y = r(8, 11)
    body = b.part("body", 0, y, 0)
    body.box(-th / 2, y, -th / 2, th / 2, y + th, th / 2)
    ab_p = b.part("abdomen", 0, y, -th / 2)
    ab_p.box(-ab / 2, y - 1, -th / 2 - ab, ab / 2, y + ab * .7, -th / 2)
    for k in range(8):
        sg = 1 if k % 2 == 0 else -1
        f = th / 2 - (k // 2) * th / 3
        p = b.part(f"leg_{k}", sg * th / 2, y + th / 2, f)
        L = r(10, 16)
        p.box(sg * th / 2, y + th / 2 - 1, f - 1, sg * (th / 2 + L), y + th / 2 + 1, f + 1)
        p.box(sg * (th / 2 + L - 2), 0, f - 1, sg * (th / 2 + L), y + th / 2, f + 1)
    hd = face(b, 0, y + 1, th / 2, th * .8, th * .6, th * .5)
    for sg in (1, -1):
        hd.box(sg * 1.5 - .5, y - 2, th / 2 + th * .5, sg * 1.5 + .5, y + 2, th / 2 + th * .5 + 2)
    if b.r.random() < .5:
        b.feat.append("stinger")
        tail(b, 0, y + ab * .5, -th / 2 - ab, 3, 3, 3, 4, "blade")


def bird(b):
    r = b.U
    bw, bh, bl = r(8, 12), r(9, 13), r(12, 17)
    ly = r(12, 18)
    y = ly
    body = b.part("body", 0, y, 0)
    body.box(-bw / 2, y, -bl / 2, bw / 2, y + bh, bl / 2)
    for sg in (1, -1):
        p = b.part(f"leg_{(0 if sg == 1 else 1)}", sg * bw * .25, y, 0)
        p.box(sg * bw * .25 - 1, 0, -1, sg * bw * .25 + 1, y, 1)
        p.box(sg * bw * .25 - 2, 0, -1, sg * bw * .25 + 2, 1, 5)
    nk = b.part("neck", 0, y + bh, bl / 2)
    nl = r(5, 12)
    nk.box(-2, y + bh - 2, bl / 2 - 2, 2, y + bh + nl, bl / 2 + 2)
    hd = face(b, 0, y + bh + nl - 1, bl / 2 - 3, 5, 5, 5, snout=r(3, 7))
    horns(b, hd, 0, y + bh + nl + 4, bl / 2 - 3, 5, 5, k := b.r.choice(["none", "crown", "spikes", "frill", "single"])); b.feat.append(k)
    wl = r(14, 24)
    def wg(p, s):
        for k in range(3):
            p.box(s * (bw / 2 + k * wl / 3), y + bh - 1 + k, -bl / 2 + k * 2, s * (bw / 2 + (k + 1) * wl / 3), y + bh + 1 + k, bl / 3 - k * 3)
    b.sym("wing", 0, y + bh, 0, wg)
    for k in range(b.r.randint(3, 5)):
        a = (k - 2) * .35
        tp = b.part(f"tail_{k}", 0, y + bh * .5, -bl / 2)
        tp.box(math.sin(a) * 6 - 1, y + bh * .3, -bl / 2 - 10 - abs(k - 2), math.sin(a) * 6 + 1, y + bh * .7, -bl / 2)


def insect(b):
    r = b.U
    th, ab = r(8, 11), r(11, 17)
    y = r(7, 10)
    body = b.part("body", 0, y, 0)
    body.box(-th / 2, y, -th / 2, th / 2, y + th, th / 2)
    ap = b.part("abdomen", 0, y, -th / 2)
    ap.box(-th * .6, y, -th / 2 - ab, th * .6, y + th * .9, -th / 2)
    for k in range(6):
        sg = 1 if k % 2 == 0 else -1
        f = th / 2 - (k // 2) * th / 2.2
        p = b.part(f"leg_{k}", sg * th / 2, y + 2, f)
        p.box(sg * th / 2, y + 1, f - 1, sg * (th / 2 + 8), y + 3, f + 1)
        p.box(sg * (th / 2 + 7), 0, f - 1, sg * (th / 2 + 9), y + 3, f + 1)
    hd = face(b, 0, y + 1, th / 2, th * .8, th * .7, th * .6)
    for sg in (1, -1):
        hd.box(sg * th * .25 - 1, y, th / 2 + th * .6, sg * th * .25 + 1, y + 2, th / 2 + th * .6 + 4)  # mandibles
        hd.box(sg * 2 - .5, y + th * .7, th / 2 + 2, sg * 2 + .5, y + th * .7 + 7, th / 2 + 3)  # antennae
    if b.r.random() < .7:
        def wg(p, s):
            p.box(s * 2, y + th, -th / 2, s * (th * .6 + r(10, 16)), y + th + 1, th / 2 + ab / 3)
        b.sym("wing", 0, y + th, 0, wg); b.feat.append("wings")
    if b.r.random() < .5:
        hd.box(-1, y + th * .7, th / 2 + th * .5, 1, y + th * .7 + 8, th / 2 + th * .5 + 2); b.feat.append("horn")


def golem(b):
    r = b.U
    lh, lw = r(8, 12), r(6, 9)
    th, tw, td = r(12, 17), r(13, 19), r(8, 12)
    for sg in (1, -1):
        p = b.part(f"leg_{0 if sg == 1 else 1}", sg * tw * .25, lh, 0)
        p.box(sg * tw * .25 - lw / 2, 0, -lw / 2, sg * tw * .25 + lw / 2, lh, lw / 2)
    body = b.part("body", 0, lh, 0)
    body.box(-tw / 2, lh, -td / 2, tw / 2, lh + th, td / 2)
    body.box(-tw * .35, lh + th, -td * .4, tw * .35, lh + th + 3, td * .4)
    body.box(-3, lh + th * .5 - 2, -td / 2 - 1, 3, lh + th * .5 + 2, -td / 2, "g")  # core
    hs = r(6, 9)
    hd = face(b, 0, lh + th + 3, -hs / 2, hs, hs * .8, hs)
    for sg in (1, -1):
        sh = r(4, 8)
        def ar(p, s):
            p.box(s * (tw / 2 + 1), lh + th - r(12, 16), -3.5, s * (tw / 2 + 7), lh + th, 3.5)
            p.box(s * (tw / 2 + 1) - 1, lh + th - 20, -4.5, s * (tw / 2 + 8), lh + th - 11, 4.5)
        if sg == 1:
            b.sym("arm0", 0, lh + th, 0, ar)
    for k in range(b.r.randint(2, 5)):  # crystal / rock growths
        x = b.U(-tw / 2, tw / 2)
        body.box(x - 1, lh + th, -td / 2 + b.U(0, td - 2), x + 1, lh + th + b.U(3, 9), -td / 2 + b.U(2, td)); b.feat.append("growth")


def spirit(b):
    r = b.U
    n = b.r.randint(4, 6)
    w0 = r(11, 15)
    y0 = r(6, 12)
    for k in range(n):
        s = 1 - k * .15
        w = w0 * s
        p = b.part(f"body_{k}" if k else "body", 0, y0, 0)
        p.box(-w / 2, y0 + k * 4, -w / 3, w / 2, y0 + (k + 1) * 4, w / 3)
    top = y0 + n * 4
    hs = r(7, 10)
    hd = face(b, 0, top, -hs / 2, hs, hs, hs)
    horns(b, hd, 0, top + hs, -hs / 2, hs, hs, k := b.r.choice(["crown", "antlers", "spikes", "curved", "none"])); b.feat.append(k)
    for k in range(b.r.randint(3, 6)):
        o = b.part(f"orbit_{k}", 0, top, 0)
        a = 2 * math.pi * k / 5
        ox, of = math.cos(a) * r(10, 16), math.sin(a) * r(10, 16)
        o.box(ox - 1.5, top - 6 + k * 3, of - 1.5, ox + 1.5, top - 3 + k * 3, of + 1.5, "g")
    def ar(p, s):
        p.box(s * (w0 / 2), top - 10, -1.5, s * (w0 / 2 + 3), top - 2, 1.5)
        p.box(s * (w0 / 2 + 1), top - 10 - r(6, 12), -1, s * (w0 / 2 + 3), top - 10, 1)
    b.sym("arm0", 0, top - 2, 0, ar)
    for k in range(b.r.randint(2, 4)):  # trailing wisps below the hem
        x = (k - 1.5) * 4
        w = b.part(f"tail_{k}", x, y0, 0)
        w.box(x - 1, 0, -1, x + 1, y0, 1)


def aquatic(b):
    r = b.U
    sub = b.r.choice(["fish", "crab", "kraken"])
    b.feat.append(sub)
    if sub == "fish":
        bl, bw, bh = r(24, 34), r(8, 12), r(10, 16)
        y = 4
        body = b.part("body", 0, y, 0)
        body.box(-bw / 2, y, -bl / 2, bw / 2, y + bh, bl / 2)
        hd = face(b, 0, y + 1, bl / 2 - 1, bw * .9, bh * .8, bw, snout=r(3, 7))
        body.box(-1, y + bh, -bl / 3, 1, y + bh + r(5, 11), bl / 4)
        for sg in (1, -1):
            body.box(sg * bw / 2, y + 1, bl / 6, sg * (bw / 2 + 7), y + 3, bl / 6 + 6)
        tail(b, 0, y + bh / 2, -bl / 2, 3, bw * .8, bh * .8, 5, "blade")
        for k in range(b.r.choice([0, 2, 3])):
            hd.box(-3 + k * 3, y + bh * .8, bl / 2 + 2 + k, -2 + k * 3, y + bh * .8 + 6, bl / 2 + 3 + k)
    elif sub == "crab":
        cw, cd, ch = r(18, 26), r(14, 20), r(7, 10)
        y = 6
        body = b.part("body", 0, y, 0)
        body.box(-cw / 2, y, -cd / 2, cw / 2, y + ch, cd / 2)
        hd = b.part("head", 0, y + ch, cd / 2)
        hd.box(-cw / 3, y + ch - 1, cd / 2 - 2, cw / 3, y + ch + 5, cd / 2 + 1, "h")
        for sg in (1, -1):
            def cl(p, s):
                p.box(s * (cw / 2 + 1), y, cd / 4, s * (cw / 2 + 5), y + 5, cd / 4 + 12)
                p.box(s * (cw / 2 + 1), y + 5, cd / 4 + 6, s * (cw / 2 + 5), y + 9, cd / 4 + 14)
            if sg == 1: b.sym("arm0", 0, y, cd / 4, cl)
        for k in range(6):
            sg = 1 if k % 2 == 0 else -1
            f = -cd / 3 + (k // 2) * cd / 3
            p = b.part(f"leg_{k}", sg * cw / 2, y, f)
            p.box(sg * cw / 2, y - 1, f - 1, sg * (cw / 2 + 6), y + 1, f + 1)
            p.box(sg * (cw / 2 + 5), 0, f - 1, sg * (cw / 2 + 7), y, f + 1)
    else:
        dome = r(14, 18)
        y = r(10, 14)
        body = b.part("body", 0, y, 0)
        body.box(-dome / 2, y, -dome / 2, dome / 2, y + dome, dome / 2)
        hd = b.part("head", 0, y + dome * .3, -dome / 2)
        hd.box(-dome * .35, y + dome * .3, -dome / 2 - 1, dome * .35, y + dome * .65, -dome / 2, "h")
        for k in range(8):
            a = 2 * math.pi * k / 8
            x, f = math.cos(a) * dome * .38, math.sin(a) * dome * .38
            p = b.part(f"leg_{k}", x, y, f)
            p.box(x - 1.5, 0, f - 1.5, x + 1.5, y, f + 1.5)
            p.box(x - 1, 0, f - 1 + math.copysign(3, math.sin(a)), x + 1, 3, f + 1 + math.copysign(3, math.sin(a)))


def treant(b):
    r = b.U
    tw, th = r(12, 17), r(20, 28)
    rh = r(5, 9)
    for k in range(4):
        a = math.pi / 4 + k * math.pi / 2
        x, f = math.cos(a) * tw * .45, math.sin(a) * tw * .45
        p = b.part(f"leg_{k}", x, rh, f)
        p.box(x - 2.5, 0, f - 2.5, x + 2.5, rh, f + 2.5)
        p.box(x - 4 + math.cos(a) * 2, 0, f - 4 + math.sin(a) * 2, x + 4 + math.cos(a) * 2, 2, f + 4 + math.sin(a) * 2)
    body = b.part("body", 0, rh, 0)
    body.box(-tw / 2, rh, -tw / 2, tw / 2, rh + th, tw / 2)
    body.box(-tw * .6, rh + th * .5, -tw * .4, tw * .6, rh + th * .7, tw * .4)
    hd = b.part("head", 0, rh + th * .55, -tw / 2)
    hd.box(-tw * .3, rh + th * .5, -tw / 2 - 2, tw * .3, rh + th * .75, -tw / 2, "h")
    hd.box(-1, rh + th * .5 - 3, -tw / 2 - 4, 1, rh + th * .5, -tw / 2 - 2)
    for sg in (1, -1):
        def ar(p, s):
            p.box(s * tw / 2, rh + th * .7, -2, s * (tw / 2 + 12), rh + th * .8, 2)
            for k in range(3):
                p.box(s * (tw / 2 + 12 - k * 2), rh + th * .8, -2 + k * 2, s * (tw / 2 + 14 - k * 2), rh + th * .8 + 5, -1 + k * 2)
        if sg == 1: b.sym("arm0", 0, rh + th * .75, 0, ar)
    cr = b.part("crown", 0, rh + th, 0)
    for k in range(b.r.randint(4, 8)):
        x, f = b.U(-tw / 2 - 3, tw / 2 + 3), b.U(-tw / 2 - 3, tw / 2 + 3)
        s = b.U(4, 8)
        cr.box(x - s / 2, rh + th + b.U(-2, 4), f - s / 2, x + s / 2, rh + th + b.U(4, 10), f + s / 2, "l")


def centaur(b):
    r = b.U
    bl, bw, bh = r(20, 26), r(10, 13), r(10, 13)
    ll = r(12, 16)
    y = ll
    body = b.part("body", 0, y, 0)
    body.box(-bw / 2, y, -bl / 2, bw / 2, y + bh, bl / 2)
    for i, (f, sg) in enumerate([(bl / 2 - 2, 1), (bl / 2 - 2, -1), (-bl / 2 + 2, 1), (-bl / 2 + 2, -1)]):
        p = b.part(f"leg_{i}", sg * (bw / 2 - 2), y, f)
        p.box(sg * (bw / 2 - 2) - 2, 0, f - 2, sg * (bw / 2 - 2) + 2, y, f + 2)
    tw, th = r(7, 10), r(11, 14)
    ft = bl / 2 - 3
    body.box(-tw / 2, y + bh, ft - 3, tw / 2, y + bh + th, ft + 3)
    hs = r(6, 8)
    hd = face(b, 0, y + bh + th, ft - hs / 2, hs, hs, hs)
    horns(b, hd, 0, y + bh + th + hs, ft - hs / 2, hs, hs, k := b.r.choice(["none", "crown", "antlers", "single"])); b.feat.append(k)
    def ar(p, s):
        p.box(s * (tw / 2), y + bh + th - 10, ft - 1.5, s * (tw / 2 + 3), y + bh + th - 1, ft + 1.5)
    b.sym("arm0", 0, y + bh + th - 1, ft, ar)
    tail(b, 0, y + bh * .8, -bl / 2, 3, 3, 3, 4, "none")


ARCH_FN = {
    "quadruped": quadruped, "serpent": serpent, "dragon": dragon, "arachnid": arachnid, "bird": bird, "insect": insect,
    "golem": golem, "spirit": spirit, "aquatic": aquatic, "treant": treant, "centaur": centaur,
}
HITBOX = {"humanoid": "tall", "giant": "tall", "golem": "tall", "treant": "tall", "bird": "tall", "centaur": "wide", "quadruped": "wide",
          "dragon": "wide", "hydra": "wide", "arachnid": "wide", "serpent": "low", "aquatic": "low", "insect": "low", "spirit": "float"}


def build_look(arch, rng):
    b = Build(rng)
    if arch.startswith("humanoid:"):
        humanoid(b, arch.split(":")[1])
    elif arch == "giant":
        humanoid(b, "brute")
    elif arch == "hydra":
        serpent(b, heads=rng.randint(3, 5))
    else:
        ARCH_FN[arch](b)
    return b


# ---------------------------------------------------------------- texture
def noise(seed, x, y):
    h = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    h = (h ^ (h >> 13)) * 1274126177 & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 255) / 255.0


def pack(parts):
    """Box-unwrap each cube (standard MC layout) onto a shelf-packed sheet; returns size and per-cube uv."""
    boxes = []
    for pi, p in enumerate(parts):
        for ci, c in enumerate(p.cubes):
            w, h, d = c[3] - c[0], c[4] - c[1], c[5] - c[2]
            boxes.append((pi, ci, 2 * d + 2 * w, d + h, w, h, d))
    for size in (64, 128, 256):
        x = y = rowh = 0
        ok, uv = True, {}
        for pi, ci, W, H, w, h, d in sorted(boxes, key=lambda t: -t[3]):
            if x + W > size:
                x, y, rowh = 0, y + rowh, 0
            if y + H > size:
                ok = False
                break
            uv[(pi, ci)] = (x, y, w, h, d)
            x += W
            rowh = max(rowh, H)
        if ok:
            return size, uv
    raise RuntimeError("texture sheet too small")


def palette(rng, soil, realm, text, name):
    base = SOIL_HUE.get(soil, 0.5)
    sat, val = 0.45, 0.55
    for rx, hue, v in WORD_HUE:
        if re.search(rx, f"{name} {text}", re.I):
            base, val = hue, v
            sat = 0.12 if v < 0.2 else 0.55
            break
    base = (base + rng.uniform(-.06, .06)) % 1
    sat = max(0.05, min(.9, sat + rng.uniform(-.12, .12)))
    val = max(.2, min(.85, val + rng.uniform(-.1, .1)))
    c = lambda h, s, v: tuple(int(255 * x) for x in colorsys.hsv_to_rgb(h % 1, max(0, min(1, s)), max(0, min(1, v))))
    acc = (base + rng.choice([.5, .33, .17, .08])) % 1
    eye = c(rng.choice([acc, 0.14, 0.0, 0.5, 0.33]), .85, 1.0)
    return dict(a=c(base, sat, val), b=c(base + .04, sat * .8, val * .65), acc=c(acc, .6, .85), eye=eye, hue=base), \
        dict(hue=round(base, 3), sat=round(sat, 2), val=round(val, 2))


PATTERNS = ["plain", "stripes", "spots", "scales", "plates", "cracks", "runes", "gradient"]


def shade(col, k):
    return tuple(max(0, min(255, int(v * k))) for v in col)


def mix(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3))


def paint(parts, size, uv, pal, pattern, seed, eyes):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    FACE_K = {"top": 1.18, "bottom": .55, "left": .8, "front": 1.0, "right": .8, "back": .88}
    for (pi, ci), (u, v, w, h, d) in uv.items():
        tag = parts[pi].cubes[ci][6]
        faces = {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "left": (u, v + d, d, h), "front": (u + d, v + d, w, h),
                 "right": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}
        ssd = seed + pi * 131 + ci * 17
        for fname, (fx, fy, fw, fh) in faces.items():
            k = FACE_K[fname]
            for yy in range(fh):
                for xx in range(fw):
                    X, Y = fx + xx, fy + yy
                    n = noise(ssd, X, Y)
                    col = pal["a"]
                    t = 0.0
                    if pattern == "stripes": t = 1.0 if (X + (Y >> 2)) % 6 < 2 else 0
                    elif pattern == "spots": t = 1.0 if noise(ssd + 1, X >> 1, Y >> 1) > .82 else 0
                    elif pattern == "scales": t = 1.0 if ((X + (Y & 1)) % 4 == 0 or Y % 4 == 0) else 0
                    elif pattern == "plates": t = 1.0 if (X % 8 == 0 or Y % 6 == 0) else 0
                    elif pattern == "cracks": t = 1.0 if noise(ssd + 2, X, Y >> 1) > .9 else 0
                    elif pattern == "runes": t = 1.0 if (noise(ssd + 3, X >> 1, Y >> 1) > .9 and (X + Y) % 3 != 0) else 0
                    elif pattern == "gradient": t = Y / max(1, fh)
                    base = mix(pal["a"], pal["b"], t * .8 if pattern == "gradient" else t * .65) if pattern != "runes" else (pal["acc"] if t else pal["a"])
                    if pattern == "spots" and t: base = pal["acc"]
                    c = shade(base, k * (0.9 + 0.2 * n))
                    if xx in (0, fw - 1) or yy in (0, fh - 1):
                        c = shade(c, .78)
                    if tag == "g":
                        c = mix(pal["acc"], (255, 255, 255), .35 + .3 * n)
                    elif tag == "l":
                        c = shade(mix(pal["a"], (60, 140, 60), .5), k * (.8 + .4 * n))
                    px[X, Y] = c + (255,)
        if tag == "h":  # face on the front of the head box
            fx, fy, fw, fh = faces["front"]
            ey = fy + max(1, fh // 3)
            ne = eyes
            if ne == 1:
                xs = [fx + fw // 2 - 1]
            elif ne == 2:
                xs = [fx + max(1, fw // 4), fx + fw - max(1, fw // 4) - 2]
            elif ne == 3:
                xs = [fx + max(1, fw // 5), fx + fw // 2 - 1, fx + fw - max(1, fw // 5) - 2]
            else:
                xs = [fx + 1 + i * max(2, (fw - 2) // ne) for i in range(ne)]
            for ex in xs:
                for dx in range(2):
                    for dy in range(1 if fh < 5 else 2):
                        if fx <= ex + dx < fx + fw and ey + dy < fy + fh:
                            px[ex + dx, ey + dy] = pal["eye"] + (255,)
            my = fy + fh - max(1, fh // 4) - 1
            for xx in range(fx + 1, fx + fw - 1, 1):
                if my < fy + fh and xx % 2 == 0:
                    px[xx, my] = shade(pal["b"], .45) + (255,)
    return img


# ---------------------------------------------------------------- templates
ATK = {
    "slam": dict(type="slam", telegraph=40, cooldown=100, radius=6, knockback=1.2),
    "charge": dict(type="charge", telegraph=36, cooldown=130, speed=1.5, knockback=1.0),
    "leap": dict(type="leap", telegraph=30, cooldown=120, radius=5, knockback=1.0),
    "bolt": dict(type="bolt", telegraph=30, cooldown=110, count=3, range=32),
    "summon": dict(type="summon", telegraph=40, cooldown=260, count=2, cap=6, minion="minecraft:zombie"),
    "lightning": dict(type="lightning", telegraph=40, cooldown=170, count=3, radius=5),
    "cloud": dict(type="cloud", telegraph=30, cooldown=200, radius=4, duration=160, effect="minecraft:poison", amp=0),
    "wave": dict(type="wave", telegraph=36, cooldown=150, radius=11, knockback=1.5),
    "heal": dict(type="heal", telegraph=50, cooldown=400, amount=0.05),
    "roar": dict(type="roar", telegraph=40, cooldown=220, radius=10, effect="minecraft:slowness", amp=1, duration=100),
}
ARCH_ATTACKS = {  # (ordered attacks, hp factor, damage factor, speed)
    "humanoid:knight": (["slam", "charge", "roar", "summon"], 1.0, 1.0, .3), "humanoid:mage": (["bolt", "lightning", "cloud", "heal"], .8, 1.1, .26),
    "humanoid:brute": (["slam", "charge", "wave", "roar"], 1.3, 1.1, .26), "humanoid:multiarm": (["slam", "leap", "bolt", "charge"], 1.0, 1.0, .32),
    "humanoid:winged": (["bolt", "lightning", "leap", "heal"], .9, 1.0, .3), "humanoid:king": (["summon", "slam", "roar", "wave"], 1.1, 1.0, .27),
    "humanoid:reaper": (["cloud", "bolt", "summon", "heal"], .8, 1.1, .28), "humanoid:fiend": (["leap", "charge", "bolt", "cloud"], 1.0, 1.1, .32),
    "giant": (["slam", "wave", "roar", "charge"], 1.5, 1.2, .24), "golem": (["slam", "wave", "bolt", "roar"], 1.6, 1.0, .22),
    "quadruped": (["charge", "leap", "roar", "slam"], 1.1, 1.1, .34), "serpent": (["charge", "cloud", "wave", "summon"], 1.0, 1.0, .3),
    "hydra": (["bolt", "cloud", "wave", "heal"], 1.5, 1.0, .26), "dragon": (["bolt", "wave", "leap", "lightning"], 1.5, 1.2, .3),
    "arachnid": (["leap", "cloud", "summon", "charge"], .9, 1.0, .34), "bird": (["leap", "bolt", "wave", "lightning"], .8, 1.0, .34),
    "insect": (["leap", "summon", "cloud", "charge"], .8, 1.0, .36), "spirit": (["bolt", "lightning", "cloud", "heal"], .8, 1.1, .26),
    "aquatic": (["wave", "charge", "slam", "cloud"], 1.2, 1.0, .28), "treant": (["slam", "summon", "cloud", "heal"], 1.5, 1.0, .2),
    "centaur": (["charge", "bolt", "leap", "roar"], 1.1, 1.0, .36),
}
BAR = {"Arcane": "purple", "Martial": "red", "Ki": "yellow", "Mythic": "blue", "Stellar": "white", "Grounded": "green", "Outer": "pink"}


def template(arch):
    ats, hf, df, sp = ARCH_ATTACKS[arch]
    attacks = {a: dict(ATK[a]) for a in ats}
    return dict(hp_factor=hf, damage_factor=df, speed=sp, attacks=attacks,
                phases=[dict(at=1.0, name="I", speed=.85, damage=1.0, cooldown=1.0, attacks=ats[:2]),
                        dict(at=.6, name="II", speed=.95, damage=1.15, cooldown=.85, attacks=ats[:3], invuln=30),
                        dict(at=.25, name="III", speed=1.1, damage=1.3, cooldown=.7, attacks=ats, invuln=30)])


# ---------------------------------------------------------------- emit
rites = defaultdict(list)
for ln in open(rites_tsv, encoding="utf8"):
    pid, rid, gate, rname, school = ln.rstrip("\n").split("\t")
    rites[(pid, int(gate))].append(rid)
used_rites = set()

os.makedirs(f"{out}/assets/diaithirbosses/looks", exist_ok=True)
os.makedirs(f"{out}/assets/diaithirbosses/textures/entity/planet", exist_ok=True)
os.makedirs(f"{out}/data/diaithirbosses", exist_ok=True)

def emit(bid, eid, arch, b, pal, pat, eyes):
    size, uv = pack(b.parts)
    seed = int(hashlib.sha256(eid.encode()).hexdigest()[:6], 16)
    paint(b.parts, size, uv, pal, pat, seed, eyes).save(f"{out}/assets/diaithirbosses/textures/entity/planet/{bid}.png", optimize=True)
    parts = []
    for pi, p in enumerate(b.parts):
        px, py, pf = p.pivot
        cubes = []
        for ci, c in enumerate(p.cubes):
            x0, y0, f0, x1, y1, f1, tag = c
            u, v, w, h, d = uv[(pi, ci)]
            cubes.append([x0 - px, round(py - y1, 2), round(pf - f1, 2), x1 - x0, y1 - y0, f1 - f0, u, v])
        parts.append(dict(n=p.name, o=[round(px, 2), round(24 - py, 2), round(-pf, 2)], r=[round(x, 3) for x in p.rot], c=cubes))
    json.dump(dict(id=bid, tex=size, arch=arch, parts=parts), open(f"{out}/assets/diaithirbosses/looks/{bid}.json", "w"), separators=(",", ":"))


sigs, bosses, rite_map, per_arch = {}, [], {}, defaultdict(int)
for R in roster:
    P, eid = R["P"], R["eid"]
    bid = re.sub(r"[^a-z0-9_]", "_", eid.lower())
    gate = tier_gate(P["opens"], R["tier"])
    r0 = rng_for("arch", eid)
    arch, how = classify(R["text"], R["name"], P["realm"], r0)
    for attempt in range(40):  # re-roll the look until its discrete signature is new
        rng = rng_for("look", eid, attempt)
        b = build_look(arch, rng)
        pal, pq = palette(rng, P["soil"], P["realm"], R["text"], R["name"])
        pat = rng.choice(PATTERNS)
        eyes = rng.choice([1, 2, 2, 2, 3, 4, 6])
        sig = (arch, tuple(b.feat), pat, int(pal["hue"] * 12), eyes, len(b.parts))
        if sig not in sigs:
            break
    sigs[sig] = bid
    emit(bid, eid, arch, b, pal, pat, eyes)
    top = max((c[4] for p in b.parts for c in p.cubes), default=32)
    hp = round((140 + 95 * gate) * ARCH_ATTACKS[arch][1])
    sc = round(0.9 + 0.12 * gate + rng.uniform(0, .25), 2)
    rl = rites.get((P["id"], gate), []) or [r for (pid, g), rs in rites.items() if pid == P["id"] for r in rs]
    rl = [x for x in rl if x not in used_rites] or rl
    rite = rl[int(rng_for("rite", eid).random() * len(rl))] if rl else None
    if rite:
        used_rites.add(rite)
        rite_map[rite] = bid
    per_arch[arch] += 1
    bosses.append(dict(id=bid, planet=P["id"], dimension=P["dim"], name=R["name"], entry=eid, tier=R["tier"], age=gate, arch=arch,
                       hitbox=HITBOX[arch.split(":")[0]], scale=sc, hp=hp, bar=BAR.get(P["realm"], "red"), rite=rite, pattern=pat,
                       eyes=eyes, source=how, height=round(top)))


# ---------------------------------------------------------------- Ascendant bosses (own profiles, same look system)
ASC = {"antlered_hollow": ("quadruped", "Hollow"), "cauldron_heart": ("golem", "Ember"), "tide_reaver": ("aquatic", "Storm"),
       "foundry_sovereign": ("giant", "Iron"), "mist_weaver": ("arachnid", "Quiet"), "ash_seraph": ("humanoid:winged", "Ember"),
       "dusk_wyrm": ("dragon", "Hollow"), "rootbreaker": ("treant", "Bloom"), "last_wheel": ("spirit", "Iron"),
       # the seven older own bosses and the mythic-court kits (each gets its own look instead of a vanilla body)
       "tribal_chief": ("humanoid:brute", "Grounded"), "stone_colossus": ("giant", "Hollow"), "bronze_warking": ("humanoid:king", "Ember"),
       "rot_swarm_lord": ("insect", "Bloom"), "iron_walker": ("golem", "Iron"), "steel_marshal": ("humanoid:multiarm", "Iron"),
       "ceramic_warlord": ("humanoid:knight", "Storm"), "botunkas": ("arachnid", "Bloom"), "driegunmorgil": ("dragon", "Frost"),
       "driegunsalund": ("serpent", "Storm"), "famuirmeirgi": ("golem", "Quiet"), "maerkistechuimni": ("spirit", "Quiet"),
       "maktirikriuch": ("quadruped", "Frost"), "natuirkriegach": ("insect", "Hollow"), "natuirmoir": ("aquatic", "Storm"),
       "natuirrig": ("serpent", "Bloom"), "natuirsaegul": ("aquatic", "Frost"), "natuirtuar": ("bird", "Quiet"),
       "niechgleididdouras": ("golem", "Storm"), "oilebeistbouglach": ("hydra", "Bloom"), "ridiriknok": ("humanoid:king", "Bloom"),
       "stoirmiduislach": ("spirit", "Storm"), "tarbiumachuoirtach": ("humanoid:fiend", "Ember"), "tarbspeiri": ("quadruped", "Iron"),
       "toimsechunleomund": ("quadruped", "Ember"), "toisechmarb": ("humanoid:reaper", "Storm")}
OLD_H = {"zombie": 1.95, "illager": 1.95, "golem": 2.7, "spider": 0.9, "ravager": 2.2, "blaze": 1.8, "silverfish": 0.3, "phantom": 0.5,
         "guardian": 0.85, "hoglin": 1.4}
for aid, (arch, soil) in ASC.items():
    pf = f"{out}/data/diaithirbosses/boss/{aid}.json"
    if not os.path.exists(pf):
        continue
    prof = json.load(open(pf))
    rng = rng_for("asc", aid)
    b = build_look(arch, rng)
    pal, _ = palette(rng, soil, "Mythic", prof.get("name_en") or aid, prof.get("name_en") or aid)
    emit(aid, aid, arch, b, pal, rng.choice(PATTERNS), rng.choice([2, 3, 4, 6]))
    old_kind = prof["own"]["kind"]
    if not old_kind.startswith("look_"):  # re-size: the vanilla body was another height, keep the boss about as tall
        want = max(OLD_H.get(old_kind, 2.0) * prof["own"]["scale"], 2.2)
        top = max(c[4] for p in b.parts for c in p.cubes) / 16
        prof["own"]["scale"] = round(max(0.8, min(4.5, want / top)), 2)
    prof["look"] = True
    prof["own"]["kind"] = "look_" + HITBOX[arch.split(":")[0]]
    json.dump(prof, open(pf, "w"), indent=1, ensure_ascii=False)
    print("ascendant", aid, arch, prof["own"]["kind"])

json.dump(bosses, open(f"{out}/data/diaithirbosses/planet_bosses.json", "w"), separators=(",", ":"), ensure_ascii=False)
json.dump({a: template(a) for a in ARCH_ATTACKS}, open(f"{out}/data/diaithirbosses/planet_templates.json", "w"), indent=1)
json.dump(rite_map, open(f"{out}/data/diaithirbosses/rite_bosses.json", "w"), separators=(",", ":"))

per_planet = defaultdict(int)
for x in bosses: per_planet[x["planet"]] += 1
rep = ["# Planet bosses report", "", f"{len(bosses)} bosses on {len(per_planet)} planets (min {min(per_planet.values())}, max {max(per_planet.values())} per planet).",
       f"Distinct discrete look signatures: {len(sigs)} of {len(bosses)}. Rites tied: {len(rite_map)} distinct rites.", "", "| body plan | bosses |", "|---|---|"]
rep += [f"| {a} | {n} |" for a, n in sorted(per_arch.items(), key=lambda t: -t[1])]
open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "planet_bosses_report.md"), "w").write("\n".join(rep) + "\n")
print("\n".join(rep))
