#!/usr/bin/env python3 -I
"""Point every self-hosted metafile URL at one release branch (checklist B6, C2).
Usage: python3 -I repoint_pack_urls.py <pack_root> <branch>   (e.g. claude/pack-v1-3-wave17)
Each URL's file must exist at the same path in this tree and match the metafile sha256, else it is reported and left alone."""
import glob, hashlib, re, sys
root, branch = sys.argv[1:3]
pat = re.compile(r'(https://raw\.githubusercontent\.com/GeddyLifeson/diaithir-ports/refs/heads/)(claude/[^/]+)/([^"\s]+)')
bad = 0
for f in glob.glob(f"{root}/**/*.pw.toml", recursive=True):
    t = open(f, encoding="utf8").read()
    def fix(m):
        global bad
        path = m.group(3)
        try:
            h = hashlib.sha256(open(f"{root}/{path}", "rb").read()).hexdigest()
        except OSError:
            h = None
        if h is None or h not in t:
            print("SKIP", f, m.group(2), path); bad += 1
            return m.group(0)
        return f"{m.group(1)}{branch}/{path}"
    n = pat.sub(fix, t)
    if n != t:
        open(f, "w", encoding="utf8", newline="").write(n)
sys.exit(1 if bad else 0)
