#!/usr/bin/env python3
"""(S6902) Harvests every US and UK locomotive class on Wikipedia (via Wikidata class list) into data/locos_raw.csv.
Usage: python3 tools/harvest_locos.py CACHE_DIR   (CACHE_DIR keeps raw wikitext so reruns are cheap)
Keeps only standard gauge (1435 mm) and narrow gauge (500-1067 mm except metre gauge, folded onto one game narrow gauge). Skips other gauges."""
import csv, json, os, re, sys, time, urllib.parse, urllib.request
from concurrent.futures import ThreadPoolExecutor
UA = {"User-Agent": "diaithir-trains/1.0 (game data harvest)"}
Q = """SELECT ?i ?art WHERE { ?i wdt:P31 wd:Q19832486 . { ?i wdt:P495 ?c } UNION { ?i wdt:P17 ?c }
 FILTER(?c IN (wd:Q30, wd:Q145)) ?art schema:about ?i ; schema:isPartOf <https://en.wikipedia.org/> . }"""
def get(url):
    for k in range(4):
        try:
            return urllib.request.urlopen(urllib.request.Request(url, headers=UA), timeout=60).read().decode()
        except Exception:
            time.sleep(2 + 3 * k)
    return ""
def titles():
    u = "https://query.wikidata.org/sparql?format=json&query=" + urllib.parse.quote(Q)
    return sorted({urllib.parse.unquote(b["art"]["value"].rsplit("/wiki/", 1)[1]) for b in json.loads(get(u))["results"]["bindings"]})
def raw(t, cache):
    f = os.path.join(cache, re.sub(r"[^A-Za-z0-9_.-]", "_", t)[:150] + ".txt")
    if not os.path.exists(f):
        txt = get("https://en.wikipedia.org/w/index.php?title=" + urllib.parse.quote(t) + "&action=raw")
        open(f, "w").write(txt); time.sleep(0.4)
    return open(f).read()
def field(txt, name):
    m = re.search(r"^\s*\|\s*" + name + r"\s*=(.*?)(?=^\s*\|\s*\w+\s*=|\n\}\})", txt, re.S | re.M)
    return m.group(1).strip() if m else ""
def gauge_mm(g):
    g = g.lower()
    if re.search(r"ussg|\bsg\b|standard|1,?435", g): return 1435
    m = re.search(r"track gauge\|(\d+)ft(\d*)(?:\+?(\d)/(\d))?", g.replace(" ", ""))
    if m:
        ft = int(m.group(1)); inch = int(m.group(2) or 0)
        return round((ft * 12 + inch) * 25.4)
    m = re.search(r"(\d[\d,]*)\s*mm", g)
    return int(m.group(1).replace(",", "")) if m else 0
def num(s):
    s = re.sub(r"<[^>]+>|\{\{.*?\}\}", " ", s)
    m = re.findall(r"\d[\d,]*\.?\d*", s)
    return float(m[0].replace(",", "")) if m else 0
def weight_kg(w):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(lb|long ton|LT|ST|short ton|t|tonnes?|kg)\b", w)
    if not m: return 0
    v = float(m.group(1).replace(",", "")); u = m.group(2).lower()
    return round(v * {"lb": .45359, "long ton": 1016, "lt": 1016, "st": 907, "short ton": 907}.get(u, 1000 if u.startswith("t") else 1))
def power_w(p):
    m = re.search(r"([\d,\.]+)\s*(?:\|)?\s*(hp|bhp|ihp|kw|mw|shp)\b", p, re.I)
    if not m: return 0
    v = float(m.group(1).replace(",", "")); u = m.group(2).lower()
    return round(v * {"kw": 1000, "mw": 1e6}.get(u, 745.7))
def year(*fs):
    for f in fs:
        m = re.search(r"\b(1[5-9]\d\d|20[0-2]\d)\b", f)
        if m: return int(m.group(1))
    return 0
def parse(t, txt):
    if "nfobox locomotive" not in txt and "nfobox rail" not in txt: return None
    pt = field(txt, "powertype").lower()
    kind = "loco_steam" if "steam" in pt else "loco_electric" if ("electric" in pt or "battery" in pt) else "loco_diesel" if ("diesel" in pt or "petrol" in pt or "gas" in pt or "turbine" in pt) else ""
    if not kind: return None
    gm = gauge_mm(field(txt, "gauge"))
    gauge = "standard" if gm == 1435 else "narrow" if 500 <= gm <= 1067 and not 950 <= gm <= 1050 else ""   # metre gauge was never a US or UK gauge: skipped
    if not gauge: return None
    name = re.sub(r"\s*\(.*?\)$", "", t.replace("_", " "))
    y = year(field(txt, "introduced"), field(txt, "builddate"), field(txt, "inservicedate"), field(txt, "firstrun"))
    mass = weight_kg(field(txt, "locoweight") or field(txt, "weight") or field(txt, "locotenderweight"))
    pw = power_w(field(txt, "poweroutput"))
    return [t, kind, name, y, mass, pw, gauge, gm, "https://en.wikipedia.org/wiki/" + t]
if __name__ == "__main__":
    cache = sys.argv[1]; os.makedirs(cache, exist_ok=True)
    ts = titles(); print(len(ts), "titles", flush=True)
    with ThreadPoolExecutor(3) as ex: texts = list(ex.map(lambda t: raw(t, cache), ts))
    rows = [r for r in (parse(t, x) for t, x in zip(ts, texts)) if r]
    w = csv.writer(open("data/locos_raw.csv", "w", newline="")); w.writerow(["page", "category", "name", "year", "mass_kg", "power_w", "gauge", "gauge_mm", "source"]); w.writerows(rows)
    print(len(rows), "locos kept")
