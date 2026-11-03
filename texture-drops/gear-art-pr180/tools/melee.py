from lib import *
import json,sys,os,math
def g(d,k,v):
    x=d.get(k,v); return v if x in(None,'') else x
def num(d,k,v):
    try: return float(g(d,k,v))
    except Exception: return float(v)
def render(sp,outdir,wid):
    L=min(78,max(24,num(sp,'L',60))); H=sp.get('head') or {}; G=sp.get('guard') or {}; HD=sp.get('handle') or {}
    ht=g(H,'type','blade'); gt=g(G,'type','bar'); shaft=bool(sp.get('shaft',False)) or ht in('spear','axe','mace','hook','club') and L>=48
    hl=Layer(); gl=Layer(); dl=Layer(); bl=Layer()
    hlen=num(HD,'len',12 if not shaft else L*0.8); hw=max(1,min(3,num(HD,'w',2)))
    if shaft: hlen=L-num(H,'len',10)*0.6
    pom=g(HD,'pommel','none')
    # handle / shaft
    hw_=hw if not shaft else 1.4
    dpoly(dl,[(0,-hw_),(hlen,-hw_),(hlen,hw_),(0,hw_)],190); dline(dl,(0,hw_*0.6),(hlen,hw_*0.6),0.8,230)
    if g(HD,'wrap',False) and not shaft:
        for u in range(2,int(hlen),3): dline(bl,(u,-hw_-0.3),(u+1.2,hw_+0.3),0.9,150)
    if pom=='ball': ddisc(bl,-0.5,0,hw_+1.4,215)
    elif pom=='disc': dpoly(bl,[(-1.5,-hw_-2),(0.5,-hw_-2),(0.5,hw_+2),(-1.5,hw_+2)],215)
    elif pom=='spike': dpoly(bl,[(0,-hw_),(0,hw_),(-4,0)],235)
    elif pom=='ring': ddisc(bl,-1,0,hw_+2,200); ddisc(bl,-1,0,hw_,0)
    elif pom=='tassel': dline(bl,(0,0),(-4,-3),1.2,160)
    # head
    bs=hlen  # base of head along axis
    if ht=='blade':
        bl_=num(H,'len',L-hlen); w=max(3,min(14,num(H,'w',5)*1.5)); tp=max(0,min(1,num(H,'taper',0.3))); cv=max(-1,min(1,num(H,'curve',0)))
        tip=g(H,'tip','point'); bk=g(H,'back','flat'); n=14; top=[];bot=[]
        for i in range(n+1):
            t=i/n; u=bs+bl_*t
            wid_=w*(1-(1-tp)*t) if bk!='swell' else w*(0.6+0.7*math.sin(min(1,t*1.3)*math.pi*0.75))*(1-0.6*max(0,t-0.85)*6.6*0.15)
            off=cv*bl_*0.18*t*t*(1 if True else 0)
            top.append((u+0*off,off+wid_/2)); bot.append((u,off-wid_/2))
        tu,tv=top[-1][0],(top[-1][1]+bot[-1][1])/2
        if tip=='round': tu+=1.5
        elif tip=='clip': tu+=3; tv+=-0.5
        elif tip=='chisel': tu+=0.5
        else: tu+=w*0.8
        pts=top+[(tu,tv)]+bot[::-1]
        dpoly(hl,pts,205)
        # bevel highlight on the cutting half and fuller
        dpoly(hl,[(p[0],p[1]) for p in top]+[(tu,tv)]+[((a[0]),(a[1]+b[1])/2) for a,b in zip(top[::-1],bot[::-1])],235)
        if g(H,'fuller',False): dline(hl,(bs+bl_*0.1,(top[0][1]+bot[0][1])/2),(bs+bl_*0.75,(top[-1][1]+bot[-1][1])/2),0.9,150)
    elif ht=='axe':
        al=num(H,'len',12); w=num(H,'w',9); h=num(H,'h',12); bd=num(H,'beard',0.3)
        u0=L-al-1
        pts=[(u0,0),(u0+al*0.35,w*0.35),(u0+al*0.1,w),(u0+al,w*1.05),(u0+al*1.1,w*0.5) ,(u0+al,-0.3)]
        pts=[(u0+0.1*al,1.2),(u0+0.05*al,w*0.7),(u0+al*(1-bd*0.5),w+0.5),(u0+al*1.1,w*0.55),(u0+al*1.05,1.2)]
        dpoly(hl,pts,210); dline(hl,(u0+al*0.9,w*0.9),(u0+al*0.9,1.5),0.9,245)
        if g(H,'double',False): dpoly(hl,[(u0+0.1*al,-1.2),(u0+0.05*al,-w*0.7),(u0+al*(1-bd*0.5),-w-0.5),(u0+al*1.1,-w*0.55),(u0+al*1.05,-1.2)],210)
        if g(H,'spike',False): dpoly(hl,[(u0+al*1.05,-1.2),(u0+al*1.05,1.2),(u0+al+7,0)],225)
        dpoly(hl,[(u0,-1.4),(u0+al*0.3,-1.4),(u0+al*0.3,1.4),(u0,1.4)],200)
    elif ht in('mace','club'):
        r=num(H,'r',5) if ht=='mace' else num(H,'w',5)/2; sh=g(H,'shape','ball'); cu=L-r-1
        if ht=='club':
            cl=num(H,'len',22); w=num(H,'w',6); dpoly(hl,[(L-cl,-1.6),(L-cl*0.4,-w/2),(L,-w/2*1.1),(L+1,0),(L,w/2*1.1),(L-cl*0.4,w/2),(L-cl,1.6)],205)
            if g(H,'studs',False):
                for u in range(int(L-cl*0.7),int(L),4): ddisc(hl,u,w/2,1.1,245); ddisc(hl,u,-w/2,1.1,245)
        else:
            ddisc(hl,cu,0,r,205); ddisc(hl,cu+r*0.2,r*0.25,r*0.5,240)
            if sh=='flange':
                for k in range(6): a=k*math.pi/3; dpoly(hl,[(cu+(r-1)*math.cos(a)-0.9*math.sin(a),(r-1)*math.sin(a)+0.9*math.cos(a)),(cu+(r+2.5)*math.cos(a),(r+2.5)*math.sin(a)),(cu+(r-1)*math.cos(a)+0.9*math.sin(a),(r-1)*math.sin(a)-0.9*math.cos(a))],225)
            elif sh in('spike','star','teardrop'):
                for k in range(8 if sh=='star' else 5): a=k*2*math.pi/(8 if sh=='star' else 5); dpoly(hl,[(cu+(r-1)*math.cos(a)-1*math.sin(a),(r-1)*math.sin(a)+math.cos(a)),(cu+(r+3)*math.cos(a),(r+3)*math.sin(a)),(cu+(r-1)*math.cos(a)+math.sin(a),(r-1)*math.sin(a)-math.cos(a))],235)
            elif sh=='block': dpoly(hl,[(cu-r,-r),(cu+r,-r),(cu+r,r),(cu-r,r)],205); dpoly(hl,[(cu-r,r*0.4),(cu+r,r*0.4),(cu+r,r),(cu-r,r)],235)
            elif sh=='disc': ddisc(hl,cu,0,r+1,0); dpoly(hl,[(cu-r*0.6,-r*1.3),(cu+r*0.6,-r*1.3),(cu+r*0.6,r*1.3),(cu-r*0.6,r*1.3)],210)
    elif ht=='spear':
        sl=num(H,'len',14); w=max(3,num(H,'w',5)*1.4); shp=g(H,'shape','leaf'); u0=L-sl; ex=g(H,'extra','none')
        shape={'leaf':[(0,0),(0.3,.5),(0.55,.5),(1.0,0)],'triangle':[(0,.5),(0,-.5)] ,'barbed':[(0,.4),(0.3,.6),(0.5,.2),(1,0)],'broad':[(0,.5),(0.3,.5),(0.7,.3),(1,0)],'needle':[(0,.18),(1,0)],'diamond':[(0,0),(0.35,.5),(1,0)]}.get(shp,[(0,0),(0.3,.5),(0.55,.5),(1.0,0)])
        if shp=='triangle': top=[(0,.5),(1,0)]
        else: top=shape
        pts=[(u0+a*sl,b*w) for a,b in top]+[(u0+a*sl,-b*w) for a,b in top[::-1] if a<1]
        dpoly(hl,pts,210); dline(hl,(u0+sl*0.1,0),(u0+sl*0.8,0),0.9,245)
        if ex=='hook': dpoly(hl,[(u0+2,0),(u0+5,w*1.4),(u0+7,w*1.4),(u0+4,0)],215)
        elif ex=='axe': dpoly(hl,[(u0,0),(u0-3,w*1.8),(u0+5,w*2),(u0+6,0)],210)
        elif ex=='crescent': dpoly(hl,[(u0,-w*1.6),(u0+4,-w*1.8),(u0+4,w*1.8),(u0,w*1.6),(u0+2,0)],215)
        elif ex=='sidebar': dpoly(hl,[(u0+1,-w*1.8),(u0+2.5,-w*1.8),(u0+2.5,w*1.8),(u0+1,w*1.8)],215)
        elif ex=='blade_back': dpoly(hl,[(u0,0),(u0+4,-w*1.6),(u0+6,-w*1.6),(u0+5,0)],215)
    elif ht=='hook':
        hl_=num(H,'len',20); w=num(H,'w',3); cvv=num(H,'curve',0.6); u0=L-hl_*0.5
        pts=[];pts2=[]
        for i in range(13):
            t=i/12; a=t*math.pi*0.9*max(0.3,cvv); rr=hl_*0.6; u=u0+rr*math.sin(a); v=rr*(1-math.cos(a)); pts.append((u,v+w*(1-t*0.7)/2)); pts2.append((u,v-w*(1-t*0.7)/2))
        dpoly(hl,pts+pts2[::-1],210)
    elif ht in('ring','star','boomerang'):
        r=max(11,num(H,'r',14)); cu=L*0.5
        if ht=='ring': ddisc(hl,cu,0,r,205); ddisc(hl,cu,0,r-3.5,0); ddisc(hl,cu-r*0.3,r*0.3,1.2,245)
        elif ht=='star':
            n=int(num(H,'points',4)); pts=[]
            for k in range(2*n): a=k*math.pi/n+math.pi/4; rr=r if k%2==0 else r*0.35; pts.append((cu+rr*math.cos(a),rr*math.sin(a)))
            dpoly(hl,pts,215); ddisc(hl,cu,0,1.6,0)
        else:
            dpoly(hl,[(cu-r,-2),(cu,0),(cu+r*0.3,r),(cu+r*0.3+3.5,r-1),(cu+3,0),(cu-r+1,-4+0)],200) if False else dpoly(hl,[(cu-r*0.9,r*0.5),(cu,-3),(cu+r*0.9,r*0.5),(cu+r*0.9,r*0.5+3.5),(cu,0.5),(cu-r*0.9,r*0.5+3.5)],205)
        dl=Layer(); bl=Layer()
    elif ht=='katar':
        kl=num(H,'len',30); w=num(H,'w',6); dpoly(hl,[(L-kl,-w/2),(L,0),(L-kl,w/2)],215); dline(gl,(L-kl,-w/2-2),(L-kl,w/2+2),1.5,200)
        dline(gl,(L-kl-6,-w/2-2),(L-kl-6,w/2+2),1.2,190)
    # guard
    gw=num(G,'w',8)
    if ht in('blade','spear') or gt=='cross':
        gu=hlen+0.5 if ht=='blade' else None
        if gu is not None and gt!='none':
            if gt in('bar','cross'): dpoly(gl,[(gu-1,-gw/2),(gu+1.2,-gw/2),(gu+1.2,gw/2),(gu-1,gw/2)],215)
            elif gt=='swept': dpoly(gl,[(gu-2.5,-gw/2),(gu,-gw/2+1),(gu+1.2,0),(gu,gw/2-1),(gu-2.5,gw/2)],215)
            elif gt=='disc': dpoly(gl,[(gu-0.6,-gw/2),(gu+0.8,-gw/2),(gu+0.8,gw/2),(gu-0.6,gw/2)],225); ddisc(gl,gu,0,gw/2,220)
            elif gt=='basket': dpoly(gl,[(gu-5,-gw/2),(gu+1,-gw/2),(gu+1,gw/2),(gu-5,gw/2)],195); dpoly(gl,[(gu-4,-gw/2+1.2),(gu,-gw/2+1.2),(gu,gw/2-1.2),(gu-4,gw/2-1.2)],0)
            elif gt in('knuckle','hilt_ring'): dpoly(gl,[(gu-1,-gw/2),(gu+1,-gw/2),(gu+1,gw/2),(gu-1,gw/2)],215); dline(gl,(gu-1,-gw/2),(gu-hlen*0.6,-gw/2+1),1.1,200)
    nz=[l.out(f'{outdir}/{wid}_{n}.png') for l,n in((hl,'head'),(gl,'guard'),(dl,'handle'),(bl,'binding'))]
    return nz
