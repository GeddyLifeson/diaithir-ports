import os,sys,math,numpy as np
from PIL import Image,ImageDraw,ImageFilter
OUT=sys.argv[1]+'/assets/antique_atlas/textures/gui/'; os.makedirs(OUT+'icons',exist_ok=True); os.makedirs(OUT+'sprites/middle',exist_ok=True)
rng=np.random.default_rng(7)
def noise(h,w,s=1.0,sm=2):
    n=rng.normal(0,s,(h,w)); im=Image.fromarray(((n*20)+128).clip(0,255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(sm)); return (np.array(im).astype(np.float32)-128)/20
def parch(h,w):
    yy,xx=np.mgrid[0:h,0:w]; base=np.array([236,214,160],np.float32)
    v=1-0.35*((np.abs(xx-w/2)/(w/2))**3+(np.abs(yy-h/2)/(h/2))**3)
    img=base[None,None,:]*v[...,None]
    n=noise(h,w,1.0,3)[...,None]*8+noise(h,w,1.0,0.7)[...,None]*3
    img=img+n*np.array([1,1,1.3]); return img
def leather(h,w):
    base=np.array([112,58,34],np.float32); g=noise(h,w,1,0.6)[...,None]*9+noise(h,w,1,4)[...,None]*8
    return base[None,None,:]+g*np.array([1,0.8,0.7])
def book(W,H,win=None,spine=True,edge_l=True,edge_r=True,cover=13):
    img=leather(H,W)
    # page block
    x0=cover if edge_l else 0; x1=W-cover if edge_r else W; y0=cover-2; y1=H-cover+2
    pg=parch(H,W); img[y0:y1,x0:x1]=pg[y0:y1,x0:x1]
    # page edge shading and cover bevel
    img[y0:y0+2,x0:x1]*=0.78; img[y1-2:y1,x0:x1]*=0.7
    if edge_l: img[y0:y1,x0:x0+2]*=0.74
    if edge_r: img[y0:y1,x1-2:x1]*=0.74
    # cover highlight/shadow
    img[0:2]*=1.25; img[H-3:H]*=0.55
    if edge_l: img[:,0:2]*=1.18
    if edge_r: img[:,W-2:W]*=0.7
    if spine:
        cx=W//2; xs=np.arange(W); d=np.exp(-((xs-cx)/9.0)**2)
        img[y0:y1,:]*= (1-0.30*d)[None,:,None]
        img[y0:y1,cx-1:cx+1]*=0.55; img[0:y0,cx-4:cx+4]*=0.6; img[y1:H,cx-4:cx+4]*=0.6
    # corner scrolls
    out=np.concatenate([img.clip(0,255),np.full((H,W,1),255,np.float32)],axis=2)
    if win is not None:
        a,b,c,d=win; out[b:d,a:c,3]=0
        # soft glow around window
        for k in range(1,5):
            f=1-0.04*(5-k)
            for sl in ((slice(b-k,b-k+1),slice(a-k,c+k)),(slice(d+k-1,d+k),slice(a-k,c+k)),(slice(b-k,d+k),slice(a-k,a-k+1)),(slice(b-k,d+k),slice(c+k-1,c+k))):
                out[sl][...,:3]*=f if k<4 else 1
    return Image.fromarray(out.astype(np.uint8),'RGBA')
def save(im,name): im.save(OUT+name)
W,H=310,218
save(book(W,H),'book.png')
save(book(W,H,(20,22,W-20,H-22)),'book_frame.png')
save(book(W,H,(34,34,W-34,H-34)),'book_frame_narrow.png')
# fullscreen pieces: left cover+pages, right (mirror), middle tile
def fs(win_m=None):
    big=book(2*140+29,H,None,spine=False)
    l=big.crop((0,0,140,H)); r=big.crop((140+29,0,2*140+29,H)); m=big.crop((140,0,140+29,H)); return l,r,m
def fsw(m):
    big=book(2*140+29,H,(m,m,2*140+29-m,H-m),spine=False); return big.crop((0,0,140,H)),big.crop((169,0,309,H)),big.crop((140,0,169,H))
l,r,m=fs(); save(l,'sprites/book_fullscreen.png'); save(r,'sprites/book_fullscreen_r.png'); save(m,'sprites/middle/book_fullscreen_m.png')
l,r,m=fsw(20); save(l,'sprites/book_frame_fullscreen.png'); save(r,'sprites/book_frame_fullscreen_r.png'); save(m,'sprites/middle/book_frame_fullscreen_m.png')
l,r,m=fsw(34); save(l,'sprites/book_frame_narrow_fullscreen.png'); save(r,'sprites/book_frame_narrow_fullscreen_r.png'); save(m,'sprites/middle/book_frame_narrow_fullscreen_m.png')
# arrows (8 glyphs at the original boxes)
def arrow(d,box,dirn,big):
    x0,y0,x1,y1=box; fill=(203,128,26,255); hi=(217,176,114,255); lo=(161,85,17,255); ol=(83,46,42,255)
    w=x1-x0; h=y1-y0
    if dirn=='right': pts=[(x0,y0+h*0.3),(x1-h*0.55,y0+h*0.3),(x1-h*0.55,y0),(x1,y0+h/2),(x1-h*0.55,y1),(x1-h*0.55,y0+h*0.7),(x0,y0+h*0.7)]
    elif dirn=='left': pts=[(x1,y0+h*0.3),(x0+h*0.55,y0+h*0.3),(x0+h*0.55,y0),(x0,y0+h/2),(x0+h*0.55,y1),(x0+h*0.55,y0+h*0.7),(x1,y0+h*0.7)]
    elif dirn=='up': pts=[(x0+w*0.3,y1),(x0+w*0.3,y0+w*0.55),(x0,y0+w*0.55),(x0+w/2,y0),(x1,y0+w*0.55),(x0+w*0.7,y0+w*0.55),(x0+w*0.7,y1)]
    else: pts=[(x0+w*0.3,y0),(x0+w*0.3,y1-w*0.55),(x0,y1-w*0.55),(x0+w/2,y1),(x1,y1-w*0.55),(x0+w*0.7,y1-w*0.55),(x0+w*0.7,y0)]
    d.polygon(pts,fill=fill,outline=ol)
S=4; ar=Image.new('RGBA',(32*S,64*S),(0,0,0,0)); d=ImageDraw.Draw(ar)
for box,dn in (((19,2,29,8),'right'),((3,8,13,14),'left'),((8,19,14,29),'up'),((18,19,24,29),'down'),((18,33,30,41),'right'),((2,39,14,47),'left'),((7,50,15,62),'up'),((17,50,25,62),'down')):
    arrow(d,tuple(v*S for v in box),dn,0)
a=ar.resize((32,64),Image.NEAREST if False else Image.BOX); al=a.getchannel('A').point(lambda v:255 if v>100 else 0); a.putalpha(al); save(a,'arrow.png')
# frames
for nm,col in (('frame',(60,56,56)),('frame_selected',(225,222,218))):
    im=Image.new('RGBA',(34,34),(0,0,0,0)); ImageDraw.Draw(im).rectangle([0,0,33,33],outline=col+(255,),width=2); save(im,nm+'.png')
# bookmarks: grayscale paper tabs, tinted at runtime
def tab(w,h,side):
    S=4; im=Image.new('RGBA',(w*S,h*S),(0,0,0,0)); d=ImageDraw.Draw(im)
    if side in('left','right'): box=(3*S if side=='left' else 0,1*S,(w-1 if side=='left' else w-3)*S,(h-1)*S)
    else: box=(1*S,3*S if side=='top' else 0,(w-1)*S,(h-1 if side=='top' else h-3)*S)
    d.rectangle(box,fill=(222,222,222,255),outline=(110,110,110,255),width=S)
    for k in range(4):
        if side in('left','right'): y=(6+k*7)*S; d.line([(box[0]+3*S,y),(box[2]-2*S,y)],fill=(170,170,170,255),width=S)
        else: x=(6+k*7)*S; d.line([(x,box[1]+3*S),(x,box[3]-2*S)],fill=(170,170,170,255),width=S)
    a=im.resize((w,h),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>100 else 0)); return a
