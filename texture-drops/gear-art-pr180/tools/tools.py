from lib import *
import json,sys,os,math
def num(d,k,v):
    try: return float(d.get(k,v))
    except Exception: return float(v)
HAND={'axe','hammer','pick','blade','saw','scoop','fork','tongs','brush','needle','rod','chisel','plane','sickle','rake','hoe','spade'}
def handheld(sp,slots,outdir,wid):
    a=sp.get('arch','generic'); L=min(76,max(22,num(sp,'L',44))); hw=max(1,min(3,num(sp,'handle_w',2))); hl_=num(sp,'head_len',12); hw2=max(3,min(16,num(sp,'head_w',8))); teeth=int(num(sp,'teeth',0))
    H,D,B=Layer(),Layer(),Layer(); hdl=sp.get('handle','wood'); hlen=L-hl_ if a not in('plane',) else L*0.5
    if hdl!='none': dpoly(D,[(0,-hw),(hlen+2,-hw),(hlen+2,hw),(0,hw)],190); dline(D,(0,hw*0.5),(hlen,hw*0.5),0.8,235)
    if hdl=='knob': ddisc(D,0,0,hw+1.5,200)
    if hdl=='loop': ddisc(D,2,0,hw+3,200); ddisc(D,2,0,hw+1,0)
    if hdl=='crossbar': dpoly(D,[(-1,-6),(1,-6),(1,6),(-1,6)],200)
    if hdl=='ring': ddisc(D,0,0,4,200); ddisc(D,0,0,2.2,0)
    for u in (hlen-1,hlen+1.6): dline(B,(u,-hw-0.5),(u,hw+0.5),1.1,150)
    u0=hlen; w=hw2
    if a in('axe','hammer','pick'):
        if a=='axe': dpoly(H,[(u0,-1.4),(u0+hl_*0.2,-1.4),(u0+hl_*0.1,w*0.4),(u0+hl_*0.2,w),(u0+hl_,w*1.1),(u0+hl_*0.8,w*0.3),(u0+hl_*0.8,-1.4)][:0] or [(u0,-1.4),(u0,1.4),(u0+hl_*0.15,w*0.7),(u0+hl_,w*1.1),(u0+hl_*0.7,0.2),(u0+hl_*0.6,-1.4)],215)
        elif a=='hammer': dpoly(H,[(u0,-w/2),(u0+hl_,-w/2),(u0+hl_,w/2),(u0,w/2)],205); dpoly(H,[(u0,w*0.1),(u0+hl_,w*0.1),(u0+hl_,w/2),(u0,w/2)],240)
        else: dpoly(H,[(u0,-1.4),(u0-1,0),(u0+1,w*1.2),(u0+hl_,w*0.3),(u0+hl_,-w*0.3),(u0+1,-w*1.2),(u0-1,0),(u0,1.4)],215)
    elif a in('blade','chisel','sickle'):
        if a=='sickle':
            pts=[];p2=[]
            for i in range(13):
                t=i/12; ang=t*math.pi*0.85; rr=hl_*0.55; pts.append((u0+rr*math.sin(ang),rr*(1-math.cos(ang))+2.2*(1-t*0.6))); p2.append((u0+rr*math.sin(ang),rr*(1-math.cos(ang))-2.2*(1-t*0.6)))
            dpoly(H,pts+p2[::-1],210)
        else:
            ww=max(2,w*0.45); dpoly(H,[(u0,-ww),(u0+hl_*0.85,-ww*(0.5 if a=='blade' else 0.9)),(u0+hl_,0 if a=='blade' else -ww*0.9),(u0+hl_,ww*0.9 if a=='chisel' else 0),(u0+hl_*0.85,ww*(0.5 if a=='blade' else 0.9)),(u0,ww)],215); dline(H,(u0+1,ww*0.5),(u0+hl_*0.8,ww*0.3),0.8,245)
    elif a=='saw':
        w2=max(4,w*0.6); dpoly(H,[(u0,-w2),(u0+hl_*1.6,-w2*0.6),(u0+hl_*1.6,w2*0.6),(u0,w2)],205)
        for i in range(max(teeth,8)): u=u0+1+i*(hl_*1.5/max(teeth,8)); dpoly(H,[(u,-w2),(u+1,-w2-1.5),(u+2,-w2)],225)
    elif a in('scoop','spade'):
        r=max(3,w/2); dpoly(H,[(u0,-1.3),(u0+hl_*0.4,-1.3),(u0+hl_*0.4,1.3)],200) if False else None
        ddisc(H,u0+hl_*0.55,0,r*1.15,205) if a=='scoop' else dpoly(H,[(u0,-r),(u0+hl_*0.7,-r),(u0+hl_,0),(u0+hl_*0.7,r),(u0,r)],205)
        if a=='scoop': ddisc(H,u0+hl_*0.6,0.5,r*0.6,235); dpoly(H,[(u0-1,-1.2),(u0+hl_*0.3,-1.2),(u0+hl_*0.3,1.2),(u0-1,1.2)],200)
    elif a in('fork','rake','hoe'):
        n=max(2,teeth or (3 if a=='fork' else 6)); span=max(6,w)
        if a=='hoe': dpoly(H,[(u0,-1.2),(u0+3,-1.2),(u0+hl_*0.6,span*0.9),(u0+hl_*0.6+4,span*0.9),(u0+hl_*0.6+4,span*0.4),(u0+6,0),(u0,1.2)],210)
        else:
            dpoly(H,[(u0,-span/2),(u0+3,-span/2),(u0+3,span/2),(u0,span/2)],205)
            for i in range(n): v=-span/2+i*span/(n-1) if n>1 else 0; dpoly(H,[(u0+2,v-0.8),(u0+hl_,v-0.4),(u0+hl_+1,v),(u0+hl_,v+0.4),(u0+2,v+0.8)],220)
    elif a=='tongs':
        dpoly(H,[(u0,-0.6),(u0+hl_*1.5,-3.5),(u0+hl_*1.5,-1.8),(u0,0.8)],205); dpoly(H,[(u0,0.6),(u0+hl_*1.5,3.5),(u0+hl_*1.5,1.8),(u0,-0.8)],205)
    elif a=='brush':
        dpoly(H,[(u0,-w*0.4),(u0+3,-w*0.4),(u0+3,w*0.4),(u0,w*0.4)],200); dpoly(H,[(u0+3,-w*0.5),(u0+hl_,-w*0.3),(u0+hl_,w*0.3),(u0+3,w*0.5)],235)
    elif a in('needle','rod'):
        dline(H,(u0,0),(u0+hl_+6,0),1.2 if a=='rod' else 0.8,225)
        if a=='needle': ddisc(H,u0+1,0,1.3,225)
    elif a=='plane':
        dpoly(H,[(u0-2,-w*0.5),(u0+hl_*1.7,-w*0.5),(u0+hl_*1.7,w*0.4),(u0-2,w*0.4)],200); dpoly(H,[(u0+hl_*0.6,w*0.4),(u0+hl_*0.9,w*0.4),(u0+hl_*0.9,w*0.9),(u0+hl_*0.6,w*0.9)],235)
    else: ddisc(H,u0+hl_*0.5,0,w/2,205)
    return [({'head':H,'handle':D,'binding':B}.get(s,H)).out(f'{outdir}/{wid}_{s}.png') for s in slots]
