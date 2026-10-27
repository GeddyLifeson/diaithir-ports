import zipfile,json,glob,os,re,sys,collections
OK={'minecraft','create','c','neoforge'}
def ing(x):
    if isinstance(x,str): return x
    if isinstance(x,list): return [ing(i) for i in x]
    if 'item' in x: return x['item']
    if 'tag' in x: return '#'+x['tag']
    raise ValueError('ing')
def isfluid(x): return isinstance(x,dict) and ('fluid' in x or 'fluid_tag' in x or x.get('type','').endswith('fluid') or ('amount' in x and ('tag' in x)) )
def fl(x):
    amt=x.get('amount',1000)*81
    if 'fluid' in x: return {'type':'fluid_stack','amount':amt,'fluid':x['fluid']}
    t=x.get('fluid_tag') or x.get('tag'); return {'type':'fluid_tag','amount':amt,'fluid_tag':'#'+t.lstrip('#')}
def fix(r):
    t=r.get('type','')
    if t.startswith('create:') :
        if 'ingredients' in r:
            items=[];fluids=[]
            for i in r['ingredients']:
                (fluids if isfluid(i) else items).append(fl(i) if isfluid(i) else ing(i))
            r['ingredients']=items
            if fluids: r['fluid_ingredients']=r.get('fluid_ingredients',[])+fluids
        if 'ingredient' in r: r['ingredient']=ing(r['ingredient'])
        k=t[7:]
        if k in('pressing','cutting','milling','crushing','haunting','splashing','sandpaper_polishing') and 'ingredients' in r and len(r['ingredients'])==1:
            r['ingredient']=r.pop('ingredients')[0]
        if k in('deploying','item_application') and len(r.get('ingredients',[]))==2:
            r['target'],r['ingredient']=r.pop('ingredients')
        if k=='filling':
            if len(r.get('ingredients',[]))==1: r['ingredient']=r.pop('ingredients')[0]
            if r.get('fluid_ingredients'): r['fluid_ingredient']=r.pop('fluid_ingredients')[0]
        if 'results' in r:
            res=[];fr=[]
            for o in r['results']:
                if 'fluid' in o: fr.append({'amount':o.get('amount',1000)*81,'id':o['fluid']})
                else:
                    o=dict(o)
                    if 'item' in o: o['id']=o.pop('item')
                    res.append(o)
            r['results']=res
            if fr: r['fluid_results']=fr
        if k=='filling' and len(r.get('results',[]))==1: r['result']=r.pop('results')[0]
    elif t in('minecraft:smelting','minecraft:blasting','minecraft:smoking','minecraft:stonecutting','minecraft:campfire_cooking'):
        r['ingredient']=ing(r['ingredient'])
        if 'item' in r.get('result',{}): r['result']=r['result']['item']
    elif t in('minecraft:crafting_shaped','minecraft:crafting_shapeless'):
        if 'key' in r: r['key']={k:ing(v) for k,v in r['key'].items()}
        if 'ingredients' in r: r['ingredients']=[ing(i) for i in r['ingredients']]
        if 'item' in r.get('result',{}): r['result']['id']=r['result'].pop('item')
    return r
def refs(o,acc):
    if isinstance(o,dict):
        for k,v in o.items():
            if k in('item','id','fluid','tag','fluid_tag') and isinstance(v,str): acc.add(v)
            else: refs(v,acc)
    elif isinstance(o,list):
        for v in o: refs(v,acc)
    elif isinstance(o,str) and ':' in o and ' ' not in o: acc.add(o)
res={};out=collections.OrderedDict()
slugs=sys.argv[1:]
for s in slugs:
    z=zipfile.ZipFile(f'jars/{s}.jar');kept=drop=0;why=set()
    for n in z.namelist():
        m=re.match(r'data/([^/]+)/(recipes?)/(.+\.json)$',n)
        if not m: continue
        try: r=json.loads(z.read(n))
        except Exception: drop+=1;continue
        if not isinstance(r,dict) or 'type' not in r: drop+=1;continue
        if 'neoforge:conditions' in r or 'conditions' in r:
            r.pop('neoforge:conditions',None);r.pop('conditions',None)
        t=r['type']
        if not (t.startswith('create:') or t.startswith('minecraft:crafting') or t in('minecraft:smelting','minecraft:blasting','minecraft:smoking','minecraft:stonecutting')): drop+=1;why.add('type '+t);continue
        if t=='create:pressing' and len(r.get('ingredients',[r]))>1: drop+=1;why.add('2-ing pressing');continue
        try: r=fix(r)
        except Exception as e: drop+=1;why.add('shape');continue
        r=json.loads(json.dumps(r).replace('create:copper_nugget','minecraft:copper_nugget'))
        a=set();refs({k:v for k,v in r.items() if k!='type'},a)
        bad={x.lstrip('#').split(':')[0] for x in a if ':' in x}-OK
        if bad: drop+=1;why|=bad;continue
        out[f'data/diaithir_b3_{s.replace("-","_")}/recipe/{m.group(3)}']=json.dumps(r,indent=1);kept+=1
    res[s]=(kept,drop,sorted(why)[:6])
    print(s,res[s])
json.dump(out,open('pk/recipes.json','w'))
