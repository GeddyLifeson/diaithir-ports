# Merges planet-{text,mobs,loot}-sNN job outputs into module data, validating cited entry ids against each row's input.
# python3 -I merge_planet_content.py  -> data/diaithir_planets/{codex_planets,planet_mobs,planet_loot}.json and adds summary/tags to planet_colony.json
import json,glob,re,os
J='/mnt/project-files/compute/jobs'
R='/home/user/diaithir-ports/diaithir-overworld-biomes/src/main/resources/data/diaithir_planets/'
def rows(kind):
    out={}
    for d in sorted(glob.glob(f'{J}/planet-{kind}-s*')):
        inp={}
        for l in open(d+'/in.jsonl'):
            r=json.loads(l); inp[r['id']]=set(re.findall(r'^(\S+) \|',r['prompt'],re.M))
        if not os.path.exists(d+'/out.jsonl'): continue
        for l in open(d+'/out.jsonl'):
            o=json.loads(l)
            try:
                t=o['text']; k=json.loads(t[t.index('{'):t.rindex('}')+1]); out[o['id']]=(k,inp[o['id']])
            except Exception: pass
    return out
ok={}
V="zombie skeleton spider creeper enderman witch slime pillager vindicator husk stray drowned phantom blaze wither_skeleton piglin hoglin strider ghast cave_spider silverfish endermite wolf fox bee cow pig sheep chicken horse llama goat frog axolotl turtle cat parrot rabbit panda polar_bear camel sniffer armadillo bat squid salmon cod".split()
cx={}
for pid,(k,ids) in rows('text').items():
    c=[x for x in k.get('cites',[]) if x in ids]
    if str(k.get('summary','')).strip() and c: cx[pid]={"summary":k['summary'].strip(),"tags":[t for t in k.get('tags',[]) if isinstance(t,str)][:8],"cites":c}
mb={}
for pid,(k,ids) in rows('mobs').items():
    m=[x for x in k.get('mobs',[]) if x.get('entry') in ids and x.get('base') in V and x.get('role') in('hostile','passive','neutral','boss')][:10]
    if m: mb[pid]=m
lt={}
for pid,(k,ids) in rows('loot').items():
    m=[x for x in k.get('loot',[]) if x.get('entry') in ids and re.fullmatch(r'[a-z0-9_]+',str(x.get('stand_in','')))][:12]
    if m: lt[pid]=m
for name,d in (('codex_planets',cx),('planet_mobs',mb),('planet_loot',lt)): json.dump(d,open(R+name+'.json','w'),separators=(',',':'))
pc=json.load(open(R+'planet_colony.json'))
for pid,v in cx.items():
    if pid in pc: pc[pid]['summary']=v['summary']; pc[pid]['tags']=v['tags']
json.dump(pc,open(R+'planet_colony.json','w'),separators=(',',':'))
print('text',len(cx),'mobs',len(mb),'loot',len(lt))
