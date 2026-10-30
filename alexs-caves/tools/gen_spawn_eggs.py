#!/usr/bin/env python3
"""Spawn egg textures/models for 26.1 (per-entity item textures, no tinting): recolours vanilla allay egg shading with AC colours."""
import re, json, pathlib, zipfile, io
from PIL import Image
JAR='/home/user/client/libraries/net/neoforged/minecraft-client-patched/26.1.2.116/minecraft-client-patched-26.1.2.116.jar'
base=Image.open(io.BytesIO(zipfile.ZipFile(JAR).read('assets/minecraft/textures/item/ghast_spawn_egg.png'))).convert('RGBA')
R=pathlib.Path('src/main/resources/assets/alexscaves')
src=open('src/main/java/com/github/alexmodguy/alexscaves/server/item/ACItemRegistry.java').read()
eggs=re.findall(r'spawnEgg\("(\w+)", [\w.]+, 0X([0-9A-Fa-f]{6}), 0X([0-9A-Fa-f]{6})',src)
def lum(p): return 0.3*p[0]+0.59*p[1]+0.11*p[2]
px=base.load(); W,H=base.size
SPOTS={(6,5),(7,5),(6,6),(7,6),(10,8),(11,8),(10,9),(8,11),(9,11),(8,12),(5,9),(5,10),(6,10),(9,4),(10,4)}
cls={}
for y in range(H):
    for x in range(W):
        p=px[x,y]
        if p[3]==0: continue
        cls[(x,y)]='spot' if (x,y) in SPOTS else 'body'
mean={k:sum(lum(px[c]) for c,v in cls.items() if v==k)/max(1,sum(1 for v in cls.values() if v==k)) for k in ('spot','body')}
for name,c1,c2 in eggs:
    cols={'body':tuple(int(c1[i:i+2],16) for i in (0,2,4)),'spot':tuple(int(c2[i:i+2],16) for i in (0,2,4))}
    out=Image.new('RGBA',base.size,(0,0,0,0)); o=out.load()
    for (x,y),k in cls.items():
        f=lum(px[x,y])/mean[k]
        o[x,y]=tuple(max(0,min(255,int(v*f))) for v in cols[k])+(255,)
    out.save(R/'textures/item'/('spawn_egg_%s.png'%name))
    (R/'models/item'/('spawn_egg_%s.json'%name)).write_text(json.dumps({"parent":"minecraft:item/generated","textures":{"layer0":"alexscaves:item/spawn_egg_"+name}},indent=2))
print(len(eggs),'eggs')
