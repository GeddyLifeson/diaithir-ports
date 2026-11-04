#!/usr/bin/env python3
"""Ascendant rite sites (diaithir-bosses #125): writes blueprints/*.json, a specs.json entry and the biome tags. Then run gen.py. Additive."""
import json,math,os,random
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
NS="diaithir_worldgen"
STONE="diaithirbosses:ogham_stone"
# Diaithiris names via diaithiris-archaic archaic.py from modern Goidelic: cearcall > kierkuld, clach > klach, talla > talda, fleadh > flied, tobhta > tobta
NATIVE={"ogham_stone_circle":"Kierkuld Klachun","ogham_great_circle":"Mòr Kierkuld Klachun","ogham_hollow_ring":"Kierkuld Doiri",
        "ruined_feast_hall":"Tobta Talda Flied","ruined_feast_hall_long":"Tobta Talda Fada Flied"}
DISHES=[{"item":"minecraft:cake","weight":6,"min":1,"max":1},{"item":"minecraft:pumpkin_pie","weight":10,"min":1,"max":3},
        {"item":"minecraft:suspicious_stew","weight":8,"min":1,"max":2},{"item":"minecraft:rabbit_stew","weight":10,"min":1,"max":3}]
FILL=[{"item":"minecraft:bread","weight":8,"min":2,"max":6},{"item":"minecraft:honey_bottle","weight":4,"min":1,"max":3},
      {"item":"minecraft:bone","weight":8,"min":4,"max":16},{"item":"minecraft:candle","weight":3,"min":1,"max":3}]

def grid(X,Y,Z): return [[[" "]*X for _ in range(Z)] for _ in range(Y)]
def out(g): return [["".join(r) for r in layer] for layer in g]

def circle(i,r,outer,moss):
    """Nine ogham stones, rune n at angle n*40 deg (all within 24 blocks of the centre plinth, as Ritual.seal needs)."""
    S=2*(r+2)+1; c=S//2; g=grid(S,6,S)
    pal={"g":"minecraft:grass_block","p":"minecraft:coarse_dirt","m":"minecraft:mossy_cobblestone","b":"minecraft:stone_bricks","B":"minecraft:cracked_stone_bricks",
         "s":"minecraft:smooth_stone","w":"minecraft:mossy_cobblestone_wall","l":"minecraft:lantern[hanging=false]","t":"minecraft:cobblestone_wall","c":"minecraft:chest[facing=south]",
         "f":"minecraft:fern","k":"minecraft:stone_brick_slab"}
    for n in range(9):
        pal[str(n)]=f"{STONE}[rune={n}]"
    rnd=random.Random(i)
    for z in range(S):
        for x in range(S):
            d=math.hypot(x-c,z-c)
            if d<=r+1.5: g[0][z][x]="p" if rnd.random()<.3 else "g"
    for y in (0,):  # plinth: 3x3 stone-brick pad, centre left free for the player-raised altar
        for dz in (-1,0,1):
            for dx in (-1,0,1): g[0][c+dz][c+dx]="b" if (dx or dz) else "s"
    for dz,dx in ((-1,-1),(-1,1),(1,-1),(1,1)): g[1][c+dz][c+dx]="l"
    for n in range(9):
        a=math.radians(n*40-90); x=round(c+r*math.cos(a)); z=round(c+r*math.sin(a))
        g[1][z][x]=str(n); g[2][z][x]=rnd.choice("mw"); 
        if moss: g[3][z][x]="w"
    if outer:  # unmarked menhirs between the rune stones, plain markers only
        for n in range(18):
            a=math.radians(n*20-80); x=round(c+(r+1)*math.cos(a)); z=round(c+(r+1)*math.sin(a))
            if g[1][z][x]==" ": g[1][z][x]="m"; g[2][z][x]=rnd.choice("m w".replace(" ",""))
    for _ in range(S):
        x,z=rnd.randrange(S),rnd.randrange(S)
        if g[0][z][x] in "gp" and g[1][z][x]==" " and math.hypot(x-c,z-c)>2: g[1][z][x]="f"
    return {"id":i,"size":[S,6,S],"palette":pal,"layers":out(g),"loot":[],"spawners":[],"surface":"surface","y_offset":-1}