def vessel(sp,slots,outdir,wid):
    a=sp.get('arch','pot'); w=max(14,min(52,num(sp,'w',30))); h=max(10,min(52,num(sp,'h',24))); P,Hd=Layer(),Layer()
    cx=30; base=56; top=base-h; nk=num(sp,'neck',0.5)
    if a in('pot','jar','jug','kettle','bucket','cup','basket','box'):
        if a in('pot','kettle'): P.ell(cx,base-h*0.45,w/2,h*0.5,200); P.rect(cx-w*0.32,top,cx+w*0.32,top+3,215)
        elif a=='jug':
            P.ell(cx,base-h*0.35,w/2,h*0.38,200); P.rect(cx-w*0.16,top,cx+w*0.16,base-h*0.55,200); P.rect(cx-w*0.22,top,cx+w*0.22,top+2.5,225)
        elif a=='jar': P.rect(cx-w/2,top+4,cx+w/2,base,200); P.rect(cx-w*0.38,top,cx+w*0.38,top+4,225)
        elif a=='cup': P.poly([(cx-w/2,top),(cx+w/2,top),(cx+w*0.38,base),(cx-w*0.38,base)],200); P.rect(cx-w/2,top,cx+w/2,top+2,240)
        elif a=='bucket': P.poly([(cx-w/2,top),(cx+w/2,top),(cx+w*0.4,base),(cx-w*0.4,base)],195); [P.rect(cx-w/2+1,top+h*k,cx+w/2-1,top+h*k+1.5,150) for k in (0.3,0.7)]
        elif a=='basket': P.poly([(cx-w/2,top),(cx+w/2,top),(cx+w*0.4,base),(cx-w*0.4,base)],190); [P.line((cx-w/2,top+k),(cx+w/2,top+k),1,140) for k in range(3,int(h),4)]
        else: P.rect(cx-w/2,top,cx+w/2,base,195); P.rect(cx-w/2,top,cx+w/2,top+3,230)
        if sp.get('lid') and a!='box': P.ell(cx,top,w*0.34,2.5,235); Hd_=1
        if sp.get('spout'): P.poly([(cx+w/2-2,top+h*0.3),(cx+w/2+9,top+h*0.1),(cx+w/2+9,top+h*0.2),(cx+w/2-2,top+h*0.5)],200)
        n=int(num(sp,'handles',0))
        if n>=1: Hd.ell(cx+w/2+2,base-h*0.5,6,h*0.28,200); Hd.ell(cx+w/2+2,base-h*0.5,3.3,h*0.18,0)
        if n==2: Hd.ell(cx-w/2-2,base-h*0.5,6,h*0.28,200); Hd.ell(cx-w/2-2,base-h*0.5,3.3,h*0.18,0)
        if a=='kettle' or a=='bucket': Hd.line((cx-w/2,top+2),(cx,top-5),1.2,190); Hd.line((cx,top-5),(cx+w/2,top+2),1.2,190)
        if sp.get('foot'): P.rect(cx-w*0.3,base,cx+w*0.3,base+2,170)
    elif a in('pan','bowl'):
        if a=='pan':
            P.ell(cx-4,base-6,w/2,5,200); P.ell(cx-4,base-7,w/2-2,3,235); Hd.poly([(cx-4+w/2-1,base-7.5),(cx-4+w/2+16,base-9),(cx-4+w/2+16,base-6.5),(cx-4+w/2-1,base-5)],190)
        else: P.poly([(cx-w/2,base-h*0.8),(cx+w/2,base-h*0.8),(cx+w*0.35,base-2),(cx-w*0.35,base-2)],200); P.ell(cx,base-h*0.8,w/2,3,235); P.rect(cx-w*0.25,base-2,cx+w*0.25,base,170)
    elif a=='lamp': P.ell(cx,base-6,w*0.4,5,200); P.poly([(cx+w*0.3,base-8),(cx+w*0.5,base-14),(cx+w*0.5,base-10),(cx+w*0.3,base-3)],200); Hd.ell(cx-w*0.45,base-8,4,5,200); Hd.ell(cx-w*0.45,base-8,2,3,0)
    elif a=='scale': P.rect(cx-1,top,cx+1,base,200); P.rect(cx-w/2,top+2,cx+w/2,top+4,215); [P.ell(cx+s*w*0.45,top+h*0.45,w*0.18,3,205) for s in (-1,1)]; P.rect(cx-w*0.25,base-2,cx+w*0.25,base,190)
    elif a=='wheel': P.ell(cx,base-h/2,min(w,h)/2,min(w,h)/2,205); P.ell(cx,base-h/2,min(w,h)/2-3,min(w,h)/2-3,0); [P.line((cx,base-h/2),(cx+math.cos(k*math.pi/4)*min(w,h)/2,base-h/2+math.sin(k*math.pi/4)*min(w,h)/2),1,190) for k in range(8)]
    elif a=='book': P.poly([(cx-w*0.4,top),(cx+w*0.4,top),(cx+w*0.4,base),(cx-w*0.4,base)],195); P.rect(cx-w*0.4,top,cx-w*0.32,base,150); P.rect(cx-w*0.25,top+4,cx+w*0.3,top+8,225)
    elif a=='sack': P.ell(cx,base-h*0.4,w/2,h*0.45,195); P.poly([(cx-w*0.2,top+2),(cx+w*0.2,top+2),(cx+w*0.28,top+h*0.3),(cx-w*0.28,top+h*0.3)],195); P.rect(cx-w*0.22,top+h*0.28,cx+w*0.22,top+h*0.34,150)
    elif a=='spool': P.rect(cx-w*0.2,top,cx+w*0.2,base,200); P.rect(cx-w*0.35,top,cx+w*0.35,top+3,225); P.rect(cx-w*0.35,base-3,cx+w*0.35,base,225)
    else: P.ell(cx,base-h/2,w/2,h/2,200)
    out={}
    for s in slots:
        l=Hd if s=='handle' else P
        out[s]=l
    res=[]
    for s,l in out.items():
        n=l.out(f'{outdir}/{wid}_{s}.png')
        if n==0 and s=='handle': # keep file non-empty: tiny hook
            l.rect(cx-1,top-1,cx+1,top+1,200); n=l.out(f'{outdir}/{wid}_{s}.png')
        res.append(n)
    return res
