import json,os,re,shutil,glob
X='/home/user/work-B1/x'; M='/home/user/work-B1/mod'
SRC=M+'/src/main/resources'
shutil.rmtree(SRC,ignore_errors=True); os.makedirs(SRC+'/META-INF')
# slug -> (namespace list handled as items/blocks from assets) ; data-only slugs
CODE=['create-synthetic-pressure','create-dense-ores','create-droid-netherrack','create-templates-gears','create-bananas','create-blockzapper-memorial','create-more-sandpapers']
DATA=['create-perfect-processing','create-stam1o-tweaks','create-fd-dough','createrelics-integration']
OWN={'minecraft','c','create','neoforge'}
DROP=set(open('/home/user/work-B1/drop.txt').read().split()) if os.path.exists('/home/user/work-B1/drop.txt') else set()
man={'items':[],'blocks':[]}
def cond(mods): return [{"type":"neoforge:mods_loaded","modids":sorted(mods)}] if False else [{"type":"neoforge:mod_loaded","modid":m} for m in sorted(mods)]
def fixing(o):
    if isinstance(o,dict):
        if set(o)=={'item'}: return o['item']
        if set(o)=={'tag'}: return '#'+o['tag'].lstrip('#')
        if 'processingTime' in o: o['processing_time']=o.pop('processingTime')
        if 'transitionalItem' in o: o['transitional_item']=o.pop('transitionalItem')
        return {k:fixing(v) for k,v in o.items()}
    if isinstance(o,list): return [fixing(v) for v in o]
    return o

SINGLE={'create:crushing','create:milling','create:pressing','create:haunting','create:splashing','create:cutting','create:sandpaper_polishing','create:emptying','create:filling','create:deploying','create:item_application'}
def isfluid(x): return isinstance(x,dict) and ('fluid' in x or 'fluid_tag' in x)
def fl(x):
    x=dict(x)
    if 'amount' in x: x['amount']=x['amount']*81
    if 'fluid_tag' in x: x={'type':'fluid_tag','fluid_tag':x['fluid_tag'],'amount':x.get('amount',0)}
    elif 'type' not in x: x['type']='fluid_stack'
    return x
def res1(r):
    return r
def conv(j,trans=None):
    if not isinstance(j,dict): return j
    j=fixing(j); t=j.get('type','')
    if t=='create:sequenced_assembly':
        res=j.pop('results',None)
        if res: 
            j['result']=res[0]; 
            if len(res)>1: j['junks']=res[1:]
        tr=j.get('transitional_item')
        tid=tr['id'] if isinstance(tr,dict) else tr
        steps=[]
        for st in j['sequence']:
            st=conv(st,tid)
            steps.append(st)
        j['sequence']=steps
        if isinstance(tr,str): j['transitional_item']={'id':tr}
        return j
    ing=j.get('ingredients')
    if isinstance(ing,list):
        items=[i for i in ing if not isfluid(i)]; fls=[fl(i) for i in ing if isfluid(i)]
        if t in ('create:filling',) : 
            j.pop('ingredients'); 
            if items: j['ingredient']=items[0]
            if fls: j['fluid_ingredient']=fls[0]
        elif t in ('create:deploying','create:item_application') and len(items)==2:
            j.pop('ingredients'); j['target']=items[0]; j['ingredient']=items[1]
        elif t in SINGLE and len(items)==1 and not fls:
            j.pop('ingredients'); j['ingredient']=items[0]
        else:
            j['ingredients']=items
            if fls: j['fluid_ingredients']=fls
    if 'fluid_ingredients' in j and isinstance(j['fluid_ingredients'],list): j['fluid_ingredients']=[fl(x) for x in j['fluid_ingredients']]
    if t in ('create:filling','create:sandpaper_polishing','create:emptying') and isinstance(j.get('results'),list):
        j['result']=j.pop('results')[0]
        if t=='create:emptying' and len(j.get('results',[]))==0: pass
    if trans:
        for k in ('ingredient','target'):
            if j.get(k)==trans or (isinstance(j.get(k),dict) and j[k].get('id')==trans): j[k]='$ingredient'
        if 'results' in j: j['results']=['$result' if (isinstance(r,dict) and r.get('id')==trans) or r==trans else r for r in j['results']]
        if isinstance(j.get('result'),dict) and j['result'].get('id')==trans: j['result']='$result'
    return j

