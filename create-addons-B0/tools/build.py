import zipfile,glob,json,re,os,sys,shutil,collections
W='/home/user/work-B0'
SHIP=set(open('/home/user/diaithir-ports/src/main/resources/data/create/shipped_items.txt').read().split())
OWN={'minecraft','create','c','neoforge','forge'}
def tag(t):
    t=re.sub(r'^forge:','c:',t); return '#'+t
class Drop(Exception):pass
def ing(x):
    if isinstance(x,str): return x
    if isinstance(x,list): return [ing(i) for i in x]
    if 'item' in x and isinstance(x['item'],str): return x['item']
    if 'item' in x and isinstance(x['item'],dict): return x['item']['id']
    if 'tag' in x: return tag(x['tag'])
    if 'id' in x: return x['id']
    raise Drop('ing')
def isfluid(x): return isinstance(x,dict) and (x.get('type') in('fluid_stack','fluid_tag') or 'fluid' in x or 'fluid_tag' in x)
def fl(x):
    if x.get('type')=='fluid_tag' or 'fluid_tag' in x: raise Drop('fluidtag')
    a=x.get('amount',1000)*81
    return {'type':'fluid_stack','amount':a,'fluid':x.get('fluid') or x['id']}
def res(r):
    if isinstance(r,str): return {'id':r}
    o={}
    if 'item' in r and isinstance(r['item'],dict): i=r['item']['id']
    elif 'item' in r: i=r['item']
    else: i=r['id']
    o['id']=i
    for k in('count','chance'):
        if k in r: o[k]=r[k]
    return o
def isfres(r): return isinstance(r,dict) and 'amount' in r and 'id' in r
def proc(j,step=False):
    t=j['type'];o={k:v for k,v in j.items() if k not in('ingredients','results','ingredient','result')}
    ins=j.get('ingredients',[j['ingredient']] if 'ingredient' in j else [])
    items=[ing(i) for i in ins if not isfluid(i)]
    fluids=[fl(i) for i in ins if isfluid(i)]
    rs=j.get('results',[j['result']] if 'result' in j else [])
    ri=[res(r) for r in rs if not isfres(r)]
    rf=[{'amount':r['amount']*81,'id':r['id']} for r in rs if isfres(r)]
    short=t.split(':')[1]
    if short in('mixing','compacting'):
        o['ingredients']=items
        if fluids:o['fluid_ingredients']=fluids
        o['results']=ri
        if rf:o['fluid_results']=rf
    elif short=='filling':
        o['ingredient']=items[0];o['fluid_ingredient']=fluids[0];o['result' if True else '']=ri[0]
    elif short=='emptying':
        o['ingredient']=items[0];o['fluid_result']=rf[0]
        if ri:o['result']=ri[0]
    elif short=='deploying':
        o['target']=items[0];o['ingredient']=items[1]
        if 'keep_held_item' in j:o['keep_held_item']=j['keep_held_item']
        o['results']=ri
    elif short=='sandpaper_polishing':
        o['ingredient']=items[0];o['result']=ri[0]
    else:
        o['ingredient']=items[0];o['results']=ri
    return o
def seq(j):
    o={k:v for k,v in j.items() if k not in('ingredient','transitional_item','results','result','sequence','junks')}
    o['ingredient']=ing(j['ingredient'])
    o['transitional_item']=res(j['transitional_item'])
    rs=j.get('results',[j.get('result')])
    o['results']=[res(r) for r in rs]  # fixed below
    # port form used by batch0: result + junks
    o.pop('results');o['result']=res(rs[0]);
    if j.get('junks'):o['junks']=[res(r) for r in j['junks']]
    tr=o['transitional_item']['id']
    steps=[]
    for s in j['sequence']:
        s=dict(s);t=s['type'].split(':')[1];ins=s.get('ingredients',[]);
        rest=[i for i in ins if not(not isfluid(i) and ing(i)==tr)]
        d={'type':s['type']}
        if t=='filling':
            d['ingredient']='$ingredient';d['fluid_ingredient']=fl([i for i in rest if isfluid(i)][0]);d['result']='$result'
        elif t=='deploying':
            d['target']='$ingredient';d['ingredient']=ing(rest[0]);d['results']=['$result']
        else:
            d['ingredient']='$ingredient';d['results']=['$result']
            for k in('processing_time',):
                if k in s:d[k]=s[k]
        steps.append(d)
    o['sequence']=steps;return o