def comp(outdir,wid,slots=('head','guard','handle','binding'),cols=((160,165,175),(150,150,160),(140,95,55),(110,80,50))):
    c=Image.new('RGBA',(64,64),(0,0,0,0))
    for p,t in sorted(zip(slots,cols),key=lambda x:{'handle':0,'binding':1,'guard':2,'head':3}.get(x[0],2)):
        f=f'{outdir}/{wid}_{p}.png'
        if not os.path.exists(f): continue
        r,gg,b,a=Image.open(f).split()
        c.alpha_composite(Image.merge('RGBA',[r.point(lambda v,k=t[0]:v*k//255),r.point(lambda v,k=t[1]:v*k//255),r.point(lambda v,k=t[2]:v*k//255),a]))
    return c
if __name__=='__main__':
    os.makedirs('t',exist_ok=True)
    T=[('katana','{"L":70,"head":{"type":"blade","len":46,"w":4,"taper":0.6,"curve":0.5,"tip":"clip"},"guard":{"type":"disc","w":7},"handle":{"len":18,"w":2,"wrap":true,"pommel":"none"}}'),
       ('spear','{"L":78,"shaft":true,"head":{"type":"spear","len":14,"w":4,"shape":"leaf"},"handle":{"len":60,"w":1,"pommel":"spike"}}'),
       ('axe','{"L":60,"shaft":true,"head":{"type":"axe","len":14,"w":10,"h":12,"beard":0.5,"double":false,"spike":true},"handle":{"len":50,"w":1}}'),
       ('mace','{"L":55,"shaft":true,"head":{"type":"mace","shape":"flange","r":6},"handle":{"len":44,"w":1,"pommel":"ball"}}'),
       ('shuriken','{"L":40,"head":{"type":"star","r":16,"points":4}}'),('dagger','{"L":32,"head":{"type":"blade","len":20,"w":5,"taper":0.2,"tip":"point","fuller":true},"guard":{"type":"bar","w":9},"handle":{"len":10,"w":2,"wrap":true,"pommel":"ball"}}'),
       ('scythe','{"L":70,"shaft":true,"head":{"type":"hook","len":24,"w":4,"curve":0.8},"handle":{"len":60,"w":1}}')]
    s=Image.new('RGB',(128*len(T),128),(60,60,60))
    for i,(n,j) in enumerate(T):
        render(json.loads(j),'t',n); c=comp('t',n).resize((128,128),Image.NEAREST); s.paste(c,(i*128,0),c)
    s.save('t/m.png')
