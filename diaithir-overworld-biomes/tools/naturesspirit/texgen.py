# Procedural redraw of Nature's Spirit textures. Reads only dimensions, mean colour and transparency fraction of the
# (ARR) source file as a palette hint; every pixel is synthesised here. Writes PNGs + TEXTURE_MANIFEST.csv.
import sys,os,glob,random,csv,hashlib,colorsys
from PIL import Image
import numpy as np
SRC='/tmp/ns/src/src/main/resources/assets/natures_spirit/textures'
OUT=sys.argv[1]  # .../assets/natures_spirit/textures
def stats(p):
    im=Image.open(p).convert('RGBA'); a=np.asarray(im).astype(float)
    m=a[...,3]>10
    if not m.any(): return im.size,(128,128,128),0.0
    rgb=a[...,:3][m].mean(0)
    return im.size,tuple(int(x) for x in rgb),float(m.mean())
def hs(rgb,dl=0.0,ds=0.0,dh=0.0):
    h,l,s=colorsys.rgb_to_hls(*[c/255 for c in rgb]); 
    r=colorsys.hls_to_rgb((h+dh)%1,min(1,max(0,l+dl)),min(1,max(0,s+ds)))
    return tuple(int(c*255) for c in r)
def put(px,x,y,c,a=255):
    w,h=px.shape[1],px.shape[0]
    if 0<=x<w and 0<=y<h: px[y,x]=(*c,a)
def gen(name,cat,size,rgb,cov,rng):
    w,h=size; px=np.zeros((h,w,4),np.uint8)
    pal=[hs(rgb,-0.12),hs(rgb,-0.05),rgb,hs(rgb,0.06),hs(rgb,0.13)]
    n=name
    def noise(bias=2):
        for y in range(h):
            for x in range(w): px[y,x]=(*pal[min(4,max(0,int(rng.gauss(bias,0.9))))],255)
    if cat=='block' and cov>0.97 or cat in ('entity','gui') and cov>0.97:
        if 'log_top' in n or n.endswith('_top') and 'log' in n or 'wood' in n and 'log' not in n and False:
            noise(); cx=cy=w/2
            for y in range(h):
                for x in range(w):
                    d=max(abs(x+.5-cx),abs(y+.5-cy)); 
                    if int(d)%3==0: px[y,x]=(*pal[0],255)
                    elif d<2: px[y,x]=(*pal[4],255)
            return px,'log_top'
        if 'log' in n or 'wood' in n or 'stem' in n or 'bundle' in n:
            for x in range(w):
                col=pal[rng.choice([0,1,1,2,2,3])]
                for y in range(h):
                    px[y,x]=(*(col if rng.random()>.15 else pal[rng.choice([0,3])]),255)
            return px,'bark'
        if 'planks' in n or 'mosaic' in n or 'door' in n or 'trapdoor' in n or 'boat' in n or 'sign' in n:
            bh=max(2,h//4)
            for y in range(h):
                for x in range(w):
                    c=pal[2+((y//bh+x//(w//2 or 1)*(1 if y//bh%2 else 0))%2)-1+ (1 if rng.random()<.12 else 0)]
                    if y%bh==bh-1: c=pal[0]
                    px[y,x]=(*c,255)
            return px,'boards'
        if 'brick' in n or 'tile' in n:
            bh=max(3,h//4); bw=max(4,w//2)
            for y in range(h):
                for x in range(w):
                    off=(bw//2) if (y//bh)%2 else 0
                    c=pal[2+rng.choice([-1,0,0,1])]
                    if y%bh==bh-1 or (x+off)%bw==bw-1: c=pal[0]
                    px[y,x]=(*c,255)
            return px,'bricks'
        if 'ore' in n:
            noise(2); 
            for _ in range(7):
                x,y=rng.randrange(1,w-1),rng.randrange(1,h-1)
                for dx,dy in ((0,0),(1,0),(0,1)): put(px,x+dx,y+dy,hs((220,200,90),rng.uniform(-.1,.1)))
            return px,'ore'
        noise(); return px,'speckle'
    if cat=='block' and ('leaves' in n or 'bush' in n):
        for y in range(h):
            for x in range(w):
                if rng.random()<min(.93,cov+.08): px[y,x]=(*pal[rng.choice([0,1,2,2,3,4])],255)
        return px,'leaves'
    if cat=='particle':
        cx=cy=(w-1)/2
        for y in range(h):
            for x in range(w):
                d=((x-cx)**2+(y-cy)**2)**.5
                if d<=w/2: px[y,x]=(*pal[3],int(255*(1-d/(w/2+1))))
        return px,'dot'
    # alpha-shaped sprites (plants, flowers, items)
    cx=w//2
    if cat=='item' and any(k in n for k in ('boat','sign','door','bucket','slice','pizza','cheese','shard','seed','bulb','petal','bundle','berries','berry')) or cat=='item':
        # chunky blob icon with outline
        r=int(min(w,h)*0.32)
        for y in range(h):
            for x in range(w):
                d=((x-w/2+.5)**2+(y-h/2+.5)**2)**.5
                if d<=r: px[y,x]=(*pal[min(4,max(0,int(2+rng.gauss(0,.8)+(h/2-y)/r)))],255)
                elif d<=r+1: px[y,x]=(*pal[0],255)
        return px,'icon'
    # cross plant: stem + head
    top=int(h*(0.15 if cov<.4 else .05)); 
    stem=hs((60,130,50),rng.uniform(-.08,.05))
    for y in range(top+int(h*.3),h): put(px,cx,y,stem); 
    if cov>.35:
        for y in range(h//2,h):
            for x in range(cx-2-rng.randrange(0,3),cx+3+rng.randrange(0,3)):
                if rng.random()<cov*1.2 and abs(x-cx)<=(h-y)*.6: put(px,x,y,hs(stem,rng.uniform(-.12,.08)))
    for _ in range(int(max(4,cov*w*h*.35))):
        a=rng.random()*6.283; r=rng.random()*max(2,w*.2)
        put(px,cx+int(r*np.cos(a)),top+int(h*.18)+int(r*np.sin(a)*.8),pal[rng.choice([2,3,3,4,1])])
    return px,'plant'
rows=[]
for p in sorted(glob.glob(SRC+'/**/*.png',recursive=True)):
    rel=os.path.relpath(p,SRC); outp=os.path.join(OUT,rel)
    if os.path.exists(outp) and '--force' not in sys.argv: rows.append((rel,'kept-existing-hand-built','','','')); continue
    size,rgb,cov=stats(p); cat=rel.split('/')[0]; name=os.path.basename(rel)[:-4]
    seed=int(hashlib.sha256(rel.encode()).hexdigest()[:8],16); rng=random.Random(seed)
    px,kind=gen(name,cat,size,rgb,cov,rng)
    os.makedirs(os.path.dirname(outp),exist_ok=True); Image.fromarray(px,'RGBA').save(outp)
    rows.append((rel,kind,f'{size[0]}x{size[1]}','%d,%d,%d'%rgb,seed))
with open(os.path.join(OUT,'..','TEXTURE_MANIFEST.csv'),'w',newline='') as f:
    w=csv.writer(f); w.writerow(['path','generator','size','palette_hint_rgb','seed']); w.writerows(rows)
print(len(rows))
