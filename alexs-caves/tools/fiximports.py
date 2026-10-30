#!/usr/bin/env python3
"""Rewrite imports whose class moved in 26.1: index simple name -> FQN from MC + NeoForge sources."""
import re, sys, pathlib, collections
roots=['/home/user/src/mc26','/home/user/src/nf26']
idx=collections.defaultdict(set); fq=set()
for r in roots:
    for p in pathlib.Path(r).rglob('*.java'):
        rel=str(p.relative_to(r))[:-5].replace('/','.')
        fq.add(rel); idx[rel.rsplit('.',1)[1]].add(rel)
# nested classes: add Outer.Inner by scanning is skipped; imports of nested resolved via outer
def ok(fqn):
    if fqn in fq: return True
    parts=fqn.split('.')
    for i in range(len(parts)-1,1,-1):
        if '.'.join(parts[:i]) in fq: return True
    return False
n=0; unresolved=collections.Counter()
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    def f(m):
        st,fqn=m.group(1),m.group(2)
        if not (fqn.startswith('net.minecraft') or fqn.startswith('net.neoforged')): return m.group(0)
        if ok(fqn): return m.group(0)
        simple=fqn.rsplit('.',1)[1]
        c=idx.get(simple,set())
        if len(c)==1: return 'import %s%s;'%(st or '',next(iter(c)))
        if len(c)>1:
            # prefer longest common package prefix
            best=max(c,key=lambda x:len(__import__('os').path.commonprefix([x,fqn])))
            return 'import %s%s;'%(st or '',best)
        unresolved[fqn]+=1
        return m.group(0)
    s=re.sub(r'import (static )?([\w.]+);',lambda m:f(m) if not m.group(1) else m.group(0),s)
    if s!=o: p.write_text(s); n+=1
print(n,'files fixed'); 
for k,v in unresolved.most_common(60): print(v,k)
