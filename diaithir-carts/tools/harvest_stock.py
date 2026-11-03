#!/usr/bin/env python3
"""(S6902) Harvests every locomotive class, multiple unit, railcar, tram, coach and wagon on Wikipedia that runs on standard gauge (1435 mm)
or narrow gauge (500-1067 mm except metre gauge, folded onto the one game narrow gauge), any country, into data/stock_raw.csv with the spec
fields the yards need (wheel arrangement, cylinders, valve gear, boiler pressure, fuel, drivers, prime mover, transmission, role).
Lists: Wikidata classes + Wikipedia category pages (HTML, depth 2). Articles: raw wikitext. Skips other gauges. Resumable: CACHE_DIR keeps every page.
Usage: python3 tools/harvest_stock.py CACHE_DIR [THREADS]"""
import csv, html, json, os, re, sys, time, urllib.parse, urllib.request
from concurrent.futures import ThreadPoolExecutor
UA = {"User-Agent": "diaithir-trains/1.0 (game data harvest)"}
WD_CLASSES = {"Q19832486": "loco", "Q811704": "unit", "Q76154857": "tram", "Q63040754": "mu", "Q3959904": "mu", "Q752392": "mu", "Q208502": "freight",
              "Q753779": "passenger", "Q483373": "mu", "Q1567915": "mu", "Q16814621": "passenger", "Q15825863": "tram", "Q9166501": "freight"}
SEED_CATS = {"Freight_rolling_stock": "freight", "Passenger_railroad_cars": "passenger", "Tram_vehicles": "tram", "Diesel_multiple_units": "mu",
             "Electric_multiple_units": "mu", "Railcars": "mu", "Tank_locomotives": "loco", "Pullman_Company": "passenger",
             "Locomotives_by_wheel_arrangement": "loco", "Steam_locomotives_by_wheel_arrangement": "loco", "Narrow_gauge_steam_locomotives": "loco",
             "Narrow_gauge_diesel_locomotives": "loco", "Industrial_locomotives_by_country": "loco", "Logging_locomotives_of_the_United_States": "loco",
             "Railway_freight_wagons_of_the_United_Kingdom": "freight", "Passenger_railway_vehicles_of_the_United_Kingdom": "passenger",
             "British_Rail_coaching_stock": "passenger", "Wagons_of_the_United_Kingdom": "freight", "Railroad_cars_of_the_United_States": "freight"}
def get(url, tries=4):
    for k in range(tries):
        try: return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60).read().decode()
        except Exception as e:
            if "404" in str(e): return ""
            time.sleep(3 + 5 * k)
    return ""
def wikidata():
    out = {}
    for q, hint in WD_CLASSES.items():
        Q = "SELECT ?art WHERE { ?i wdt:P31 wd:%s . ?art schema:about ?i ; schema:isPartOf <https://en.wikipedia.org/> . }" % q
        for b in json.loads(get("https://query.wikidata.org/sparql?format=json&query=" + urllib.parse.quote(Q)) or '{"results":{"bindings":[]}}')["results"]["bindings"]:
            out.setdefault(urllib.parse.unquote(b["art"]["value"].rsplit("/wiki/", 1)[1]), hint)
    return out
def category(c, hint, depth, seen, out):
    if c in seen or depth < 0: return
    seen.add(c); print('cat', c, len(out), flush=True); url = "https://en.wikipedia.org/wiki/Category:" + c
    while url:
        page = get(url); time.sleep(1.0)
        if not page: return
        sub = re.search(r'id="mw-subcategories"(.*?)(?:id="mw-pages"|printfooter)', page, re.S)
        for s in re.findall(r'href="/wiki/Category:([^"#?]+)"', sub.group(1) if sub else ""):
            category(urllib.parse.unquote(s), hint, depth - 1, seen, out)
        pg = re.search(r'id="mw-pages"(.*?)(?:printfooter|id="catlinks")', page, re.S)
        if not pg: return
        for t in re.findall(r'<li><a href="/wiki/([^"#?]+)"', pg.group(1)):
            if ":" not in urllib.parse.unquote(t): out.setdefault(urllib.parse.unquote(t), hint)
        nxt = re.search(r'<a href="(/w/index\.php\?title=Category:[^"]*pagefrom=[^"]*)"[^>]*>next page', pg.group(1))
        url = "https://en.wikipedia.org" + html.unescape(nxt.group(1)) if nxt else ""