def vanilla(j):
    t=j['type'].replace('minecraft:','');o=dict(j)
    if t=='crafting_shaped':
        o['key']={k:ing(v) for k,v in j['key'].items()}
        o['result']=res(j['result'])
    elif t=='crafting_shapeless':
        o['ingredients']=[ing(i) for i in j['ingredients']];o['result']=res(j['result'])
    elif t in('smelting','blasting','smoking','campfire_cooking'):
        o['ingredient']=ing(j['ingredient']);o['result']=res(j['result']) if not isinstance(j['result'],str) else {'id':j['result']}
    elif t=='stonecutting':
        o['ingredient']=ing(j['ingredient']);o['result']=res(j['result'])
    else: raise Drop('type '+t)
    o['type']='minecraft:'+t
    return o
def mech(j):
    o=dict(j);o['key']={k:ing(v) for k,v in j['key'].items()}
    r=j.get('result') or j['results'][0];o.pop('results',None);o['result']=res(r);return o
def conv(j):
    t=j.get('type','')
    if t=='crafting_shapeless' or t=='crafting_shaped': j['type']='minecraft:'+t;t=j['type']
    if t=='create:sequenced_assembly':return seq(j)
    if t=='create:mechanical_crafting':return mech(j)
    if t.startswith('create:') and t.split(':')[1] in('mixing','compacting','filling','emptying','milling','cutting','crushing','pressing','splashing','haunting','deploying','sandpaper_polishing'):return proc(j)
    if t.startswith('minecraft:'):return vanilla(j)
    raise Drop('type '+t)
def ids(o,acc):
    if isinstance(o,dict):
        for k,v in o.items():
            if k=='type': continue
            if k in('id','fluid') and isinstance(v,str) and ':' in v: acc.add(v)
            ids(v,acc)
    elif isinstance(o,list):
        for v in o: ids(v,acc)
    elif isinstance(o,str) and re.match(r'^[a-z0-9_.-]+:[a-z0-9_./-]+$',o): acc.add(o)
def ok_ids(j):
    acc=set();ids(j,acc)
    for i in acc:
        ns,p=i.split(':',1)
        if ns not in OWN: return False
        if ns=='create' and p not in SHIP and p not in('honey','chocolate','tea','potion','builders_tea') and not p.startswith('crushed_raw'):
            # unknown create ids pass only if shipped
            return False
    return True
def main(slugs):
    out=f'{W}/pack/src';shutil.rmtree(out,ignore_errors=True)
    stats=collections.defaultdict(lambda:collections.Counter())
    for s in slugs:
        z=zipfile.ZipFile(f'{W}/jars/{s}.jar')
        for n in z.namelist():
            m=re.match(r'data/([^/]+)/(recipes?)/(.*\.json)$',n)
            if not m: continue
            try:
                j=json.loads(z.read(n).decode('utf-8-sig'))
                if 'conditions' in j or 'neoforge:conditions' in j: stats[s]['cond']+=1;continue
                c=conv(j)
            except Drop as e: stats[s]['drop:'+str(e)[:20]]+=1;continue
            except Exception as e: stats[s]['err']+=1;continue
            if not ok_ids(c): stats[s]['foreign']+=1;continue
            p=f'{out}/data/dc_{re.sub(r"[^a-z0-9_]","_",s)}/recipe/{m[3]}'
            os.makedirs(os.path.dirname(p),exist_ok=True);json.dump(c,open(p,'w'),indent=1);stats[s]['ok']+=1
    return stats
if __name__=='__main__':
    sl=json.load(open('/mnt/project-files/create-addons/slice_B0.json'))['B0']
    st=main(sl)
    for s,c in st.items(): print(s,dict(c))
    json.dump({s:dict(c) for s,c in st.items()},open(f'{W}/stats.json','w'))
