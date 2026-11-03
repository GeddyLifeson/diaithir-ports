#!/usr/bin/env python3
"""Blueprints (blueprints/*.json) + specs.json -> data pack under src/main/resources. Usage: gen.py"""
import gzip,io,json,os,re,struct,zlib,hashlib
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NS="diaithir_worldgen"
OUT=R+"/src/main/resources"
DV=4556  # data version placeholder; vanilla upgrades old templates on load

def tag(t,b): return bytes([t])+struct.pack(">H",len(b))+b
def ns(s): return struct.pack(">H",len(s.encode()))+s.encode()
class N:  # minimal NBT writer
    def __init__(s): s.b=io.BytesIO()
    def named(s,t,name,v): s.b.write(bytes([t])+ns(name)); s.pay(t,v)
    def pay(s,t,v):
        w=s.b.write
        if t==3: w(struct.pack(">i",v))
        elif t==8: w(ns(v))
        elif t==1: w(struct.pack(">b",v))
        elif t==9:
            et,items=v; w(bytes([et])+struct.pack(">i",len(items)))
            for i in items: s.pay(et,i)
        elif t==10:
            for k,(tt,vv) in v.items(): s.named(tt,k,vv)
            w(b"\0")
def nbt(root):
    n=N(); n.named(10,"",root); return gzip.compress(n.b.getvalue(),9,mtime=0)

def block(spec):
    m=re.match(r"([^\[]+)(?:\[(.*)\])?$",spec); name=m.group(1)
    if ":" not in name: name="minecraft:"+name
    props={}
    if m.group(2):
        for kv in m.group(2).split(","):
            if "=" in kv: k,v=kv.split("=",1); props[k.strip()]=(8,v.strip())
    return name,props

def template(bp):
    X,Y,Z=bp["size"]; pal={k:fixblock(v) for k,v in bp["palette"].items()}; pal[" "]="minecraft:air"
    chest={c["chest_char"]:c for c in bp.get("loot",[])}; spw={s["char"]:s for s in bp.get("spawners",[])}
    keys=[];blocks=[]
    def pid(k):
        if k not in keys: keys.append(k)
        return keys.index(k)
    for y,layer in enumerate(bp["layers"][:Y]):
        for z,row in enumerate(layer[:Z]):
            for x,ch in enumerate(row[:X]):
                if ch==" ": continue
                if ch in chest:
                    spec=pal.get(ch,"minecraft:chest"); e={"id":(8,"minecraft:chest"),"LootTable":(8,f"{NS}:chests/{bp['id']}")}
                elif ch in spw:
                    spec="minecraft:spawner"; e={"id":(8,"minecraft:mob_spawner"),"SpawnData":(10,{"entity":(10,{"id":(8,spw[ch]["entity"])})})}
                elif ch in pal: spec=pal[ch]; e=None
                else: continue
                ent={"pos":(9,(3,[x,y,z])),"state":(3,pid(spec))}
                if e: ent["nbt"]=(10,e)
                blocks.append(ent)
    pal_l=[]
    for k in keys:
        n,p=block(k); d={"Name":(8,n)}
        if p: d["Properties"]=(10,p)
        pal_l.append(d)
    root={"size":(9,(3,[X,Y,Z])),"palette":(9,(10,pal_l)),"blocks":(9,(10,blocks)),"entities":(9,(10,[])),"DataVersion":(3,DV)}
    return nbt(root)

