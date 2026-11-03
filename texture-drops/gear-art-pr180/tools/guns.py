from lib import *
import json,sys,os
def g(sp,k,d): 
    v=sp.get(k,d); return d if v in (None,'') else v
def render(sp,outdir,gid):
    st=g(sp,'stock','straight'); lock=g(sp,'lock','semi'); cls=g(sp,'class','rifle')
    S={'none':0,'straight':15,'pistol_grip':9,'skeleton':14,'folding':13,'bullpup':0,'thumbhole':15,'full_wood':17,'club':14}.get(st,12)
    pistol = cls in('pistol','revolver','flintlock_pistol','wheellock','handcannon') and st in('none','pistol_grip','club')
    R=10 if not pistol else 9
    B=int(g(sp,'barrel_len',30)); B=max(8,min(B,60-S-R-3))
    bw=max(1,min(5,int(g(sp,'barrel_w',2)))); cy=24
    x0=3; xr=x0+S; xb=xr+R; xe=xb+B
    bar,lk,sk=Layer(),Layer(),Layer()
    style=g(sp,'barrel_style','round')
    bar.rect(xb-1,cy-bw/2,xe,cy+bw/2+0.4,215); bar.rect(xb-1,cy-bw/2,xe,cy-bw/2+0.9,250)
    if style=='octagon': bar.rect(xb,cy-bw/2-0.6,xe,cy-bw/2,235)
    if style=='ribbed': 
        for x in range(int(xb)+2,int(xe),3): bar.rect(x,cy-bw/2-0.8,x+1,cy-bw/2,170)
    if style in('shrouded','heavy'): bar.rect(xb+2,cy-bw/2-1.2,xe-3,cy+bw/2+1.4,190)
    mz=g(sp,'muzzle','none')
    if mz=='suppressor': bar.rect(xe-1,cy-bw/2-1.2,xe+10,cy+bw/2+1.4,160); xe+=10
    elif mz=='brake': bar.rect(xe-3,cy-bw/2-1,xe+1,cy+bw/2+1.2,235); [bar.rect(xe-2+i,cy-bw/2-1,xe-1.4+i,cy-bw/2,90) for i in (0,1.6)]
    elif mz=='flare': bar.poly([(xe-2,cy-bw/2),(xe+3,cy-bw/2-2),(xe+3,cy+bw/2+2),(xe-2,cy+bw/2)],225); xe+=3
    if cls=='handcannon': bar.rect(xb-3,cy-bw/2-1.5,xe,cy+bw/2+1.5,205)
    if cls=='launcher': bar.rect(xb-2,cy-4,xe,cy+4,190); bar.rect(xe-4,cy-5,xe+2,cy+5,230); xe+=2
    if cls=='minigun':
        for i in range(-3,4,2): bar.rect(xb,cy+i-0.5,xe,cy+i+0.6,215 if i%4==0 else 175)
        bar.rect(xe-5,cy-4,xe-3,cy+4,235)
    sc=g(sp,'scope','none')
    if sc in('short','long'):
        L=11 if sc=='short' else 20; sx=xr+2
        bar.rect(sx,cy-bw/2-6,sx+L,cy-bw/2-2.5,200); bar.rect(sx,cy-bw/2-6,sx+L,cy-bw/2-5,245); bar.rect(sx+L-2,cy-bw/2-7,sx+L,cy-bw/2-1.5,225); bar.rect(sx+3,cy-bw/2-2.5,sx+5,cy-bw/2,170); bar.rect(sx+L-5,cy-bw/2-2.5,sx+L-3,cy-bw/2,170)
    elif sc=='iron': bar.rect(xe-4,cy-bw/2-2,xe-3,cy-bw/2,230); bar.rect(xr+3,cy-bw/2-2,xr+4,cy-bw/2,230)
    if g(sp,'bayonet',False): bar.poly([(xe,cy+bw/2),(xe+9,cy+bw/2+1.4),(xe,cy+bw/2+2.2)],240)
    ry0=cy-bw/2-1.5; ry1=cy+bw/2+3.5
    lk.rect(xr,ry0,xb,ry1,195); lk.rect(xr,ry0,xb,ry0+1,245)
    if lock in('flint','match','wheel','percussion'):
        lk.rect(xr+2,cy-bw/2-3.5,xr+4,cy-bw/2,230); lk.poly([(xr+4,cy-bw/2-3.5),(xr+7,cy-bw/2-1),(xr+5,cy-bw/2-1)],215)
        if lock=='match': lk.line((xr+2,cy-bw/2-3),(xr-3,cy-bw/2-5),1,160)
        if lock=='wheel': lk.ell(xr+5,cy+1,2.2,2.2,235); lk.ell(xr+5,cy+1,0.8,0.8,90)
    elif lock=='revolver_cyl': lk.rect(xr+1,cy-bw/2-3,xr+8,cy+bw/2+3,230); [lk.rect(xr+1+i*2.2,cy-bw/2-3,xr+1.6+i*2.2,cy+bw/2+3,110) for i in range(1,3)]
    elif lock=='bolt': lk.rect(xr+5,cy-bw/2-2.5,xr+9,cy-bw/2-0.5,240); lk.line((xr+9,cy-bw/2-1.5),(xr+11,cy-bw/2+0.5),1.4,240)
    elif lock=='lever': lk.line((xr+2,ry1),(xr+2,ry1+4),1,150); lk.line((xr+2,ry1+4),(xr+8,ry1+4),1,150); lk.line((xr+8,ry1+4),(xr+8,ry1),1,150)
    elif lock in('gas','semi'): lk.rect(xr+3,cy-bw/2-1.2,xb-2,cy-bw/2,250)
    tg=ry1; lk.line((xr+3,tg),(xr+3,tg+3),1,150); lk.line((xr+3,tg+3),(xr+7,tg+3),1,150); lk.line((xr+7,tg+3),(xr+7,tg),1,150)
    if pistol: lk.poly([(xr-1,ry1),(xr+5,ry1),(xr+2,ry1+14),(xr-5,ry1+14)],175)
    elif st in('pistol_grip','none') or cls in('smg','mg'):
        gx=xr+7; lk.poly([(gx+2,ry1),(gx+7,ry1),(gx+5,ry1+13),(gx-1,ry1+13)],175)
    mag=g(sp,'mag','none'); ml=int(g(sp,'mag_len',10)); mx=xr+(5 if not pistol else 3)
    if mag=='box': lk.rect(mx,ry1,mx+4,ry1+max(5,min(ml,14)),185)
    elif mag=='banana': lk.poly([(mx,ry1),(mx+4,ry1),(mx+8,ry1+ml),(mx+4,ry1+ml)],185)
    elif mag=='drum': lk.ell(mx+3,ry1+6,6,6,195); lk.ell(mx+3,ry1+6,2,2,100)
    elif mag=='tube': bar.rect(xb,cy+bw/2+0.5,xe-4,cy+bw/2+2.5,185)
    elif mag=='belt': lk.rect(mx,ry1,mx+5,ry1+7,180); [lk.rect(mx+i*1.6-3,ry1+6,mx+i*1.6-2.2,ry1+9,240) for i in range(0,6)]
    elif mag=='top_pan': lk.ell(xr+8,ry0-2,4.5,1.8,215)
    fo=g(sp,'fore','none')
    if fo in('wood','pump_slide'): sk.rect(xb+1,cy+bw/2,xb+min(B-2,16 if fo=='wood' else 11),cy+bw/2+3.6,190); sk.rect(xb+1,cy+bw/2,xb+min(B-2,16),cy+bw/2+0.8,235)
    elif fo=='rail': lk.rect(xb,cy+bw/2,xb+min(B-2,14),cy+bw/2+3,170)
    elif fo=='ramrod': sk.rect(xb,cy+bw/2+0.6,xe-4,cy+bw/2+1.6,205)
    if S>0:
        top=cy-bw/2-0.5
        if st in('straight','full_wood','club','thumbhole'):
            dr={'straight':3,'full_wood':5,'club':8,'thumbhole':5}[st]
            sk.poly([(x0,top+dr),(xr+1,top),(xr+1,ry1),(x0+2,top+dr+8+(3 if st=='club' else 0))],195)
            sk.poly([(x0,top+dr),(xr+1,top),(xr+1,top+1.5),(x0,top+dr+1.5)],240)
        elif st=='pistol_grip': sk.poly([(x0,top+4),(xr+1,top),(xr+1,ry1),(x0,top+9)],190)
        elif st in('skeleton','folding'):
            sk.line((xr,top+1),(x0,top+5),1.6,215); sk.line((x0,top+5),(x0+1,top+10),1.6,215); sk.line((xr,ry1-2),(x0+1,top+10),1.4,190)
    if st=='bullpup':
        sk.poly([(x0,cy-bw/2-1.5),(xr+R-2,cy-bw/2-2),(xr+R-2,cy+bw/2+4),(x0+1,cy+bw/2+6)],190); sk.rect(x0,cy-bw/2-1.5,xr+R-2,cy-bw/2-0.4,240)
    if g(sp,'sling',False): sk.line((x0+2,cy+6),(xb+8,cy+7),0.8,150)
    return [l.out(f'{outdir}/{gid}_{name}.png') for l,name in((bar,'barrel'),(lk,'lock'),(sk,'stock'))]