def hall(i,W,L,long):
    X,Y,Z=W,8,L; g=grid(X,Y,Z); rnd=random.Random(i)
    pal={"B":"minecraft:stone_bricks","C":"minecraft:cracked_stone_bricks","M":"minecraft:mossy_stone_bricks","O":"minecraft:oak_log[axis=y]","P":"minecraft:spruce_planks",
         "S":"minecraft:oak_slab[type=bottom]","F":"minecraft:oak_fence","H":"minecraft:campfire[lit=false]","c":"minecraft:chest[facing=south]","b":"minecraft:barrel[facing=up]",
         "T":"minecraft:spruce_slab[type=top]","w":"minecraft:cobweb","l":"minecraft:lantern[hanging=false]","d":"minecraft:coarse_dirt","r":"minecraft:red_carpet","q":"minecraft:candle","k":"minecraft:cake",
         "s":"minecraft:stone_brick_stairs[facing=south]","p":"minecraft:smooth_stone"}
    for z in range(Z):
        for x in range(X):
            g[0][z][x]="P" if 0<x<X-1 and 0<z<Z-1 else "M"
    h=Y-2
    for z in range(Z):
        for x in range(X):
            edge=x in(0,X-1) or z in(0,Z-1)
            if not edge: continue
            for y in range(1,h):
                corner=(x in(0,X-1)) and (z in(0,Z-1)); pillar=(x in(0,X-1) and z%4==0) or (z in(0,Z-1) and x%4==0)
                if corner or pillar: ch="O"
                else: ch=rnd.choice("BBMC")
                # ruin: broken tops and doorway gaps
                if y>=h-2 and rnd.random()<.45: continue
                if z==0 and abs(x-X//2)<=1 and y<=3: continue
                g[y][z][x]=ch
    # long table and benches down the middle
    tx=[X//2-1,X//2+1] if X>=9 else [X//2]
    for z in range(3,Z-5):
        for x in tx:
            g[1][z][x]="S" if rnd.random()>.15 else " "
        if z%3==0:
            for x in (X//2-3,X//2+3):
                if 0<x<X-1: g[1][z][x]="T"
        if z%4==1: g[2][z][X//2]="q"
    g[1][Z//2][X//2]="H"
    for z in range(1,Z-1,4): g[0][z][X//2]="p"
    # dais at the far end with the offering chests
    for x in range(2,X-2):
        g[1][Z-3][x]="p"; g[1][Z-2][x]="p"
    chest=[x for x in range(3,X-3,2)]
    for k,x in enumerate(chest): g[2][Z-2][x]="c" if k%2==0 else "b"
    g[2][Z-2][X//2]="c"
    g[2][Z-3][X//2]="k"; g[3][Z-2][1]="l"; g[3][Z-2][X-2]="l"
    for _ in range(X*Z//10):
        x,z,y=rnd.randrange(1,X-1),rnd.randrange(1,Z-1),rnd.randrange(3,h)
        if g[y][z][x]==" ": g[y][z][x]="w"
    loot=[{"chest_char":"c","table":f"{NS}:chests/{i}","entries":DISHES+FILL,"rolls":[3,5] if not long else [4,7]}]
    return {"id":i,"size":[X,Y,Z],"palette":pal,"layers":out(g),"loot":loot,"spawners":[],"surface":"surface","y_offset":-1}

SITES={
 "ogham_stone_circle":(lambda:circle("ogham_stone_circle",6,False,False),"#diaithir_worldgen:has_ogham_circle","stone circle of nine ogham stones around a plinth for the Ascendant altar","spacing 36 separation 14"),
 "ogham_great_circle":(lambda:circle("ogham_great_circle",10,True,True),"#diaithir_worldgen:has_ogham_great_circle","great stone circle: nine ogham stones with a ring of plain menhirs and moss walls","spacing 64 separation 24"),
 "ogham_hollow_ring":(lambda:circle("ogham_hollow_ring",8,False,True),"#diaithir_worldgen:has_ogham_hollow_ring","stone circle hidden in a wet wood, nine ogham stones draped in moss","spacing 48 separation 18"),
 "ruined_feast_hall":(lambda:hall("ruined_feast_hall",11,19,False),"#diaithir_worldgen:has_feast_hall","ruin of a feast hall with a long table, hearth and a dais of offering-dish chests","spacing 40 separation 16"),
 "ruined_feast_hall_long":(lambda:hall("ruined_feast_hall_long",13,29,True),"#diaithir_worldgen:has_feast_hall_long","great ruined feast hall, two tables, hearth and a dais of offering-dish chests","spacing 72 separation 28"),
}
TAGS={ # vanilla/c biome tags plus the Ascendant biomes from PR #131 (optional; missing ids are ignored)
 "has_ogham_circle":["#minecraft:is_forest","#minecraft:is_taiga","minecraft:plains","minecraft:meadow",{"id":"diaithir_biomes:thornwake","required":False}],
 "has_ogham_great_circle":["#minecraft:is_mountain","#minecraft:is_taiga","minecraft:windswept_hills","minecraft:meadow",{"id":"diaithir_biomes:the_unwound","required":False}],
 "has_ogham_hollow_ring":["minecraft:swamp","minecraft:mangrove_swamp","minecraft:dark_forest","minecraft:old_growth_birch_forest",{"id":"diaithir_biomes:thornwake","required":False},{"id":"diaithir_biomes:cinderfen","required":False}],
 "has_feast_hall":["minecraft:plains","minecraft:meadow","#minecraft:is_forest","minecraft:savanna",{"id":"diaithir_biomes:thornwake","required":False}],
 "has_feast_hall_long":["#minecraft:is_taiga","#minecraft:is_mountain","minecraft:plains",{"id":"diaithir_biomes:cinderfen","required":False}],
}
def main():
    sp=json.load(open(R+"/tools/specs.json")); key="ascendant-rite-sites (diaithir-bosses #125)"
    ent=[]
    for i,(mk,bt,lay,rar) in SITES.items():
        json.dump(mk(),open(f"{R}/blueprints/{i}.json","w"))
        ent.append({"id":i,"biome_tag":bt,"dimension":"overworld","footprint":mk()["size"],"layout":lay,"loot_theme":"offering dishes (cake, pumpkin pie, stews)" if "hall" in i else "none","mobs":"none","rarity":rar,"merge_with":None,"native_name":NATIVE[i]})
    sp[key]={"mod":"diaithir-worldgen-structures","old_id_prefix":"diaithir_ascendant_sites","notes":"New for Ascendant rites. Stones use diaithirbosses:ogham_stone[rune=0..8] in rune order; all within 24 blocks of the plinth (Ritual.seal limit). Feast chests hold items of #diaithirbosses:offering_feast.","structures":ent}
    json.dump(sp,open(R+"/tools/specs.json","w"),indent=1)
    d=f"{R}/src/main/resources/data/{NS}/tags/worldgen/biome"; os.makedirs(d,exist_ok=True)
    for k,v in TAGS.items(): json.dump({"values":v},open(f"{d}/{k}.json","w"),indent=1)
    json.dump(NATIVE,open(R+"/tools/rite_native_names.json","w"),indent=1,ensure_ascii=False)
if __name__=="__main__": main()
