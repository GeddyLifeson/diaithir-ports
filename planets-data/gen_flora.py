# Named per-planet tree/plant/ore -> block assets, data, features. python3 -I gen_flora.py [planet_id ..]  (reads specs/<id>.json["flora"])
# Writes into the biomes module resources (MODR); kit_overlay.py then attaches flora_features.json to each planet's land biomes.
# Textures are recoloured vanilla 26.1.2 textures (needs Pillow).
import json,re,sys,os,glob,zipfile,io,colorsys
from PIL import Image
MODR='/home/user/diaithir-ports/diaithir-overworld-biomes/src/main/resources'
A=MODR+'/assets/diaithir_planets'; D=MODR+'/data/diaithir_planets'; MC=MODR+'/data/minecraft'
SP='/mnt/project-files/planets/specs'; VF='/mnt/project-files/planets/base/vf'
Z=zipfile.ZipFile('/root/.gradle/caches/neoformruntime/artifacts/minecraft_26.1.2_client.jar')
def wj(path,o):
    os.makedirs(os.path.dirname(path),exist_ok=True); json.dump(o,open(path,'w'),indent=0)
def tex(n): return Image.open(io.BytesIO(Z.read(f'assets/minecraft/textures/block/{n}.png'))).convert('RGBA')
def save(im,p):
    os.makedirs(os.path.dirname(p),exist_ok=True); im.save(p)
def rgb(h): return tuple(int(h[i:i+2],16) for i in (1,3,5))
def tint(im,col):
    """recolour: keep the texture's light/dark pattern and alpha, set its colour"""
    r,g,b=rgb(col); px=im.load(); w,h=im.size
    ls=[sum(px[x,y][:3])/3 for x in range(w) for y in range(h) if px[x,y][3]>0] or [128]
    m=sum(ls)/len(ls) or 1; out=im.copy(); po=out.load()
    for x in range(w):
        for y in range(h):
            p=px[x,y]
            if p[3]==0: continue
            k=(sum(p[:3])/3)/m
            po[x,y]=(min(255,int(r*k)),min(255,int(g*k)),min(255,int(b*k)),p[3])
    return out
def hue_shift(im,col,keep_green=True):
    """flowers/ores: move the coloured (non-grey, non-green-stem) pixels to the target hue, keep saturation/value"""
    th=colorsys.rgb_to_hsv(*[c/255 for c in rgb(col)]); out=im.copy(); po=out.load(); px=im.load(); w,h=im.size
    for x in range(w):
        for y in range(h):
            p=px[x,y]
            if p[3]==0: continue
            hh,s,v=colorsys.rgb_to_hsv(p[0]/255,p[1]/255,p[2]/255)
            if s<0.25: continue
            if keep_green and 0.18<hh<0.45: continue
            r,g,b=colorsys.hsv_to_rgb(th[0],min(1,max(s,th[1]*0.8)),v)
            po[x,y]=(int(r*255),int(g*255),int(b*255),p[3])
    return out
def slug(s): return re.sub(r'[^a-z0-9]+','_',s.lower()).strip('_') or 'x'
TAGS={'logs':[],'leaves':[],'axe':[],'pick':[],'hoe':[],'small_flowers':[]}
LANG={}; IDS=[]
def simple_block(bid,model_dir='block'):
    wj(f'{A}/blockstates/{bid}.json',{"variants":{"":{"model":f"diaithir_planets:block/{bid}"}}})
    wj(f'{A}/items/{bid}.json',{"model":{"type":"minecraft:model","model":f"diaithir_planets:{model_dir}/{bid}"}})
def loot(bid,kind):
    me=f"diaithir_planets:{bid}"
    if kind=='leaves':
        pool={"rolls":1.0,"entries":[{"type":"minecraft:alternatives","children":[
          {"type":"minecraft:item","name":me,"conditions":[{"condition":"minecraft:any_of","terms":[{"condition":"minecraft:match_tool","predicate":{"items":"minecraft:shears"}},{"condition":"minecraft:match_tool","predicate":{"predicates":{"minecraft:enchantments":[{"enchantments":"minecraft:silk_touch","levels":{"min":1}}]}}}]}]},
          {"type":"minecraft:item","name":"minecraft:stick","conditions":[{"condition":"minecraft:random_chance","chance":0.1}]}]}]}
    else:
        pool={"rolls":1.0,"conditions":[{"condition":"minecraft:survives_explosion"}],"entries":[{"type":"minecraft:item","name":me}]}
    wj(f'{D}/loot_table/blocks/{bid}.json',{"type":"minecraft:block","random_sequence":f"diaithir_planets:blocks/{bid}","pools":[pool]})
