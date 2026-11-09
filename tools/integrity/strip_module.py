"""Remove one folded mod from a suite jar so the mod can ship as its original linked jar.

Usage: python3 -I tools/integrity/strip_module.py <suite.jar> <original.jar> <modId> <out.jar> [keep_ns ...]

Removes from the suite: every entry whose path exists byte-identical in the original; every
entry under a namespace the original owns (assets/<ns>, data/<ns>, kubejs-free); the
[[mods]], [[dependencies.<modId>]] and [[mixins]] blocks of that mod in neoforge.mods.toml;
the licences/<dir> folder; the original's lines in merged text files (services, access
transformer). Prints what it removed that the original did not contain, for review.
"""
import re
import sys
import zipfile
from collections import Counter

suite, orig, mod_id, out = sys.argv[1:5]
keep_ns = set(sys.argv[5:])
SHARED = {"minecraft", "c", "neoforge", "forge", "fabric", "common"} | keep_ns

zs, zo = zipfile.ZipFile(suite), zipfile.ZipFile(orig)
o_crc = {i.filename: i.CRC for i in zo.infolist() if not i.is_dir()}
owned = set()
for n in o_crc:
    p = n.split("/")
    if len(p) > 2 and p[0] in ("assets", "data") and p[1] not in SHARED:
        owned.add(p[1])
mixin_cfgs = {n for n in o_crc if n.endswith(".mixins.json") and "/" not in n}
o_licence_key = re.sub(r"[^a-z0-9]", "", mod_id.lower())


def merged_text(name):
    return name.startswith("META-INF/services/") or name == "META-INF/accesstransformer.cfg"


removed, extra, edited = Counter(), Counter(), []
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as zw:
    for i in zs.infolist():
        n = i.filename
        if i.is_dir():
            continue
        data = zs.read(n)
        p = n.split("/")
        if n in o_crc and o_crc[n] == i.CRC and n != "META-INF/MANIFEST.MF":
            removed["identical"] += 1
            continue
        if len(p) > 2 and p[0] in ("assets", "data") and p[1] in owned:
            removed["owned-namespace"] += 1
            if n not in o_crc:
                extra[p[1]] += 1
            continue
        if p[0] == "licenses" and len(p) > 2 and o_licence_key in re.sub(r"[^a-z0-9]", "", p[1].lower()):
            removed["licence-dir"] += 1
            continue
        if n in mixin_cfgs:
            removed["mixin-config"] += 1
            continue
        if n in o_crc and merged_text(n):
            theirs = set(zo.read(n).decode("utf8", "ignore").splitlines())
            lines = [l for l in data.decode("utf8", "ignore").splitlines() if l not in theirs]
            edited.append(n)
            if not [l for l in lines if l.strip()]:
                removed["merged-text-empty"] += 1
                continue
            data = ("\n".join(lines) + "\n").encode()
        if n == "META-INF/neoforge.mods.toml":
            t = data.decode("utf8")
            blocks = re.split(r"(?m)^(?=\[\[)", t)
            keep = []
            for b in blocks:
                m0 = re.match(r"\[\[([^\]]+)\]\]", b)
                head = f"[[{m0.group(1)}]]" if m0 else ""
                if head == "[[mods]]" and re.search(r'(?m)^\s*modId\s*=\s*"%s"' % re.escape(mod_id), b):
                    removed["mods-block"] += 1
                    continue
                if head == f"[[dependencies.{mod_id}]]":
                    removed["dep-block"] += 1
                    continue
                if head == "[[mixins]]":
                    m = re.search(r'config\s*=\s*"([^"]+)"', b)
                    if m and m.group(1) in mixin_cfgs:
                        removed["mixins-block"] += 1
                        continue
                keep.append(b)
            data = "".join(keep).encode()
        zw.writestr(zipfile.ZipInfo(n, date_time=i.date_time), data, zipfile.ZIP_DEFLATED, 9)

print("removed", dict(removed))
print("edited merged files", edited)
print("owned-namespace entries the original does not contain (review):", dict(extra))