save(tab(24,36,'left'),'bookmark_left.png'); save(tab(24,36,'right'),'bookmark_right.png'); save(tab(36,24,'top'),'bookmark_top.png'); save(tab(36,24,'bottom'),'bookmark_bottom.png')
# ruler (grey capsule), eraser, player
S=4; im=Image.new('RGBA',(24*S,36*S),(0,0,0,0)); d=ImageDraw.Draw(im); d.polygon([(2*S,26*S),(14*S,4*S),(21*S,8*S),(9*S,30*S)],fill=(205,208,212,255),outline=(90,92,98,255)); d.line([(6*S,24*S),(17*S,6*S)],fill=(240,242,245,255),width=S)
a=im.resize((24,36),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>100 else 0)); save(a,'ruler_right.png')
im=Image.new('RGBA',(12*S,14*S),(0,0,0,0)); d=ImageDraw.Draw(im); d.polygon([(1*S,10*S),(8*S,3*S),(11*S,6*S),(4*S,13*S)],fill=(236,150,160,255),outline=(90,40,50,255)); d.polygon([(1*S,10*S),(4*S,13*S),(2*S,13*S)],fill=(240,240,240,255))
a=im.resize((12,14),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>100 else 0)); save(a,'eraser.png')
im=Image.new('RGBA',(7*S,8*S),(0,0,0,0)); d=ImageDraw.Draw(im); d.polygon([(3.5*S,0),(7*S,7.5*S),(3.5*S,5.5*S),(0,7.5*S)],fill=(245,245,245,255),outline=(60,60,70,255)); a=im.resize((7,8),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>90 else 0)); save(a,'player.png')
# 16x16 icons: parchment card with a symbol
def card(sym):
    S=8; im=Image.new('RGBA',(16*S,16*S),(0,0,0,0)); d=ImageDraw.Draw(im); d.polygon([(1*S,2*S),(14*S,1*S),(15*S,14*S),(2*S,15*S)],fill=(238,214,160,255),outline=(120,70,30,255))
    c=(190,40,36,255); k=(60,36,28,255)
    if sym=='add': d.ellipse([5*S,3*S,10*S,8*S],fill=c); d.polygon([(6*S,7*S),(9*S,7*S),(7.5*S,12*S)],fill=c)
    elif sym=='del': d.ellipse([5*S,3*S,10*S,8*S],fill=c); d.polygon([(6*S,7*S),(9*S,7*S),(7.5*S,12*S)],fill=c); d.line([(3*S,13*S),(13*S,3*S)],fill=k,width=2*S)
    elif sym=='export': d.polygon([(6*S,6*S),(10*S,6*S),(10*S,10*S),(12*S,10*S),(8*S,15*S),(4*S,10*S),(6*S,10*S)],fill=(50,170,50,255),outline=(20,90,20,255))
    elif sym=='show': d.ellipse([3*S,5*S,13*S,11*S],fill=(255,255,240,255),outline=k,width=S); d.ellipse([6*S,6*S,10*S,10*S],fill=k)
    elif sym=='hide': d.ellipse([3*S,5*S,13*S,11*S],fill=(255,255,240,255),outline=k,width=S); d.line([(3*S,13*S),(13*S,3*S)],fill=c,width=int(S*1.5))
    elif sym=='q': d.arc([5*S,3*S,11*S,9*S],200,380,fill=k,width=2*S); d.line([(8*S,8*S),(8*S,10*S)],fill=k,width=2*S); d.rectangle([7*S,12*S,9*S,13.5*S],fill=k)
    a=im.resize((16,16),Image.BOX); a.putalpha(a.getchannel('A').point(lambda v:255 if v>100 else 0)); return a
for nm,s in (('add_marker','add'),('del_marker','del'),('export','export'),('show_markers','show'),('hide_markers','hide'),('unknown','q')): save(card(s),'icons/'+nm+'.png')
print(sum(len(f) for _,_,f in os.walk(OUT)))