def emit(pid,sp):
    f=sp['flora']; used=set()
    def mk(slot,kind):
        base=f'{pid}_{slug(f[slot]["name"])}'; bid=f'{base}_{kind}'; i=2
        while bid in used: bid=f'{base}{i}_{kind}'; i+=1
        used.add(bid); return bid
    t,pl,o=f['tree'],f['plant'],f['ore']; nm=t['name']
    lg=mk('tree','log'); lv=mk('tree','leaves')
    save(tint(tex(t['log_base']+'_log'),t['log_color']),f'{A}/textures/block/{lg}.png')
    save(tint(tex(t['log_base']+'_log_top'),t['log_color']),f'{A}/textures/block/{lg}_top.png')
    save(tint(tex(t['leaf_base']+'_leaves'),t['leaf_color']),f'{A}/textures/block/{lv}.png')
    tx={"end":f"diaithir_planets:block/{lg}_top","side":f"diaithir_planets:block/{lg}"}
    wj(f'{A}/models/block/{lg}.json',{"parent":"minecraft:block/cube_column","textures":tx})
    wj(f'{A}/models/block/{lg}_horizontal.json',{"parent":"minecraft:block/cube_column_horizontal","textures":tx})
    wj(f'{A}/blockstates/{lg}.json',{"variants":{"axis=x":{"model":f"diaithir_planets:block/{lg}_horizontal","x":90,"y":90},"axis=y":{"model":f"diaithir_planets:block/{lg}"},"axis=z":{"model":f"diaithir_planets:block/{lg}_horizontal","x":90}}})
    wj(f'{A}/items/{lg}.json',{"model":{"type":"minecraft:model","model":f"diaithir_planets:block/{lg}"}})
    wj(f'{A}/models/block/{lv}.json',{"parent":"minecraft:block/leaves","textures":{"all":f"diaithir_planets:block/{lv}"}})
    simple_block(lv); loot(lg,'log'); loot(lv,'leaves')
    LANG[f'block.diaithir_planets.{lg}']=f'{nm} Log'; LANG[f'block.diaithir_planets.{lv}']=f'{nm} Leaves'
    TAGS['logs'].append(lg); TAGS['axe'].append(lg); TAGS['leaves'].append(lv); TAGS['hoe'].append(lv)
    IDS.extend([{"id":lg,"kind":"log"},{"id":lv,"kind":"leaves"}])
    pb=mk('plant','plant'); save(hue_shift(tex(pl['base']),pl['color']),f'{A}/textures/block/{pb}.png')
    wj(f'{A}/models/block/{pb}.json',{"parent":"minecraft:block/cross","textures":{"cross":f"diaithir_planets:block/{pb}"}})
    wj(f'{A}/models/item/{pb}.json',{"parent":"minecraft:item/generated","textures":{"layer0":f"diaithir_planets:block/{pb}"}})
    simple_block(pb,'item'); loot(pb,'plant'); LANG[f'block.diaithir_planets.{pb}']=pl['name']
    TAGS['small_flowers'].append(pb); IDS.append({"id":pb,"kind":"plant"})
    ob=mk('ore','ore'); save(hue_shift(tex(o['base']+'_ore'),o['color'],keep_green=False),f'{A}/textures/block/{ob}.png')
    wj(f'{A}/models/block/{ob}.json',{"parent":"minecraft:block/cube_all","textures":{"all":f"diaithir_planets:block/{ob}"}})
    simple_block(ob); loot(ob,'ore'); LANG[f'block.diaithir_planets.{ob}']=f'{o["name"]} Ore'
    TAGS['pick'].append(ob); IDS.append({"id":ob,"kind":"ore"})
    # features
    txt=(open(f'{VF}/cf_{t["shape"]}.json').read())
    txt=re.sub(r'minecraft:[a-z_]+_log',f'diaithir_planets:{lg}',txt); txt=re.sub(r'minecraft:[a-z_]+_leaves',f'diaithir_planets:{lv}',txt)
    wj(f'{D}/worldgen/configured_feature/{lg}_tree.json',json.loads(txt))
    pf=json.load(open(f'{VF}/pf_trees_plains.json')); pf['feature']=f'diaithir_planets:{lg}_tree'; pf['placement'][0]={"type":"minecraft:count","count":3}
    wj(f'{D}/worldgen/placed_feature/{lg}_tree.json',pf)
    st={"Name":f"diaithir_planets:{pb}"}
    wj(f'{D}/worldgen/configured_feature/{pb}.json',{"type":"minecraft:simple_block","config":{"to_place":{"type":"minecraft:simple_state_provider","state":st}}})
    wj(f'{D}/worldgen/placed_feature/{pb}.json',{"feature":f"diaithir_planets:{pb}","placement":[{"type":"minecraft:count","count":10},{"type":"minecraft:in_square"},{"type":"minecraft:heightmap","heightmap":"MOTION_BLOCKING_NO_LEAVES"},{"type":"minecraft:biome"},{"type":"minecraft:block_predicate_filter","predicate":{"type":"minecraft:all_of","predicates":[{"type":"minecraft:replaceable"},{"type":"minecraft:matching_fluids","fluids":"minecraft:empty"},{"type":"minecraft:would_survive","state":st}]}}]})
    S={"Name":f"diaithir_planets:{ob}"}
    wj(f'{D}/worldgen/configured_feature/{ob}.json',{"type":"minecraft:ore","config":{"discard_chance_on_air_exposure":0.0,"size":9,"targets":[{"state":S,"target":{"predicate_type":"minecraft:tag_match","tag":"minecraft:stone_ore_replaceables"}},{"state":S,"target":{"predicate_type":"minecraft:tag_match","tag":"minecraft:deepslate_ore_replaceables"}}]}})
    cnt={1:8,2:4,3:2}[o['rarity']]
    wj(f'{D}/worldgen/placed_feature/{ob}.json',{"feature":f"diaithir_planets:{ob}","placement":[{"type":"minecraft:count","count":cnt},{"type":"minecraft:in_square"},{"type":"minecraft:height_range","height":{"type":"minecraft:trapezoid","min_inclusive":{"absolute":-48},"max_inclusive":{"absolute":96}}},{"type":"minecraft:biome"}]})
    return {'veg':[f'diaithir_planets:{lg}_tree',f'diaithir_planets:{pb}'],'ore':[f'diaithir_planets:{ob}']}
