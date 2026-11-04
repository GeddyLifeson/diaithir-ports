"""64 px planet art: flora blocks (log, log top, leaves, plant, ore) and colony picker icons.
Drawn from specs/<planet>.json only, no vanilla pixels. Usage: python3 -I planet_art.py OUT [planet_id ..]
Block ids match gen_flora.py exactly. Needs Pillow and numpy."""
import json,re,sys,os,glob,math,zlib
import numpy as np
from PIL import Image,ImageDraw
SP=os.environ.get('SPECS','/mnt/project-files/planets/specs')
N=64
def slug(s): return re.sub(r'[^a-z0-9]+','_',s.lower()).strip('_') or 'x'
def rgb(h): return np.array([int(h[i:i+2],16) for i in (1,3,5)],float)
def rng(*k): return np.random.default_rng(zlib.crc32('|'.join(map(str,k)).encode()))
def tnoise(r,w,h,cx,cy):
    """tileable value noise, cx x cy lattice cells"""
    g=r.random((cy,cx)); xs=np.linspace(0,cx,w,endpoint=False); ys=np.linspace(0,cy,h,endpoint=False)
    x0=xs.astype(int); y0=ys.astype(int); fx=xs-x0; fy=ys-y0; fx=fx*fx*(3-2*fx); fy=fy*fy*(3-2*fy)
    x1=(x0+1)%cx; y1=(y0+1)%cy
    a=g[np.ix_(y0,x0)];b=g[np.ix_(y0,x1)];c=g[np.ix_(y1,x0)];d=g[np.ix_(y1,x1)]
    return (a*(1-fx)+b*fx)*(1-fy[:,None])+(c*(1-fx)+d*fx)*fy[:,None]
def fbm(r,w,h,cx,cy,o=4):
    t=0;a=1;s=0
    for i in range(o): t=t+a*tnoise(r,w,h,cx*2**i,cy*2**i); s+=a; a*=.5
    return t/s
def shade(col,k):
    """k<1 darkens toward a cool shadow, k>1 lightens toward a warm light; muted like the Factorio look"""
    c=np.asarray(col,float); k=np.asarray(k,float)[...,None]
    dark=c*np.array([.55,.58,.7]); light=c+(255-c)*np.array([.5,.45,.3])
    out=np.where(k<1,dark+(c-dark)*k,c+(light-c)*(k-1)*1.6 if False else c+(light-c)*np.clip(k-1,0,1)*1.6)
    return np.clip(out,0,255)
def grade(img,grit=0.05,r=None):
    """muted painted grade: slight desaturation and pixel grit"""
    a=img[...,:3]; l=a.mean(-1,keepdims=True); a=l+(a-l)*.88
    if r is not None: a=a*(1+(r.random(a.shape[:2])[...,None]-.5)*grit*2)
    img[...,:3]=np.clip(a,0,255); return img
def toimg(a): return Image.fromarray(np.clip(a,0,255).astype(np.uint8),'RGBA')
def lit(a):
    """top-left light: gentle gradient"""
    yy,xx=np.mgrid[0:a.shape[0],0:a.shape[1]]; g=1.07-0.16*((xx+yy)/(a.shape[0]+a.shape[1]))
    a[...,:3]*=g[...,None]; return a
# ---------- logs
def log_side(c,base,r):
    col=rgb(c); n=fbm(r,N,N,8,2,4)           # stretched: many columns, few rows
    furrow=np.abs(np.sin((np.arange(N)[None,:]+n*7)*2*math.pi/ (6+int(r.integers(0,4)))))
    k=.62+.5*n-.28*(1-furrow)**2
    crack=(tnoise(r,N,N,16,3)>.78); k=np.where(crack,k*.55,k)
    img=np.dstack([shade(col,k),np.full((N,N),255.)])
    if base=='birch' or base=='pale_oak':
        m=(tnoise(r,N,N,5,14)>.8)&(tnoise(r,N,N,22,3)>.55); img[m,:3]=img[m,:3]*.25 if base=='birch' else img[m,:3]*.55
    if base=='cherry':
        m=(tnoise(r,N,N,4,16)>.78); img[m,:3]=shade(col,np.full(m.sum(),1.35))
    if base=='jungle':
        m=(tnoise(r,N,N,6,6)>.82); img[m,:3]=img[m,:3]*.6
    if base=='mangrove':
        m=(tnoise(r,N,N,10,3)>.72); img[m,:3]=img[m,:3]*np.array([1.05,.8,.8])
    return toimg(grade(lit(img),.05,r))
