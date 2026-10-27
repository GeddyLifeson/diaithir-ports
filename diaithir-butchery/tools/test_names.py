"""Every Diaithiris name and compound part must be absent from modern Goidelic spellings (archaic_map keys) and Old Irish (oldirish_known.txt).
Run: python3 -I tools/test_names.py   (reference files: DIAITHIRIS_DIR, default /mnt/project-files/diaithiris-archaic)"""
import json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import names

root = os.environ.get("DIAITHIRIS_DIR", "/mnt/project-files/diaithiris-archaic")
modern = {k.lower() for k in json.load(open(f"{root}/archaic_map.json"))}
old = set(open(f"{root}/tools/oldirish_known.txt").read().lower().split())
# parts that make up the compounds, each also checked on its own
PARTS = "ròstad bòird bloka dietach teigi feòili phrund salund tiurum staein dabach fàsu falumachd bougsa kùis ùini seòmur fuor poiti moilend bleid tunda kliad stud skian feòlad duir dubun kùl maed foil sierrag smiur knàim fèit lùtaid kas ein tork loisni giel lauma kàmul bòi balgun buochuir iask iumadat sligi seòlad".split()
words = {w for v in list(names.NAMES.values()) + list(names.SPECIES.values()) + [names.COOKED, names.CONCEPT] + list(names.SIZES.values()) + list(names.CLASS.values())
         for w in v.split()} | set(PARTS)
bad = [w for w in sorted(words) if w.lower() in modern or w.lower() in old]
allnames = [names.item(n) for n in list(names.NAMES) + ["cooked_" + c for c in names.NAMES if c in ("haunch", "shoulder", "loin", "ribs", "flank")]]
dup = {n for n in allnames if allnames.count(n) > 1}
if bad or dup:
    sys.exit(f"modern or Old Irish forms: {bad}; duplicates: {sorted(dup)}")
print("names ok:", len(words), "words")