def mods_in(txt,own):
    return {m for m in re.findall(r'"#?([a-z0-9_]+):[a-z0-9_/.]+"',txt) if m not in OWN and m not in own}
for slug in CODE+DATA:
    d=f'{X}/{slug}'
    nss=[n for n in os.listdir(d+'/data')] if os.path.isdir(d+'/data') else []
    ans=[n for n in os.listdir(d+'/assets')] if os.path.isdir(d+'/assets') else []
    own=set(nss)|set(ans)
    if slug in CODE:
        for ns in ans:
            if ns=='minecraft': continue
            bs=set(os.path.basename(p)[:-5] for p in glob.glob(f'{d}/assets/{ns}/blockstates/*.json'))
            its=set(os.path.basename(p)[:-5] for p in glob.glob(f'{d}/assets/{ns}/models/item/*.json'))
            for i in sorted(bs):
                like='deepslate_iron_ore' if i.startswith('deepslate_') else 'iron_ore' if 'ore' in i else 'stone'
                man['blocks'].append({'ns':ns,'id':i,'like':like})
            for i in sorted(its):
                kind='sequenced' if i.startswith('incomplete_') else 'sandpaper' if i.endswith('sand_paper') else 'food' if i=='banana' else 'simple'
                man['items'].append({'ns':ns,'id':i,'kind':kind,'block':i in bs})
                if os.path.exists(f'{d}/assets/{ns}/models/item/{i}.json'):
                    os.makedirs(f'{SRC}/assets/{ns}/items',exist_ok=True)
                    json.dump({"model":{"type":"minecraft:model","model":f"{ns}:item/{i}"}},open(f'{SRC}/assets/{ns}/items/{i}.json','w'))
    if os.path.isdir(d+'/assets'): shutil.copytree(d+'/assets',SRC+'/assets',dirs_exist_ok=True,ignore=shutil.ignore_patterns('items') if False else None)
    for p in glob.glob(d+'/data/**/*.json',recursive=True):
        rel=os.path.relpath(p,d)
        if '/advancement/' in rel: continue
        try: j=json.load(open(p))
        except Exception: continue
        ctxt=json.dumps(j)
        if rel.replace('data/','',1).replace('/recipe/',':',1).removesuffix('.json') in DROP: continue
        if '/recipe/' in rel:
            if j=={}: continue
            j=conv(j)
            if isinstance(j,dict) and j.get('type','').startswith('create:') or True:
                pass
            m=mods_in(json.dumps(j),own)
            if m and isinstance(j,dict): j['neoforge:conditions']=cond(m)
        out=f'{SRC}/{rel}'; os.makedirs(os.path.dirname(out),exist_ok=True); json.dump(j,open(out,'w'),indent=1)
json.dump(man,open(SRC+'/b1_manifest.json','w'),indent=1)
open(SRC+'/META-INF/neoforge.mods.toml','w').write('''modLoader="javafml"
loaderVersion="[1,)"
license="MIT"
[[mods]]
modId="diaithir_create_addons_b1"
version="0.1.0"
displayName="Diaithir Create Addons (B1)"
authors="Ported from MIT/GPL Create addons: synthetic pressure, dense ores, droid netherrack, templates gears, bananas, blockzapper memorial, more sandpapers, perfect processing, stam1o tweaks, FD dough, relics integration"
description=\'\'\'Item/block registrations and recipes from several small Create addons, retargeted to the Diaithir Create port.\'\'\'
[[dependencies.diaithir_create_addons_b1]]
modId="diaithir_create"
type="required"
versionRange="[6.0.9,)"
ordering="AFTER"
side="BOTH"
''')
print(len(man['items']),len(man['blocks']))
