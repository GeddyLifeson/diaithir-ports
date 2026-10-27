#!/usr/bin/env python3
"""Rewrite 1.21.1 Create-addon recipe JSON into the Diaithir Create port (26.1) shapes.
usage: fixrecipes.py SRC_DATA_DIR  (in place)"""
import json,sys,glob,os,copy
MB=81  # port fluid units per mB (81000 per bucket)
SINGLE={'create:crushing','create:milling','create:cutting','create:pressing','create:haunting','create:splashing'}
TRANS='create:incomplete_precision_mechanism'
def isfluid(x): return isinstance(x,dict) and ('fluid' in x or x.get('type') in('neoforge:single','fluid_stack','neoforge:tag','neoforge:compound','create:fluid_tag') and 'item' not in x and 'tag' not in x and x.get('type')!='neoforge:compound' or 'fluid_tag' in x)
def ing(x):
    if isinstance(x,str): return x
    if isinstance(x,list): return [ing(i) for i in x]
    if 'item' in x and len(set(x)-{'item','count'})==0: return x['item']
    if 'tag' in x and len(set(x)-{'tag','count'})==0: return '#'+x['tag']
    return x
def cnt(x): return x.get('count',1) if isinstance(x,dict) else 1
def fluid(x):
    f=dict(x)
    if f.get('type') in('neoforge:single',None): f['type']='fluid_stack'
    if 'name' in f: f['fluid']=f.pop('name')
    if 'amount' in f: f['amount']=f['amount']*MB
    return f
def out(x):
    if isinstance(x,str): return {'id':x}
    o=dict(x)
    if 'item' in o: o['id']=o.pop('item')
    return o
def fout(x):
    o=dict(x)
    if 'fluid' in o: o['id']=o.pop('fluid')
    if 'amount' in o: o['amount']=o['amount']*MB
    o.pop('type',None)
    return o
def process(j,trans=None):
    t=j.get('type','')
    if 'heatRequirement' in j: j['heat_requirement']=j.pop('heatRequirement')
    if t=='create:sequenced_assembly': return seq(j)
    if t.startswith('minecraft:crafting'):
        if 'key' in j: j['key']={k:ing(v) for k,v in j['key'].items()}
        if 'ingredients' in j: j['ingredients']=[ing(i) for i in j['ingredients']]
        if isinstance(j.get('result'),dict): j['result']=out(j['result'])
        return j
    if not t.startswith('create:'): return j
    ins=j.pop('ingredients',None)
    if ins is not None:
        items=[];fl=[]
        for i in ins:
            if isfluid(i): fl.append(fluid(i))
            else:
                items+= [ing(i)]*cnt(i)
        if t in SINGLE or t in('create:filling','create:emptying','create:deploying','create:item_application','create:sandpaper_polishing'):
            if t=='create:deploying' or t=='create:item_application':
                j['ingredient']=items[1];j['target']=items[0] if False else None
                j.pop('target')
                j['ingredients']=items  # keep list form; verified at boot
                j.pop('ingredient')
            else:
                j['ingredient']=items[0]
                if fl: j['fluid_ingredient']=fl[0]
        else:
            if items: j['ingredients']=items
            if fl: j['fluid_ingredients']=fl
    elif 'ingredient' in j:
        j['ingredient']=ing(j['ingredient'])
    rs=j.get('results')
    if rs is not None:
        items=[];fr=[]
        for r in rs:
            (fr if 'fluid' in r else items).append(fout(r) if 'fluid' in r else out(r))
        if t=='create:filling' or t=='create:sandpaper_polishing':
            j['result']=items[0]; j.pop('results')
        else:
            j['results']=items
            if fr: j['fluid_results']=fr
            if not items: j.pop('results')
    return j
def seq(j):
    base=ing(j['ingredient'])
    tr=j.get('transitional_item') or {'item':TRANS}
    old=tr.get('id') or tr.get('item') if isinstance(tr,dict) else tr
    old_ids={old}|({j['ingredient'].get('item')} if isinstance(j['ingredient'],dict) else set())
    # in 1.21.1 shape the ingredient is the start item; transitional is separate
    j['ingredient']=base
    j['transitional_item']={'id':TRANS}
    rs=[out(r) for r in j.pop('results',[])]
    j['result']=rs[0]
    if len(rs)>1: j['junks']=rs[1:]
    steps=[]
    for s in j['sequence']:
        s=dict(s); t=s['type']
        s.pop('results',None)
        ins=s.pop('ingredients',[])
        items=[];fl=[]
        for i in ins:
            if isfluid(i): fl.append(fluid(i))
            else: items.append(ing(i))
        extra=[i for i in items if not (isinstance(i,str) and (i in old_ids or i.startswith('new_recipes:incomplete') or i.endswith(':incomplete') ))]
        if 'heatRequirement' in s: s['heat_requirement']=s.pop('heatRequirement')
        if t=='create:deploying':
            s['target']='$ingredient'; s['ingredient']=extra[0] if extra else items[-1]; s['results']=['$result']
        elif t=='create:filling':
            s['ingredient']='$ingredient'; s['fluid_ingredient']=fl[0]; s['result']='$result'
        else:
            s['ingredient']='$ingredient'; s['results']=['$result']
        steps.append(s)
    j['sequence']=steps
    return j
def main(root):
    n=0
    for f in glob.glob(root+'/**/recipe/**/*.json',recursive=True):
        try: j=json.load(open(f))
        except Exception as e: print('BAD',f,e); continue
        if not isinstance(j,dict) or not str(j.get('type','')).startswith(('create:','minecraft:crafting')): continue
        j=process(j); json.dump(j,open(f,'w'),indent=1); n+=1
    print('fixed',n)
if __name__=='__main__': main(sys.argv[1])
