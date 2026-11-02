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
    X,Y,Z=bp["size"]; pal=dict(bp["palette"]); pal[" "]="minecraft:air"
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

DIM={"overworld":"minecraft:overworld","nether":"minecraft:the_nether","end":"minecraft:the_end"}
def biome_tag(s,dim):
    s=s.lower()
    if dim=="nether": return "minecraft:is_nether"
    if dim=="end": return "minecraft:is_end"
    for k,v in (("ocean","is_ocean"),("deep","is_deep_ocean"),("river","is_river"),("desert","is_badlands" if "badland" in s else "is_overworld"),("jungle","is_jungle"),("taiga","is_taiga"),("mountain","is_mountain"),("forest","is_forest"),("swamp","is_overworld"),("plain","is_overworld"),("savanna","is_savanna"),("badland","is_badlands"),("snow","is_taiga")):
        if k in s: return "minecraft:"+v
    return "minecraft:is_overworld"
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
        cod.append({"id":f"{NS}:{i}","kind":"structure","name":nm,"text":s.get("layout",""),"dimension":s["dimension"],"biomes":s["biome_tag"],"loot":s.get("loot_theme"),"mobs":s.get("mobs"),"age":a,"quest":f"{NS}:find/{i}","connects":["tags:"+biome_tag(s["biome_tag"],s["dimension"]),"events:diaithir:calendar_year","loot:"+NS+":chests/"+i]})
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
    sets=[];mig={}
    for fn in sorted(os.listdir(R+"/blueprints")):
        bp=json.load(open(R+"/blueprints/"+fn)); i=fn[:-5]; bp["id"]=i
        if i not in meta: continue
        f,d,s=meta[i]
        try: w(f"data/{NS}/structure/{i}.nbt",template(bp))
        except Exception as e: print("skip",i,e); continue
        w(f"data/{NS}/worldgen/template_pool/{i}.json",{"name":f"{NS}:{i}","fallback":"minecraft:empty","elements":[{"weight":1,"element":{"element_type":"minecraft:single_pool_element","location":f"{NS}:{i}","projection":"rigid","processors":"minecraft:empty"}}]})
        tg=s["biome_tag"].lstrip("#") if s["biome_tag"].startswith("#") and ":" in s["biome_tag"] else biome_tag(s["biome_tag"],s["dimension"])
        adj={"surface":"beard_thin","buried":"none","underwater":"beard_thin"}.get(bp.get("surface"),"none")
        step={"surface":"surface_structures","buried":"underground_structures","underwater":"surface_structures","nether_lava":"underground_structures"}.get(bp.get("surface"),"surface_structures")
        w(f"data/{NS}/worldgen/structure/{i}.json",{"type":"minecraft:jigsaw","biomes":"#"+tg,"step":step,"spawn_overrides":{},"terrain_adaptation":adj,"start_pool":f"{NS}:{i}","size":1,"start_height":{"absolute":bp.get("y_offset",0)},"project_start_to_heightmap":"WORLD_SURFACE_WG" if bp.get("surface")=="surface" else "OCEAN_FLOOR_WG" if bp.get("surface")=="underwater" else None,"max_distance_from_center":16,"use_expansion_hack":False} if False else {"type":"minecraft:jigsaw","biomes":"#"+tg,"step":step,"spawn_overrides":{},"terrain_adaptation":adj,"start_pool":f"{NS}:{i}","size":1,"start_height":{"absolute":bp.get("y_offset",0)},**({"project_start_to_heightmap":"WORLD_SURFACE_WG"} if bp.get("surface")=="surface" else {"project_start_to_heightmap":"OCEAN_FLOOR_WG"} if bp.get("surface")=="underwater" else {}),"max_distance_from_center":16,"use_expansion_hack":False})
        h=int(hashlib.md5(i.encode()).hexdigest()[:6],16)
        sp=int(re.findall(r"\d+",s.get("rarity","")+" 28 10")[0]); sp=max(sp,12); sep=max(4,min(sp-2,int(re.findall(r"\d+",s.get("rarity","")+" 28 10")[1] if len(re.findall(r"\d+",s.get("rarity","")))>1 else sp//3)))
        w(f"data/{NS}/worldgen/structure_set/{i}.json",{"structures":[{"structure":f"{NS}:{i}","weight":1}],"placement":{"type":"minecraft:random_spread","spacing":sp,"separation":sep,"salt":h}})
        lt=bp.get("loot",[{}])[0] if bp.get("loot") else None
        if lt and lt.get("entries"):
            w(f"data/{NS}/loot_table/chests/{i}.json",{"type":"minecraft:chest","pools":[{"rolls":{"type":"minecraft:uniform","min":lt.get("rolls",[2,5])[0],"max":lt.get("rolls",[2,5])[1]},"entries":[{"type":"minecraft:item","name":e["item"],"weight":e["weight"],"functions":[{"function":"minecraft:set_count","count":{"type":"minecraft:uniform","min":e["min"],"max":e["max"]}}]} for e in lt["entries"]]}]})
        mig[i]={"from_mod":d.get("old_id_prefix"),"source_file":f,"merge_with":s.get("merge_with")}
        sets.append(i)
    idmap=json.load(open(R+"/tools/id_map.json")) if os.path.exists(R+"/tools/id_map.json") else {}
    w("migration.json",{"registry":"worldgen/structure","old_to_new":{o:n for o,n in idmap.items() if n.split(":")[1] in mig},"new_from_mod":mig})
    integrate(meta,sets)
    print(len(sets),"structures")
if __name__=="__main__": main()
