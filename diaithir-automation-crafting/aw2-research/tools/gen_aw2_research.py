#!/usr/bin/env python3
"""Convert Ancient Warfare 2 research goals (GPL-3.0) into Researcher nodes plus a cost table.
Usage: python3 -I gen_aw2_research.py AW2_RESEARCH_DIR OUT_MODULE_DIR
AW2 source: github.com/P3pp3rF1y/AncientWarfare2 src/main/resources/assets/ancientwarfare/registry/research/*.json
"""
import json, os, sys, glob
src, out = sys.argv[1], sys.argv[2]
ORE = {  # AW2 ore dictionary name -> 26.1 item or #tag ; None = no equivalent yet (flagged, not invented)
 'plankWood':'#minecraft:planks','stickWood':'minecraft:stick','string':'minecraft:string','paper':'minecraft:paper',
 'torch':'minecraft:torch','ingotIron':'minecraft:iron_ingot','ingotGold':'minecraft:gold_ingot','netherrack':'minecraft:netherrack',
 'gunpowder':'minecraft:gunpowder','cropWheat':'minecraft:wheat','sand':'#minecraft:sand','dustRedstone':'minecraft:redstone',
 'stone':'minecraft:stone','leather':'minecraft:leather','gemEmerald':'minecraft:emerald','gemDiamond':'minecraft:diamond',
 'dyeBlue':'minecraft:blue_dye','dyeBlack':'minecraft:black_dye',
 'ingotSteel':None,'shaftSteel':None,'gearSteel':None,'bearingSteel':None,'gearIron':None,'bearingIron':None}
ITEM = {'minecraft:wool':'#minecraft:wool','minecraft:map':'minecraft:map'}
def res(r):
    if r.get('type','').endswith('ore_dict_count'):
        k=r['ore']; return {'aw2':k,'item':ORE[k],'count':r['count']}
    i=r['item']; return {'aw2':i,'item':ITEM.get(i,i),'count':r.get('count',1)}
costs={}; 
for f in sorted(glob.glob(os.path.join(src,'*.json'))):
    d=json.load(open(f)); n=d['name']
    rs=[res(r) for r in d['resources']]
    costs[n]={'time_s':d['time'],'dependencies':d['dependencies'],'resources':rs}
base=os.path.join(out,'datapack/aw2_research/data/aw2research/research')
os.makedirs(base,exist_ok=True)
for n,c in costs.items():
    first=next((r for r in c['resources'] if r['item']),None)
    node={'title':n.replace('_',' ').title(),
          'description':'Ancient Warfare 2 research: %s.'%n.replace('_',' '),
          'prerequisites':['aw2research:'+p for p in c['dependencies']]}
    if first:
        it=first['item']
        node['to_unlock']={'criterion':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':it,'count':{'min':first['count']}}]}},'count':1}
        node['display']={'type':'item','item':{'id':it if not it.startswith('#') else 'minecraft:paper'}}
    json.dump(node,open(os.path.join(base,n+'.json'),'w'),indent=2); open(os.path.join(base,n+'.json'),'a').write('\n')
json.dump(costs,open(os.path.join(out,'aw2_research_costs.json'),'w'),indent=2)
print(len(costs),'goals',sum(1 for c in costs.values() for r in c['resources'] if not r['item']),'unmapped resource rows')
