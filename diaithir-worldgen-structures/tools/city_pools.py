"""Procedural city template pools replacing big_lost_city:* pools. Writes pool blueprints (same format as blueprints/)."""
import json,os,math
R=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
def grid(X,Y,Z): return [[[" "]*X for _ in range(Z)] for _ in range(Y)]  # [y][z][x]
def out(name,g,pal,X,Y,Z):
    layers=[["".join(r) for r in layer] for layer in g]
    os.makedirs(R+"/pools",exist_ok=True)
    json.dump({"id":name,"size":[X,Y,Z],"palette":pal,"layers":layers,"loot":[],"spawners":[]},open(f"{R}/pools/{name}.json","w"))
def tower(name,W,D,H,wall,glass,roof,floor,trim):
    g=grid(W,H,D)
    for y in range(H):
        for z in range(D):
            for x in range(W):
                edge=x in(0,W-1) or z in(0,D-1)
                if y==0: g[y][z][x]="t" if edge else "f"
                elif y==H-1: g[y][z][x]="r"
                elif y%4==0: g[y][z][x]="f"
                elif edge:
                    corner=x in(0,W-1) and z in(0,D-1)
                    g[y][z][x]="w" if corner or y%4==1 and False else ("g" if y%4 in(2,3) and not corner else "w")
    for y in (1,2): g[y][0][W//2]=" "
    for y in (1,2): g[y][0][W//2-1]=" "
    pal={"t":trim,"f":floor,"w":wall,"g":glass,"r":roof}
    out(name,g,pal,W,H,D)
def wheel(name):
    N=23;g=grid(N,N,5);c=N//2;r=10
    for a in range(0,360,3):
        x=c+round(r*math.cos(math.radians(a)));y=c+round(r*math.sin(math.radians(a)))
        for z in (1,3): g[y][z][x]="m"
    for a in range(0,360,45):
        for k in range(1,r):
            x=c+round(k*math.cos(math.radians(a)));y=c+round(k*math.sin(math.radians(a)));g[y][2][x]="b"
        x=c+round(r*math.cos(math.radians(a)));y=c+round(r*math.sin(math.radians(a)))
        for dy in (0,-1,-2):
            if 0<=y+dy<N: g[y+dy][2][x]="q"
    g[c][2][c]="m"
    for y in range(0,c):
        g[y][0][c-4+ (0)]="m" if False else g[y][0][c-4]
    for y in range(0,c+1):
        g[y][1][c-3]="m";g[y][3][c-3]="m";g[y][1][c+3]="m";g[y][3][c+3]="m"
    out(name,g,{"m":"minecraft:iron_bars","b":"minecraft:chain" if False else "minecraft:iron_bars","q":"minecraft:quartz_block"},N,N,5)
def plant(name):
    X,Y,Z=21,32,13;g=grid(X,Y,Z)
    for z in range(Z):
        for x in range(15):
            e=x in(0,14) or z in(0,Z-1)
            for y in range(8):
                g[y][z][x]="c" if y==0 else ("w" if e and y<7 else ("c" if y==7 else " "))
    for y in range(2,6): g[y][0][7]=" "
    for y in range(3,5):
        for x in range(1,14,2): g[y][0][x]="g"
    for cx in (17,):
        for cz in (2,9):
            for y in range(32):
                for dz in range(3):
                    for dx in range(3):
                        ring=not(dx==1 and dz==1)
                        if ring: g[y][cz+dz-1][cx+dx-1]="k" if y<30 else "h"
    for x in range(15,17): g[6][3][x]="p";g[6][10][x]="p"
    out(name,g,{"c":"minecraft:stone_bricks","w":"minecraft:gray_concrete","g":"minecraft:gray_stained_glass","k":"minecraft:bricks","h":"minecraft:blackstone","p":"minecraft:iron_bars"},X,Y,Z)
tower("blackskyscraper",15,15,44,"minecraft:black_concrete","minecraft:black_stained_glass","minecraft:polished_blackstone","minecraft:gray_concrete","minecraft:polished_blackstone_bricks")
tower("redskyscraper",13,13,38,"minecraft:red_terracotta","minecraft:red_stained_glass","minecraft:stone_bricks","minecraft:smooth_stone","minecraft:bricks")
tower("tallskyscraper",11,11,60,"minecraft:light_gray_concrete","minecraft:light_blue_stained_glass","minecraft:smooth_stone","minecraft:stone","minecraft:iron_block")
wheel("ferriswheel");plant("powerplant")
