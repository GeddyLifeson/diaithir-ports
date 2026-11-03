import os,sys,json,re,math,random,zlib
import numpy as np
sys.path.insert(0,'/home/user/work/atlas')
from PIL import Image,ImageDraw,ImageFilter
OUT=sys.argv[1]; W,H=64,96
def sh(c,d): return tuple(int(max(0,min(255,v+d))) for v in c)
def fallback(name):
    n=name.lower(); g=(150,170,90); r='flat'; feats=[]; water='none'
    if 'desert' in n or 'sand' in n: g=(224,200,140)
    if 'badland' in n or 'terracotta' in n: g=(190,110,70)
    if 'snow' in n or 'ice' in n or 'frozen' in n or 'taiga' in n and 'snow' in n: g=(236,240,244)
    if 'swamp' in n: g=(100,120,70); water='swamp'
    if 'ocean' in n or 'sea' in n or 'water' in n or 'river' in n: g=(110,150,190); water='sea'
    if 'mushroom' in n: g=(150,120,140)
    if 'nether' in n: g=(130,50,50)
    if 'end' in n: g=(220,220,160)
    for k in ('oak','spruce','birch','jungle','acacia','dark_oak','cherry','palm','cactus','shrub','flower','bamboo','reed','mushroom','fern'):
        if k in n or (k=='cactus' and 'cacti' in n) or (k=='shrub' and 'shrubs' in n): feats.append({'kind':k,'density':0.5})
    if 'forest' in n or 'wooded' in n or 'trees' in n: feats.append({'kind':'oak','density':0.7})
    for k,v in (('plateau_low','plateau_low'),('plateau','plateau'),('hills_low','hills_low'),('hills','hills'),('cliffs','cliffs'),('mountain','mountains'),('tiered','tiered'),('pillar','pillars'),('fingers','fingers'),('spikes','spikes')):
        if k in n: r=v; break
    return {'ground':g,'ground2':sh(g,-18),'relief':r,'relief_colour':sh(g,-50),'water':water,'features':feats,'outline':(60,44,30)}
def canopy(d,x,y,kind,s,col):
    ol=(50,38,28)
    if kind in('oak','birch','dark_oak','cherry','mangrove_tree','jungle'):
        c={'oak':(70,120,50),'birch':(110,150,70),'dark_oak':(40,80,40),'cherry':(236,150,170),'mangrove_tree':(70,100,50),'jungle':(40,130,50)}[kind]
        d.rectangle([x-1,y,x,y+3],fill=(90,60,40)); d.ellipse([x-s,y-s-1,x+s,y+s-1],fill=c,outline=sh(c,-55)); d.ellipse([x-s+2,y-s,x-1,y-2],fill=sh(c,35))
    elif kind=='spruce':
        c=(40,95,60); d.polygon([(x,y-s-3),(x+s,y+2),(x-s,y+2)],fill=c,outline=sh(c,-40)); d.rectangle([x,y+2,x,y+4],fill=(90,60,40))
    elif kind=='acacia':
        c=(130,150,60); d.line([(x,y+4),(x,y)],fill=(110,80,50),width=1); d.ellipse([x-s-1,y-3,x+s+1,y],fill=c,outline=sh(c,-50))
    elif kind=='palm':
        d.line([(x,y+4),(x+1,y-3)],fill=(150,110,60)); [d.line([(x+1,y-3),(x+1+dx,y-3+dy)],fill=(60,130,60)) for dx,dy in((-4,1),(4,1),(-3,-2),(3,-2),(0,-4))]
    elif kind=='cactus':
        c=(70,130,70); d.rectangle([x,y-4,x+1,y+3],fill=c,outline=sh(c,-45)); d.rectangle([x-3,y-2,x-2,y],fill=c); d.rectangle([x+3,y-3,x+4,y-1],fill=c)
    elif kind in('shrub','fern'): d.ellipse([x-2,y-1,x+2,y+2],fill=(110,140,70),outline=(60,80,40))
    elif kind=='flower': d.point((x,y),fill=(230,80,90)); d.point((x+2,y+1),fill=(240,220,80))
    elif kind=='mushroom': d.rectangle([x,y,x,y+2],fill=(230,220,200)); d.ellipse([x-2,y-3,x+2,y],fill=(190,50,50),outline=(80,20,20))
    elif kind=='bamboo': d.line([(x,y+4),(x,y-5)],fill=(120,170,70),width=1); d.line([(x+2,y+4),(x+2,y-3)],fill=(100,150,60))
    elif kind=='reed': d.line([(x,y+3),(x,y-3)],fill=(150,160,80)); d.line([(x+2,y+3),(x+1,y-2)],fill=(130,150,70))
    elif kind=='rock': d.polygon([(x-3,y+2),(x-1,y-2),(x+2,y-2),(x+3,y+2)],fill=(150,150,150),outline=(80,80,80))
    elif kind in('ice_spike','snow_cap'): d.polygon([(x,y-6),(x+2,y+2),(x-2,y+2)],fill=(210,235,250),outline=(100,140,170))
    elif kind=='dune': d.arc([x-5,y-2,x+5,y+3],200,340,fill=(190,160,100))
    elif kind=='lava': d.ellipse([x-3,y-2,x+3,y+2],fill=(240,110,30),outline=(120,30,10))