def log_top(c,base,r):
    col=rgb(c); yy,xx=np.mgrid[0:N,0:N]; d=np.hypot(xx-31.5,yy-31.5); n=fbm(r,N,N,4,4,3)
    ring=np.sin((d+n*4)*1.15); inner=shade(col*np.array([1.18,1.05,.8]),.78+.22*ring+.1*(n-.5))
    bark=shade(col,.55+.35*fbm(r,N,N,8,8,3))
    edge=np.minimum.reduce([xx,yy,N-1-xx,N-1-yy]); mix=(edge<6)[...,None]
    a=np.where(mix,bark,inner); a=np.dstack([a,np.full((N,N),255.)])
    a[(edge==5)]*=np.array([.7,.7,.7,1]); a[(d<2.2)][...,:3]
    return toimg(grade(lit(a),.05,r))
# ---------- leaves
def leaves(c,base,r):
    col=rgb(c); back=fbm(r,N,N,5,5,3)>.34
    big=np.zeros((N*3,N*3,4)); S=4
    im=Image.new('RGBA',(N*S,N*S),(0,0,0,0)); dr=ImageDraw.Draw(im)
    A=np.zeros((N,N,4)); A[...,:3]=shade(col,.5+.2*fbm(r,N,N,6,6,3)); A[...,3]=np.where(back,255,0)
    base_im=toimg(A); base_im=base_im.resize((N*S,N*S),Image.NEAREST); dr=ImageDraw.Draw(base_im)
    for layer,(k,n) in enumerate([(.8,95),(1.0,85),(1.22,60)]):
        for _ in range(n):
            x,y=r.random(2)*N*S; L=(5+r.random()*4)*S/1.6; W=L*.42; t=r.random()*math.tau
            pts=[]
            for u in np.linspace(0,1,9):
                w=math.sin(u*math.pi)*W; pts.append((u*L,w))
            pts+= [(p[0],-p[1]) for p in reversed(pts)]
            P=[(x+px*math.cos(t)-py*math.sin(t),y+px*math.sin(t)+py*math.cos(t)) for px,py in pts]
            kk=k*(.9+r.random()*.2); cc=tuple(int(v) for v in shade(col,kk))
            for ox in (-N*S,0,N*S):
                for oy in (-N*S,0,N*S):
                    if -L<x+ox<N*S+L and -L<y+oy<N*S+L:
                        Q=[(p[0]+ox,p[1]+oy) for p in P]; dr.polygon(Q,fill=cc+(255,),outline=tuple(int(v) for v in shade(col,kk*.62))+(255,))
                        e=(x+ox+L*math.cos(t),y+oy+L*math.sin(t)); dr.line([(x+ox,y+oy),e],fill=tuple(int(v) for v in shade(col,min(1.4,kk*1.12)))+(255,),width=max(1,S//3))
    a=np.asarray(base_im.resize((N,N),Image.BOX),float).copy()
    a[...,3]=np.where(a[...,3]>110,255,0)
    return toimg(grade(lit(a),.04,r))
# ---------- plants (cross model, transparent)
def plant(c,base,r):
    S=6; W=N*S; col=rgb(c); im=Image.new('RGBA',(W,W),(0,0,0,0)); dr=ImageDraw.Draw(im)
    fol=tuple(int(v) for v in shade(np.array([70,120,60.]),.9))+(255,)
    def P(x,y): return (x*S,y*S)
    def ell(cx,cy,rx,ry,t,fill,out=None):
        pts=[(cx+rx*math.cos(a)*math.cos(t)-ry*math.sin(a)*math.sin(t),cy+rx*math.cos(a)*math.sin(t)+ry*math.sin(a)*math.cos(t)) for a in np.linspace(0,math.tau,28)]
        dr.polygon([P(*p) for p in pts],fill=fill,outline=out)
    cl=lambda k:tuple(int(v) for v in shade(col,k))+(255,)
    if base=='fern':
        dr.line([P(32,63),P(33,12)],fill=fol,width=3*S//2)
        for i in range(14):
            y=60-i*3.4; L=17*(1-i/17); 
            for sgn in(-1,1):
                ell(32+sgn*L/2*.95,y-L*.15,L/2,1.6,sgn*-.35,cl(.75+.5*i/14),cl(.5))
        ell(33,10,1.2,5,1.57,cl(1.2))
    elif base=='lily_of_the_valley':
        for sgn in(-1,1): ell(32+sgn*8,52,10,3.6,sgn*-.8,fol,tuple(int(v) for v in shade(np.array([70,120,60.]),.55))+(255,))
        pts=[(32+7*math.sin(u*2.1)*u,60-u*46) for u in np.linspace(0,1,16)]
        dr.line([P(*p) for p in pts],fill=fol,width=S)
        for u in np.linspace(.25,.95,6):
            x,y=32+7*math.sin(u*2.1)*u,60-u*46; ell(x+3.5,y+3.5,3,3.6,0,cl(1.25),cl(.6)); dr.line([P(x,y),P(x+3.5,y+1.5)],fill=fol,width=S//2)
    else:
        sw={'blue_orchid':7,'poppy':5,'cornflower':12,'allium':0,'torchflower':0,'red_tulip':0,'oxeye_daisy':14,'azure_bluet':4,'dandelion':16}.get(base,7)
        stem=tuple(int(v) for v in shade(np.array([75,125,60.]),.85))+(255,)
        top=30 if base!='torchflower' else 26
        dr.line([P(32,63),P(32,top+6)],fill=stem,width=S+2)
        ell(26,52,6,2.4,-.7,stem); ell(38,48,6,2.4,.7,stem)
        if base=='allium':
            for _ in range(70):
                a=r.random()*math.tau; d=r.random()**.5*13; dr.ellipse([*P(32+d*math.cos(a)-1.8,22+d*math.sin(a)-1.8),*P(32+d*math.cos(a)+1.8,22+d*math.sin(a)+1.8)],fill=cl(.8+.5*r.random()))
        elif base=='torchflower':
            ell(32,22,6.5,10,1.57,cl(1.05),cl(.5)); ell(32,31,3.5,5,1.57,cl(.7)); ell(32,13,3,3.6,0,tuple(int(v) for v in shade(np.array([255,200,80.]),1.2))+(255,))
        elif base=='red_tulip':
            ell(32,24,7,9.5,1.57,cl(1.0),cl(.5)); 
            for sgn in(-1,0,1): ell(32+sgn*4.2,19,2.6,8,1.57,cl(1.2-.2*abs(sgn)))
        else:
            for i in range(sw):
                a=i/sw*math.tau+.2; ell(32+9*math.cos(a)*(.8 if base!='dandelion' else .55),top-4+9*math.sin(a)*.8,6 if sw<10 else 5.4,2.9 if sw<10 else 1.6,a,cl(1.0+.25*(i%2)),cl(.5))
            cc={'poppy':(30,20,20),'oxeye_daisy':(240,200,60),'dandelion':(255,220,90),'cornflower':(70,40,110),'azure_bluet':(250,230,120),'blue_orchid':(240,235,255)}.get(base,(240,220,100))
            dr.ellipse([*P(29,top-7),*P(35,top-1)],fill=cc+(255,),outline=(40,30,20,255))
    a=np.asarray(im.resize((N,N),Image.BOX),float).copy(); al=a[...,3]>96
    a[...,3]=np.where(al,255,0)
    # outline: opaque pixels touching transparent get a darker rim; light from top-left
    pad=np.pad(al,1); nb=~(pad[:-2,1:-1]&pad[2:,1:-1]&pad[1:-1,:-2]&pad[1:-1,2:])
    a[al&nb,:3]*=.62
    up=np.pad(al,1)[:-2,:-2]; hi=al&~nb&~up; a[hi,:3]=np.minimum(255,a[hi,:3]*1.15)
    return toimg(grade(a,.04,r))
# ---------- ores
def stone(r,tint):
    n=fbm(r,N,N,8,8,4); f=tnoise(r,N,N,32,32); col=np.array([122,122,120.])*.88+np.asarray(tint)*.12
    k=.74+.38*n+.1*(f-.5); cr=np.abs(tnoise(r,N,N,5,5)-.5)<.012; k=np.where(cr,k*.5,k)
    a=np.dstack([shade(col,k),np.full((N,N),255.)]); gr=(f>.88); a[gr,:3]*=1.12
    return a
def ore(c,base,tint,r):
    col=rgb(c); S=4; st=stone(r,tint); im=Image.new('RGBA',(N*S,N*S),(0,0,0,0)); sh=Image.new('RGBA',(N*S,N*S),(0,0,0,0)); dr=ImageDraw.Draw(im); ds=ImageDraw.Draw(sh)
    nb=int(r.integers(5,8)); cl=lambda k:tuple(int(v) for v in shade(col,k))+(255,)
    for i in range(nb):
        x,y=8+r.random()*48,8+r.random()*48; x,y=x*S,y*S; rad=(4.5+r.random()*4)*S
        if base in('diamond','emerald'): sides=6 if base=='emerald' else 5; pts=[(x+rad*(1.2 if i%2 else 1)*math.cos(a+r.random()*.3),y+rad*math.sin(a+r.random()*.3)*(1.3 if base=='emerald' else 1)) for i,a in enumerate(np.linspace(0,math.tau,sides,endpoint=False))]
        elif base=='redstone': rad*=.45; pts=[(x+rad*math.cos(a),y+rad*math.sin(a)) for a in np.linspace(0,math.tau,10,endpoint=False)]
        else: pts=[(x+rad*(.65+.5*r.random())*math.cos(a),y+rad*(.65+.5*r.random())*math.sin(a)) for a in np.linspace(0,math.tau,9,endpoint=False)]
        ds.polygon([(p[0]+S*1.4,p[1]+S*1.4) for p in pts],fill=(0,0,0,150))
        dr.polygon(pts,fill=cl(.9),outline=cl(.5))
        cx=sum(p[0] for p in pts)/len(pts); cy=sum(p[1] for p in pts)/len(pts)
        if base in('diamond','emerald'):
            for j,p in enumerate(pts): dr.polygon([(cx,cy),p,pts[(j+1)%len(pts)]],fill=cl([1.4,1.05,.8,1.2,.9,1.1][j%6]))
        else:
            dr.polygon([(cx-rad*.4,cy-rad*.5),(cx+rad*.1,cy-rad*.55),(cx-rad*.15,cy-rad*.05)],fill=cl(1.45 if base in('gold','copper','iron') else 1.2))
        if base in('gold','copper','iron','lapis','coal'):
            for _ in range(4): fx,fy=x+(r.random()-.5)*rad*2.6,y+(r.random()-.5)*rad*2.6; dr.rectangle([fx,fy,fx+S*1.4,fy+S*1.4],fill=cl(1.1))
    sha=np.asarray(sh.resize((N,N),Image.BOX),float); o=np.asarray(im.resize((N,N),Image.BOX),float)
    out=st.copy(); out[...,:3]=out[...,:3]*(1-sha[...,3:4]/255*.7)+0
    al=(o[...,3:4]/255); out[...,:3]=out[...,:3]*(1-al)+o[...,:3]*al
    if base=='redstone': g=np.asarray(Image.fromarray((o[...,3]).astype(np.uint8)).resize((N,N)).filter(__import__('PIL.ImageFilter',fromlist=['x']).GaussianBlur(2)),float)/255; out[...,:3]+=g[...,None]*col*.5
    return toimg(grade(out,.04,r))
# ---------- planet icon
def n3(r,pts,cells):
    h=r.random((cells,cells,cells)); p=pts*cells; i=np.floor(p).astype(int); f=p-i; f=f*f*(3-2*f); i%=cells; j=(i+1)%cells
    def g(a,b,c): return h[a[...,0]*0+ [a,b,c][0][...,0] if False else 0,0,0]
    out=0
    for dx in(0,1):
        for dy in(0,1):
            for dz in(0,1):
                ii=np.where(dx,j[...,0],i[...,0]); jj=np.where(dy,j[...,1],i[...,1]); kk=np.where(dz,j[...,2],i[...,2])
                w=(f[...,0] if dx else 1-f[...,0])*(f[...,1] if dy else 1-f[...,1])*(f[...,2] if dz else 1-f[...,2])
                out=out+h[ii,jj,kk]*w
    return out
def icon(pid,sp):
    r=rng('icon',pid); sky=rgb(sp['sky']['sky']); fog=rgb(sp['sky']['fog']); liq=sp['sea'].get('liquid','water'); wat=rgb(sp['sea']['water']) if sp['sea'].get('water') else sky
    cloud=rgb(sp['sky']['cloud'][:7]) if sp['sky'].get('cloud') else np.array([235,235,240.])
    B=sp['biomes']; cols=[rgb(b['grass']) for b in B]+[rgb(b['foliage']) for b in B]; arch=[b['archetype'] for b in B]
    S=3; W=N*S; yy,xx=np.mgrid[0:W,0:W]; R=W*.42; cx=cy=(W-1)/2
    X=(xx-cx)/R; Y=(yy-cy)/R; d2=X*X+Y*Y; inside=d2<=1; Z=np.sqrt(np.clip(1-d2,0,1))
    ax=r.random()*math.tau; ca,sa=math.cos(ax),math.sin(ax); X2=X*ca+Z*sa; Z2=-X*sa+Z*ca; tilt=.35; Y2=Y*math.cos(tilt)-Z2*math.sin(tilt); Z3=Y*math.sin(tilt)+Z2*math.cos(tilt)
    pts=np.dstack([X2,Y2,Z3])*.5+.5
    h=.6*n3(r,pts,3)+.3*n3(r,pts,6)+.1*n3(r,pts,12)
    land_t=np.quantile(h[inside],.45 if liq!='none' else 0.0)
    land=h>land_t
    # surface colour: land from biome colours chosen by a second noise, snow near poles when cold/frozen
    pick=n3(r,pts,3); idx=np.clip((pick*len(cols)).astype(int),0,len(cols)-1)
    cs=np.array(cols); surf=cs[idx]
    cold=sum(1 for a in arch if 'frozen' in a or 'snow' in a or 'ice' in a)/len(arch); hot=sum(1 for a in arch if a in('desert','badlands'))/len(arch)
    ld=np.clip((h-land_t)*5,0,1)[...,None]
    if liq=='none': base=surf*(.75+.5*n3(r,pts,12)[...,None]); 
    else:
        if liq=='lava': sea=np.array([205,70,20.])+np.array([50,60,0.])*n3(r,pts,10)[...,None]
        else: sea=(wat*.8+40)*(.65+.55*np.clip((land_t-h)*4,0,1)[...,None]*0+.0)*(0.8+.4*(h[...,None]/land_t))
        base=np.where(land[...,None],surf*(.85+.3*ld),sea)
        if liq=='lava': base=np.where(land[...,None],surf*.45*(.7+.5*ld),sea)
    if cold>.12 or liq=='water' and cold>0:
        pole=np.abs(Y2)>1-(.05+cold*.5); base=np.where(pole[...,None],np.array([225,232,240.]),base)
    lum=np.clip(.52*(-X*.55-Y*.6+Z*.62)+.62,.16,1.18)
    img=base*lum[...,None]
    cm=n3(r,pts*1.0+.37,5)*.6+n3(r,pts+.11,12)*.4; wk=sp['weather'].get('kind','')
    ccol=np.array([120,115,112.]) if wk=='ash' else cloud
    ca_=np.clip((cm-.5)*3.2,0,.85)*(1 if liq!='none' or wk else .0)
    img=img*(1-ca_[...,None])+(ccol*lum[...,None]*1.0)*ca_[...,None]
    rim=np.clip(1-Z,0,1)**2.2; glow=(sky*.5+fog*.5)
    img=img+glow*rim[...,None]*.55*(lum[...,None]*.7+.4)
    out=np.zeros((W,W,4)); out[...,:3]=np.clip(img,0,255); out[...,3]=np.where(inside,255,0)
    # halo
    dd=np.sqrt(d2); halo=np.clip(1-(dd-1)/.12,0,1)*(dd>1)
    out[...,:3]=np.where(inside[...,None],out[...,:3],glow); out[...,3]=np.maximum(out[...,3],halo*140)
    im=toimg(out).resize((N,N),Image.BOX); return im
# ---------- driver
def ids(pid,f):
    used=set(); res={}
    for slot,kind in(('tree','log'),('tree','leaves'),('plant','plant'),('ore','ore')):
        base=f'{pid}_{slug(f[slot]["name"])}'; bid=f'{base}_{kind}'; i=2
        while bid in used: bid=f'{base}{i}_{kind}'; i+=1
        used.add(bid); res[kind]=bid
    return res
def main():
    out=sys.argv[1]; want=set(sys.argv[2:]); nb=ni=0
    os.makedirs(out+'/textures/block',exist_ok=True); os.makedirs(out+'/textures/gui/colony',exist_ok=True)
    for f in sorted(glob.glob(SP+'/*.json')):
        pid=os.path.basename(f)[:-5]
        if want and pid not in want: continue
        sp=json.load(open(f))
        try: icon(pid,sp).save(f'{out}/textures/gui/colony/{pid}.png'); ni+=1
        except Exception as e: print('icon fail',pid,repr(e))
        fl=sp.get('flora')
        if not fl: continue
        b=ids(pid,fl); t=fl['tree']; r=rng(pid)
        log_side(t['log_color'],t['log_base'],rng(pid,'log')).save(f'{out}/textures/block/{b["log"]}.png')
        log_top(t['log_color'],t['log_base'],rng(pid,'top')).save(f'{out}/textures/block/{b["log"]}_top.png')
        leaves(t['leaf_color'],t['leaf_base'],rng(pid,'leaf')).save(f'{out}/textures/block/{b["leaves"]}.png')
        plant(fl['plant']['color'],fl['plant']['base'],rng(pid,'plant')).save(f'{out}/textures/block/{b["plant"]}.png')
        ore(fl['ore']['color'],fl['ore']['base'],rgb(sp['sky']['fog']),rng(pid,'ore')).save(f'{out}/textures/block/{b["ore"]}.png'); nb+=4
    print(nb,'block textures (+log tops),',ni,'icons')
if __name__=='__main__': main()