def _reg(n): return set(json.load(open(R+"/tools/registries/"+n+".json")))
BL,IT=_reg("block"),_reg("item")
BFIX={"blackstone_bricks":"polished_blackstone_bricks","chain":"iron_chain","cut_sandstone_stairs":"sandstone_stairs","door_placeholder_unused":"air","infested_end_stone_bricks":"end_stone_bricks","sandstone_bricks":"cut_sandstone"}
IFIX={"pottery_sherd_angler":"angler_pottery_sherd","smithing_template":"netherite_upgrade_smithing_template"}
def fixblock(spec):
    if re.match(r"(?!minecraft:)[a-z0-9_.-]+:",spec): return spec  # other mods' blocks (e.g. diaithirbosses:ogham_stone)
    m=re.match(r"(?:minecraft:)?([^\[]+)(.*)$",spec); n,rest=m.group(1),m.group(2)
    n=BFIX.get(n,n)
    if n not in BL: n="stone_bricks"; rest=""
    return "minecraft:"+n+rest
DIM={"overworld":"minecraft:overworld","nether":"minecraft:the_nether","end":"minecraft:the_end"}
CANON={"overworld":["#minecraft:is_overworld"],"ocean":["#minecraft:is_ocean"],"deep_ocean":["#minecraft:is_deep_ocean"],"river":["#minecraft:is_river"],"beach":["#minecraft:is_beach"],"mountain":["#minecraft:is_mountain"],"badlands":["#minecraft:is_badlands"],"forest":["#minecraft:is_forest"],"taiga":["#minecraft:is_taiga"],"jungle":["#minecraft:is_jungle"],"savanna":["#minecraft:is_savanna"],"hill":["#minecraft:is_hill"],"nether":["#minecraft:is_nether"],"end":["#minecraft:is_end"],
"desert":["minecraft:desert"],"plains":["minecraft:plains","minecraft:sunflower_plains","minecraft:meadow"],"snowy":["minecraft:snowy_plains","minecraft:ice_spikes","minecraft:snowy_taiga","minecraft:snowy_slopes","minecraft:frozen_peaks","minecraft:grove","minecraft:snowy_beach"],"swamp":["minecraft:swamp","minecraft:mangrove_swamp"],"warm_ocean":["minecraft:warm_ocean","minecraft:lukewarm_ocean","minecraft:deep_lukewarm_ocean"],"cold_ocean":["minecraft:cold_ocean","minecraft:deep_cold_ocean","minecraft:frozen_ocean","minecraft:deep_frozen_ocean"],"nether_wastes":["minecraft:nether_wastes"],"soul_sand_valley":["minecraft:soul_sand_valley"],"crimson":["minecraft:crimson_forest"],"warped":["minecraft:warped_forest"],"basalt":["minecraft:basalt_deltas"],"end_highlands":["minecraft:end_highlands","minecraft:end_midlands"],"stony":["minecraft:stony_peaks","minecraft:dripstone_caves"],"dark_forest":["minecraft:dark_forest"]}
RULES=[("end_highlands","end_highlands"),("end","end"),("soul","soul_sand_valley"),("warped","warped"),("crimson","crimson"),("basalt","basalt"),("nether_wastes","nether_wastes"),("nether","nether"),("deep_ocean","deep_ocean"),("warm_ocean","warm_ocean"),("cold_ocean","cold_ocean"),("ocean","ocean"),("coast","beach"),("beach","beach"),("river","river"),("swamp","swamp"),("mangrove","swamp"),("jungle","jungle"),("savanna","savanna"),("badland","badlands"),("mesa","badlands"),("desert","desert"),("snow","snowy"),("frozen","snowy"),("taiga","taiga"),("dark_oak","dark_forest"),("forest","forest"),("plains","plains"),("hill","hill"),("mountain","mountain"),("peak","stony"),("stony","stony"),("dripstone","stony")]
def biome_keys(s,dim):
    s=s.lower(); ks=[k for p,k in RULES if p in s]
    if not ks: ks=["nether"] if dim=="nether" else ["end"] if dim=="end" else ["overworld"]
    return list(dict.fromkeys(ks))
def w(path,data):
    p=os.path.join(OUT,path); os.makedirs(os.path.dirname(p),exist_ok=True)
    open(p,"wb" if isinstance(data,bytes) else "w").write(data if isinstance(data,bytes) else json.dumps(data,indent=1))

