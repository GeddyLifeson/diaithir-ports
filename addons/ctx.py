#!/usr/bin/env python3
"""ctx.py ADDON [file-substring ...] : errors with source line and symbol info"""
import re, sys
addon=sys.argv[1]; filt=sys.argv[2:]
log=open(f'/tmp/build_{addon}.log').read()
base=f'/home/user/work-1a/addons/{addon}/src/main/java/'
seen=set()
for m in re.finditer(r'^(com/[\w/]+\.java):(\d+): error: (.*)\n((?:.*\n)?(?:.*\n)?(?:  symbol:.*\n)?(?:  location:.*\n)?)', log, re.M):
    f,l,msg,rest=m.groups()
    if filt and not any(x in f for x in filt): continue
    key=(f,l,msg)
    if key in seen: continue
    seen.add(key)
    try:
        lines=open(base+f).read().split('\n'); src=lines[int(l)-1].strip()[:150]
        if 'does not override' in msg: src+=' || '+lines[int(l)].strip()[:150]
    except Exception: src='?'
    sym=re.search(r'symbol:\s*(.*)',rest)
    print(f"{f.split('/')[-1]}:{l}: {msg} {('['+sym.group(1).strip()+']') if sym else ''}\n    {src}")
