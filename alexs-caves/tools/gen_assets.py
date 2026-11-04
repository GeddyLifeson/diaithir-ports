#!/usr/bin/env python3
"""Generate 26.1 client item definitions + equipment assets from the 1.21 resources. Run from module root. Idempotent."""
import json, os, glob, shutil
A='src/main/resources/assets/alexscaves'
M=lambda m:{"type":"minecraft:model","model":m}
os.makedirs(A+'/items',exist_ok=True)
ov={}
ids=set()
for f in glob.glob(A+'/models/item/*.json'):
    i=os.path.basename(f)[:-5]; ids.add(i); d=json.load(open(f))
    if d.get('parent')=='builtin/entity':
        d['parent']='minecraft:item/generated'
        d['textures']={'layer0':'alexscaves:item/'+i} if os.path.exists(f'{A}/textures/item/{i}.png') else {'layer0':'minecraft:item/spear'}
        d.pop('display',None)
    if 'overrides' in d:
        ov[i]=d.pop('overrides')
    json.dump(d,open(f,'w'),indent=2)
for f in glob.glob(A+'/blockstates/*.json'):
    i=os.path.basename(f)[:-5]
    if i not in ids and os.path.exists(f'{A}/models/block/{i}.json'): ids.add(i)
def ovmodel(i,n):
    return M(ov[i][-1]['model']) if i in ov else None
using={'limestone_spear','extinction_spear','frostmint_spear','magic_conch','ortholance'}
for i in sorted(ids):
    base=M(f'alexscaves:item/{i}') if os.path.exists(f'{A}/models/item/{i}.json') else M(f'alexscaves:block/{i}')
    if i in ov and i in using:
        # throwing/tooting variants shown while using the item
        base={"type":"minecraft:condition","property":"minecraft:using_item","on_false":base,"on_true":M(ov[i][-1]['model'])}
    json.dump({"model":base},open(f'{A}/items/{i}.json','w'),indent=2)
# equipment
for n in ('primordial','hazmat_suit','diving_suit','darkness','gingerbread','rainbounce'):
    os.makedirs(f'{A}/equipment',exist_ok=True)
    layers={"humanoid":[{"texture":f"alexscaves:{n}"}]}
    if n!='rainbounce': layers["humanoid_leggings"]=[{"texture":f"alexscaves:{n}"}]
    json.dump({"layers":layers},open(f'{A}/equipment/{n}.json','w'),indent=2)
    for k,sub in ((0,'humanoid'),(1,'humanoid_leggings')):
        s=f'{A}/textures/armor/{n}_{k}.png'
        if os.path.exists(s):
            os.makedirs(f'{A}/textures/entity/equipment/{sub}',exist_ok=True)
            shutil.copy(s,f'{A}/textures/entity/equipment/{sub}/{n}.png')
s=f'{A}/textures/armor/rainbounce_boots.png'
if os.path.exists(s):
    os.makedirs(f'{A}/textures/entity/equipment/humanoid',exist_ok=True); shutil.copy(s,f'{A}/textures/entity/equipment/humanoid/rainbounce.png')
print(len(os.listdir(A+'/items')),'item defs')