if __name__=='__main__':
    ids=set(sys.argv[1:]); feats={}
    for f in sorted(glob.glob(SP+'/*.json')):
        pid=os.path.basename(f)[:-5]; sp=json.load(open(f))
        if 'flora' not in sp or (ids and pid not in ids): continue
        feats[pid]=emit(pid,sp)
    wj(f'{D}/planet_flora.json',IDS); wj('/mnt/project-files/planets/flora_features.json',feats)
    def tag(path,vals):
        p=f'{MC}/tags/block/{path}.json'; new=['diaithir_planets:'+v for v in vals]
        d=json.load(open(p)) if os.path.exists(p) else {"replace":False,"values":[]}
        d['values']=list(dict.fromkeys(d['values']+new)); wj(p,d)
    tag('logs',TAGS['logs']); tag('leaves',TAGS['leaves']); tag('mineable/axe',TAGS['axe']); tag('mineable/pickaxe',TAGS['pick']); tag('mineable/hoe',TAGS['hoe']); tag('small_flowers',TAGS['small_flowers']); tag('needs_stone_tool',TAGS['pick'])
    lp=f'{MODR}/assets/diaithir_planets/lang/en_us.json'; L=json.load(open(lp)); L.update(LANG); json.dump(L,open(lp,'w'),indent=0)
    print(len(IDS),'blocks for',len(feats),'planets')