def comp(outdir,gid,cols=((140,140,150),(120,120,130),(150,100,60))):
    c=Image.new('RGBA',(64,64),(0,0,0,0))
    for p,t in zip(('barrel','lock','stock'),cols):
        r,gg,b,a=Image.open(f'{outdir}/{gid}_{p}.png').split()
        c.alpha_composite(Image.merge('RGBA',[r.point(lambda v,k=t[0]:v*k//255),r.point(lambda v,k=t[1]:v*k//255),r.point(lambda v,k=t[2]:v*k//255),a]))
    return c
if __name__=='__main__':
    os.makedirs('t',exist_ok=True)
    T=[('rifle1','{"class":"rifle","barrel_len":32,"barrel_w":2,"stock":"full_wood","lock":"bolt","mag":"box","mag_len":8,"fore":"wood","scope":"long"}'),('pist1','{"class":"pistol","barrel_len":14,"barrel_w":3,"stock":"pistol_grip","lock":"semi","mag":"box","mag_len":8}'),('smg1','{"class":"smg","barrel_len":16,"barrel_w":2,"stock":"folding","lock":"gas","mag":"banana","mag_len":12,"muzzle":"suppressor"}'),('musk','{"class":"flintlock_long","barrel_len":34,"barrel_w":2,"stock":"full_wood","lock":"flint","fore":"ramrod","bayonet":true}')]
    s=Image.new('RGB',(128*len(T),128),(60,60,60))
    for i,(n,j) in enumerate(T):
        print(n,render(json.loads(j),'t',n)); c=comp('t',n).resize((128,128),Image.NEAREST); s.paste(c,(i*128,0),c)
    s.save('t/g.png')