ROM=["I","II","III","IV","V","VI","VII","VIII"]
def age(i,s):
    t=(i+" "+s.get("layout","")).lower()
    for ks,a in ((("stone circle","passage tomb","megalith","cairn","cave","mine"),"I"),(("temple","pyramid","tomb","oracle","ziggurat","moai","shrine","ruin"),"II"),(("castle","fort","keep","tower","abbey","manor","tavern"),"IV"),(("airship","floating","sky"),"VI"),(("lab","bunker","factory","city"),"VII")):
        if any(k in t for k in ks): return a
    return "III"
def integrate(meta,ids):
    cod=[];quests=[];ev=[];byage={}
    for i in ids:
        f,d,s=meta[i]; a=age(i,s); byage.setdefault(a,[]).append(f"{NS}:{i}")
        nm=i.replace("_"," ").title()
        cod.append({"id":f"{NS}:{i}","kind":"structure","name":nm,"text":s.get("layout",""),"dimension":s["dimension"],"biomes":s["biome_tag"],"loot":s.get("loot_theme"),"mobs":s.get("mobs"),"age":a,"quest":f"{NS}:find/{i}","connects":["tags:"+NS+":s/"+i,"events:diaithir:calendar_year","loot:"+NS+":chests/"+i]})
        quests.append({"id":f"{NS}:find/{i}","title":"Find "+nm,"age":a,"goal":{"type":"locate_structure","structure":f"{NS}:{i}"},"next":f"{NS}:find/{s['merge_with']}" if False else None})
        ev.append({"structure":f"{NS}:{i}","ruin_states":["intact","looted","overgrown"]})
    for a,l in byage.items(): w(f"data/{NS}/tags/worldgen/structure/age_{a.lower()}.json",{"values":l})
    w(f"data/{NS}/tags/worldgen/structure/all.json",{"values":[f"#{NS}:age_{a.lower()}" for a in byage]})
    w(f"data/{NS}/codex/structures.json",cod); w(f"data/{NS}/quests/structures.json",quests)
    w(f"data/{NS}/diaithir_events.json",{"listens":["diaithir:calendar_year","diaithir:disaster","diaithir:war","diaithir:raid"],"fires":["diaithir:structure_discovered"],"ruin_states":ev})
