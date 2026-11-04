#!/usr/bin/env python3
"""Checks data/rolling_stock.csv against the full Wikipedia article text, slowly, with backoff (the shared cloud proxy is rate-limited, so run this from the PC).
For each page it checks that the first-service year appears in the article and, for locomotives, that a power figure within a factor of 2 appears.
Writes data/roster_verify.csv (id,page,status,notes). Usage: python3 -I tools/verify_roster.py   (about 6 s per page)"""
import csv, json, re, time, urllib.parse, urllib.request
rows = list(csv.DictReader(open("data/rolling_stock.csv")))
cache = {}

def text(page):
    for a in range(8):
        try:
            u = "https://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=1&format=json&redirects=1&titles=" + urllib.parse.quote(urllib.parse.unquote(page))
            req = urllib.request.Request(u, headers={"User-Agent": "diaithir-roster-check/1.0"})
            d = json.load(urllib.request.urlopen(req, timeout=30))
            return next(iter(d["query"]["pages"].values())).get("extract", "")
        except urllib.error.HTTPError as e:
            if e.code != 429: return None
            time.sleep(30 * (a + 1))
        except Exception:
            time.sleep(10)
    return None

out = []
for r in rows:
    page = r["source"].split("/wiki/")[1]
    if page not in cache:
        cache[page] = text(page); time.sleep(6)
    t = cache[page]
    if t is None: out.append((r["id"], page, "fetch-failed", "")); continue
    notes = []
    y = abs(int(r["year"]))
    if not re.search(r"\b%d\b" % y, t): notes.append(f"year {r['year']} not in article")
    if r["category"].startswith("loco"):
        kw = int(r["power_w_or_capacity"]) / 1000
        nums = [float(n.replace(",", "")) for n in re.findall(r"([\d,]+(?:\.\d+)?)\s*(?:kW|kilowatt)", t)] + [float(n.replace(",", "")) * 0.7457 for n in re.findall(r"([\d,]+(?:\.\d+)?)\s*(?:hp|horsepower)", t)]
        if kw and nums and not any(0.5 < kw / n < 2 for n in nums if n): notes.append(f"power {kw:.0f} kW not near any figure in article")
    out.append((r["id"], page, "ok" if not notes else "check", "; ".join(notes)))
csv.writer(open("data/roster_verify.csv", "w", newline="")).writerows([("id", "page", "status", "notes")] + out)
print(sum(1 for o in out if o[2] == "ok"), "ok of", len(out))
