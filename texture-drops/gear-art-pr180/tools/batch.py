import json,os,re,sys
import guns,melee,tools
from PIL import Image
OUT=sys.argv[1]; J='/mnt/project-files/compute/jobs/'
A=OUT+'/assets/diaithirgear'; TX=A+'/textures/item/workshop'; os.makedirs(TX,exist_ok=True); os.makedirs(A+'/models/item/workshop',exist_ok=True); os.makedirs(A+'/items/workshop',exist_ok=True)
def specs(job):
    d={}
    p=J+job+'/out.jsonl'
    if not os.path.exists(p): return d
    for l in open(p):
        r=json.loads(l)
        try: d[r['id']]=json.loads(re.search(r'\{.*\}',r['text'],re.S).group(0))
        except Exception: pass
    return d
fb={'melee':{"L":55,"head":{"type":"blade","len":38,"w":4,"taper":0.4,"tip":"point"},"guard":{"type":"bar","w":8},"handle":{"len":14,"w":2,"wrap":True}},
    'guns':{"class":"rifle","barrel_len":28,"barrel_w":2,"stock":"straight","lock":"semi"},'tools':{"arch":"generic","L":44,"head_w":8,"head_len":12}}
stat={}; mm={}
for kind,bpf,job in(('melee','bp_historical.json','gear_spec_melee'),('guns','bp_guns.json','gear_spec_guns'),('tools','bp_everyday.json','gear_spec_tools')):
    sp=specs(job); bps=json.load(open(bpf)); ok=miss=0
    for b in bps:
        bid=b['id']; slots=[s['name'] for s in b['slots']]; s=sp.get(bid) or sp.get(bid[3:] if bid.startswith('hw_') else bid)
        if s is None: miss+=1; s=fb[kind]
        else: ok+=1
        try:
            if kind=='guns': guns.render(s,TX,bid)
            elif kind=='melee': melee.render(s,TX,bid)
            else: tools.render(s,slots,TX,bid)
        except Exception as e:
            print('ERR',bid,repr(e)[:90]); s=fb[kind]
            (guns.render if kind=='guns' else melee.render if kind=='melee' else None)(s,TX,bid) if kind!='tools' else tools.render(s,slots,TX,bid)
        for sl in slots:
            f=f'{TX}/{bid}_{sl}.png'
            if not os.path.exists(f): Layer_=Image.new('RGBA',(64,64),(0,0,0,0)); Layer_.save(f)
        tex={f'layer{i}':f'diaithirgear:item/workshop/{bid}_{sl}' for i,sl in enumerate(slots)}
        parent='minecraft:item/handheld' if kind=='melee' or (kind=='tools' and 'pot' not in slots) else 'minecraft:item/generated'
        json.dump({'parent':parent,'textures':tex},open(f'{A}/models/item/workshop/{bid}.json','w'),indent=1)
        json.dump({'model':{'type':'minecraft:model','model':f'diaithirgear:item/workshop/{bid}','tints':[{'type':'minecraft:custom_model_data','index':i,'default':16777215 if i==0 else 9202494} for i in range(len(slots))]}},open(f'{A}/items/workshop/{bid}.json','w'),indent=1)
        mm[bid]=bid
    stat[kind]=(ok,miss,len(bps))
json.dump(mm,open(OUT+'/model_map.json','w'),indent=0)
print(stat)
