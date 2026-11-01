#!/usr/bin/env python3
"""Structured stubs + compute job files for cave mob / biome / structure Library entries (text is written by the API lane, not here).
Names come from tools/diaithiris_names output (names.json). Writes /mnt/project-files/compute/jobs/caves_<kind>/{in.jsonl,README,merge.py}."""
import json,glob,os,re,sys
names=json.load(open('diaithiris_names.json'))
J='/mnt/project-files/compute/jobs/'
R='src/main/resources/data/alexscaves/'
SYS="You write 1 to 3 plain sentences for an in-game encyclopedia in a Minecraft RPG set on a 1:5 Earth across seven Ages, with Celtic and Irish myth flavour. Use only the facts given and keep every number. No em dashes, no real people, no franchise names, no invented gods. Output only the sentences, no quotes."
title=lambda s:s.replace('_',' ').title()
bio={}
for f in glob.glob(R+'worldgen/biome/*.json'):
    b=os.path.basename(f)[:-5]; d=json.load(open(f))
    for cat,l in d['spawners'].items():
        for e in l:
            t=e['type']
            if t.startswith('alexscaves:'): bio.setdefault(t[11:],[]).append((b,cat,e['weight'],e['minCount'],e['maxCount']))
ents=sorted(set(re.findall(r'DEF_REG\.register\("([a-z_0-9]*)"',open(glob.glob('src/main/java/**/ACEntityRegistry.java',recursive=True)[0]).read())))
mobs=[];
for e in ents:
    n=names.get('alexscaves:entity/'+e)
    if not n: continue
    sp=bio.get(e)
    fact=('Wild spawn: '+'; '.join(f'{title(b)} cave ({cat.replace("alexscaves:","").replace("_"," ")}, weight {w}, groups of {a} to {z})' for b,cat,w,a,z in sp)) if sp else 'Not a wild spawn: found only as a boss, summoned, hatched, bred, ridden or placed by a structure.'
    mobs.append({'id':'alexscaves:entity/'+e,'name':n,'prompt':f'Creature: {title(e)} (Diaithiris name: {n}). Lives in the deep caves of the Ages from IV onward. {fact} Write the entry.','kind':'being','locale':'cave-beings'})
bios=[]
for f in sorted(glob.glob(R+'worldgen/biome/*.json')):
    b=os.path.basename(f)[:-5]; n=names.get('alexscaves:biome/'+b)
    m=[e for e,l in bio.items() for x in l if x[0]==b]
    bios.append({'id':'alexscaves:biome/'+b,'name':n,'prompt':f'Place: {title(b)} cave (Diaithiris name: {n}). A sealed underground chamber 20 to 28 blocks wide, found below the surface from Age IV, never open to the sky. Native creatures: '+', '.join(sorted(set(title(x) for x in m)))+'. Write the entry.','kind':'place','locale':'caves'})
sts=[]
for f in sorted(glob.glob(R+'worldgen/structure/*.json')):
    s=os.path.basename(f)[:-5]; n=names.get('alexscaves:structure/'+s)
    sts.append({'id':'alexscaves:structure/'+s,'name':n or title(s),'prompt':f'Place: {title(s)} (Diaithiris name: {n}). A built or grown structure inside the cave realms of Age IV and later. Write the entry.','kind':'place','locale':'cave-sites'})
MERGE='''import json,pathlib
d=pathlib.Path(__file__).parent
meta={json.loads(l)['id']:json.loads(l) for l in open(d/'in.jsonl')}
out=pathlib.Path('/mnt/project-files/library-sync/alexs-caves.json')
rows=json.load(open(out)) if out.exists() else []
have={r[0] for r in rows}
add=0
for l in open(d/'out.jsonl'):
    o=json.loads(l); t=o['text'].strip().strip('"'); m=meta[o['id']]
    if m['name'] not in have and 6<=len(t.split())<=90 and '\\u2014' not in t and m['name']:
        rows.append([m['name'],m['kind'],4,m['locale'],t]); have.add(m['name']); add+=1
json.dump(rows,open(out,'w'),ensure_ascii=False)
print(add,'added;',len(rows),'rows in alexs-caves.json')
'''
for kind,rows in (('mobs',mobs),('biomes',bios),('structures',sts)):
    d=J+'caves_'+kind; os.makedirs(d,exist_ok=True)
    with open(d+'/in.jsonl','w') as f:
        for r in rows: f.write(json.dumps({'id':r['id'],'system':SYS,'prompt':r['prompt'],'name':r['name'],'kind':r['kind'],'locale':r['locale'],'max_tokens':160},ensure_ascii=False)+'\n')
    open(d+'/README','w').write(f"Owner: alexs-caves port thread (Lane E1). Rows: cave {kind} of the alexscaves module (id = alexscaves:<kind>/<id>); one Library entry each, Age IV.\nCheck rule: 1 to 3 sentences, 6 to 90 words, no em dash, no quotes, keep the numbers given. Failing rows go to Claude Batch. max_tokens 160.\nMerge: python3 {d}/merge.py (appends to library-sync/alexs-caves.json as [name, kind, 4, locale, text]).\n")
    open(d+'/merge.py','w').write(MERGE)
    print(kind,len(rows),sum(1 for r in rows if not r['name']))