def raw(t, cache):
    f = os.path.join(cache, re.sub(r"[^A-Za-z0-9_.-]", "_", t)[:150] + ".txt")
    if not os.path.exists(f):
        txt = get("https://en.wikipedia.org/w/index.php?title=" + urllib.parse.quote(t) + "&action=raw"); time.sleep(0.4)
        if txt: open(f, "w").write(txt)
        return txt
    return open(f).read()
def field(txt, *names):
    for name in names:
        m = re.search(r"^\s*\|\s*" + name + r"\s*=(.*?)(?=^\s*\|\s*\w[\w ]*=|\n\}\})", txt, re.S | re.M | re.I)
        if m and m.group(1).strip(): return m.group(1).strip()
    return ""
def clean(s): return re.sub(r"\s+", " ", re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]*)\]\]|<[^>]+>|\{\{[^}]*\}\}|'''?", r"\1", s)).strip()
def gauge_mm(g):
    g = g.lower()
    if re.search(r"ussg|\bsg\b|standard|1,?435", g): return 1435
    m = re.search(r"track gauge\|(\d+)ft(\d*)", g.replace(" ", ""))
    if m: return round((int(m.group(1)) * 12 + int(m.group(2) or 0)) * 25.4)
    m = re.search(r"track gauge\|(\d[\d,]*)\s*mm", g)
    if m: return int(m.group(1).replace(",", ""))
    m = re.search(r"(\d[\d,]*)\s*mm", g)
    return int(m.group(1).replace(",", "")) if m else 0
def weight_kg(w):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(lb|long ton|LT|ST|short ton|t|tonnes?|tons?|kg)\b", w)
    if not m: return 0
    v = float(m.group(1).replace(",", "")); u = m.group(2).lower()
    return round(v * {"lb": .45359, "long ton": 1016, "lt": 1016, "st": 907, "short ton": 907}.get(u, 1000 if u.startswith("t") else 1))
def power_w(p):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(hp|bhp|ihp|kw|mw|shp)\b", p, re.I)
    if not m: return 0
    return round(float(m.group(1).replace(",", "")) * {"kw": 1000, "mw": 1e6}.get(m.group(2).lower(), 745.7))
def year(txt, ib):
    for f in ("builddate", "build date", "introduced", "inservicedate", "firstrun", "manufactured", "built", "serviceentry", "service"):
        ys = re.findall(r"\b(1[7-9]\d\d|20[0-2]\d)\b", field(txt, f))
        if ys: return min(map(int, ys))   # earliest date: a class is dated by its first build
    m = re.search(r"\b(1[7-9]\d\d|20[0-2]\d)\b", ib)   # any date in the infobox
    if m: return int(m.group(1))
    lead = re.split(r"\n==", txt, 1)[0]
    m = re.search(r"\b(1[7-9]\d\d|20[0-2]\d)\b", clean(lead[lead.find("}}\n") + 3:] if "}}\n" in lead else lead))   # first date in the article's lead
    return int(m.group(1)) if m else 0
def psi_bar(s):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(lb/in2|psi|lbf/in2|bar|MPa|kgf/cm)", s, re.I)
    if not m: return 0
    v = float(m.group(1).replace(",", "")); u = m.group(2).lower()
    return round(v * (0.0689476 if u in ("lb/in2", "psi", "lbf/in2") else 10 if u == "mpa" else 0.980665 if u.startswith("kgf") else 1), 1)
def mm(s):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(in|mm|ft|m)\b", s)
    if not m: return 0
    v = float(m.group(1).replace(",", "")); return round(v * {"in": 25.4, "ft": 304.8, "m": 1000}.get(m.group(2), 1))
def infobox(txt):
    m = re.search(r"\{\{\s*Infobox\s+(locomotive|rolling stock|rail vehicle|train|tram|railcar|multiple unit|rail car|rail transport|railway vehicle)", txt, re.I)
    return txt[m.start():] if m else ""
def parse(t, txt, hint):
    ib = infobox(txt)
    if not ib: return None
    pt = clean(field(ib, "powertype", "power type", "traction")).lower()
    whyte = clean(field(ib, "whytetype", "wheelarrangement", "uicclass", "wheel arrangement"))
    if "locomotive" in ib[:40].lower() or hint == "loco":
        cat = "loco_steam" if "steam" in pt else "loco_diesel" if re.search(r"diesel|petrol|gas|turbine|internal|hydraulic", pt) else "loco_electric" if re.search(r"electric|battery|overhead|third rail", pt) else ""
        if not cat and re.search(r"\d-\d-\d", whyte): cat = "loco_steam"
    elif hint in ("mu", "tram") or re.search(r"multiple unit|railcar|tram", ib[:60].lower()) or pt:
        cat = "mu"
    else:
        cat = "passenger" if hint == "passenger" or field(ib, "seating", "capacity", "seats") else "freight"
    if not cat: return None
    g = gauge_mm(field(ib, "gauge", "track gauge", "gauges"))
    gauge = "standard" if g == 1435 else "narrow" if 500 <= g <= 1067 and not 950 <= g <= 1050 else ""
    if not gauge: return None
    name = re.sub(r"\s*\(.*?\)$", "", t.replace("_", " "))
    mass = weight_kg(field(ib, "locoweight", "weight", "unitweight", "mass", "tareweight", "emptyweight", "locotenderweight"))
    cap = int(re.sub(r"[^\d]", "", re.search(r"[\d,]+", field(ib, "seating", "capacity", "seats", "carryingcapacity", "payload", "loadlimit") or "0").group(0) or 0) or 0)
    pw = power_w(field(ib, "poweroutput", "power", "powerout", "continuouspower"))
    role = clean(field(ib, "type")).lower()[:40]
    row = dict(page=t, category=cat, name=name, year=year(txt, ib), mass_kg=mass, power_w=pw if cat != "passenger" and cat != "freight" else cap, gauge=gauge, gauge_mm=g,
               source="https://en.wikipedia.org/wiki/" + t, wheel=re.sub(r"\s+", "", whyte)[:24], cylinders=int(num(field(ib, "cylindercount", "cylinders")) or 0),
               valve_gear=clean(field(ib, "valvegear", "valve gear"))[:30], boiler_bar=psi_bar(field(ib, "boilerpressure", "boiler pressure")), fuel=clean(field(ib, "fueltype", "fuel type"))[:20].lower(),
               driver_mm=mm(field(ib, "driverdiameter", "driver diameter")), prime_mover=clean(field(ib, "primemover", "prime mover", "enginetype"))[:40],
               transmission=clean(field(ib, "transmission"))[:40], role=role)
    return row
def num(s):
    m = re.findall(r"\d[\d,]*\.?\d*", re.sub(r"<[^>]+>|\{\{.*?\}\}", " ", s)); return float(m[0].replace(",", "")) if m else 0
COLS = "page category name year mass_kg power_w gauge gauge_mm source wheel cylinders valve_gear boiler_bar fuel driver_mm prime_mover transmission role".split()
if __name__ == "__main__":
    cache = sys.argv[1]; threads = int(sys.argv[2]) if len(sys.argv) > 2 else 6; os.makedirs(cache, exist_ok=True)
    lst = wikidata(); print(len(lst), "wikidata pages", flush=True)
    seen = set()
    with ThreadPoolExecutor(6) as ex: list(ex.map(lambda ch: category(ch[0], ch[1], 2, seen, lst), SEED_CATS.items()))
    print(len(lst), "pages with categories", flush=True)
    ts = sorted(lst)
    with ThreadPoolExecutor(threads) as ex: texts = list(ex.map(lambda t: raw(t, cache), ts))
    rows = [r for r in (parse(t, x, lst[t]) for t, x in zip(ts, texts)) if r]
    w = csv.DictWriter(open("data/stock_raw.csv", "w", newline=""), COLS); w.writeheader(); w.writerows(rows)
    print(len(rows), "kept;", sum(1 for r in rows if r["year"] == 0), "with no date")