def render(name,sp):
    rnd=random.Random(zlib.crc32(name.encode())); g=tuple(sp.get('ground') or (150,170,90)); g2=tuple(sp.get('ground2') or sh(g,-18)); rc=tuple(sp.get('relief_colour') or sh(g,-50)); ol=tuple(sp.get('outline') or (60,44,30))
    img=np.zeros((H,W,3),np.float32); n1=np.array(Image.fromarray(rnd_noise(rnd,H,W,3)).resize((W,H)),np.float32)/255
    img[:]=np.array(g,np.float32); img+= (n1[...,None]-0.5)*40*np.array([1,1,0.8])
    # patches of secondary colour on 16px subtile grid
    im=Image.fromarray(img.clip(0,255).astype(np.uint8)); d=ImageDraw.Draw(im)
    for i in range(14): x=rnd.randrange(W); y=rnd.randrange(H); d.ellipse([x-5,y-3,x+5,y+3],fill=g2)
    w=sp.get('water','none')
    if w in('sea','ice_sea'):
        wc=(96,146,190) if w=='sea' else (190,220,235); d.rectangle([0,0,W,H],fill=wc)
        for k in range(10): x=rnd.randrange(W); y=rnd.randrange(H); d.arc([x-5,y-2,x+5,y+3],200,340,fill=sh(wc,-40))
    elif w in('river',):
        d.line([(0,40),(20,36),(44,48),(64,44)],fill=(96,146,190),width=9); d.line([(0,40),(20,36),(44,48),(64,44)],fill=(150,190,220),width=3)
    elif w in('swamp','mangrove'):
        for k in range(8): x=rnd.randrange(W); y=rnd.randrange(H); d.ellipse([x-8,y-4,x+8,y+4],fill=(80,110,100))
    r=sp.get('relief','flat')
    if r in('hills','hills_low','mountains','spikes'):
        cnt={'hills':5,'hills_low':4,'mountains':6,'spikes':7}[r]; s=7 if r in('hills','spikes') else 5
        if r=='mountains': s=10
        for k in range(cnt):
            x=rnd.randrange(8,56); y=rnd.randrange(12,88)
            if r=='spikes': d.polygon([(x,y-12),(x+4,y+3),(x-4,y+3)],fill=rc,outline=ol)
            elif r=='mountains': d.polygon([(x-s,y+5),(x,y-s-2),(x+s,y+5)],fill=rc,outline=ol); d.polygon([(x,y-s-2),(x+s,y+5),(x+2,y+5)],fill=sh(rc,-30))
            else: d.arc([x-s,y-s//2,x+s,y+s//2+2],190,350,fill=ol); d.pieslice([x-s,y-s//2,x+s,y+s+2],180,360,fill=sh(rc,40),outline=ol)
    elif r in('plateau','plateau_low','plateau_square','tiered','cliffs','fingers','pillars'):
        n=2 if r in('tiered',) else 1; hh=10 if r!='plateau_low' else 6
        for t in range(n):
            x0=8+t*6; x1=56-t*6; y0=18+t*12; y1=70-t*8
            if r in('pillars','fingers'):
                for k in range(4): x=rnd.randrange(10,54); y=rnd.randrange(24,80); d.rectangle([x-4,y-12,x+4,y+4],fill=rc,outline=ol); d.rectangle([x-4,y-12,x+4,y-9],fill=sh(g,15),outline=ol)
            else:
                d.rectangle([x0,y0+hh,x1,y1+hh],fill=rc,outline=ol); d.rectangle([x0,y0,x1,y1],fill=sh(g,10),outline=ol)
                for k in range(int((x1-x0)/4)): d.line([(x0+k*4+2,y1),(x0+k*4+2,y1+hh)],fill=sh(rc,-25))
    for f in sp.get('features') or []:
        kind=f.get('kind','shrub'); dens=max(0.05,min(1.0,float(f.get('density',0.5)))); cnt=max(1,int(dens*9))
        for k in range(cnt):
            x=rnd.randrange(6,58); y=rnd.randrange(8,90); canopy(d,x,y,kind,rnd.choice((3,4,4,5)),g)
    arr=np.array(im).astype(np.float32); arr+=(rnd_noise(rnd,H,W,1).astype(np.float32)[...,None]-128)*0.06
    return Image.fromarray(arr.clip(0,255).astype(np.uint8))
def rnd_noise(rnd,h,w,sm):
    a=np.array([[rnd.randrange(256) for _ in range(w//4+1)] for _ in range(h//4+1)],np.uint8); return np.array(Image.fromarray(a).resize((w,h),Image.BICUBIC))
if __name__=='__main__':
    specs={}
    p='/mnt/project-files/compute/jobs/atlas_tile_spec/out.jsonl'
    if os.path.exists(p):
        for l in open(p):
            r=json.loads(l)
            try: specs[r['id']]=json.loads(re.search(r'\{.*\}',r['text'],re.S).group(0))
            except Exception: pass
    names=open('/home/user/work/atlas_tiles.txt').read().split(); ok=0
    for nm in names:
        sp=specs.get(nm); ok+=bool(sp); sp=sp or fallback(nm)
        try:
            im=render(nm,sp)
            if nm.startswith('structure/'):
                import atlas_struct as _s; im=_s.struct(nm,im)
        except Exception as e: print('ERR',nm,repr(e)[:80]); im=render(nm,fallback(nm))
        f=OUT+'/assets/antique_atlas/textures/atlas/tile/'+nm+'.png'; os.makedirs(os.path.dirname(f),exist_ok=True); im.save(f)
    print(len(names),'tiles',ok,'from specs')
