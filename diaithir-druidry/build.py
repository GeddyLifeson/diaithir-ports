#!/usr/bin/env python3
"""Zip src/main/resources into build/diaithir_druidry-<version>.jar (data-only mod, lowcodefml)."""
import os, sys, zipfile
root = os.path.dirname(os.path.abspath(__file__))
ver = sys.argv[1] if len(sys.argv) > 1 else "0.1.0"
res = os.path.join(root, "src", "main", "resources")
out = os.path.join(root, "build"); os.makedirs(out, exist_ok=True)
jar = os.path.join(out, f"diaithir_druidry-{ver}.jar")
with zipfile.ZipFile(jar, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n")
    for d, _, fs in os.walk(res):
        for f in sorted(fs):
            p = os.path.join(d, f); rel = os.path.relpath(p, res).replace(os.sep, "/")
            data = open(p, "rb").read()
            if rel == "META-INF/neoforge.mods.toml":
                data = data.replace(b"${version}", ver.encode())
            z.writestr(rel, data)
    z.write(os.path.join(root, "LICENSE"), "LICENSE")
print(jar)