def main():
    specs=json.load(open(R+"/tools/specs.json")); meta={}
    for f,d in specs.items():
        for s in d["structures"]: meta.setdefault(s["id"],(f,d,s))
    sets=[];mig={};usedk=set()
    for fn in sorted(os.listdir(R+"/blueprints")):
        bp=json.load(open(R+"/blueprints/"+fn)); i=fn[:-5]; bp["id"]=i
        if i not in meta: continue
        f,d,s=meta[i]
        try: w(f"data/{NS}/structure/{i}.nbt",template(bp))
        except Exception as e: print("skip",i,e); continue
        w(f"data/{NS}/worldgen/template_pool/{i}.json",{"name":f"{NS}:{i}","fallback":"minecraft:empty","elements":[{"weight":1,"element":{"element_type":"minecraft:single_pool_element","location":f"{NS}:{i}","projection":"rigid","processors":"minecraft:empty"}}]})
        ks=biome_keys(s["biome_tag"],s["dimension"]); usedk.update(ks)
        w(f"data/{NS}/tags/worldgen/biome/s/{i}.json",{"values":[s["biome_tag"]] if s["biome_tag"].startswith("#"+NS+":") else [f"#{NS}:b/{k}" for k in ks]}); tg=f"{NS}:s/{i}"
        adj={"surface":"beard_thin","buried":"none","underwater":"beard_thin"}.get(bp.get("surface"),"none")
        step={"surface":"surface_structures","buried":"underground_structures","underwater":"surface_structures","nether_lava":"underground_structures"}.get(bp.get("surface"),"surface_structures")
        w(f"data/{NS}/worldgen/structure/{i}.json",{"type":"minecraft:jigsaw","biomes":"#"+tg,"step":step,"spawn_overrides":{},"terrain_adaptation":adj,"start_pool":f"{NS}:{i}","size":1,"start_height":{"absolute":bp.get("y_offset",0)},"project_start_to_heightmap":"WORLD_SURFACE_WG" if bp.get("surface")=="surface" else "OCEAN_FLOOR_WG" if bp.get("surface")=="underwater" else None,"max_distance_from_center":16,"use_expansion_hack":False} if False else {"type":"minecraft:jigsaw","biomes":"#"+tg,"step":step,"spawn_overrides":{},"terrain_adaptation":adj,"start_pool":f"{NS}:{i}","size":1,"start_height":{"absolute":bp.get("y_offset",0)},**({"project_start_to_heightmap":"WORLD_SURFACE_WG"} if bp.get("surface")=="surface" else {"project_start_to_heightmap":"OCEAN_FLOOR_WG"} if bp.get("surface")=="underwater" else {}),"max_distance_from_center":16,"use_expansion_hack":False})
        h=int(hashlib.md5(i.encode()).hexdigest()[:6],16)
        sp=int(re.findall(r"\d+",s.get("rarity","")+" 28 10")[0]); sp=max(sp,12); sep=max(4,min(sp-2,int(re.findall(r"\d+",s.get("rarity","")+" 28 10")[1] if len(re.findall(r"\d+",s.get("rarity","")))>1 else sp//3)))
        w(f"data/{NS}/worldgen/structure_set/{i}.json",{"structures":[{"structure":f"{NS}:{i}","weight":1}],"placement":{"type":"minecraft:random_spread","spacing":sp,"separation":sep,"salt":h}})
        lt=bp.get("loot",[{}])[0] if bp.get("loot") else None
        if lt:
            ents=[]
            for e in lt.get("entries",[]):
                n=e["item"].replace("minecraft:",""); n=IFIX.get(n,n)
                if n in IT: e=dict(e,item="minecraft:"+n); ents.append(e)
            lt=dict(lt,entries=ents)
        if lt and lt.get("entries"):
            w(f"data/{NS}/loot_table/chests/{i}.json",{"type":"minecraft:chest","pools":[{"rolls":{"type":"minecraft:uniform","min":lt.get("rolls",[2,5])[0],"max":lt.get("rolls",[2,5])[1]},"entries":[{"type":"minecraft:item","name":e["item"],"weight":e["weight"],"functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":e["min"],"max":e["max"]}}]} for e in lt["entries"]]}]})
        mig[i]={"from_mod":d.get("old_id_prefix"),"source_file":f,"merge_with":s.get("merge_with")}
        sets.append(i)
    idmap=json.load(open(R+"/tools/id_map.json")) if os.path.exists(R+"/tools/id_map.json") else {}
    w("migration.json",{"registry":"worldgen/structure","old_to_new":{o:n for o,n in idmap.items() if n.split(":")[1] in mig or n.endswith(":none")},"new_from_mod":mig})
    for k in usedk|{"overworld"}: w(f"data/{NS}/tags/worldgen/biome/b/{k}.json",{"values":CANON[k]})
    w(f"data/{NS}/tags/worldgen/biome/never.json",{"values":[]})
    w(f"data/{NS}/worldgen/template_pool/none.json",{"name":f"{NS}:none","fallback":"minecraft:empty","elements":[]})
    w(f"data/{NS}/worldgen/structure/none.json",{"type":"minecraft:jigsaw","biomes":f"#{NS}:never","step":"surface_structures","spawn_overrides":{},"terrain_adaptation":"none","start_pool":f"{NS}:none","size":1,"start_height":{"absolute":0},"max_distance_from_center":16,"use_expansion_hack":False})
    integrate(meta,sets)
    print(len(sets),"structures")
if __name__=="__main__": main()
