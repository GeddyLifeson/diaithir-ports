import zipfile,glob,json,re,os,sys,collections
CIDS=set(json.load(open('/home/user/work-B4/tools/create_ids.json')))
OK={'minecraft','create','c','neoforge','forge','diaithir_create'}
def norm(o):
    if isinstance(o,dict):
        if set(o)<= {'item','count','components'} and 'item' in o and len(o)==1: return o['item']
        if set(o)=={'tag'}: return '#'+o['tag']
        if set(o)=={'item'}: return o['item']
        o={('id' if k=='item' and 'id' not in o else k):norm(v) for k,v in o.items()}
        return o
    if isinstance(o,list): return [norm(x) for x in o]
    return o
def ids(o,acc):
    if isinstance(o,dict):
        for k,v in o.items():
            if k=='type': continue
            if k in('id','item','fluid','ingredient','tag') and isinstance(v,str): acc.add(v)
            ids(v,acc)
    elif isinstance(o,list):
        for x in o: ids(x,acc)
    elif isinstance(o,str) and re.match(r'^#?[a-z0-9_.-]+:[a-z0-9_./-]+$',o): acc.add(o)
def seqfix(j):
    ti=j.get('transitional_item');ti=ti['id'] if isinstance(ti,dict) else ti
    if 'sequence' not in j: return j
    r=j.pop('results',[])
    if r:
        j['result']=r[0]
        if len(r)>1: j['junks']=r[1:]
    for st in j['sequence']:
        t=st['type'];ing=st.pop('ingredients',None)
        if ing is not None:
            fl=[x for x in ing if isinstance(x,dict)]
            its=[x for x in ing if not isinstance(x,dict)]
            oth=[x for x in its if x!=ti and x!=j.get('ingredient')]
            if t=='create:filling':
                st['ingredient']='$ingredient';st['fluid_ingredient']=fl[0] if fl else None
            elif t=='create:deploying':
                st['target']='$ingredient';st['ingredient']=oth[0] if oth else '$ingredient'
            else: st['ingredient']='$ingredient'
        st.pop('results',None);st.pop('result',None)
        if t=='create:filling': st['result']='$result'
        else: st['results']=['$result']
    return j
SINGLE={'create:crushing','create:milling','create:haunting','create:splashing','create:cutting','create:pressing','create:washing','create:smoking','create:blasting'}
def shape(j):
    t=j.get('type')
    if 'processingTime' in j: j['processing_time']=j.pop('processingTime')
    def fl(x): return isinstance(x,dict) and 'fluid' in x
    def fix_fluid(x): x=dict(x);x['type']='fluid_stack';return x
    if t in('create:mixing','create:compacting','create:filling') and 'ingredients' in j:
        ing=j.pop('ingredients');f=[fix_fluid(x) for x in ing if fl(x)];it=[x for x in ing if not fl(x)]
        if t=='create:filling':
            if len(it)!=1 or len(f)!=1: return None
            j['ingredient']=it[0];j['fluid_ingredient']=f[0]
        else:
            if not it: return None
            j['ingredients']=it
            if f: j['fluid_ingredients']=f
    elif t in SINGLE and 'ingredients' in j:
        ing=j.pop('ingredients')
        if len(ing)!=1: return None
        j['ingredient']=ing[0]
    for k in('fluid_ingredients',):
        if k in j: j[k]=[fix_fluid(x) for x in j[k]]
    if j.get('type')=='create:filling' and 'results' in j and 'result' not in j and len(j['results'])==1: j['result']=j.pop('results')[0]
    return j
def run(outroot,slugs=None):
    keep=collections.defaultdict(list);drop=collections.defaultdict(list)
    for p in sorted(glob.glob('/home/user/work-B4/jars/*.jar')):
        k=os.path.basename(p)[:-4]
        if slugs and k not in slugs: continue
        z=zipfile.ZipFile(p)
        for n in z.namelist():
            m=re.match(r'data/([^/]+)/(recipes?)/(.+\.json)$',n)
            if not m: continue
            try: j=json.loads(z.read(n))
            except Exception: drop[k].append(n);continue
            if 'neoforge:conditions' in j or 'conditions' in j: j.pop('neoforge:conditions',None);j.pop('conditions',None)
            j=norm(j);j=seqfix(j) if j.get('type')=='create:sequenced_assembly' else j;j=shape(j)
            if j is None: drop[k].append((n,['shape']));continue
            a=set();ids(j,a)
            if any(isinstance(j.get(r),list) and not j[r] for r in('results','ingredients')) or any(x.startswith('create:') and not x.startswith('#') and x[7:] not in CIDS and x[7:] not in('shaft',) for x in a if True):
                drop[k].append((n,['missing-create-item']));continue
            bad=[x for x in a if not x.startswith('$') and x.lstrip('#').split(':')[0] not in OK]
            if bad or not str(j.get('type','')).startswith(('create:','minecraft:')): drop[k].append((n,bad[:2]));continue
            if m.group(3).endswith('ores_stone/dark_zinc.json'): drop[k].append((n,['bad-vanilla-id']));continue
            if j.get('type')=='create:sequenced_assembly': drop[k].append((n,['sequenced-unverified']));continue
            keep[k].append((f'data/{m.group(1)}/recipe/{m.group(3)}',j))
    return keep,drop
if __name__=='__main__':
    keep,drop=run(None)
    for k in sorted(set(keep)|set(drop)): print(k,'keep',len(keep[k]),'drop',len(drop[k]),drop[k][:1])