def render(sp,slots,outdir,wid):
    a=sp.get('arch','generic')
    if a in HAND or set(slots)=={'head','handle','binding'} and a not in('pot','pan','bowl','jug','cup','kettle','jar','basket','bucket','box','lamp','scale','wheel','book','sack','spool'):
        return handheld(sp,slots,outdir,wid)
    return vessel(sp,slots,outdir,wid)
def comp(outdir,wid,slots):
    cols={'head':(160,165,175),'handle':(140,95,55),'binding':(110,80,50),'pot':(150,110,80)}
    c=Image.new('RGBA',(64,64),(0,0,0,0))
    for p in sorted(slots,key=lambda s:{'handle':0,'binding':1}.get(s,2)):
        t=cols.get(p,(150,150,150)); r,gg,b,a=Image.open(f'{outdir}/{wid}_{p}.png').split()
        c.alpha_composite(Image.merge('RGBA',[r.point(lambda v,k=t[0]:v*k//255),r.point(lambda v,k=t[1]:v*k//255),r.point(lambda v,k=t[2]:v*k//255),a]))
    return c
if __name__=='__main__':
    os.makedirs('t',exist_ok=True)
    T=[('adze','{"arch":"axe","L":46,"head_w":9,"head_len":14,"handle_w":2}',['head','handle','binding']),('saw','{"arch":"saw","L":50,"head_w":8,"head_len":28,"teeth":12}',['head','handle','binding']),('ladle','{"arch":"scoop","L":50,"head_w":10,"head_len":14}',['head','handle']),('pot','{"arch":"pot","w":34,"h":28,"lid":true,"handles":2}',['pot','handle']),('kettle','{"arch":"kettle","w":30,"h":28,"spout":true,"handles":1,"lid":true}',['pot','handle']),('tongs','{"arch":"tongs","L":50,"head_len":24}',['head','handle','binding']),('hammer','{"arch":"hammer","L":44,"head_w":9,"head_len":10}',['head','handle','binding']),('fork','{"arch":"fork","L":50,"head_w":9,"head_len":14,"teeth":3}',['head','handle','binding'])]
    s=Image.new('RGB',(128*len(T),128),(60,60,60))
    for i,(n,j,sl) in enumerate(T):
        render(json.loads(j),sl,'t',n); c=comp('t',n,sl).resize((128,128),Image.NEAREST); s.paste(c,(i*128,0),c)
    s.save('t/tl.png')
